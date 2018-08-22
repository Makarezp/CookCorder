package fp.cookcorder.app.util

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.view.View
import android.widget.TextView
import fp.cookcorder.screen.utils.SingleLiveEvent
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
    if (charSequence != null) {
        this.visibility = View.VISIBLE
        this.text = charSequence
    } else {
        this.visibility = View.GONE
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