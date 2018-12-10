package fp.cookcorder.screen.record

import android.arch.lifecycle.MutableLiveData
import fp.cookcorder.domain.record.RecordUseCase
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.screen.utils.SingleLiveEvent
import fp.cookcorder.screen.utils.isToday
import fp.cookcorder.screen.utils.minutestToMilliseconds
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RecordViewModel @Inject constructor(
        private val recordUseCase: RecordUseCase
) : BaseViewModel() {

    val isRecording = MutableLiveData<Boolean>()

    var permissionGranted = false

    val isToday = MutableLiveData<Boolean>()

    val requestRecordingPermission = SingleLiveEvent<Void>()

    val recordSuccess = MutableLiveData<Any>()

    val recordCancelled = SingleLiveEvent<Void>()

    val currentRecordTime = MutableLiveData<String>()

    var currentMinutesToSchedule = MutableLiveData<Int>()

    val minutes = MutableLiveData<String>()

    var title = MutableLiveData<String>()

    var repeats = MutableLiveData<Int>().apply { value = 0 }

    private val timer = Observable.interval(1, TimeUnit.SECONDS)

    private var recordTimerDisposable: Disposable? = null

    @Inject
    fun init() {
       compDisposable.add(timer
               .observeOn(schedulerFactory.ui())
               .subscribe {
           setMinuteAndHour()
        })

        currentMinutesToSchedule.observeForever {
            setMinuteAndHour()
        }
    }

    private fun setMinuteAndHour() {
        currentMinutesToSchedule.value?.let {
            val now = LocalDateTime.now()
            val alarmTime = now.plusMinutes(it.toLong())
            isToday.value = alarmTime.isToday()
            minutes.value = alarmTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        }

    }

    fun requestNewRecord() {
        if (permissionGranted) {
            exe(recordUseCase.startRecordingNewTask()) { _ ->
                isRecording.value = true
                recordTimerDisposable = recordTimeCounter()
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
            recordTimerDisposable?.dispose()
            resetRecordTime()
        }
    }

    fun finishRecording() {
        exe(recordUseCase
                .finishRecordingNewTask(
                        currentMinutesToSchedule.value!!.minutestToMilliseconds(),
                        title.value,
                        repeats.value ?: 1),
                onError = {
                    Timber.d(it)
                    isRecording.value = false
                    recordTimerDisposable?.dispose()
                }) {
            recordSuccess.value = Any()
            recordSuccess.value = null
            isRecording.postValue(false)
            recordTimerDisposable?.dispose()
        }
    }

    fun setTitle(text: String) {
        title.value = text
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

    private fun resetRecordTime() {
        currentRecordTime.value = "00:00:00"
    }

}

