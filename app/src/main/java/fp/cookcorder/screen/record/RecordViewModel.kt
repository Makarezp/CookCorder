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

    val shouldShowRecordingScreen = MutableLiveData<Boolean>()

    var permissionGranted = false

    val requestRecordingPermission = SingleLiveEvent<Void>()

    val adapter = recordCellController.adapter

    @Inject
    fun init() {
        recordCellController.viewModel = this
        recordCellController.setData(1.rangeTo(5).toList())
    }

    fun requestNewRecord() {
        if (permissionGranted) {
            exe(taskManager.startRecordingNewTask()) {
                shouldShowRecordingScreen.value = true
            }
        } else requestRecordingPermission.call()
    }

    fun cancelRecording() {
        exe(taskManager.cancelRecordingNewTask()) {
            shouldShowRecordingScreen.value = false
        }
    }

    fun finishRecording() {
        exe(taskManager.finishRecordingNewTask(), onError = {
            Timber.d(it)
            shouldShowRecordingScreen.value = false
        }) {
            shouldShowRecordingScreen.postValue(false)
        }

    }
}

