package fp.cookcorder.screen.record

import android.arch.lifecycle.MutableLiveData
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.screen.utils.SingleLiveEvent
import fp.cookcorder.service.Recorder
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class RecordViewModel @Inject constructor(
        private val recorder: Recorder,
        private val taskRepo: TaskRepo,
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
            exe(recorder.startRecording("r${Random().nextInt()}")) {
                shouldShowRecordingScreen.value = true
            }
        } else requestRecordingPermission.call()
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

