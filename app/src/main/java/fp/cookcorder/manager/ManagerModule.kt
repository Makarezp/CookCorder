package fp.cookcorder.manager

import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class ManagerModule {

    @Singleton
    @Binds
    abstract fun bindTaskManager(impl: TaskManagerImpl): TaskManager
}