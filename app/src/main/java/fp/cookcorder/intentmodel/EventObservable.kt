package fp.cookcorder.intentmodel

import io.reactivex.Observable

interface EventObservable<E> {
    fun events(): Observable<E>
}