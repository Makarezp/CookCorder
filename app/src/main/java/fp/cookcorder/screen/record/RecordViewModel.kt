package fp.cookcorder.screen.record

import android.arch.lifecycle.MutableLiveData
import fp.cookcorder.app.util.minutestToMilliseconds
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.screen.utils.SingleLiveEvent
import fp.cookcorder.manager.TaskManager
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class RecordViewModel @Inject constructor(
        private val taskManager: TaskManager
) : BaseViewModel() {

    val isRecording = MutableLiveData<Boolean>()

    var permissionGranted = false

    val requestRecordingPermission = SingleLiveEvent<Void>()

    var blockStartingNewRecording = false

    fun requestNewRecord() {
        if (permissionGranted) {
            exe(taskManager.startRecordingNewTask()) {
                isRecording.value = true
            }
        } else requestRecordingPermission.call()
    }

    fun cancelRecording() {
        exe(taskManager.cancelRecordingNewTask()) {
            isRecording.value = false
        }
    }

    fun finishRecording(minutesToSchedule: Int) {
        exe(taskManager.finishRecordingNewTask(minutesToSchedule.minutestToMilliseconds()),
                onError = {
            Timber.d(it)
            isRecording.value = false
        }) {
            isRecording.postValue(false)
        }
    }

}

