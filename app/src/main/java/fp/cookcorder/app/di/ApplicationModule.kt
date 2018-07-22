package fp.cookcorder.app.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import fp.cookcorder.repo.RepoModule
import fp.cookcorder.infrastructure.InfrastructureModule
import fp.cookcorder.manager.ManagerModule
import javax.inject.Singleton

@Module(includes = [
    RepoModule::class,
    InfrastructureModule::class,
    ManagerModule::class])
abstract class ApplicationModule {

    @Binds
    @Singleton
    abstract fun bindContext(app: Application): Context
}