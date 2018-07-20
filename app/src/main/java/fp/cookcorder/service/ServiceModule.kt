package fp.cookcorder.service

import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindRecorder(impl: RecorderImpl): Recorder

    @Binds
    @Singleton
    abstract fun bindPlayer(impl: PlayerImpl): Player

}