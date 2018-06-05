package fp.cookcorder.app.extensions

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.view.View
import timber.log.Timber

fun View.onClick(onClickListener: (View) -> Unit) = this.setOnClickListener(onClickListener)

fun <T> LifecycleOwner.observe(liveData: LiveData<T?>, lambda: (T) -> Unit) {
    liveData.observe(this,
            Observer {
                if (it != null) lambda(it) else Timber.e(NullPointerException("Received null from live data"))
            })
}