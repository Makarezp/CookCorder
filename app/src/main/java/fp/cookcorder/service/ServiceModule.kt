package fp.cookcorder.service

import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
@Singleton
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindRecorder(impl: RecorderImpl): Recorder

    @Binds
    @Singleton
    abstract fun bindPlayer(impl: PlayerImpl): Player

}