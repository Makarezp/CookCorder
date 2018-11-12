package fp.cookcorder.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.crashlytics.android.Crashlytics
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import fp.cookcorder.BuildConfig
import fp.cookcorder.R
import fp.cookcorder.app.di.DaggerAppComponent
import fp.cookcorder.infrastructure.KEY_NOTIFICATION_CHANNEL
import io.fabric.sdk.android.Fabric
import timber.log.Timber

class App : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()
        Fabric.with(this, Crashlytics())
        initTimber()
        buildNotificationChannel(this)
        AndroidThreeTen.init(this)
    }

    private fun initTimber() {
        if (BuildConfig.ENABLE_CRASHYLITCS) {
            Timber.plant(CrashlyticsTree())
        } else {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }

    private fun buildNotificationChannel(context: Context) {
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifChannel = NotificationChannel(
                    KEY_NOTIFICATION_CHANNEL,
                    context.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notifChannel)
        }
    }
}

class CrashlyticsTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        Crashlytics.log(message)
        if (t != null) {
            Crashlytics.logException(t)
        }
    }
}