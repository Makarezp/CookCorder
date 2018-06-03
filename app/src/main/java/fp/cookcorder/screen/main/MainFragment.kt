package fp.cookcorder.screen.main

import android.Manifest.permission.RECORD_AUDIO
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.app.extensions.onClick
import kotlinx.android.synthetic.main.main_fragment.*
import org.jetbrains.anko.design.longSnackbar
import timber.log.Timber
import javax.inject.Inject

class MainFragment : DaggerFragment() {

    companion object {
        private const val RECORDING_PERMISSION_REQUEST = 1
        fun newInstance() = MainFragment()
    }

    @Inject
    lateinit var vmFactory: ViewModelProviderFactory<MainViewModel>

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!, vmFactory).get(MainViewModel::class.java)
        setupClicks()
    }

    private fun setupClicks() {
        mainFragmentFABRecord.onClick { requestRecordIfPermissionIsGranted() }
        mainFragmentFABRecord.setOnTouchListener { v, m ->
            when (m.action) {
                MotionEvent.ACTION_DOWN -> {
                    Timber.d("Action down")
                    viewModel.requestNewRecord()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    viewModel.finishRecording()
                    Timber.d("Action Up")
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val rect = Rect()
                    v.getHitRect(rect)
                    if (rect.contains(v.left + m.x.toInt(), v.top + m.y.toInt())) {
                        Timber.d("Inside")
                    } else {
                        Timber.d("Outside")
                        viewModel.cancelRecording()
                    }
                    true
                }
                else -> false
            }
        }

        button.onClick { viewModel.playRecording() }
    }

    private fun requestRecordIfPermissionIsGranted() {

        fun requestPermissions() {
            ActivityCompat.requestPermissions(activity!!, arrayOf(RECORD_AUDIO), RECORDING_PERMISSION_REQUEST)
        }

        if (ContextCompat.checkSelfPermission(context!!, RECORD_AUDIO) == PERMISSION_GRANTED) {
            viewModel.requestNewRecord()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, RECORD_AUDIO))
                longSnackbar(view!!,
                        "We cannot continue without your permission ",
                        "Grand permission"
                ) { requestPermissions() }
            else requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == RECORDING_PERMISSION_REQUEST) {
            permissions
                    .filterIndexed { index, s -> s == RECORD_AUDIO && grantResults[index] == PERMISSION_GRANTED }
                    .forEach { viewModel.requestNewRecord() }
        }
    }

}
