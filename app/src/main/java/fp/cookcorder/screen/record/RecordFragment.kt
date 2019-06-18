package fp.cookcorder.screen.record

import android.Manifest.permission.RECORD_AUDIO
import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.jakewharton.rxrelay2.PublishRelay
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import dagger.android.support.DaggerFragment
import fp.cookcorder.R
import fp.cookcorder.intentmodel.record.RecordModelStore
import fp.cookcorder.intentmodel.record.RecordViewProcessor
import fp.cookcorder.intentmodel.record.RecorderState
import fp.cookcorder.intentmodel.record.RecorderState.Event.RequestRecordingPermission
import fp.cookcorder.intentmodel.record.RecorderStatus.*
import fp.cookcorder.screen.MainPagerAdapter
import fp.cookcorder.utils.*
import fp.cookcorder.intentmodel.EventObservable
import fp.cookcorder.intentmodel.record.RecordViewEvent
import fp.cookcorder.intentmodel.record.RecordViewEvent.*
import fp.cookcorder.intentmodel.StateSubscriber
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.action_button.*
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.android.synthetic.main.options_fragment.*
import org.jetbrains.anko.design.longSnackbar
import timber.log.Timber
import javax.inject.Inject


class RecordFragment : DaggerFragment(),
        EventObservable<RecordViewEvent>,
        StateSubscriber<RecorderState> {

    companion object {

        const val IS_FIRST_RUN_KEY = "IS_FIRST_RUN"

        const val RECORDING_PERMISSION_REQUEST = 1

        fun newInstance() = RecordFragment()
    }

    val disposables = CompositeDisposable()

    @Inject
    lateinit var recordViewProcessor: RecordViewProcessor

    @Inject
    lateinit var recordModelStore: RecordModelStore

    private var recordPermissionRelay = PublishRelay.create<Boolean>()
    private var requestNewRecordRelay = PublishRelay.create<Unit>()
    private var finishRecordingRelay = PublishRelay.create<Unit>()
    private var cancelRecordingRelay = PublishRelay.create<Unit>()

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

    private var isFirstRun = true

    var currentTextViewAnimation: ObjectAnimator? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewPager.adapter = recordAdapter
        tabLayout.setupWithViewPager(viewPager)
        main.setOnTouchListener { _, event -> viewPager.onTouchEvent(event) }
        handleFirstRun()
        setupRecordingButton()
        setupSuccessAnimationListener()
        setupSlidingUpLayout()
    }


    fun addSlidingPanelListener(listener: SlidingUpPanelLayout.PanelSlideListener) {
        slidingLayout.addPanelSlideListener(listener)
    }

    fun setSlidingPanelScrollViewListener(scrollableView: View) {
        slidingLayout.setScrollableView(scrollableView)
    }

    override fun events(): Observable<RecordViewEvent> {
        return Observable.merge(
                requestNewRecordRelay.map { RequestRecordingClick },
                finishRecordingRelay.map { FinishRecordingClick },
                cancelRecordingRelay.map { CancelRecordingClick },
                recordPermissionRelay.map { RecordPermissionGranted(it) }
        )
    }

    override fun Observable<RecorderState>.subscribeToState(): Disposable {
        return subscribe {
            if (it.event is RequestRecordingPermission) {
                requestAudioRecordingPermission()
            }

            it.isToday.let {
                dateText.text = getString(if (it) R.string.today else R.string.tomorrow)
            }
            it.uiAlarmTime.let { minTV.text = it }

            with(it.recorderStatus) {
                when (this) {
                    is Success -> showSuccess()
                    is Cancelled -> showCancel()
                    is Recording -> handleRecording(this)
                }
                if (this !is Recording) {
                    stopRecordingAnimation()
                }
            }
        }
    }

    private fun handleRecording(recording: Recording) {
        timeTV.text = recording.currentTimeString
        if (recording.justStarted) {
            startRecordingAnimation()
        }
    }

    private fun setupSlidingUpLayout() {
        iconUpIMG.setOnClickListener {
            slidingLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }

        with(mainActivityTL) {

            mainActivityVP.adapter = pageAdapter
            setupWithViewPager(mainActivityVP)
        }

        var tabViewHeight = 0
        var initialTabHeight = 0
        var tabDifference = 0

        val tabContainer = (tabContainer as InterceptingFrameLayout)
        tabContainer.viewToDispatch = mainActivityVP
        tabContainer.disableClicking = true

        //get access to views of tablayout
        //tablayout at 0
        val tab = mainActivityTL.getChildAt(0) as ViewGroup
        val tabViewNext = tab.getChildAt(0) as ViewGroup
        val textViewNext = tabViewNext.getChildAt(1)
        val tabViewPast = tab.getChildAt(1) as ViewGroup
        val textViewPast = tabViewPast.getChildAt(1)


        textViewNext.setOnClickListener { mainActivityTL.getTabAt(0)?.select() }
        textViewPast.setOnClickListener { mainActivityTL.getTabAt(1)?.select() }

        mainActivityTL.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                mainActivityTL.removeOnLayoutChangeListener(this)
                tabViewHeight = tab.height
                initialTabHeight = tabLayout.height
                textViewNext.x = 0f + tabViewNext.paddingStart
                textViewNext.y = 0f + tabViewNext.paddingTop
                textViewPast.x = (tabViewPast.width - tabViewPast.paddingEnd - textViewPast.width).toFloat()
                textViewPast.y = 0f + tabViewNext.paddingTop
                tabDifference = tabViewHeight - initialTabHeight
            }
        })

        slidingLayout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {


                val layoutParams = mainActivityTL.layoutParams as FrameLayout.LayoutParams
                layoutParams.height = (((1 - slideOffset) * tabDifference) + initialTabHeight).toInt()
                textViewNext.x = (slideOffset * (tabViewNext.width / 2 - textViewNext.width / 2)) + ((1 - slideOffset) * tabViewNext.paddingStart)
                textViewNext.y = (slideOffset * (tabViewNext.height / 2 - textViewNext.height / 2)) + ((1 - slideOffset) * tabViewNext.paddingTop)
                textViewPast.x = (slideOffset * (tabViewPast.width / 2 - textViewPast.width / 2)) +
                        ((1 - slideOffset) * (tabViewPast.width - tabViewPast.paddingStart - textViewPast.width))
                textViewPast.y = (slideOffset * (tabViewPast.height / 2 - textViewPast.height / 2)) + ((1 - slideOffset) * tabViewPast.paddingTop)
                mainActivityTL.layoutParams = layoutParams

                mainActivityVP.translationY = -((1 - slideOffset) * 65.px)

                iconUpIMG.translationY = slideOffset * 500
                iconUpIMG.scaleX = (1 - (10 * slideOffset))
                iconUpIMG.scaleY = (1 - (10 * slideOffset))
                iconUpIMG.visibility = if (iconUpIMG.scaleX > 0) View.VISIBLE else View.INVISIBLE

                iconUpIMG.alpha = (1 - slideOffset)
                iconUpIMG.rotation = slideOffset * 360

                mainActivityTL.setSelectedTabIndicatorColor(Color.argb((slideOffset * 255).toInt(), 55, 172, 255))

            }

            override fun onPanelStateChanged(panel: View?,
                                             previousState: SlidingUpPanelLayout.PanelState?,
                                             newState: SlidingUpPanelLayout.PanelState?) {
                tabContainer.disableClicking = newState == SlidingUpPanelLayout.PanelState.COLLAPSED

            }
        })
    }


    private fun showSuccess() {
        titleET.text = null
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
            animateTimerTVFinishRecording(300)
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
                            val lastRecorderStatus = recordModelStore
                                    .modelState().blockingFirst().recorderStatus
                            if (lastRecorderStatus !is Recording) {
                                animateTimerTVFinishRecording(600)
                            }
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
                        { requestNewRecordRelay.accept(Unit) },
                        { finishRecordingRelay.accept(Unit) },
                        { cancelRecordingRelay.accept(Unit) }
                ).invoke()
        )
    }

    private fun startRecordingAnimation() {
        currentTextViewAnimation?.cancel()
        currentTextViewAnimation = ObjectAnimator.ofFloat(timeTV, View.ALPHA, 0f, 1f)
                .setDuration(300)
        currentTextViewAnimation?.start()

        vibrate()
        recordAnimation.visible()
        recordAnimation.playAnimation()
    }

    private fun stopRecordingAnimation() {
        recordAnimation.invisible()
        recordAnimation.playAnimation()
    }

    private fun animateTimerTVFinishRecording(duration: Long) {
        currentTextViewAnimation?.end()
        currentTextViewAnimation = ObjectAnimator.ofFloat(timeTV, View.ALPHA, 1f, 0f)
                .setDuration(duration)
        currentTextViewAnimation?.start()

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
        if (requestCode == RECORDING_PERMISSION_REQUEST) {
            permissions
                    .filter { it == RECORD_AUDIO }
                    .forEachIndexed { index, s ->
                        recordPermissionRelay.accept(grantResults[index] == PERMISSION_GRANTED)
                    }
        }
    }

    private fun handleFirstRun() {
        //check if is first run
        isFirstRun = prefs.getBoolean(IS_FIRST_RUN_KEY, true)
        if (isFirstRun) {
            prefs.edit().putBoolean(IS_FIRST_RUN_KEY, false).apply()
        }

        view!!.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                view!!.removeOnLayoutChangeListener(this)
                if (isFirstRun) showIntro()
            }
        })
    }


    private fun showIntro() {
        val pirckerView = view!!.findViewById<View>(R.id.minutePicker)
        val tapTargetPicker = TapTarget.forView(pirckerView, getString(R.string.time_picker_description_title), getString(R.string.time_picker_description))
                // All options below are optional
                .titleTextSize(30)                  // Specify the size (in sp) of the title text
                .titleTextColor(R.color.white)      // Specify the color of the title text
                .descriptionTextSize(20)            // Specify the size (in sp) of the description text
                .descriptionTextColor(R.color.white)  // Specify the color of the description text
                .textColor(R.color.white)            // Specify a color for both the title and description text
                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                .drawShadow(true)                   // Whether to draw a drop shadow or not
                .cancelable(false)
                .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                .tintTarget(true)
                .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                .targetRadius(150)// Specify the target radius (in dp)
        val fab = view!!.findViewById<View>(R.id.floatingActionButton)
        val tapTargetFab = TapTarget.forView(fab, getString(R.string.record_button_description_title), getString(R.string.record_button_description))
                // All options below are optional
                .titleTextSize(30)                  // Specify the size (in sp) of the title text
                .titleTextColor(R.color.white)      // Specify the color of the title text
                .descriptionTextSize(20)            // Specify the size (in sp) of the description text
                .descriptionTextColor(R.color.white)  // Specify the color of the description text
                .textColor(R.color.white)            // Specify a color for both the title and description text
                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                .drawShadow(true)                   // Whether to draw a drop shadow or not
                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                .tintTarget(true)
                .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                .targetRadius(80)// Specify the target radius (in dp)

        TapTargetSequence(activity!!).targets(tapTargetPicker, tapTargetFab)
                .considerOuterCircleCanceled(true)
                .continueOnCancel(true)
                .start()
    }

    override fun onResume() {
        super.onResume()
        disposables += recordModelStore.modelState().subscribeToState()
        disposables += events().subscribe(recordViewProcessor::process)
        recordPermissionRelay.accept(isPermissionGranted())
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

}

class InterceptingFrameLayout : FrameLayout {

    var viewToDispatch: ViewGroup? = null
    var disableClicking: Boolean = false

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (disableClicking && viewToDispatch?.dispatchTouchEvent(ev) != false) true
        else super.dispatchTouchEvent(ev)
    }
}



