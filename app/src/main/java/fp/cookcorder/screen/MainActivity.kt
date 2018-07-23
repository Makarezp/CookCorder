package fp.cookcorder.screen

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.content.PermissionChecker
import android.support.v4.view.ViewPager
import dagger.android.support.DaggerAppCompatActivity
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.app.util.observe
import fp.cookcorder.screen.record.RecordFragment
import fp.cookcorder.screen.record.RecordViewModel
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var pageAdapter: SimplePagerAdapter

    @Inject
    lateinit var recordVmFactory: ViewModelProviderFactory<RecordViewModel>

    lateinit var recordViewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recordViewModel = ViewModelProviders.of(this, recordVmFactory).get(RecordViewModel::class.java)
        setContentView(R.layout.main_activity)
        setupViewPager()

        observeIsRecording()
    }

    private fun setupViewPager() {
        mainActivityVP.adapter = pageAdapter
        mainActivityTL.setupWithViewPager(mainActivityVP)
        mainActivityVP.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                recordViewModel.blockStartingNewRecording = state == ViewPager.SCROLL_STATE_DRAGGING
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {}
        })
    }

    private fun observeIsRecording() {
        observe(recordViewModel.isRecording) {
            mainActivityVP?.swipingEnabled = !it
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == RecordFragment.RECORDING_PERMISSION_REQUEST) {
            permissions
                    .filter { it == Manifest.permission.RECORD_AUDIO }
                    .forEachIndexed { index, s ->
                        recordViewModel.permissionGranted =
                                grantResults[index] == PermissionChecker.PERMISSION_GRANTED
                    }
        }
    }
}
