package fp.cookcorder.utils

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


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


fun <T> Observable<T>.applySchedulers(): Observable<T> = this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Flowable<T>.applyShcedulers(): Flowable<T> = this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Maybe<T>.applySchedulers() = this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())