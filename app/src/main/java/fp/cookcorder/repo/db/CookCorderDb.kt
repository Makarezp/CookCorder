package fp.cookcorder.repo.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import fp.cookcorder.model.Task

@Database(entities = [Task::class], version = 1)
abstract class CookCorderDb : RoomDatabase() {

    abstract val taskDao: TaskDao

    companion object {
        fun build(context: Context) = Room
                .databaseBuilder(context, CookCorderDb::class.java, "appdb")
                .fallbackToDestructiveMigration()
                .build()
    }
}

