package fp.cookcorder.screen.utils

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import timber.log.Timber

/**
 * This is implementation of onTouchListener with 3 callback
 * [onStart] callback invoked when finger touch view
 * [onFinish] when finger is lifted up while being on view
 * [onCancel] when finger moves outside view or action is cancelled
 */
fun handleCancellableTouch(onStart: () -> Unit,
                           onFinish: () -> Unit,
                           onCancel: () -> Unit) = { v: View, m: MotionEvent ->
    val rect = Rect()
    v.getHitRect(rect)
    val isInside = rect.contains(v.left + m.x.toInt(), v.top + m.y.toInt())

    when (m.action) {
        MotionEvent.ACTION_DOWN -> {
            Timber.d("Action down")
            onStart()
            true
        }
        MotionEvent.ACTION_UP -> {
            Timber.d("Action Up")
            if (isInside) onFinish()
            true
        }
        MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL -> {
            if (!isInside) {
                Timber.d("Outside")
                onCancel()
            }
            true
        }
        else -> false
    }
}
