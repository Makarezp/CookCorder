package fp.cookcorder.app.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fp.cookcorder.infrastructure.TaskBroadcastReceiver
import fp.cookcorder.screen.MainActivity
import fp.cookcorder.screen.record.RecordFragmentModule
import fp.cookcorder.screen.play.PlayModule

@Module
abstract class ActivityBindingModule {

    @ContributesAndroidInjector(modules = [
        RecordFragmentModule::class,
        PlayModule::class])
    abstract fun mainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun taskBroadcastReceiver(): TaskBroadcastReceiver
}

