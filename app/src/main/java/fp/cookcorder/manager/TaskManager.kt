package fp.cookcorder.manager

import fp.cookcorder.infrastructure.Player
import fp.cookcorder.infrastructure.Recorder
import fp.cookcorder.infrastructure.TaskScheduler
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import java.util.*
import javax.inject.Inject

interface TaskManager {

    fun startRecordingNewTask(): Maybe<Any>

    fun finishRecordingNewTask(msToSchedule: Long, title: String?): Maybe<Task>

    fun cancelRecordingNewTask(): Maybe<Any>

    fun playTask(task: Task)

    fun getCurrentTasks(): Flowable<List<Task>>

    fun getPastTasks(): Flowable<List<Task>>

    fun deleteTask(task: Task): Completable
}

class TaskManagerImpl @Inject constructor(
        private val recorder: Recorder,
        private val player: Player,
        private val taskRepo: TaskRepo,
        private val taskScheduler: TaskScheduler
) : TaskManager {

    override fun startRecordingNewTask(): Maybe<Any> {
        return recorder.startRecording("r${Random().nextInt()}")
    }

    override fun finishRecordingNewTask(msToSchedule: Long, title: String?): Maybe<Task> {
        return recorder.finishRecording()
                .map { record ->
                    val scheduleTime = System.currentTimeMillis() + msToSchedule
                    taskRepo.saveTask(Task(0,
                            title = title,
                            name = record.fileName,
                            duration = record.duration,
                            scheduleTime = scheduleTime))
                }
                .doAfterSuccess {
                    taskScheduler.scheduleTask(it)
                }
    }

    override fun cancelRecordingNewTask() = recorder.cancelRecording()

    override fun playTask(task: Task) {
        player.startPlaying(task.name)
    }

    override fun getCurrentTasks(): Flowable<List<Task>> {
        return taskRepo.getAllTasks().map {
            val currTime = System.currentTimeMillis()
            it.filter { it.scheduleTime - currTime > 0 }
        }
    }

    override fun getPastTasks(): Flowable<List<Task>> {
        return taskRepo.getAllTasks().map {
            val currTime = System.currentTimeMillis()
            it.filter { it.scheduleTime - currTime < 0 }
        }
    }

    override fun deleteTask(task: Task): Completable = taskRepo.deleteTask(task)
}
