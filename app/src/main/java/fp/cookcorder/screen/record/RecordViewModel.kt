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
                        .subscribeOn(schedulerFactory.io())
                        .observeOn(schedulerFactory.ui())
                        .subscribe { currentRecordTime.value = it }
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

    fun finishRecording(minutesToSchedule: Int, title: String?) {
        exe(taskManager.finishRecordingNewTask(minutesToSchedule.minutestToMilliseconds(), title),
                onError = {
                    Timber.d(it)
                    isRecording.value = false
                    timerDisposable?.dispose()
                }) {
            recordSuccess.call()
            isRecording.postValue(false)
            timerDisposable?.dispose()
        }
    }

    private fun recordTimeCounter(): Observable<String> {
        //this code is so weird because of memory optimizations
        return Observable.concat(
                Observable.just(0L),
                Observable.interval(100, TimeUnit.MILLISECONDS))
                .map {
                    val miliseconds =(it * 10) % 100
                    val seconds = (it / 10) % 60
                    val minutes = it / 600
                    "${if(minutes == 0L) {
                        "00"
                    } else String.format("%02d", minutes)
                    }:${String.format("%02d", seconds)}:${
                    if(miliseconds == 0L) String.format("%02d", miliseconds) else miliseconds.toString()}"
                }
    }

}

