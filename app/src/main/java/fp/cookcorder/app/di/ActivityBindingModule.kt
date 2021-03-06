package fp.cookcorder.app.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fp.cookcorder.infrastructure.PlayService
import fp.cookcorder.infrastructure.TaskBroadcastReceiver
import fp.cookcorder.screen.MainActivity
import fp.cookcorder.screen.editdialog.EditDialogModule
import fp.cookcorder.screen.play.PlayModule
import fp.cookcorder.screen.record.RecordFragmentModule

@Module
abstract class ActivityBindingModule {

    @ContributesAndroidInjector(modules = [
        RecordFragmentModule::class,
        PlayModule::class,
        EditDialogModule::class])
    abstract fun mainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun taskBroadcastReceiver(): TaskBroadcastReceiver

    @ContributesAndroidInjector
    abstract fun playIntentService(): PlayService
}

