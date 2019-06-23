package fp.cookcorder.intentmodel.play

import fp.cookcorder.infrastructure.Progress
import fp.cookcorder.intentmodel.ModelStore
import fp.cookcorder.intentmodel.play.TaskStatus.NotPlaying
import fp.cookcorder.intentmodel.play.TaskStatus.Playing
import fp.cookcorder.interactors.model.Task
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
    fun findTaskState(taskId: Long): TaskState = currentTaskStates.plus(pastTaskStates)
            .find { it.task.id == taskId } ?: throw IllegalArgumentException("Task not found")

    fun setPlayStatusForTask(taskId: Long, progress: Progress): PlayerState {
        val task = findTaskState(taskId).copy(taskStatus = Playing(progress))
        return replace(task)
    }

    fun setNotPlayingStatusForTask(taskId: Long): PlayerState {
        val task = findTaskState(taskId).copy(taskStatus = NotPlaying)
        return replace(task)
    }

    private fun replace(taskState: TaskState): PlayerState {
        val replaceTask = { state: TaskState ->
            if (state.task.id == taskState.task.id) taskState else state
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
    data class Playing(val progress: Progress) : TaskStatus()
    object NotPlaying : TaskStatus()
}