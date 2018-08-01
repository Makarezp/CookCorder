package fp.cookcorder.screen.utils

import android.view.View
import android.view.ViewAnimationUtils
import android.animation.Animator
import android.animation.AnimatorListenerAdapter


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

fun circularHide(myView: View, onAnimationEnd: () -> Unit = {}) {
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
                    onAnimationEnd()
                }
            }
        })
        anim.start()
    }
}
