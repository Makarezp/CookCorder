package fp.cookcorder.screen.utils

import android.content.Context
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import timber.log.Timber

/**
 * This is implementation of onTouchListener with 3 callback
 * [onStart] callback invoked when finger touch view
 * [onFinish] when finger is lifted up while being on view
 * [onCancel] when finger moves outside view or action is cancelled
 */
var handleCancellableTouch = { onStart: () -> Unit,
                               onFinish: () -> Unit,
                               onCancel: () -> Unit ->
    { view: View ->

        val gestureDetector = GestureDetector(view.context,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onLongPress(e: MotionEvent?) {
                        //used to disable stealing touch event by scroll view while recording
                        view.parent.requestDisallowInterceptTouchEvent(true)
                        onStart()
                    }
                })

       val touchListenerToReturn = { v: View, m: MotionEvent ->

            val rect = Rect()
            v.getHitRect(rect)
            val isInside = rect.contains(v.left + m.x.toInt(), v.top + m.y.toInt())

           gestureDetector.onTouchEvent(m)
            when (m.action) {
                MotionEvent.ACTION_DOWN -> {
                    true
                }
                MotionEvent.ACTION_UP -> {
                    Timber.d("Action Up")
                    if (isInside) onFinish()
                    true
                }
                MotionEvent.ACTION_MOVE,
                MotionEvent.ACTION_CANCEL -> {
                    if (!isInside) {
                        Timber.d("Outside")
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
