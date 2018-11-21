package fp.cookcorder.screen

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import dagger.android.support.DaggerAppCompatActivity
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.screen.record.RecordFragment
import fp.cookcorder.screen.record.RecordViewModel
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var recordVmFactory: ViewModelProviderFactory<RecordViewModel>

    lateinit var recordViewModel: RecordViewModel

    lateinit var upper: View

    lateinit var lower: View

    private val toolbarContent: View by lazy {
       val layout = layoutInflater
                .inflate(R.layout.toolbar_content, toolbar_main, false)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        upper = layout.findViewById<View>(R.id.upper).apply {
            layoutParams.width = width
        }
        lower = layout.findViewById<View>(R.id.lower)
                .apply {
                    layoutParams.width = width }
        layout.requestLayout()
        layout
    }


    val slideListener = object : SlidingUpPanelLayout.PanelSlideListener {
        override fun onPanelSlide(panel: View?, slideOffset: Float) {
            toolbarContent.translationY = (-slideOffset * (toolbarContent.height / 2))
        }

        override fun onPanelStateChanged(panel: View?,
                                         previousState: SlidingUpPanelLayout.PanelState?,
                                         newState: SlidingUpPanelLayout.PanelState?) {

        }
    }

    val onMainScreenSlide = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            upper.translationX = -positionOffset * upper.width
        }

        override fun onPageSelected(position: Int) {
        }
    }

    val onAuxilaryScreenSlide = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        }

        override fun onPageSelected(position: Int) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recordViewModel = ViewModelProviders.of(this, recordVmFactory).get(RecordViewModel::class.java)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().run {
                replace(R.id.container, RecordFragment.newInstance(), "record")
                commit()
            }
        }
        toolbar_main.addView(toolbarContent)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val frag = supportFragmentManager.findFragmentByTag("record") as? RecordFragment
        frag?.clearFocusOnTouchOutside(ev)
        return super.dispatchTouchEvent(ev)
    }

    companion object {
        const val KEY_LAUNCH_PAGE = "KEY_LAUNCH_PAGE"
    }
}
