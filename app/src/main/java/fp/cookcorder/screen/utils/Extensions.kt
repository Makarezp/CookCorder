package fp.cookcorder.screen.utils

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.res.Resources
import android.view.View
import android.widget.TextView
import timber.log.Timber

fun View.onClick(onClickListener: (View) -> Unit) = this.setOnClickListener(onClickListener)

fun View.visibleOrGone(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun TextView.setTextHideIfNull(charSequence: CharSequence?) {
    if (charSequence != null && charSequence.isNotEmpty()) {
        this.visibility = View.VISIBLE
        this.text = charSequence
    } else {
        this.visibility = View.GONE
    }
}

fun TextView.setTextInvisibleIfEmptyOrNull(charSequence: CharSequence?) {
    if (charSequence != null && charSequence.isNotEmpty()) {
        this.visibility = View.VISIBLE
        this.text = charSequence
    } else {
        this.visibility = View.INVISIBLE
    }
}

fun <T> LifecycleOwner.observe(liveData: LiveData<T?>, lambda: (T) -> Unit) {
    liveData.observe(this,
            Observer {
                if (it != null) lambda(it) else Timber.e(NullPointerException("Received null from live data"))
            })
}

fun LifecycleOwner.observe(liveData: SingleLiveEvent<Void>, lambda: () -> Unit) {
    liveData.observe(this, Observer { lambda() })
}

fun Int.minutestToMilliseconds(): Long {
    return this.toLong() * 60000
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()


val Float.dp: Float
    get() = (this / Resources.getSystem().displayMetrics.density)
val Float.px: Float
    get() = (this * Resources.getSystem().displayMetrics.density)