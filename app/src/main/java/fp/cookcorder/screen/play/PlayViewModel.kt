package fp.cookcorder.screen.play

import com.airbnb.epoxy.TypedEpoxyController
import fp.cookcorder.model.Task
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.manager.TaskManager
import timber.log.Timber
import javax.inject.Inject

class PlayViewModel @Inject constructor(
        private val taskManager: TaskManager,
        private val playCellController: PlayCellController
) : BaseViewModel() {

    val adapter = playCellController.adapter

    @Inject
    fun init() {
        playCellController.viewModel = this
        exe(taskManager.getTasks()) {
            playCellController.setData(it)
        }

    }

    fun play(task: Task) {
        taskManager.playTask(task)
    }

    fun delete(task: Task) {
        exe(taskManager.deleteTask(task)) {
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
