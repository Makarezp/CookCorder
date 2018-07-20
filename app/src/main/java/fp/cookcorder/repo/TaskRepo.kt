package fp.cookcorder.repo

import fp.cookcorder.model.Task
import fp.cookcorder.repo.db.TaskDao
import io.reactivex.Completable
import io.reactivex.Flowable
import java.io.File
import javax.inject.Inject

interface TaskRepo {

    fun saveTask(task: Task)

    fun getTasks(): Flowable<List<Task>>

    fun deleteTask(task: Task): Completable
}

class TaskRepoImpl @Inject constructor(
        private val taskDao: TaskDao,
        private val fileRemover: FileRemover) : TaskRepo {

    override fun saveTask(task: Task) {
        taskDao.insert(task)
    }

    override fun getTasks(): Flowable<List<Task>> {
        return taskDao.getAll()
    }

    override fun deleteTask(task: Task): Completable {
       return Completable.fromCallable {
            fileRemover.deleteFile(task.name)
            taskDao.delete(task)
        }
    }
}

class FileRemover @Inject constructor() {

    fun deleteFile(path: String) {
        File(path).delete()
    }
}