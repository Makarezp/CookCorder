package fp.cookcorder.intentmodel

import fp.cookcorder.intent.Intent
import io.reactivex.Observable

interface Model<S> {
    fun process(intent: Intent<S>)
    fun modelState(): Observable<S>
}