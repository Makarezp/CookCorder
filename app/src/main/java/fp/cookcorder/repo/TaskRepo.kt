package fp.cookcorder.repo

import fp.cookcorder.model.Task
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

interface TaskRepo {

    fun saveTask(task: Task)

    fun getTasks(task: Task): Observable<List<Task>>

    fun deleteTask(task: Task)

}


class TaskRepoImpl @Inject constructor(): TaskRepo {

    private val tasks = BehaviorSubject.createDefault<List<Task>>(emptyList())

    override fun saveTask(task: Task) {
        tasks.onNext(tasks.value.plus(task))
    }

    override fun getTasks(task: Task): Observable<List<Task>> {
        return tasks
    }

    override fun deleteTask(task: Task) {
        tasks.onNext(tasks.value.filter { it != task })
    }
}