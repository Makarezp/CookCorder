package fp.cookcorder.view

sealed class RecordViewEvent {
    object RequestRecordingClick : RecordViewEvent()
    object FinishRecordingClick : RecordViewEvent()

    object CancelRecordingClick : RecordViewEvent()
    data class TitleTextChanged(val text: String) : RecordViewEvent()
    data class MinsToScheduleChanged(val mins: Int) : RecordViewEvent()
    data class RecordPermissionGranted(val isGranted: Boolean) : RecordViewEvent()
}