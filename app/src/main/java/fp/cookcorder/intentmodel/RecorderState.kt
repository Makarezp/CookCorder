package fp.cookcorder.intentmodel

sealed class RecorderState {

    object Idle: RecorderState() {
        fun startRecording(): RecorderState = Recording()
    }

    data class Recording(val currentTime: Long = 0): RecorderState() {
        fun cancel(): RecorderState = Cancelled
        fun finishRecording() = Success
    }

    object Cancelled: RecorderState() {
        fun idle(): RecorderState = Idle
    }

    object Success: RecorderState() {
        fun idle(): RecorderState = Idle
    }
}