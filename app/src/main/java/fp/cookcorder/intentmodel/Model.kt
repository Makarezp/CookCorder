package fp.cookcorder.intentmodel

import io.reactivex.Observable

interface Model<S> {
    fun process(intent: Intent<S>)
    fun processInstantEvent(intent: Intent<S>)
    fun modelState(): Observable<S>
}