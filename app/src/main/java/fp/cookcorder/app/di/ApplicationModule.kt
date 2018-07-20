package fp.cookcorder.app.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import fp.cookcorder.repo.RepoModule
import fp.cookcorder.service.ServiceModule
import javax.inject.Singleton

@Module(includes = [
    RepoModule::class,
    ServiceModule::class])
abstract class ApplicationModule {

    @Binds
    @Singleton
    abstract fun bindContext(app: Application): Context
}