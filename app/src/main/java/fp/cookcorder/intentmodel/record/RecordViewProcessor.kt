package fp.cookcorder.intentmodel.record

import fp.cookcorder.intentmodel.Intent
import fp.cookcorder.intentmodel.intent
import fp.cookcorder.interactors.record.RecorderInteractor
import fp.cookcorder.utils.applySchedulers
import fp.cookcorder.intentmodel.record.RecorderState.Event.RequestRecordingPermission
import fp.cookcorder.intentmodel.record.RecorderStatus.*
import fp.cookcorder.utils.isToday
import fp.cookcorder.utils.minutestToMilliseconds
import fp.cookcorder.intentmodel.record.RecordViewEvent.*
import fp.cookcorder.intentmodel.sideEffect
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordViewProcessor @Inject constructor(
        private val recorderInteractor: RecorderInteractor,
        private val recordModelStore: RecordModelStore) {

    private val timerDisposable = CompositeDisposable()

    fun process(viewEvent: RecordViewEvent) {
        recordModelStore.process(toIntent(viewEvent))
    }

    private fun toIntent(viewEvent: RecordViewEvent): Intent<RecorderState> {
        return when (viewEvent) {
            is RequestRecordingClick -> buildStartRecordingIntent()
            is FinishRecordingClick -> buildFinishRecordingIntent()
            is CancelRecordingClick -> buildCancelRecordingIntent()
            is TitleTextChanged -> buildChangeTitleIntent(viewEvent.text)
            is MinsToScheduleChanged -> buildScheduleTimeChangeIntent(viewEvent.mins)
            is RecordPermissionGranted -> buildRecordPermissionGrantedIntent(viewEvent.isGranted)
        }
    }

    private fun buildRecordPermissionGrantedIntent(granted: Boolean) = intent<RecorderState> {
        copy(isRecordPermissionGranted = granted)
    }

    private fun buildScheduleTimeChangeIntent(minsToSchedule: Int) = intent<RecorderState> {
        val now = LocalDateTime.now()
        val alarmTime = now.plusMinutes(minsToSchedule.toLong())
        val alarmTimeUi = alarmTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        copy(minsToSchedule = minsToSchedule,
                isToday = alarmTime.isToday(),
                uiAlarmTime = alarmTimeUi)
    }

    private fun buildChangeTitleIntent(text: String) = intent<RecorderState> {
        copy(titleForFinishedRecording = text)
    }

    private fun buildStartRecordingIntent(): Intent<RecorderState> = sideEffect {

        if (this.isRecordPermissionGranted) {
            var firsRecordingEvent = true

            fun updateRecordingState(timer: Long) = recordModelStore.process(intent {
                val recordState = copy(recorderStatus = Recording(timer, firsRecordingEvent))
                firsRecordingEvent = false
                recordState
            })

            timerDisposable += recorderInteractor
                    .startRecordingNewTask()
                    .applySchedulers()
                    .subscribe({ updateRecordingState(it) }, Timber::e)
        } else {
            recordModelStore.processInstantEvent(intent {
                copy(event = RequestRecordingPermission)
            })
        }
    }

    private fun buildCancelRecordingIntent(): Intent<RecorderState> =
            sideEffect {
                if (this.recorderStatus is Recording) {
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
                    recorderInteractor
                            .cancelRecordingNewTask()
                            .applySchedulers()
                            .subscribe({ processCancelRecord() }, Timber::e)
                } else Timber.d("Can't cancel if is not recording")
            }

    private fun buildFinishRecordingIntent(): Intent<RecorderState> = sideEffect {
        if (this.recorderStatus is Recording) {
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

            recorderInteractor
                    .finishRecordingNewTask(minsToSchedule.minutestToMilliseconds(),
                            titleForFinishedRecording, repeats)
                    .applySchedulers()
                    .subscribe({ processFinishRecord() }, ::processUnsuccessfulRecording)
        } else Timber.d("Can't finish if is not recording")

    }

    private fun recorderIntentBuilder(block: RecorderStatus.() -> RecorderStatus) {
        recordModelStore.process(intent {
            copy(recorderStatus = block(this.recorderStatus))
        })
    }
}