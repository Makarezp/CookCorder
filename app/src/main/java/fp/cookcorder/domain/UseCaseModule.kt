package fp.cookcorder.domain

import dagger.Binds
import dagger.Module
import fp.cookcorder.domain.managetaskusecase.ManageTaskUseCase
import fp.cookcorder.domain.managetaskusecase.ManageTaskUseCaseImpl
import fp.cookcorder.domain.play.PlayUseCase
import fp.cookcorder.domain.play.PlayUseCaseImpl
import fp.cookcorder.domain.record.RecordUseCase
import fp.cookcorder.domain.record.RecordUseCaseImpl

@Module
abstract class UseCaseModule {

    @Binds
    abstract fun bindManageTaskUseCase(impl: ManageTaskUseCaseImpl): ManageTaskUseCase

    @Binds
    abstract fun bindRecordUseCase(impl: RecordUseCaseImpl): RecordUseCase

    @Binds
    abstract fun bindPlayUseCase(impl: PlayUseCaseImpl): PlayUseCase
}