package fp.cookcorder.screen.record

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.animation.Animator
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import dagger.android.support.DaggerFragment
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.screen.MainActivity
import fp.cookcorder.screen.MainPagerAdapter
import fp.cookcorder.screen.utils.*
import kotlinx.android.synthetic.main.action_button.*
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.android.synthetic.main.options_fragment.*
import org.jetbrains.anko.design.longSnackbar
import timber.log.Timber
import javax.inject.Inject


class RecordFragment : DaggerFragment() {

    companion object {

        const val RECORDING_PERMISSION_REQUEST = 1

        fun newInstance() = RecordFragment()
    }

    @Inject
    lateinit var vmFactory: ViewModelProviderFactory<RecordViewModel>

    private lateinit var viewModel: RecordViewModel

    @Inject
    lateinit var prefs: SharedPreferences

    /**
     * State variable used to manipulate with success animations
     */
    private var animationSuccess = false

    @Inject
    lateinit var pageAdapter: MainPagerAdapter

    @Inject
    lateinit var recordAdapter: RecordViewPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!, vmFactory).get(RecordViewModel::class.java)
        viewModel.permissionGranted = isPermissionGranted()
        if (!viewModel.permissionGranted) requestAudioRecordingPermission()

        viewPager.adapter = recordAdapter
        indicator.setViewPager(viewPager)
        main.setOnTouchListener { _, event -> viewPager.onTouchEvent(event) }

        observeLiveData()
        setupRecordingButton()
        setupSuccessAnimationListener()
        handleFirstRun()
        setupSlidingUpLayout()
        setupSeekBar()

        addSlidingPanelListener((activity as MainActivity).slideListener)

        viewPager.addOnPageChangeListener((activity as MainActivity).onMainScreenSlide)
    }

    fun addSlidingPanelListener(listener: SlidingUpPanelLayout.PanelSlideListener) {
        slidingLayout.addPanelSlideListener(listener)
    }

    fun setSlidingPanelScrollViewListener(scrollableView: View) {
        slidingLayout.setScrollableView(scrollableView)
    }


    private fun observeLiveData() {
        with(viewModel) {
            observe(isRecording) { handleRecordingState(it) }
            observe(recordSuccess) {
                titleET.text = null
                showSuccess()
            }
            observe(recordCancelled) { showCancel() }
            observe(requestRecordingPermission) { requestAudioRecordingPermission() }
            observe(currentRecordTime) { timeTV.text = it }
            observe(minutes) { minTV.text = it }
            observe(isToday) {
                dateText.text = getString(if(it) R.string.today else R.string.tomorrow)
            }
        }
    }

    private fun setupSlidingUpLayout() {

        fun getTabTintList(): ColorStateList? {
            return if (Build.VERSION.SDK_INT >= 23) {
                resources.getColorStateList(R.color.tab_icon_selector, context!!.theme)
            } else {
                resources.getColorStateList(R.color.tab_icon_selector)
            }
        }

        with(mainActivityTL) {

            mainActivityVP.adapter = pageAdapter
            setupWithViewPager(mainActivityVP)

            for (i in 0 until tabCount) {
                getTabAt(i)?.icon =
                        DrawableCompat.wrap(
                                MainPagerAdapter.Page.get(i).getPageIcon(context)
                        ).apply {
                            DrawableCompat.setTintList(this, getTabTintList())
                        }
            }
        }

        slidingLayout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                mainActivityTL.alpha = slideOffset
                mainActivityVP.translationY = -((1 - slideOffset) * 65.px)
            }

            override fun onPanelStateChanged(panel: View?,
                                             previousState: SlidingUpPanelLayout.PanelState?,
                                             newState: SlidingUpPanelLayout.PanelState?) {
            }
        })
    }

    private fun setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.repeats.value = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun showSuccess() {

        with(success) {
            visible()
            speed = 1.0F
            playAnimation()
            animationSuccess = true
        }
    }

    private fun showCancel() {
        with(cancel) {
            visible()
            speed = 1.5F
            addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    removeAnimatorListener(this)
                    floatingActionButton.setImageDrawable(
                            resources.getDrawable(R.drawable.ic_mic, activity!!.theme))
                    invisible()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    floatingActionButton.setImageDrawable(null)
                }
            })
            playAnimation()
        }
    }

    private fun setupSuccessAnimationListener() {
        success.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                if (animationSuccess) {
                    Handler().postDelayed({
                        if (success.isAttachedToWindow) {
                            success.speed = -1.3F
                            success.playAnimation()
                            animationSuccess = false

                        }
                    }, 400)
                } else {
                    if (success.isAttachedToWindow) success.visibility = View.GONE
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
    }

    private fun setupRecordingButton() {
        floatingActionButton.setOnTouchListener(
                handleCancellableTouch(
                        { viewModel.requestNewRecord() },
                        {
                            viewModel.finishRecording()
                        },
                        { viewModel.cancelRecording() }
                ).invoke()
        )
    }

    private fun handleRecordingState(isRecording: Boolean) {
        if (isRecording) {
            timeTV.animate()
                    .alphaBy(1f)
                    .setDuration(300)
                    .start()
            vibrate()
            recordAnimation.visible()
            recordAnimation.playAnimation()
        } else {
            recordAnimation.invisible()
            recordAnimation.playAnimation()
        }
    }

    private fun vibrate() {
        val v = context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            v.vibrate(100)
        }
    }

    private fun requestAudioRecordingPermission() {

        fun request() = requestPermissions(arrayOf(RECORD_AUDIO), RECORDING_PERMISSION_REQUEST)

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, RECORD_AUDIO))
            longSnackbar(view!!,
                    "We cannot continue without your permission ",
                    "Grant permission"
            ) { request() }
        else request()
    }


    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context!!, RECORD_AUDIO) == PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        Timber.e(requestCode.toString())
        if (requestCode == RecordFragment.RECORDING_PERMISSION_REQUEST) {
            permissions
                    .filter { it == Manifest.permission.RECORD_AUDIO }
                    .forEachIndexed { index, s ->
                        viewModel.permissionGranted =
                                grantResults[index] == PermissionChecker.PERMISSION_GRANTED
                    }
        }
    }




    private fun handleFirstRun() {
//        val isFirstRunKey = "RecordFragmentFirstRun"
//        val isFirstRun = prefs.getBoolean(isFirstRunKey, true)
//        if (isFirstRun) {
//            showIntro()
//            prefs.edit().putBoolean(isFirstRunKey, false).apply()
//        }
    }

    private fun showIntro() {
        val pirckerView = view!!.findViewById<View>(R.id.minutePicker)
        val tapTargetPicker = TapTarget.forView(pirckerView, getString(R.string.time_picker_description_title), getString(R.string.time_picker_description))
                // All options below are optional
                .titleTextSize(20)                  // Specify the size (in sp) of the title text
                .titleTextColor(R.color.text)      // Specify the color of the title text
                .descriptionTextSize(10)            // Specify the size (in sp) of the description text
                .descriptionTextColor(R.color.white_70)  // Specify the color of the description text
                .textColor(R.color.text)            // Specify a color for both the title and description text
                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                .drawShadow(true)                   // Whether to draw a drop shadow or not
                .cancelable(false)
                .tintTarget(true)
                .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                .targetRadius(pirckerView.width.dp)// Specify the target radius (in dp)
        val fab = view!!.findViewById<View>(R.id.floatingActionButton)
        val tapTargetFab = TapTarget.forView(fab, getString(R.string.record_button_description_title), getString(R.string.record_button_description))
                // All options below are optional
                .titleTextSize(20)                  // Specify the size (in sp) of the title text
                .titleTextColor(R.color.text)      // Specify the color of the title text
                .descriptionTextSize(10)            // Specify the size (in sp) of the description text
                .descriptionTextColor(R.color.white_70)  // Specify the color of the description text
                .textColor(R.color.text)            // Specify a color for both the title and description text
                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                .drawShadow(true)                   // Whether to draw a drop shadow or not
                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                .tintTarget(true)
                .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                .targetRadius(50)// Specify the target radius (in dp)

        TapTargetSequence(activity!!).targets(tapTargetPicker, tapTargetFab)
                .considerOuterCircleCanceled(true)
                .continueOnCancel(true)
                .start()
    }

}



