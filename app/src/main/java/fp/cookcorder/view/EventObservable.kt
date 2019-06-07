package fp.cookcorder.view

import io.reactivex.Observable

interface EventObservable<E> {
    fun events(): Observable<E>
}