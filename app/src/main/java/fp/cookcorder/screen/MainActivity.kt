package fp.cookcorder.screen

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.content.PermissionChecker
import dagger.android.support.DaggerAppCompatActivity
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.screen.record.RecordFragment
import fp.cookcorder.screen.record.RecordViewModel
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity(), RecordFragment.RecordingListener {

    @Inject
    lateinit var pageAdapter: SimplePagerAdapter

    @Inject
    lateinit var recordVmFactory: ViewModelProviderFactory<RecordViewModel>

    lateinit var recordViewModel: RecordViewModel

    override val isRecording = {isRecording: Boolean -> mainActivityVP?.swipingEnabled = !isRecording}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recordViewModel = ViewModelProviders.of(this, recordVmFactory).get(RecordViewModel::class.java)
        setContentView(R.layout.main_activity)
        mainActivityVP.adapter = pageAdapter
        mainActivityTL.setupWithViewPager(mainActivityVP)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == RecordFragment.RECORDING_PERMISSION_REQUEST) {
            permissions
                    .filter { it == Manifest.permission.RECORD_AUDIO }
                    .forEachIndexed { index, s ->
                        recordViewModel.permissionGranted = grantResults[index] == PermissionChecker.PERMISSION_GRANTED
                    }
        }
    }
}
