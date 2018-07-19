package fp.cookcorder.screen

import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import fp.cookcorder.R
import fp.cookcorder.screen.record.RecordFragment
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity(), RecordFragment.RecordingListener {

    @Inject
    lateinit var pageAdapter: SimplePagerAdapter

    override val isRecording = {isRecording: Boolean -> mainActivityVP?.swipingEnabled = !isRecording}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        mainActivityVP.adapter = pageAdapter
        mainActivityTL.setupWithViewPager(mainActivityVP)
    }
}
