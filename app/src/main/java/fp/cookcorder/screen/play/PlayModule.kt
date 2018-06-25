package fp.cookcorder.screen.play

import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.screen.main.TaskAdapter

@Module
abstract class PlayModule {

    @ContributesAndroidInjector
    abstract fun bindPlayFragment(): PlayFragment

    @Module
    companion object {

        @JvmStatic
        @Provides
        fun provideVmFactory(vm: PlayViewModel) = ViewModelProviderFactory(vm)


        @Provides
        @JvmStatic
        fun provideTaskClickListener(clickListener: PlayViewModel): TaskAdapter.TaskClickListener = clickListener
    }

}