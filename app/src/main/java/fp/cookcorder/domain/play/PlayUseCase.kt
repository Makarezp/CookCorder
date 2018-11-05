package fp.cookcorder.domain.play

import fp.cookcorder.domain.UseCase
import fp.cookcorder.infrastructure.Player
import fp.cookcorder.model.Task
import io.reactivex.Observable
import javax.inject.Inject

typealias ProgressToMax = Pair<Int, Int>

interface PlayUseCase : UseCase {

    fun playTask(task: Task): Observable<ProgressToMax>
}

class PlayUseCaseImpl @Inject constructor(private val player: Player): PlayUseCase {

    override fun playTask(task: Task): Observable<Pair<Int, Int>> {
        return player.play(task.name)
    }
}