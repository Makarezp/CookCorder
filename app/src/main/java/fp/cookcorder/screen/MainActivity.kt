package fp.cookcorder.screen

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.view.ViewPager
import dagger.android.support.DaggerAppCompatActivity
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.app.util.observe
import fp.cookcorder.screen.record.RecordViewModel
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject
import android.os.Build
import android.content.res.ColorStateList
import android.support.v4.graphics.drawable.DrawableCompat
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Rect
import android.widget.EditText
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager


class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var pageAdapter: MainPagerAdapter

    @Inject
    lateinit var recordVmFactory: ViewModelProviderFactory<RecordViewModel>

    lateinit var recordViewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recordViewModel = ViewModelProviders.of(this, recordVmFactory).get(RecordViewModel::class.java)
        setContentView(R.layout.main_activity)
        setupViewPager()
        setupTabLayout()
        observeIsRecording()
    }

    private fun setupViewPager() {
        with(mainActivityVP) {
            adapter = pageAdapter
            offscreenPageLimit = 3
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    recordViewModel.blockStartingNewRecording = state == ViewPager.SCROLL_STATE_DRAGGING
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageSelected(position: Int) {}
            })
        }
    }

    private fun setupTabLayout() {
        with(mainActivityTL) {
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
    }

    private fun getTabTintList(): ColorStateList? {
        return if (Build.VERSION.SDK_INT >= 23) {
            resources.getColorStateList(R.color.tab_icon_selector, theme)
        } else {
            resources.getColorStateList(R.color.tab_icon_selector)
        }
    }

    private fun observeIsRecording() {
        observe(recordViewModel.isRecording) {
            mainActivityVP?.swipingEnabled = !it
        }
    }

    /**
     * After touching outside edit text, drops focus of edit text
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
