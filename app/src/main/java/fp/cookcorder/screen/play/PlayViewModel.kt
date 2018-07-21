package fp.cookcorder.screen.play

import android.arch.lifecycle.MutableLiveData
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.service.Player
import timber.log.Timber
import javax.inject.Inject

class PlayViewModel @Inject constructor(
        private val player: Player,
        private val taskRepo: TaskRepo
): BaseViewModel(), TaskAdapter.TaskClickListener {

    val tasks = MutableLiveData<List<Task>>()

    @Inject
    fun init() {
        exe(taskRepo.getTasks()) {
            tasks.value = it
        }
    }

    override fun onPlay(task: Task) {
        player.startPlaying(task.name)
    }

    override fun onDelete(task: Task) {
        exe(taskRepo.deleteTask(task)) {
            Timber.d("Task with id ${task.name} has been deleted")
        }
    }
}
