package fp.cookcorder.intent

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

fun <T> Observable<T>.applySchedulers(): Observable<T> = this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun <T> Maybe<T>.applySchedulers() = this
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

