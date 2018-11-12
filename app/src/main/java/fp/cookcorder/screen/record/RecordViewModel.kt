package fp.cookcorder.screen.record

import android.arch.lifecycle.MutableLiveData
import fp.cookcorder.domain.record.RecordUseCase
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.screen.utils.SingleLiveEvent
import fp.cookcorder.screen.utils.minutestToMilliseconds
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RecordViewModel @Inject constructor(
        private val recordUseCase: RecordUseCase
) : BaseViewModel() {

    val isRecording = MutableLiveData<Boolean>()

    var permissionGranted = false

    val requestRecordingPermission = SingleLiveEvent<Void>()

    val recordSuccess = MutableLiveData<Any>()

    val recordCancelled = SingleLiveEvent<Void>()

    val currentRecordTime = MutableLiveData<String>()

    var currentMinutesToSchedule = 0

    var title: String? = null

    var repeats = MutableLiveData<Int>().apply { value = 1 }

    private var timerDisposable: Disposable? = null

    fun requestNewRecord() {
        if (permissionGranted) {
            exe(recordUseCase.startRecordingNewTask()) { _ ->
                isRecording.value = true
                timerDisposable = recordTimeCounter()
                        .subscribeOn(schedulerFactory.io())
                        .observeOn(schedulerFactory.ui())
                        .subscribe { currentRecordTime.value = it }
            }
        } else requestRecordingPermission.call()
    }

    fun cancelRecording() {
        exe(recordUseCase.cancelRecordingNewTask()) {
            isRecording.value = false
            recordCancelled.call()
            timerDisposable?.dispose()
        }
    }

    fun finishRecording() {
        exe(recordUseCase
                .finishRecordingNewTask(
                        currentMinutesToSchedule.minutestToMilliseconds(),
                        title,
                        repeats.value ?: 1),
                onError = {
                    Timber.d(it)
                    isRecording.value = false
                    timerDisposable?.dispose()
                }) {
            recordSuccess.value = Any()
            recordSuccess.value = null
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

