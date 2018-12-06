package fp.cookcorder.screen

import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import fp.cookcorder.R
import fp.cookcorder.screen.play.PlayFragment
import javax.inject.Inject

class MainPagerAdapter @Inject constructor(
        fragmentManager: FragmentManager,
        val context: Context)
    : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int) = Page.get(position).createFragment()

    override fun getCount() = Page.values().size

    override fun getPageTitle(position: Int): CharSequence? =
            context.getString(Page.get(position).stringRes)


    enum class Page(val position: Int,
                    val createFragment: () -> Fragment,
                    @StringRes val stringRes: Int) {
        PLAY(0,
                { PlayFragment.newInstance(isCurrent = true) },
                R.string.current),
        HISTORY(1,
                { PlayFragment.newInstance(isCurrent = false) },
                R.string.past);

        companion object {
            private val map = Page.values().associateBy { it.position }
            fun get(position: Int): Page {
                return map[position] ?: throw IllegalArgumentException("Unknown page position")
            }
        }
    }
}