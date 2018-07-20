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
        exe(recorder
                .finishRecording()
                .doAfterSuccess {
                    taskRepo.saveTask(Task(it.fileName, it.duration))
                    shouldShowRecordingScreen.postValue(false)
                },
                onError = {
                    Timber.d(it)
                    shouldShowRecordingScreen.value = false
                })

    }
}

