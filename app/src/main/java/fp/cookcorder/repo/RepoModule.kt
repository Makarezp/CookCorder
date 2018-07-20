package fp.cookcorder.repo

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import fp.cookcorder.repo.db.CookCorderDb
import fp.cookcorder.repo.db.TaskDao
import javax.inject.Singleton

@Module
abstract class RepoModule {

    abstract val taskDao: TaskDao

    @Singleton
    @Binds
    abstract fun bindTaskRepo(impl: TaskRepoImpl): TaskRepo

    @Module
    companion object {

        @Provides
        @Singleton
        @JvmStatic
        fun provideDb(context: Context): CookCorderDb =  CookCorderDb.build(context)

        @Provides
        @Singleton
        @JvmStatic
        fun provideTaskDao(cookCorderDb: CookCorderDb): TaskDao =  cookCorderDb.taskDao
    }

}