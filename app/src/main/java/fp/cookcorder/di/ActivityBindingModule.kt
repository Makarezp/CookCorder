package fp.cookcorder.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fp.cookcorder.MainActivity

@Module
abstract class ActivityBindingModule {

    @ContributesAndroidInjector
    abstract fun mainActivity(): MainActivity
}

