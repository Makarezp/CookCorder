package fp.cookcorder.intentmodel

import fp.cookcorder.intent.intent
import fp.cookcorder.intentmodel.RecorderState.Event.Empty
import fp.cookcorder.intentmodel.RecorderStatus.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RecordModelStore @Inject constructor() : ModelStore<RecorderState>(RecorderState(
        titleForFinishedRecording = "",
        minsToSchedule = 0,
        repeats = 1,
        recorderStatus = Idle
)) {
    fun applyRecordIntent(block: RecorderStatus.() -> RecorderStatus) {
        process(intent {
            copy(recorderStatus = block(this.recorderStatus))
        })
    }
}

data class RecorderState(
        val titleForFinishedRecording: String,
        val minsToSchedule: Int, // How many minutes from finishing recording alarm will be scheduled
        val repeats: Int, // How many times alarm will repeat recording
        val recorderStatus: RecorderStatus,
        val isRecordPermissionGranted: Boolean = false,
        val uiAlarmTime: String = "", // String representing time when alarm will be fired
        val isToday: Boolean = true, // Is alarm schedule for today?
        val event: Event = Empty) {
    sealed class Event {
        object Empty : Event()
        object RequestRecordingPermission : Event()
    }
}

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

