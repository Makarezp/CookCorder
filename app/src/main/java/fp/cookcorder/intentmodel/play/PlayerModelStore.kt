package fp.cookcorder.intentmodel.play

import fp.cookcorder.intentmodel.ModelStore
import fp.cookcorder.interactors.model.Task
import fp.cookcorder.interactors.play.ProgressToMax
import javax.inject.Inject

class PlayerModelStore @Inject constructor() : ModelStore<PlayerState>(
        PlayerState(emptyList(), emptyList())
)

data class PlayerState(
        val currentTaskStates: List<TaskState>,
        val pastTaskStates: List<TaskState>
)


data class TaskState(
        val task: Task,
        val taskStatus: TaskStatus
)

sealed class TaskStatus {
    data class Playing(val progressToMax: ProgressToMax) : TaskStatus()
    object NotPlaying : TaskStatus()
}