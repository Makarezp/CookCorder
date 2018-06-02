package fp.cookcorder

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import fp.cookcorder.di.DaggerAppComponent

class App : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }
}