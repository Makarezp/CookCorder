package fp.cookcorder.repo.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import fp.cookcorder.model.Task
import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
abstract class TaskDao {

    @Insert
    abstract fun insert(task: Task): Long

    @Query("SELECT * from Task")
    abstract fun getAll(): Flowable<List<Task>>

    @Delete
    abstract fun delete(task: Task)

}