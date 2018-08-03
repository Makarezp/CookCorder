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
                        .doOnSubscribe { currentRecordTime.postValue("00:00") }
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
        return Observable.interval(1, TimeUnit.SECONDS).map {
            val seconds = (it) % 60
            val minutes = (it / 60 * 60) % 60
            "${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
        }
    }

}

