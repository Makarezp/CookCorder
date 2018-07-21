package fp.cookcorder.screen.play

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
) : BaseViewModel() {

    val adapter = playCellController.adapter

    @Inject
    fun init() {
        playCellController.viewModel = this
        exe(taskRepo.getTasks()) {
            playCellController.setData(it)
        }

    }

    fun play(task: Task) {
        player.startPlaying(task.name)
    }

    fun delete(task: Task) {
        exe(taskRepo.deleteTask(task)) {
            Timber.d("Task with id ${task.name} has been deleted")
        }
    }
}

class PlayCellController @Inject constructor() : TypedEpoxyController<List<Task>>() {

    lateinit var viewModel: PlayViewModel

    override fun buildModels(data: List<Task>) {
        data.forEach {
            playCell {
                id(it.name)
                pcTitle(it.name)
                pcOnPlayClicked { viewModel.play(it) }
                pcOnDeleteClicked { viewModel.delete(it) }
            }
        }
    }
}
