package fp.cookcorder.app.util

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.view.View
import timber.log.Timber

fun View.onClick(onClickListener: (View) -> Unit) = this.setOnClickListener(onClickListener)

fun View.visibleOrGone(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun <T> LifecycleOwner.observe(liveData: LiveData<T?>, lambda: (T) -> Unit) {
    liveData.observe(this,
            Observer {
                if (it != null) lambda(it) else Timber.e(NullPointerException("Received null from live data"))
            })
}