package fp.cookcorder.app.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import fp.cookcorder.screen.MainActivity

@Module
abstract class ActivityBindingModule {

    @ContributesAndroidInjector
    abstract fun mainActivity(): MainActivity
}

