package fp.cookcorder.screen.record

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import javax.inject.Inject

class RecordViewPagerAdapter @Inject constructor(
        fragmentManager: FragmentManager
): FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> TimeFragment()
            1 -> OptionsFragment()
            else -> throw NullPointerException()
        }
    }

    override fun getCount(): Int {
        return 2
    }
}