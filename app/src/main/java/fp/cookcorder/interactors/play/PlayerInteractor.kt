package fp.cookcorder.interactors.play

import fp.cookcorder.infrastructure.Player
import fp.cookcorder.infrastructure.Progress
import fp.cookcorder.interactors.model.Task
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject



interface PlayerInteractor {

    fun playTask(task: Task, repeats: Int = 1): Observable<Progress>
    fun stopPlayingTask(task: Task): Single<Any>
}

class PlayerInteractorImpl @Inject constructor(private val player: Player): PlayerInteractor {

    override fun playTask(task: Task, repeats: Int): Observable<Progress> {
        return player.play(task.name, repeats)
    }

    override fun stopPlayingTask(task: Task): Single<Any> {
        return player.stopPlaying(task.name)
    }
}