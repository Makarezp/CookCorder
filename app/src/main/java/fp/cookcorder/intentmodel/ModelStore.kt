package fp.cookcorder.intentmodel

import com.jakewharton.rxrelay2.PublishRelay
import fp.cookcorder.intent.Intent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

open class ModelStore<S>(startingState: S): Model<S> {

    private val intents = PublishRelay.create<Intent<S>>()

    private val store = intents
            .observeOn(AndroidSchedulers.mainThread())
            .scan(startingState) { oldState, intent -> intent.reduce(oldState) }
            .replay(1)
            .apply { connect() }

    private val internalDisposable = store.subscribe(::internalLogger, ::crashHandler)

    private fun internalLogger(state: S) = Timber.i("$state")

    private fun crashHandler(throwable: Throwable): Unit = throw throwable

    override fun modelState(): Observable<S> = store

    override fun process(intent: Intent<S>) = intents.accept(intent)
}
