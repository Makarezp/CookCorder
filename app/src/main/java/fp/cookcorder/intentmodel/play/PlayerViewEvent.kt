package fp.cookcorder.intentmodel.play

import fp.cookcorder.interactors.model.Task

sealed class PlayerViewEvent {
    data class PlayTask(val task: Task)
    data class StopPlayingTask(val task: Task)
}