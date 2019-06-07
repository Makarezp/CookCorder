package fp.cookcorder.view

sealed class RecordViewEvent {
    object StartRecordingClick: RecordViewEvent()
    data class FinishRecordingTask(
            val milisecondsToSchedule: Long,
            val title: String?,
            val repeats: Int): RecordViewEvent()
    object CancelRecordingClick: RecordViewEvent()
}