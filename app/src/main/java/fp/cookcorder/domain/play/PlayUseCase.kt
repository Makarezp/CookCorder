package fp.cookcorder.domain.play

import fp.cookcorder.domain.UseCase
import fp.cookcorder.infrastructure.Player
import fp.cookcorder.model.Task
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

typealias ProgressToMax = Pair<Int, Int>

interface PlayUseCase : UseCase {

    fun playTask(task: Task): Observable<ProgressToMax>
    fun stopPlayingTask(task: Task): Single<Any>
}

class PlayUseCaseImpl @Inject constructor(private val player: Player): PlayUseCase {

    override fun playTask(task: Task): Observable<Pair<Int, Int>> {
        return player.play(task.name)
    }

    override fun stopPlayingTask(task: Task): Single<Any> {
        return player.stopPlaying(task.name)
    }
}