package fp.cookcorder.screen.record

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import fp.cookcorder.screen.record.time.TimeFragment
import javax.inject.Inject

class RecordViewPagerAdapter @Inject constructor(
        fragmentManager: FragmentManager
): FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return TimeFragment()
    }

    override fun getCount(): Int {
        return 1
    }
}