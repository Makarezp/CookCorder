package fp.cookcorder.screen.main

import android.arch.lifecycle.ViewModel
import fp.cookcorder.service.Player
import fp.cookcorder.service.Recorder
import javax.inject.Inject

class MainViewModel @Inject constructor(
        private val recorder: Recorder,
        private val player: Player
): ViewModel() {

    fun requestNewRecord() {
        recorder.startRecording("r")
    }
    fun cancelRecording() {
        recorder.cancelRecording()
    }

    fun finishRecording() {
        recorder.finishRecording()
    }

    fun playRecording() {
        player.startPlaying("r")
    }

}
