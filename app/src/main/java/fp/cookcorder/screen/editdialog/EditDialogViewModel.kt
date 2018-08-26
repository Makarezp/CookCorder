package fp.cookcorder.screen.editdialog

import android.arch.lifecycle.MutableLiveData
import fp.cookcorder.app.util.minutestToMilliseconds
import fp.cookcorder.manager.TaskManager
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.screen.utils.SingleLiveEvent
import fp.cookcorder.screen.utils.calculateTimeDifference
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Named

class EditDialogViewModel @Inject constructor(
        private val taskManger: TaskManager,
        @Named(EditDialogModuleInternals.NAMED_TASK_ID) private val taskId: Long
) : BaseViewModel() {


    val title = MutableLiveData<String>()

    val hoursToMinutes = MutableLiveData<Pair<Int, Int>>()

    val dismissDialogCmd = SingleLiveEvent<Void>()

    @Inject
    fun init() {
        exe(taskManger.getTask(taskId)) {
            title.value = it.title
            val timeDifference = calculateTimeDifference(it.scheduleTime)
            hoursToMinutes.value = timeDifference.first.toInt() to timeDifference.second.toInt()
        }
    }

    fun editTaskTask(minutestToSchedule: Int, title: String?) {
        val msToSchedule = if (minutestToSchedule == 0) null
        else minutestToSchedule.minutestToMilliseconds()

        exe(taskManger.editTask(taskId, msToSchedule, title)) {
            dismissDialogCmd.call()
        }
    }
}