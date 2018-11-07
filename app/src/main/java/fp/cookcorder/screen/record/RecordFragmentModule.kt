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

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideMainFragmentVm(vm: RecordViewModel) = ViewModelProviderFactory(vm)

    }
}

@Module
class InnerModule {

    @Provides
    fun provideFragmentManager(fragment: RecordFragment): FragmentManager {
        return fragment.childFragmentManager
    }
}