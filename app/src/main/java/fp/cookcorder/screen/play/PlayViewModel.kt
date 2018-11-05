package fp.cookcorder.screen.play

import android.arch.lifecycle.MutableLiveData
import com.airbnb.epoxy.TypedEpoxyController
import fp.cookcorder.app.SchedulerFactory
import fp.cookcorder.domain.managetaskusecase.ManageTaskUseCase
import fp.cookcorder.domain.play.PlayUseCase
import fp.cookcorder.model.Task
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.screen.utils.SingleLiveEvent
import fp.cookcorder.screen.utils.getDateTimeFromEpoch
import fp.cookcorder.screen.utils.getTimeFromEpoch
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class PlayViewModel @Inject constructor(
        private val playUseCase: PlayUseCase,
        private val manageTaskUseCase: ManageTaskUseCase,
        private val playCellController: PlayCellController,
        @Named(PlayFragmentModule.NAMED_IS_CURRENT) private val isCurrent: Boolean
) : BaseViewModel() {

    val adapter = playCellController.adapter
    val showNoTasks = MutableLiveData<Boolean>()
    val editTaskCmd = SingleLiveEvent<Long>()

    @Inject
    fun init() {
        playCellController.viewModel = this
        val taskObs = if (isCurrent) manageTaskUseCase.getCurrentTasks() else manageTaskUseCase.getPastTasks()
        exe(taskObs) {
            showNoTasks.value = it.isEmpty()
            playCellController.setData(it)
        }

    }

    fun play(task: Task): Observable<Pair<Int, Int>> {
        return playUseCase.playTask(task)
    }

    fun editTask(taskId: Long) {
        editTaskCmd.value = taskId
    }

    fun delete(task: Task) {
        exe(manageTaskUseCase.deleteTask(task)) {
            Timber.d("Task with id ${task.name} has been deleted")
        }
    }
}

class PlayCellController @Inject constructor(
        private val schedulerFactory: SchedulerFactory,
        @Named(PlayFragmentModule.NAMED_IS_CURRENT) private val isCurrent: Boolean)
    : TypedEpoxyController<List<Task>>() {

    lateinit var viewModel: PlayViewModel

    /**
     * This is timer that is shared between all bound cells
     */
    private val timer = Observable.interval(100, TimeUnit.MILLISECONDS)
            .subscribeOn(schedulerFactory.single())
            .observeOn(schedulerFactory.ui())
            .publish()

    init {
        timer.connect()
    }


    override fun buildModels(data: List<Task>) {
        data.sortedBy { it.scheduleTime }.forEach {
            playCell {
                id(it.id)
                pcTitle(it.title)
                pcTimePlayed(
                        if (isCurrent) getTimeFromEpoch(it.scheduleTime)
                        else getDateTimeFromEpoch(it.scheduleTime)
                )
                pcOnPlayClicked { viewModel.play(it)
                        .subscribeOn(schedulerFactory.io())
                        .observeOn(schedulerFactory.ui()) }
                pcOnEditClicked { viewModel.editTask(it.id) }
                pcOnDeleteClicked { viewModel.delete(it) }
                pcScheduleTime(it.scheduleTime)
                if (isCurrent) pcTimer(timer)
            }
        }
    }
}

