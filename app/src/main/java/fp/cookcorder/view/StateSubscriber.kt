package fp.cookcorder.view

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

interface StateSubscriber<S> {
    fun Observable<S>.subscribeToState(): Disposable
}