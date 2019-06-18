package fp.cookcorder.screen.record

import android.support.v4.app.FragmentManager
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import fp.cookcorder.app.ViewModelProviderFactory

@Module
abstract class RecordFragmentModule {

    @ContributesAndroidInjector(modules = [InnerModule::class])
    abstract fun provideMainFragment(): RecordFragment

}

@Module
abstract class InnerModule {

    @ContributesAndroidInjector
    abstract fun provideTimeFragment(): TimeFragment

    @ContributesAndroidInjector
    abstract fun provideOptionsFragment(): OptionsFragment

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideFragmentManager(fragment: RecordFragment): FragmentManager {
            return fragment.childFragmentManager
        }
    }
}