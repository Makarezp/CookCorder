package fp.cookcorder.screen.utils

import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.ViewAnimationUtils
import android.animation.Animator
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.animation.AnimatorListenerAdapter
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth


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


fun circularReval(myView: View) {

    val cx = myView.width / 2
    val cy = myView.height / 2

    // get the final radius for the clipping circle
    val finalRadius = Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()

    // create the animator for this view (the start radius is zero)
    val anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0f, finalRadius)

    // make the view visible and start the animation
    myView.visibility = View.VISIBLE
    anim.start()

}

fun circularHide(myView: View) {
    if(myView.isAttachedToWindow) {
        val cx = myView.width / 2
        val cy = myView.height / 2

        val initialRadius = Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()

        val anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0f)

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (myView.isAttachedToWindow) {
                    myView.visibility = View.INVISIBLE
                }
            }
        })
        anim.start()
    }
}