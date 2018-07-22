package fp.cookcorder.infrastructure

import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class InfrastructureModule {

    @Binds
    @Singleton
    abstract fun bindRecorder(impl: RecorderImpl): Recorder

    @Binds
    @Singleton
    abstract fun bindPlayer(impl: PlayerImpl): Player

    @Binds
    @Singleton
    abstract fun bindScheduler(impl: TaskSchedulerImpl): TaskScheduler

}