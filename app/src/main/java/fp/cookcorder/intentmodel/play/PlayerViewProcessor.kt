package fp.cookcorder.intentmodel.play

import fp.cookcorder.intentmodel.intent
import fp.cookcorder.intentmodel.play.TaskStatus.NotPlaying
import fp.cookcorder.interactors.managetask.TaskInteractor
import fp.cookcorder.interactors.model.Task
import fp.cookcorder.interactors.play.PlayerInteractor
import fp.cookcorder.utils.applyShcedulers
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerViewProcessor @Inject constructor(
        private val playerInteractor: PlayerInteractor,
        private val taskInteractor: TaskInteractor,
        private val taskModelStore: PlayerModelStore
) {

    private val disposable = CompositeDisposable()

    init {
        disposable += subscribeToTasks(taskInteractor.getPastTasks(), false).subscribe {
            taskModelStore.process(intent {
                copy(pastTaskStates = it)
            })
        }
        disposable += subscribeToTasks(taskInteractor.getCurrentTasks(), true).subscribe {
            taskModelStore.process(intent {
                copy(currentTaskStates = it)
            })
        }
    }

    private fun subscribeToTasks(tasksStream: Flowable<List<Task>>, isCurrent: Boolean) = tasksStream
            .map { it.map { TaskState(task = it, taskStatus = NotPlaying, isCurrent = isCurrent) } }
            .applyShcedulers()
}