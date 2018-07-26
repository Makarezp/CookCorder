package fp.cookcorder.screen.utils

import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import fp.cookcorder.R
import timber.log.Timber

/**
 * This is implementation of onTouchListener with 3 callback
 * [onStart] callback invoked when finger touch view
 * [onFinish] when finger is lifted up while being on view
 * [onCancel] when finger moves outside view or action is cancelled
 */
var handleCancellableTouch = { onStart: (Float, Float) -> Unit,
                               onFinish: () -> Unit,
                               onCancel: () -> Unit,
                               isFirsItem: Boolean ->
    { view: View ->

        val gestureDetector = GestureDetector(view.context,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onLongPress(e: MotionEvent) {
                        //used to disable stealing touch event by scroll view while recording
                        view.parent.requestDisallowInterceptTouchEvent(true)
                        onStart(e.rawX, e.rawY)
                    }
                })

        val touchListenerToReturn = { v: View, m: MotionEvent ->

            val rect = Rect()
            v.getHitRect(rect)
            val isInside = rect.contains(v.left + m.x.toInt(), v.top + m.y.toInt())

            gestureDetector.onTouchEvent(m)
            when (m.action) {
                MotionEvent.ACTION_DOWN -> {
                    if(isFirsItem) view.parent.requestDisallowInterceptTouchEvent(true)
                    elevateAnimation(v)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    Timber.d("Action Up")
                    delevateAnimation(v)
                    if (isInside) onFinish()
                    true
                }
                MotionEvent.ACTION_MOVE,
                MotionEvent.ACTION_CANCEL -> {
                    if (!isInside) {
                        Timber.d("Outside")
                        delevateAnimation(v)
                        onCancel()
                        v.parent.requestDisallowInterceptTouchEvent(false)
                    }
                    true
                }
                else -> false
            }
        }
        touchListenerToReturn
    }
}


