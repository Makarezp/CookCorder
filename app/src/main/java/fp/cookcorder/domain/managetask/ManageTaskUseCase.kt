package fp.cookcorder.domain.managetask

import fp.cookcorder.domain.UseCase
import fp.cookcorder.infrastructure.TaskScheduler
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject

interface ManageTaskUseCase: UseCase {

    fun getTask(id: Long): Single<Task>

    fun editTask(id: Long, msToSchedule: Long?, title: String?): Single<Task>

    fun getCurrentTasks(): Flowable<List<Task>>

    fun getPastTasks(): Flowable<List<Task>>

    fun deleteTask(task: Task): Completable
}

class ManageTaskUseCaseImpl @Inject constructor(
        private val taskRepo: TaskRepo,
        private val taskScheduler: TaskScheduler
) : ManageTaskUseCase {


    override fun getTask(id: Long): Single<Task> {
        return taskRepo.getTask(id)
    }

    override fun editTask(id: Long, msToSchedule: Long?, title: String?): Single<Task> {
        return taskRepo.getTask(id).flatMap { task ->
            Single.fromCallable {

                val timeToSchedule = if (msToSchedule != null) System.currentTimeMillis() + msToSchedule
                else task.scheduleTime

                taskRepo.saveTask(task.copy(scheduleTime = timeToSchedule, title = title))
            }.doAfterSuccess { if(msToSchedule != null) taskScheduler.scheduleTask(it) }
        }
    }

    override fun getCurrentTasks(): Flowable<List<Task>> {
        return taskRepo.getAllTasks().map {
            val currTime = System.currentTimeMillis()
            it.filter { task -> task.scheduleTime - currTime > 0 }
        }
    }

    override fun getPastTasks(): Flowable<List<Task>> {
        return taskRepo.getAllTasks().map {
            val currTime = System.currentTimeMillis()
            it.filter { task ->  task.scheduleTime - currTime < 0 }
        }
    }

    override fun deleteTask(task: Task): Completable = taskRepo.deleteTask(task)
}
