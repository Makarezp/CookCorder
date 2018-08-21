package fp.cookcorder.screen

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import fp.cookcorder.R
import fp.cookcorder.screen.record.RecordFragment
import fp.cookcorder.screen.play.PlayFragment
import javax.inject.Inject

class MainPagerAdapter @Inject constructor(
        fragmentManager: FragmentManager)
    : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int) = Page.get(position).createFragment()

    override fun getCount() = Page.values().size

    override fun getPageTitle(position: Int): CharSequence? = null

    enum class Page(val position: Int,
                    val createFragment: () -> Fragment,
                    @DrawableRes val iconRes: Int) {
        RECORD(0,
                { RecordFragment.newInstance() },
                R.drawable.ic_tab_record),
        PLAY(1,
                { PlayFragment.newInstance(isCurrent = true) },
                R.drawable.ic_tab_scheduled),
        HISTORY(2,
                { PlayFragment.newInstance(isCurrent = false) },
                R.drawable.ic_tab_history);

        fun getPageIcon(context: Context) = context.getDrawable(iconRes)

        companion object {
            private val map = Page.values().associateBy { it.position }
            fun get(position: Int): Page {
                return map[position] ?: throw IllegalArgumentException("Unknown page position")
            }
        }
    }
}