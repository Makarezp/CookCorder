package fp.cookcorder.screen

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.view.ViewPager
import dagger.android.support.DaggerAppCompatActivity
import fp.cookcorder.R
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.app.util.observe
import fp.cookcorder.screen.record.RecordViewModel
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject


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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setCurrentPage(intent)
    }

    private fun setCurrentPage(intent: Intent?) {
        intent?.let {
            val navigateToPage = it.getIntExtra(KEY_LAUNCH_PAGE, -1)
            if (navigateToPage > -1 && navigateToPage < mainActivityVP.childCount) {
                mainActivityTL.getTabAt(navigateToPage)?.select()
                intent.removeExtra(KEY_LAUNCH_PAGE)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setCurrentPage(intent)
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

    companion object {
        const val KEY_LAUNCH_PAGE = "KEY_LAUNCH_PAGE"
    }
}
