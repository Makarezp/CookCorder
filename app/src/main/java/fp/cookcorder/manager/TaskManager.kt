package fp.cookcorder.manager

import fp.cookcorder.infrastructure.Player
import fp.cookcorder.infrastructure.Recorder
import fp.cookcorder.infrastructure.TaskScheduler
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import javax.inject.Inject

interface TaskManager {

    fun startRecordingNewTask(msToSchedule: Long): Maybe<Any>

    fun finishRecordingNewTask(): Maybe<Task>

    fun cancelRecordingNewTask(): Maybe<Any>

    fun playTask(task: Task)

    fun getTasks(): Flowable<List<Task>>

    fun deleteTask(task: Task): Completable
}

class TaskManagerImpl @Inject constructor(
        private val recorder: Recorder,
        private val player: Player,
        private val taskRepo: TaskRepo,
        private val taskScheduler: TaskScheduler
) : TaskManager {

    private var lastMsToSchedule: Long = 0


    override fun startRecordingNewTask(msToSchedule: Long): Maybe<Any> {
        lastMsToSchedule = msToSchedule
        return recorder.startRecording("r${Random().nextInt()}")
    }

    override fun finishRecordingNewTask(): Maybe<Task> {
        return recorder.finishRecording()
                .map {
                    val scheduleTime = System.currentTimeMillis() + lastMsToSchedule
                    taskRepo.saveTask(Task(0, it.fileName, it.duration, scheduleTime))
                }
                .doAfterSuccess {
                    taskScheduler.scheduleTask(it)
                }
    }

    override fun cancelRecordingNewTask() = recorder.cancelRecording()

    override fun playTask(task: Task) {
        player.startPlaying(task.name)
    }

    override fun getTasks(): Flowable<List<Task>> = taskRepo.getTasks()

    override fun deleteTask(task: Task): Completable = taskRepo.deleteTask(task)
}
