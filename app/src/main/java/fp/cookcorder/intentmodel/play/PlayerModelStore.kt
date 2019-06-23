package fp.cookcorder.intentmodel.play

import fp.cookcorder.intentmodel.ModelStore
import fp.cookcorder.interactors.model.Task
import fp.cookcorder.interactors.play.ProgressToMax
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerModelStore @Inject constructor() : ModelStore<PlayerState>(
        PlayerState(emptyList(), emptyList())
)

data class PlayerState(
        val currentTaskStates: List<TaskState>,
        val pastTaskStates: List<TaskState>
) {
    fun findTask(taskId: Long): TaskState = currentTaskStates.plus(pastTaskStates)
            .find { it.task.id == taskId } ?: throw IllegalArgumentException("Task not found")

    fun play()

    fun replace(task: Task): PlayerState {
        val replaceTask = { state: TaskState ->
            if (state.task.id == task.id) state.copy(task = task) else state
        }
        return copy(
                currentTaskStates = currentTaskStates.map(replaceTask),
                pastTaskStates = pastTaskStates.map(replaceTask)
        )
    }

}


data class TaskState(
        val task: Task,
        val isCurrent: Boolean,
        val taskStatus: TaskStatus
)

sealed class TaskStatus {
    data class Playing(val progressToMax: ProgressToMax) : TaskStatus()
    object NotPlaying : TaskStatus()
}