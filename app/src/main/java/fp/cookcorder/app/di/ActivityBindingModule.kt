package fp.cookcorder.app.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fp.cookcorder.screen.MainActivity
import fp.cookcorder.screen.main.MainFragmentModule
import fp.cookcorder.screen.play.PlayModule

@Module
abstract class ActivityBindingModule {

    @ContributesAndroidInjector(modules = [
        MainFragmentModule::class,
        PlayModule::class
    ])
    abstract fun mainActivity(): MainActivity
}

