package fp.cookcorder.screen.record

import android.arch.lifecycle.MutableLiveData
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.screen.utils.SingleLiveEvent
import fp.cookcorder.manager.TaskManager
import timber.log.Timber
import javax.inject.Inject

class RecordViewModel @Inject constructor(
        private val taskManager: TaskManager,
        private val recordCellController: RecordCellController
) : BaseViewModel() {

    val isRecording = MutableLiveData<Boolean>()

    var permissionGranted = false

    val requestRecordingPermission = SingleLiveEvent<Void>()

    var blockStartingNewRecording = false

    val adapter = recordCellController.adapter

    @Inject
    fun init() {
        recordCellController.viewModel = this
        recordCellController.setData(1.rangeTo(5).toList())
    }

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

    fun finishRecording(msToSchedule: Long) {
        exe(taskManager.finishRecordingNewTask(msToSchedule), onError = {
            Timber.d(it)
            isRecording.value = false
        }) {
            isRecording.postValue(false)
        }
    }

}

