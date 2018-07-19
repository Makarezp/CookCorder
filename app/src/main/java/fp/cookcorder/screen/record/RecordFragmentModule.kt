package fp.cookcorder.screen.record

import android.support.v4.app.FragmentManager
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import fp.cookcorder.app.ViewModelProviderFactory
import fp.cookcorder.screen.MainActivity

@Module
abstract class RecordFragmentModule {

    @ContributesAndroidInjector
    abstract fun provideMainFragment(): RecordFragment

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideMainFragmentVm(vm: RecordViewModel) = ViewModelProviderFactory(vm)


        @Provides
        @JvmStatic
        fun provideFragmentManager(activity: MainActivity): FragmentManager {
            return activity.supportFragmentManager
        }

        @Provides
        @JvmStatic
        fun provideIsRecordingFunction(
                activity: MainActivity): RecordFragment.RecordingListener = activity
    }
}