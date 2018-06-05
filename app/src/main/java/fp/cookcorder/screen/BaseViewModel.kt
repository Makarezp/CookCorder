package fp.cookcorder.screen

import android.arch.lifecycle.ViewModel
import fp.cookcorder.app.Scheduler
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

abstract class BaseViewModel : ViewModel() {

    @Inject
    lateinit var scheduler: Scheduler

    protected val compDisposable = CompositeDisposable()

    private val handleError: (Throwable) -> Unit = { Timber.e(it) }

    protected fun <T> exe(
            flowable: Flowable<T>,
            onError: (Throwable) -> Unit = handleError,
            onComplete: () -> Unit = {},
            onNext: (T) -> Unit = {}) {
        compDisposable.add(
                flowable
                        .subscribeOn(scheduler.io())
                        .observeOn(scheduler.ui())
                        .subscribe(onNext, onError, onComplete))
    }

    protected fun <T> exe(
            observable: Observable<T>,
            onError: (Throwable) -> Unit = handleError,
            onComplete: () -> Unit = {},
            onNext: (T) -> Unit = {}) {
        compDisposable.add(
                observable
                        .subscribeOn(scheduler.io())
                        .observeOn(scheduler.ui())
                        .subscribe(onNext, onError, onComplete))
    }

    protected fun <T> exe(
            single: Single<T>,
            onError: (Throwable) -> Unit = handleError,
            onSuccess: (T) -> Unit = {}) {
        compDisposable.add(
                single
                        .subscribeOn(scheduler.io())
                        .observeOn(scheduler.ui())
                        .subscribe(onSuccess, onError))
    }

    protected fun <T> exe(
            maybe: Maybe<T>,
            onError: (Throwable) -> Unit = handleError,
            onComplete: () -> Unit = {},
            onSuccess: (T) -> Unit = {}) {
        compDisposable.add(
                maybe
                        .subscribeOn(scheduler.io())
                        .observeOn(scheduler.ui())
                        .subscribe(onSuccess, onError, onComplete)
        )
    }


    override fun onCleared() {
        compDisposable.clear()
        super.onCleared()
    }
}