package fp.cookcorder.intentmodel.record

import fp.cookcorder.intentmodel.ModelStore
import fp.cookcorder.intentmodel.intent
import fp.cookcorder.intentmodel.record.RecorderState.Event.Empty
import fp.cookcorder.intentmodel.record.RecorderStatus.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RecordModelStore @Inject constructor() : ModelStore<RecorderState>(RecorderState(
        titleForFinishedRecording = "",
        minsToSchedule = 0,
        repeats = 0,
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

    object Idle : RecorderStatus()

    data class Recording(
            val currentTime: Long = 0,
            // Indicates that this is first recording state so it's possible to detect when
            // that state transitioned from idle to recording
            val justStarted: Boolean = true) : RecorderStatus() {

        val currentTimeString: String
            get() {
                val miliseconds = (currentTime * 10) % 100
                val seconds = (currentTime / 10) % 60
                val minutes = currentTime / 600
                return "${if (minutes == 0L) {
                    "00"
                } else String.format("%02d", minutes)
                }:${String.format("%02d", seconds)}:${
                if (miliseconds == 0L) String.format("%02d", miliseconds) else miliseconds.toString()}"
            }

        fun cancel(): RecorderStatus = Cancelled
        fun finishRecording() = Success
        fun failRecording() = Failed

    }

    object Cancelled : RecorderStatus(), Idlable

    object Failed : RecorderStatus(), Idlable

    object Success : RecorderStatus(), Idlable
}

