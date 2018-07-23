package fp.cookcorder.screen.utils

import android.view.View
import android.view.animation.DecelerateInterpolator

fun elevateAnimation(v: View) {
    v.animate().apply {
        translationZBy(16F)
        duration = 150
        interpolator = DecelerateInterpolator()
        start()
    }
}

fun delevateAnimation(v: View) {
    v.animate().apply {
        translationZ(0F)
        duration = 150
        interpolator = DecelerateInterpolator()
        start()
    }
}