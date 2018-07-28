package fp.cookcorder.screen.play

import com.airbnb.epoxy.TypedEpoxyController
import fp.cookcorder.app.SchedulerFactory
import fp.cookcorder.model.Task
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.manager.TaskManager
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PlayViewModel @Inject constructor(
        private val taskManager: TaskManager,
        private val playCellController: PlayCellController
) : BaseViewModel() {

    val adapter = playCellController.adapter

    @Inject
    fun init() {
        playCellController.viewModel = this
        exe(taskManager.getCurrentTasks()) {
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

class PlayCellController @Inject constructor(
        schedulerFactory: SchedulerFactory) : TypedEpoxyController<List<Task>>() {

    lateinit var viewModel: PlayViewModel

    /**
     * This is timer that is shared between all bound cells
     */
    private val timer = Observable.interval(1, TimeUnit.SECONDS)
            .subscribeOn(schedulerFactory.io())
            .observeOn(schedulerFactory.ui())

    override fun buildModels(data: List<Task>) {
        data.forEach {
            playCell {
                id(it.id)
                pcTitle(it.name)
                pcOnPlayClicked { viewModel.play(it) }
                pcOnDeleteClicked { viewModel.delete(it) }
                pcScheduleTime(it.scheduleTime)
                pcTimer(timer)
            }
        }
    }
}