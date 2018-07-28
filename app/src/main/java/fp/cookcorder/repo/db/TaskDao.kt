package fp.cookcorder.repo.db

import android.arch.persistence.room.*
import fp.cookcorder.model.Task
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
abstract class TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(task: Task): Long

    @Query("SELECT * FROM Task")
    abstract fun getAll(): Flowable<List<Task>>

    @Query("SELECT * FROM Task WHERE id = :id")
    abstract fun get(id: Long): Single<Task>

    @Delete
    abstract fun delete(task: Task)

}