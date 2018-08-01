package fp.cookcorder.screen.record

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.support.v4.view.ViewCompat.animate
import android.view.*
import com.github.florent37.kotlin.pleaseanimate.please
import dagger.android.support.DaggerFragment
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.app.util.invisible
import fp.cookcorder.app.util.observe
import fp.cookcorder.app.util.visible
import fp.cookcorder.screen.utils.circularHide
import fp.cookcorder.screen.utils.circularReval
import fp.cookcorder.screen.utils.handleCancellableTouch
import kotlinx.android.synthetic.main.main_fragment.*
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.design.snackbar
import timber.log.Timber
import javax.inject.Inject
import android.os.VibrationEffect
import android.os.Build
import android.content.Context.VIBRATOR_SERVICE
import android.os.Vibrator



class RecordFragment : DaggerFragment() {

    companion object {
        const val TITLE = "Record"

        const val RECORDING_PERMISSION_REQUEST = 1

        fun newInstance() = RecordFragment()
    }


    @Inject
    lateinit var vmFactory: ViewModelProviderFactory<RecordViewModel>

    private lateinit var viewModel: RecordViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!, vmFactory).get(RecordViewModel::class.java)
        viewModel.permissionGranted = isPermissionGranted()
        if (!viewModel.permissionGranted) requestPermission()
        observeLiveData()
        setupRecordingButton()
    }


    private fun observeLiveData() {
        observe(viewModel.isRecording) {
            if (it) {
                vibrate()
                floatingActionButton.invisible()
                circularReval(recordAnimation)
                recordAnimation.playAnimation()
            } else {
                recordAnimation.pauseAnimation()
                circularHide(recordAnimation) {
                    circularReval(floatingActionButton)
                }

            }
        }

        observe(viewModel.recordSuccess) {
            snackbar(view!!,
                    "Sukces!, tu mozna wjebac dokladny czas schedula!")
        }

        observe(viewModel.recordCancelled) {
            snackbar(view!!,
                    "Nagrywanie zskaneslowane")
        }


        observe(viewModel.requestRecordingPermission) {
            requestPermission()
        }
    }

    private fun setupRecordingButton() {
        floatingActionButton.setOnTouchListener(
                handleCancellableTouch(
                        { viewModel.requestNewRecord() },
                        { viewModel.finishRecording(getMinutesToSchedule()) },
                        { viewModel.cancelRecording() }
                ).invoke()
        )
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


    private fun requestPermission() {

        fun request() = ActivityCompat
                .requestPermissions(activity!!, arrayOf(RECORD_AUDIO), RECORDING_PERMISSION_REQUEST)

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
        if (requestCode == RecordFragment.RECORDING_PERMISSION_REQUEST) {
            permissions
                    .filter { it == Manifest.permission.RECORD_AUDIO }
                    .forEachIndexed { index, s ->
                        viewModel.permissionGranted =
                                grantResults[index] == PermissionChecker.PERMISSION_GRANTED
                    }
        }
    }
}



