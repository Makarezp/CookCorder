package fp.cookcorder.intent

import fp.cookcorder.domain.record.RecordUseCase
import fp.cookcorder.intentmodel.RecordModelStore
import fp.cookcorder.intentmodel.RecorderState
import fp.cookcorder.intentmodel.RecorderState.*
import fp.cookcorder.view.RecordViewEvent
import fp.cookcorder.view.RecordViewEvent.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordIntentFactory @Inject constructor(
        private val recordUseCase: RecordUseCase,
        private val recordModelStore: RecordModelStore) {

    fun process(viewEvent: RecordViewEvent) {
        recordModelStore.process(toIntent(viewEvent))
    }

    private fun toIntent(viewEvent: RecordViewEvent): Intent<RecorderState> {
        return when (viewEvent) {
            is StartRecordingClick -> buildStartRecordingIntent()
            is FinishRecordingClick -> buildFinishRecordingIntent(viewEvent)
            is CancelRecordingClick -> buildCancelRecordingIntent()
        }
    }

    private val timerDisposable = CompositeDisposable()

    private fun buildStartRecordingIntent(): Intent<RecorderState> = recorderSideEffect<Idle> {

        fun updateRecordingState(timer: Long) = recordModelStore.process(intent {
            Recording(timer)
        })

        timerDisposable += recordUseCase
                .startRecordingNewTask()
                .applySchedulers()
                .subscribe({ updateRecordingState(it) }, Timber::e)

    }

    private fun buildCancelRecordingIntent() = recorderSideEffect<Recording> {

        fun processCancelRecord() {
            recordModelStore.process(recorderIntent<Recording> {
                timerDisposable.clear()
                cancel()
            })
            recordModelStore.process(recorderIntent<Cancelled> {
                idle()
            })
        }
        recordUseCase
                .cancelRecordingNewTask()
                .applySchedulers()
                .subscribe({ processCancelRecord() }, Timber::e)

    }

    private fun buildFinishRecordingIntent(viewEvent: FinishRecordingClick) = recorderSideEffect<Recording> {

        fun processFinishRecord() {
            recordModelStore.process(recorderIntent<Recording> {
                timerDisposable.clear()
                finishRecording()
            })
            recordModelStore.process(recorderIntent<Success> {
                idle()
            })
        }

        fun processUnsuccessfulRecording(recordingError: Throwable) {
            Timber.d(recordingError)
            recordModelStore.process(recorderIntent<Recording> {
                timerDisposable.clear()
                failRecording()
            })
            recordModelStore.process(recorderIntent<Failed> {
                idle()
            })
        }

        with(viewEvent) {
            recordUseCase
                    .finishRecordingNewTask(milisecondsToSchedule, title, repeats)
                    .applySchedulers()
                    .subscribe({ processFinishRecord() }, ::processUnsuccessfulRecording)
        }
    }

    companion object {
        inline fun <reified S : RecorderState> recorderIntent(
                crossinline block: S.() -> RecorderState
        ): Intent<RecorderState> = intent {
            (this as? S)?.block() ?: throw java.lang.IllegalStateException(
                    "Inconsistent state should be ${this.javaClass.canonicalName}")
        }

    }

    inline fun <reified S : RecorderState> recorderSideEffect(
            crossinline block: S.() -> Unit
    ): Intent<RecorderState> = sideEffect {
        (this as? S)?.apply(block) ?: throw java.lang.IllegalStateException(
                "Inconsistent state should be ${this.javaClass.canonicalName}")
    }

}