package fp.cookcorder.screen.record

import android.Manifest.permission.RECORD_AUDIO
import android.animation.ObjectAnimator
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import dagger.android.support.DaggerFragment
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.app.util.observe
import fp.cookcorder.app.util.visibleOrGone
import kotlinx.android.synthetic.main.main_fragment.*
import org.jetbrains.anko.design.longSnackbar
import timber.log.Timber
import javax.inject.Inject

class RecordFragment : DaggerFragment() {

    companion object {
        const val TITLE = "Record"

        private const val RECORDING_PERMISSION_REQUEST = 1

        fun newInstance() = RecordFragment()
    }

    interface RecordingListener {
        val isRecording: (Boolean) -> Unit
    }

    @Inject
    lateinit var recordingListener: RecordingListener

    @Inject
    lateinit var vmFactory: ViewModelProviderFactory<RecordViewModel>

    private lateinit var viewModel: RecordViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, vmFactory).get(RecordViewModel::class.java)
        setupClickListeners()
        observeLiveData()
        setupRecycler()
    }

    private fun setupRecycler() {
        mainFragmentRV.layoutManager = LinearLayoutManager(context)
    }

    private fun observeLiveData() {
        observe(viewModel.shouldShowRecordingScreen) {
            recordingListener.isRecording(it)
            mainFragmentFLRecordIndicator.visibleOrGone(it)
        }
    }

    private fun setupClickListeners() {
        mainFragmentFABRecord.setOnTouchListener { view, motionEvent ->
            handleRecordButtonClicks(view, motionEvent)
        }
    }

    private fun handleRecordButtonClicks(v: View, m: MotionEvent): Boolean {
        val rect = Rect()
        v.getHitRect(rect)
        val isInside = rect.contains(v.left + m.x.toInt(), v.top + m.y.toInt())

        return when (m.action) {
            MotionEvent.ACTION_DOWN -> {
                Timber.d("Action down")
                if (!viewModel.permissionGranted) requestPermission()
                viewModel.requestNewRecord()
                true
            }
            MotionEvent.ACTION_UP -> {
                Timber.d("Action Up")
                if (isInside) viewModel.finishRecording()
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isInside) {
                    Timber.d("Outside")
                    viewModel.cancelRecording()
                }
                true
            }
            else -> false
        }
    }



    private fun requestPermission() {

        fun request() = ActivityCompat
                .requestPermissions(activity!!, arrayOf(RECORD_AUDIO), RECORDING_PERMISSION_REQUEST)

        if (ContextCompat.checkSelfPermission(context!!, RECORD_AUDIO) == PERMISSION_GRANTED) {
            viewModel.permissionGranted = true
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, RECORD_AUDIO))
                longSnackbar(view!!,
                        "We cannot continue without your permission ",
                        "Grand permission"
                ) { request() }
            else request()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == RECORDING_PERMISSION_REQUEST) {
            permissions
                    .filter { it == RECORD_AUDIO }
                    .forEachIndexed { index, s ->
                        viewModel.permissionGranted = grantResults[index] == PERMISSION_GRANTED
                    }
        }
    }
}
