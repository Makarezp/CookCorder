package fp.cookcorder.service

import dagger.Binds
import dagger.Module

@Module
abstract class ServiceModule {

    @Binds
    abstract fun bindRecorder(impl: RecorderImpl): Recorder

    @Binds
    abstract fun bindPlayer(impl: PlayerImpl): Player

}