package fp.cookcorder.app.di

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import fp.cookcorder.domain.UseCaseModule
import fp.cookcorder.infrastructure.InfrastructureModule
import fp.cookcorder.repo.RepoModule
import javax.inject.Singleton

@Module(includes = [
    RepoModule::class,
    InfrastructureModule::class,
    UseCaseModule::class])
abstract class ApplicationModule {

    @Binds
    @Singleton
    abstract fun bindContext(app: Application): Context

    @Module
    companion object {

        private const val SHARED_PREFERENCES_KEY = "SHARED_PREFERENCES_KEY_cook_coorder"

        @Provides
        @JvmStatic
        @Singleton
        fun provideSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(SHARED_PREFERENCES_KEY, MODE_PRIVATE)
        }
    }
}