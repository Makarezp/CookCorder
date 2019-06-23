package fp.cookcorder.intentmodel.play

import fp.cookcorder.interactors.model.Task

sealed class PlayerViewEvent {
    data class PlayTask(val taskId: Long): PlayerViewEvent()
    data class StopPlayingTask(val taskId: Long): PlayerViewEvent()
}