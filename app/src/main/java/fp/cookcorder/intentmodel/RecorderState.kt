package fp.cookcorder.intentmodel

sealed class RecorderState {

    interface Idlable {
        fun idle() = Idle
    }

    object Idle: RecorderState() {
        fun recording(progress: Long): RecorderState = Recording(progress)
    }

    data class Recording(val currentTime: Long = 0): RecorderState() {
        fun cancel(): RecorderState = Cancelled
        fun finishRecording() = Success
        fun failRecording() = Failed
    }

    object Cancelled: RecorderState(), Idlable

    object Failed: RecorderState(), Idlable

    object Success: RecorderState(), Idlable
}