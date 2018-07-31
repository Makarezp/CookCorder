package fp.cookcorder.screen.record

import android.arch.lifecycle.MutableLiveData
import fp.cookcorder.app.util.minutestToMilliseconds
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.screen.utils.SingleLiveEvent
import fp.cookcorder.manager.TaskManager
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
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

    var recordViewPosition: Pair<Int, Int>? = null

    val successAtPosition = SingleLiveEvent<Int>()

    var requestedMinutes: Int = 0

    val successObservable: Observable<Int>
    get() = successSubject

    private val successSubject = PublishSubject.create<Int>()


    @Inject
    fun init() {
        recordCellController.viewModel = this
        recordCellController.setData(1.rangeTo(100).toList())
    }

    /**
     * [x] view position at which record was requested
     * [y] view position at which record was requested
     */
    fun requestNewRecord(minutes: Int, x: Int, y: Int) {
        if (permissionGranted) {
            exe(taskManager.startRecordingNewTask(minutes.minutestToMilliseconds())) {
                recordViewPosition = x to y
                requestedMinutes = minutes
                isRecording.value = true
            }
        } else requestRecordingPermission.call()
    }

    fun cancelRecording() {
        exe(taskManager.cancelRecordingNewTask()) {
            isRecording.value = false
        }
    }

    fun finishRecording() {
        exe(taskManager.finishRecordingNewTask(), onError = {
            Timber.d(it)
            isRecording.value = false
        }) {
            isRecording.postValue(false)
            successAtPosition.postValue(requestedMinutes)
            successSubject.onNext(requestedMinutes)
        }
    }

}

