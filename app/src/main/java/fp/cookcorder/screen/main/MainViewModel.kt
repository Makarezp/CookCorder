package fp.cookcorder.screen.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.Handler
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.service.Player
import fp.cookcorder.service.Recorder
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject constructor(
        private val recorder: Recorder,
        private val player: Player,
        private val taskRepo: TaskRepo
) : BaseViewModel(), TaskAdapter.TaskClickListener {

    val shouldShowRecordingScreen = MutableLiveData<Boolean>()

    var permissionGranted = false

    val tasks = MutableLiveData<List<Task>>()

    @Inject
    fun init() {
        exe(taskRepo.getTasks()) {
            tasks.value = it
        }
    }

    fun requestNewRecord() {
        if (permissionGranted) {
            exe(recorder.startRecording("r${Random().nextInt()}")) {
                shouldShowRecordingScreen.value = true
            }
        }
    }

    fun cancelRecording() {
        exe(recorder.cancelRecording()) {
            shouldShowRecordingScreen.value = false
        }
    }

    fun finishRecording() {
        val onError = { e: Throwable ->
            Timber.d(e)
            shouldShowRecordingScreen.value = false
        }
        exe(recorder
                .finishRecording()
                .flatMapCompletable { taskRepo.saveTask(Task(10L, it.fileName, it.duration)) },
                onError) {
            shouldShowRecordingScreen.value = false
        }
    }

    override fun onTaskClicked(task: Task) {
        player.startPlaying(task.name)
    }
}
