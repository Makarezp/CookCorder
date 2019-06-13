package fp.cookcorder.intentmodel

import fp.cookcorder.domain.record.RecordUseCase
import fp.cookcorder.intent.Intent
import fp.cookcorder.intent.applySchedulers
import fp.cookcorder.intent.intent
import fp.cookcorder.intent.sideEffect
import fp.cookcorder.intentmodel.RecorderState.*
import fp.cookcorder.view.RecordViewEvent
import fp.cookcorder.view.RecordViewEvent.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordModelStore @Inject constructor(): ModelStore<RecorderState>(Idle)

sealed class RecorderState {

    interface Idlable {
        fun idle() = Idle
    }

    object Idle : RecorderState() {
        fun recording(progress: Long): RecorderState = Recording(progress)
    }

    data class Recording(val currentTime: Long = 0) : RecorderState() {
        fun cancel(): RecorderState = Cancelled
        fun finishRecording() = Success
        fun failRecording() = Failed
    }

    object Cancelled : RecorderState(), Idlable

    object Failed : RecorderState(), Idlable

    object Success : RecorderState(), Idlable
}

@Singleton
class RecordIntentFactory @Inject constructor(
        private val recordUseCase: RecordUseCase,
        private val recordModelStore: RecordModelStore) {

    private val timerDisposable = CompositeDisposable()

    fun process(intent: Intent<RecorderState>) {
        recordModelStore.process(intent)
    }

    private fun toIntent(viewEvent: RecordViewEvent): Intent<RecorderState> {
        return when (viewEvent) {
            is StartRecordingClick -> buildStartRecordingIntent()
            is FinishRecordingClick -> buildFinishRecordingIntent(viewEvent)
            is CancelRecordingClick -> buildCancelRecordingIntent()
        }
    }

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
            chainedRecorderIntent<Recording> {
                timerDisposable.clear()
                cancel()
            }
            chainedRecorderIntent<Cancelled> {
                idle()
            }
        }
        recordUseCase
                .cancelRecordingNewTask()
                .applySchedulers()
                .subscribe({ processCancelRecord() }, Timber::e)
    }

    private fun buildFinishRecordingIntent(viewEvent: FinishRecordingClick) = recorderSideEffect<Recording> {

        fun processFinishRecord() {
            chainedRecorderIntent<Recording> {
                timerDisposable.clear()
                finishRecording()
            }
            chainedRecorderIntent<Success> {
                idle()
            }
        }

        fun processUnsuccessfulRecording(recordingError: Throwable) {
            chainedRecorderIntent<Recording> {
                timerDisposable.clear()
                failRecording()
            }
            chainedRecorderIntent<Failed> {
                idle()
            }
        }

        with(viewEvent) {
            recordUseCase
                    .finishRecordingNewTask(milisecondsToSchedule, title, repeats)
                    .applySchedulers()
                    .subscribe({ processFinishRecord() }, ::processUnsuccessfulRecording)
        }
    }

    private inline fun <reified T : RecorderState> chainedRecorderIntent(
            crossinline block: T.() -> RecorderState) {
        recordModelStore.process(recorderIntent(block))
    }

    companion object {
        inline fun <reified S : RecorderState> recorderIntent(
                crossinline block: S.() -> RecorderState
        ): Intent<RecorderState> = intent {
            (this as? S)?.block() ?: throw IllegalStateException(
                    "Inconsistent state should be ${this.javaClass.canonicalName}")
        }

    }

    inline fun <reified S : RecorderState> recorderSideEffect(
            crossinline block: S.() -> Unit
    ): Intent<RecorderState> = sideEffect {
        (this as? S)?.apply(block) ?: throw IllegalStateException(
                "Inconsistent state should be ${this.javaClass.canonicalName}")
    }
}