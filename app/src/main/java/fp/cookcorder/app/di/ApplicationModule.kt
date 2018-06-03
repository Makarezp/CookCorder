package fp.cookcorder.app.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import fp.cookcorder.service.ServiceModule
import fp.cookcorder.screen.main.MainFragmentModule

@Module(includes = [
    MainFragmentModule::class,
    ServiceModule::class])
abstract class ApplicationModule {

    @Binds
    abstract fun bindContext(app: Application): Context

}