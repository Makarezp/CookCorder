package fp.cookcorder.screen.play

import com.airbnb.epoxy.TypedEpoxyController
import fp.cookcorder.app.SchedulerFactory
import fp.cookcorder.model.Task
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.manager.TaskManager
import fp.cookcorder.screen.utils.getDateTimeFromEpoch
import fp.cookcorder.screen.utils.getTimeFromEpoch
import io.reactivex.Observable
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class PlayViewModel @Inject constructor(
        private val taskManager: TaskManager,
        private val playCellController: PlayCellController,
        @Named(PlayFragmentModule.NAMED_IS_CURRENT) private val isCurrent: Boolean
) : BaseViewModel() {

    val adapter = playCellController.adapter

    @Inject
    fun init() {
        playCellController.viewModel = this
        val taskObs = if (isCurrent) taskManager.getCurrentTasks() else taskManager.getPastTasks()
        exe(taskObs) {
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
        schedulerFactory: SchedulerFactory,
        @Named(PlayFragmentModule.NAMED_IS_CURRENT) private val isCurrent: Boolean)
    : TypedEpoxyController<List<Task>>() {

    lateinit var viewModel: PlayViewModel

    /**
     * This is timer that is shared between all bound cells
     */
    private val timer = Observable.interval(1, TimeUnit.SECONDS)
            .subscribeOn(schedulerFactory.io())
            .observeOn(schedulerFactory.ui())




    override fun buildModels(data: List<Task>) {
        data.sortedBy { it.scheduleTime }.forEach {
            playCell {
                id(it.id)
                pcTitle(it.title)
                pcTimePlayed(
                        if (isCurrent) getTimeFromEpoch(it.scheduleTime)
                        else getDateTimeFromEpoch(it.scheduleTime)
                )
                pcOnPlayClicked { viewModel.play(it) }
                pcOnDeleteClicked { viewModel.delete(it) }
                pcScheduleTime(it.scheduleTime)
                if (isCurrent) pcTimer(timer)
            }
        }
    }
}

