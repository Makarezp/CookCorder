package fp.cookcorder.screen

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import fp.cookcorder.screen.record.RecordFragment
import fp.cookcorder.screen.play.PlayFragment
import javax.inject.Inject

class SimplePagerAdapter @Inject constructor(fragmentManager: FragmentManager)
    : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> RecordFragment.newInstance()
            1 -> PlayFragment.newInstance()
            else -> throw IllegalArgumentException("Unknown position")
        }
    }

    override fun getCount(): Int = 2

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> RecordFragment.TITLE
            1 -> "Play"
            else -> throw IllegalArgumentException("Unknown position")
        }
    }
}