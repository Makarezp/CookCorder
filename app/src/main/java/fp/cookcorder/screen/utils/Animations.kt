package fp.cookcorder.screen.utils

import android.view.View
import android.view.ViewAnimationUtils
import android.animation.Animator
import android.animation.AnimatorListenerAdapter


fun circularReval(myView: View, xToY: Pair<Int, Int>) {

    val cx = myView.width / 2
    val cy = myView.height / 2

    // get the final radius for the clipping circle
    val finalRadius = Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()

    // create the animator for this view (the start radius is zero)
    val anim = ViewAnimationUtils.createCircularReveal(myView, xToY.first, xToY.second, 0f, finalRadius)

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