package fp.cookcorder.screen.record

import android.arch.lifecycle.MutableLiveData
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.service.Recorder
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class RecordViewModel @Inject constructor(
        private val recorder: Recorder,
        private val taskRepo: TaskRepo
) : BaseViewModel() {

    val shouldShowRecordingScreen = MutableLiveData<Boolean>()

    var permissionGranted = false

    fun requestNewRecord() {
        if (permissionGranted) {
            exe(recorder.startRecording("r${Random().nextInt()}")) {
                shouldShowRecordingScreen.value = true
            }
        }
    }

    fun cancelRecording() {
        exe(recorder.cancelRecording()) {
            shouldShowRecordingScreen.value = false
        }
    }

    fun finishRecording() {
        val onError = { e: Throwable ->
            Timber.d(e)
            shouldShowRecordingScreen.value = false
        }
        exe(recorder
                .finishRecording()
                .flatMapCompletable { taskRepo.saveTask(Task(10L, it.fileName, it.duration)) },
                onError) {
            shouldShowRecordingScreen.value = false
        }
    }
}
