package fp.cookcorder.screen

import android.arch.lifecycle.ViewModel
import fp.cookcorder.app.SchedulerFactory
import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

abstract class BaseViewModel : ViewModel() {

    @Inject
    lateinit var schedulerFactory: SchedulerFactory

    protected val compDisposable = CompositeDisposable()

    private val handleError: (Throwable) -> Unit = { Timber.e(it) }

    protected fun <T> exe(
            flowable: Flowable<T>,
            onError: (Throwable) -> Unit = handleError,
            onComplete: () -> Unit = {},
            onNext: (T) -> Unit = {}) {
        compDisposable.add(
                flowable
                        .subscribeOn(schedulerFactory.io())
                        .observeOn(schedulerFactory.ui())
                        .subscribe(onNext, onError, onComplete))
    }

    protected fun <T> exe(
            observable: Observable<T>,
            onError: (Throwable) -> Unit = handleError,
            onComplete: () -> Unit = {},
            onNext: (T) -> Unit = {}) {
        compDisposable.add(
                observable
                        .subscribeOn(schedulerFactory.io())
                        .observeOn(schedulerFactory.ui())
                        .subscribe(onNext, onError, onComplete))
    }

    protected fun <T> exe(
            single: Single<T>,
            onError: (Throwable) -> Unit = handleError,
            onSuccess: (T) -> Unit = {}) {
        compDisposable.add(
                single
                        .subscribeOn(schedulerFactory.io())
                        .observeOn(schedulerFactory.ui())
                        .subscribe(onSuccess, onError))
    }

    protected fun <T> exe(
            maybe: Maybe<T>,
            onError: (Throwable) -> Unit = handleError,
            onComplete: () -> Unit = {},
            onSuccess: (T) -> Unit = {}) {
        compDisposable.add(
                maybe
                        .subscribeOn(schedulerFactory.io())
                        .observeOn(schedulerFactory.ui())
                        .subscribe(onSuccess, onError, onComplete)
        )
    }

    protected fun exe(
            completable: Completable,
            onError: (Throwable) -> Unit = handleError,
            onComplete: () -> Unit) {
        compDisposable.add(
                completable
                        .subscribeOn(schedulerFactory.io())
                        .observeOn(schedulerFactory.ui())
                        .subscribe(onComplete, onError)
        )
    }



    override fun onCleared() {
        compDisposable.clear()
        super.onCleared()
    }
}