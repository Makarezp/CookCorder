package fp.cookcorder.interactors

import dagger.Binds
import dagger.Module
import fp.cookcorder.interactors.managetask.TaskInteractor
import fp.cookcorder.interactors.managetask.TaskInteractorImpl
import fp.cookcorder.interactors.play.PlayerInteractor
import fp.cookcorder.interactors.play.PlayerInteractorImpl
import fp.cookcorder.interactors.record.RecorderInteractor
import fp.cookcorder.interactors.record.RecorderInteractorImpl

@Module
abstract class InteractorModule {

    @Binds
    abstract fun bindTaskInteractor(impl: TaskInteractorImpl): TaskInteractor

    @Binds
    abstract fun bindRecorderInteractor(impl: RecorderInteractorImpl): RecorderInteractor

    @Binds
    abstract fun bindPlayerInteractor(impl: PlayerInteractorImpl): PlayerInteractor
}