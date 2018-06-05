package fp.cookcorder.screen.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.Handler
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.service.Player
import fp.cookcorder.service.Recorder
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(
        private val recorder: Recorder,
        private val player: Player
) : BaseViewModel() {

    val shouldShowRecordingScreen = MutableLiveData<Boolean>()

    var permissionGranted = false

    fun requestNewRecord() {
        if(permissionGranted) {
            recorder.startRecording("r")
            shouldShowRecordingScreen.value = true
        }
    }

    fun cancelRecording() {
        recorder.cancelRecording()
        shouldShowRecordingScreen.value = false
    }

    fun finishRecording() {
        exe(recorder.finishRecording()) {
            Timber.d(it.toString())
            shouldShowRecordingScreen.value = false
        }
    }

    fun playRecording() {
        player.startPlaying("r")
    }
}
