package fp.cookcorder.screen.record

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.animation.Animator
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.view.*
import dagger.android.support.DaggerFragment
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.app.util.invisible
import fp.cookcorder.app.util.observe
import fp.cookcorder.screen.utils.handleCancellableTouch
import kotlinx.android.synthetic.main.main_fragment.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import javax.inject.Inject
import android.graphics.Rect
import android.os.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import fp.cookcorder.app.util.visible
import timber.log.Timber


class RecordFragment : DaggerFragment() {

    companion object {

        const val RECORDING_PERMISSION_REQUEST = 1

        fun newInstance() = RecordFragment()
    }

    @Inject
    lateinit var vmFactory: ViewModelProviderFactory<RecordViewModel>

    private lateinit var viewModel: RecordViewModel


    /**
     * State variable used to manipulate with success animations
     */
    private var animationSuccess = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!, vmFactory).get(RecordViewModel::class.java)
        viewModel.permissionGranted = isPermissionGranted()
        if (!viewModel.permissionGranted) requestAudioRecordingPermission()

        observeLiveData()
        setupRecordingButton()
        setupEditTextFocusRemoval()
        setupSuccessAnimationListener()

    }

    private fun observeLiveData() {
        with(viewModel) {
            observe(isRecording) { handleRecordingState(it) }
            observe(recordSuccess) { showSuccess() }
            observe(recordCancelled) { snackbar(view!!, "Cancelled") }
            observe(requestRecordingPermission) { requestAudioRecordingPermission() }
            observe(currentRecordTime) { mainFragmentTVTime.text = it }
        }
    }

    private fun showSuccess() {
        with(success) {
            visible()
            speed = 1.0F
            playAnimation()
            animationSuccess = true
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
                        { viewModel.finishRecording(getMinutesToSchedule(), "Title") },
                        { viewModel.cancelRecording() }
                ).invoke()
        )
    }

    private fun handleRecordingState(isRecording: Boolean) {
        if (isRecording) {
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

    private fun getMinutesToSchedule(): Int {
        val hours = mainFragmentMinutePicker.hours * 60
        val minutes = mainFragmentMinutePicker.minutes
        return hours + minutes
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

    private fun setupEditTextFocusRemoval() {
        removeEditTextFocusOnClickOutside()
        removeEditTextFocusOnDone()
    }

    private fun removeEditTextFocusOnClickOutside() {
        main.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val v = activity!!.currentFocus
                if (v is EditText) {
                    val outRect = Rect()
                    v.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        clearFocusAndHideKeyboard(v)
                    }
                }
            }
            false
        }
    }

    private fun clearFocusAndHideKeyboard(v: View) {
        v.clearFocus()
        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    private fun removeEditTextFocusOnDone() {
        mainFragmentETTitle.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearFocusAndHideKeyboard(v)
                true
            } else false
        }
    }
}



