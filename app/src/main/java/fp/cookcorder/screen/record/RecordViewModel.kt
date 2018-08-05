package fp.cookcorder.screen.record

import android.arch.lifecycle.MutableLiveData
import fp.cookcorder.app.util.minutestToMilliseconds
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.screen.utils.SingleLiveEvent
import fp.cookcorder.manager.TaskManager
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RecordViewModel @Inject constructor(
        private val taskManager: TaskManager
) : BaseViewModel() {

    val isRecording = MutableLiveData<Boolean>()

    var permissionGranted = false

    val requestRecordingPermission = SingleLiveEvent<Void>()

    var blockStartingNewRecording = false

    val recordSuccess = SingleLiveEvent<Void>()

    val recordCancelled = SingleLiveEvent<Void>()

    val currentRecordTime = MutableLiveData<String>()

    private var timerDisposable: Disposable? = null

    fun requestNewRecord() {
        if (permissionGranted) {
            exe(taskManager.startRecordingNewTask()) { _ ->
                isRecording.value = true
                timerDisposable = recordTimeCounter()
                        .subscribe { currentRecordTime.postValue(it) }
            }
        } else requestRecordingPermission.call()
    }

    fun cancelRecording() {
        exe(taskManager.cancelRecordingNewTask()) {
            isRecording.value = false
            recordCancelled.call()
            timerDisposable?.dispose()
        }
    }

    fun finishRecording(minutesToSchedule: Int) {
        exe(taskManager.finishRecordingNewTask(minutesToSchedule.minutestToMilliseconds()),
                onError = {
                    Timber.d(it)
                    isRecording.value = false
                }) {
            recordSuccess.call()
            isRecording.postValue(false)
            timerDisposable?.dispose()
        }
    }

    private fun recordTimeCounter(): Observable<String> {
        return Observable.interval(1, TimeUnit.MILLISECONDS).map {
            val milliseconds = it % 100
            val seconds = (it / 1000) % 60
            val minutes = (it / 1000) / 60
            "${String.format("%02d", minutes)}:${String.format("%02d", seconds)}:${String.format("%02d", milliseconds)}"
        }
    }

}

