package fp.cookcorder.view

sealed class RecordViewEvent {
    object StartRecordingClick: RecordViewEvent()
    data class FinishRecordingClick(
            val milisecondsToSchedule: Long,
            val title: String?,
            val repeats: Int): RecordViewEvent()
    object CancelRecordingClick: RecordViewEvent()
    data class TitleTextChanged(val text: String): RecordViewEvent()
    data class MinsToScheduleChanged(val mins: Int): RecordViewEvent()
}