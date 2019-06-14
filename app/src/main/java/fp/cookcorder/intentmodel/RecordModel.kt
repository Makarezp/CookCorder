package fp.cookcorder.intentmodel

import fp.cookcorder.domain.record.RecordUseCase
import fp.cookcorder.intent.Intent
import fp.cookcorder.intent.applySchedulers
import fp.cookcorder.intent.intent
import fp.cookcorder.intent.sideEffect
import fp.cookcorder.intentmodel.RecorderStatus.*
import fp.cookcorder.view.RecordViewEvent
import fp.cookcorder.view.RecordViewEvent.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordModelStore @Inject constructor() : ModelStore<RecorderState>(
        RecorderState(
                titleForFinishedRecording = "",
                timeToScheduleInMiliseconds = 0,
                repeats = 1,
                recorderStatus = Idle
        )
) {
    fun applyRecordIntent(block: RecorderStatus.() -> RecorderStatus) {
        process(intent {
            copy(recorderStatus = block(this.recorderStatus))
        })
    }
}

data class RecorderState(
        val titleForFinishedRecording: String,
        val timeToScheduleInMiliseconds: Long,
        val repeats: Int,
        val recorderStatus: RecorderStatus)

sealed class RecorderStatus {

    interface Idlable {
        fun idle() = Idle
    }

    object Idle : RecorderStatus() {
        fun recording(progress: Long): RecorderStatus = Recording(progress)
    }

    data class Recording(val currentTime: Long = 0) : RecorderStatus() {
        fun cancel(): RecorderStatus = Cancelled
        fun finishRecording() = Success
        fun failRecording() = Failed
    }

    object Cancelled : RecorderStatus(), Idlable

    object Failed : RecorderStatus(), Idlable

    object Success : RecorderStatus(), Idlable
}

@Singleton
class RecordIntentFactory @Inject constructor(
        private val recordUseCase: RecordUseCase,
        private val recordModelStore: RecordModelStore) {

    private val timerDisposable = CompositeDisposable()

    fun process(viewEvent: RecordViewEvent) {
        recordModelStore.process(toIntent(viewEvent))
    }

    private fun toIntent(viewEvent: RecordViewEvent): Intent<RecorderState> {
        return when (viewEvent) {
            is StartRecordingClick -> buildStartRecordingIntent()
            is FinishRecordingClick -> buildFinishRecordingIntent()
            is CancelRecordingClick -> buildCancelRecordingIntent()
        }
    }

    private fun buildStartRecordingIntent(): Intent<RecorderState> = sideEffect {

        fun updateRecordingState(timer: Long) = recordModelStore.process(intent {
            copy(recorderStatus = Recording(timer))
        })

        timerDisposable += recordUseCase
                .startRecordingNewTask()
                .applySchedulers()
                .subscribe({ updateRecordingState(it) }, Timber::e)
    }

    private fun buildCancelRecordingIntent(): Intent<RecorderState> =
            sideEffect {
                this.recorderStatus as Recording
                fun processCancelRecord() {
                    recorderIntentBuilder {
                        this as Recording
                        timerDisposable.clear()
                        cancel()

                    }
                    recorderIntentBuilder {
                        this as Cancelled
                        idle()
                    }
                }
                recordUseCase
                        .cancelRecordingNewTask()
                        .applySchedulers()
                        .subscribe({ processCancelRecord() }, Timber::e)
            }

    private fun buildFinishRecordingIntent(): Intent<RecorderState> = sideEffect {
        this.recorderStatus as Recording
        fun processFinishRecord() {
            recorderIntentBuilder {
                this as Recording
                timerDisposable.clear()
                finishRecording()
            }
            recorderIntentBuilder {
                this as Success
                idle()
            }
        }

        fun processUnsuccessfulRecording(recordingError: Throwable) {
            Timber.d(recordingError)
            recordModelStore.applyRecordIntent {
                this as Recording
                timerDisposable.clear()
                failRecording()
            }
            recorderIntentBuilder {
                this as Failed
                idle()
            }
        }

        recordUseCase
                .finishRecordingNewTask(timeToScheduleInMiliseconds,
                        titleForFinishedRecording, repeats)
                .applySchedulers()
                .subscribe({ processFinishRecord() }, ::processUnsuccessfulRecording)

    }

    private fun recorderIntentBuilder(block: RecorderStatus.() -> RecorderStatus) {
        recordModelStore.process(intent {
            copy(recorderStatus = block(this.recorderStatus))
        })
    }
}
