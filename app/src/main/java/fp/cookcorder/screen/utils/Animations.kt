package fp.cookcorder.screen.utils

import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator

val elevateOnTouch =
        { executeOnClick: () -> Unit ->
            { view: View, motionEvent: MotionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.animate().apply {
                            translationZBy(16F)
                            duration = 150
                            interpolator = DecelerateInterpolator()
                            start()
                        }
                        executeOnClick()
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.animate().apply {
                            translationZ(0F)
                            duration = 150
                            interpolator = DecelerateInterpolator()
                            start()
                        }
                    }
                }
                true
            }
        }

