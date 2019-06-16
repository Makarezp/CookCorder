package fp.cookcorder.screen.editdialog

import android.arch.lifecycle.MutableLiveData
import fp.cookcorder.interactors.managetask.TaskInteractor
import fp.cookcorder.screen.BaseViewModel
import fp.cookcorder.utils.SingleLiveEvent
import fp.cookcorder.utils.calculateTimeDifference
import fp.cookcorder.utils.minutestToMilliseconds
import javax.inject.Inject
import javax.inject.Named

class EditDialogViewModel @Inject constructor(
        private val taskManger: TaskInteractor,
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