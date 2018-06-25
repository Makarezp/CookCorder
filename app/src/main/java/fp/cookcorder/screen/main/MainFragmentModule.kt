package fp.cookcorder.screen.main

import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import fp.cookcorder.app.ViewModelProviderFactory

@Module
abstract class MainFragmentModule {

    @ContributesAndroidInjector
    abstract fun provideMainFragment(): MainFragment

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideMainFragmentVm(vm: MainViewModel) = ViewModelProviderFactory<MainViewModel>(vm)

        @Provides
        @JvmStatic
        fun provideTaskClickListener(clickListener: MainViewModel): TaskAdapter.TaskClickListener
                = clickListener
    }
}