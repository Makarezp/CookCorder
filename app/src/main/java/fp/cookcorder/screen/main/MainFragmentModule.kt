package fp.cookcorder.screen.main

import android.support.v4.app.FragmentManager
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.screen.MainActivity

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
        fun provideFragmentManager(activity: MainActivity): FragmentManager {
            return activity.supportFragmentManager
        }
    }
}