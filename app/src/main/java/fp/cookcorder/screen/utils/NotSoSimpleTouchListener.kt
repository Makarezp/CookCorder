package fp.cookcorder.screen.utils


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
     { v: View, m: MotionEvent ->

            val isInside = (m.x <= v.width && m.x >= 0) && (m.y <= v.height && m.y >= 0)
            when (m.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    onStart()
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
}


