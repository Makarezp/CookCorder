package fp.cookcorder.screen.play

import android.arch.lifecycle.MutableLiveData
import com.airbnb.epoxy.TypedEpoxyController
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.service.Player
import timber.log.Timber
import javax.inject.Inject

class PlayViewModel @Inject constructor(
        private val player: Player,
        private val taskRepo: TaskRepo,
        private val playCellController: PlayCellController
) : BaseViewModel(), PlayCellController.TaskClickListener {

    val adapter = playCellController.adapter

    @Inject
    fun init() {
        playCellController.clickListener = this
        exe(taskRepo.getTasks()) {
            playCellController.setData(it)
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

class PlayCellController @Inject constructor() : TypedEpoxyController<List<Task>>() {

    interface TaskClickListener {
        fun onPlay(task: Task)
        fun onDelete(task: Task)
    }

    lateinit var clickListener: TaskClickListener

    override fun buildModels(data: List<Task>) {
        data.forEach {
            playCell {
                id(it.name)
                pcTitle(it.name)
                pcOnPlayClicked { clickListener.onPlay(it) }
                pcOnDeleteClicked { clickListener.onDelete(it) }
            }
        }


    }
}
