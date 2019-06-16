package fp.cookcorder.intentmodel

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

interface StateSubscriber<S> {
    fun Observable<S>.subscribeToState(): Disposable
}