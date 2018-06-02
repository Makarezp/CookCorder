package fp.cookcorder

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import fp.cookcorder.di.DaggerAppComponent
import timber.log.Timber

class App : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }
}