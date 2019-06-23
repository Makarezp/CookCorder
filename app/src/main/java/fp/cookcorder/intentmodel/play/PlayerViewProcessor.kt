package fp.cookcorder.intentmodel.play

import fp.cookcorder.infrastructure.Progress
import fp.cookcorder.intentmodel.Intent
import fp.cookcorder.intentmodel.intent
import fp.cookcorder.intentmodel.play.PlayerViewEvent.PlayTask
import fp.cookcorder.intentmodel.play.PlayerViewEvent.StopPlayingTask
import fp.cookcorder.intentmodel.play.TaskStatus.NotPlaying
import fp.cookcorder.intentmodel.sideEffect
import fp.cookcorder.interactors.managetask.TaskInteractor
import fp.cookcorder.interactors.model.Task
import fp.cookcorder.interactors.play.PlayerInteractor
import fp.cookcorder.utils.applyShcedulers
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerViewProcessor @Inject constructor(
        private val playerInteractor: PlayerInteractor,
        private val taskInteractor: TaskInteractor,
        private val playerModelStore: PlayerModelStore
) {

    private val disposable = CompositeDisposable()

    init {
        disposable += subscribeToTasks(taskInteractor.getPastTasks(), false).subscribe {
            playerModelStore.process(intent {
                copy(pastTaskStates = it)
            })
        }
        disposable += subscribeToTasks(taskInteractor.getCurrentTasks(), true).subscribe {
            playerModelStore.process(intent {
                copy(currentTaskStates = it)
            })
        }
    }

    private fun subscribeToTasks(tasksStream: Flowable<List<Task>>, isCurrent: Boolean) = tasksStream
            .map { it.map { TaskState(task = it, taskStatus = NotPlaying, isCurrent = isCurrent) } }
            .applyShcedulers()

    fun process(viewEvent: PlayerViewEvent) {
        playerModelStore.process(toIntent(viewEvent))
    }

    private fun toIntent(viewEvent: PlayerViewEvent): Intent<PlayerState> = when(viewEvent) {
        is PlayTask -> buildPlayTaskIntent(viewEvent.taskId)
        is StopPlayingTask -> buildStopPlayingIntent(viewEvent.taskId)
    }

    private fun buildStopPlayingIntent(taskId: Long): Intent<PlayerState> = sideEffect {

    }

    private fun buildPlayTaskIntent(taskId: Long): Intent<PlayerState> = sideEffect {
        val taskState = findTaskState(taskId)
        if(taskState.taskStatus is NotPlaying) {

            fun updateProgress(progress: Progress) {
                playerModelStore.process(intent {
                    setPlayStatusForTask(taskId, progress)
                })
            }

            fun stopPlaying() {
                playerModelStore.process(intent {
                    setNotPlayingStatusForTask(taskId)
                })
            }

            fun error(throwable: Throwable) {
                Timber.e(throwable)
                stopPlaying()
            }

            playerInteractor.playTask(taskState.task, 1)
                    .subscribe(::updateProgress, ::error, ::stopPlaying)
        }
    }

}