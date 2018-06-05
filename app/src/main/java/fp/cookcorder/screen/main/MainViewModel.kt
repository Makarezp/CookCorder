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

    fun requestNewRecord() {
        recorder.startRecording("r")
    }

    fun cancelRecording() {
         recorder.cancelRecording()
    }

    fun finishRecording() {
        exe(recorder.finishRecording()) {
            Timber.d(it.toString())
        }
    }

    fun playRecording() {
        player.startPlaying("r")
    }
}
