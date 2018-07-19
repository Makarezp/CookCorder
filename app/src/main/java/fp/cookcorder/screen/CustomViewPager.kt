package fp.cookcorder.screen

import android.content.Context
import android.view.MotionEvent
import android.support.v4.view.ViewPager
import android.util.AttributeSet


class CustomViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    var swipingEnabled: Boolean = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.swipingEnabled) {
            super.onTouchEvent(event)
        } else false

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (this.swipingEnabled) {
            super.onInterceptTouchEvent(event)
        } else false

    }

}