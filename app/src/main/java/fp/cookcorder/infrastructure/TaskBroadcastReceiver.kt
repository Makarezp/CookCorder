package fp.cookcorder.infrastructure

import android.content.Context
import android.content.Intent
import dagger.android.DaggerBroadcastReceiver
import timber.log.Timber
import javax.inject.Inject

class TaskBroadcastReceiver: DaggerBroadcastReceiver() {

    @Inject
    lateinit var player: Player


    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Timber.d("bydle")
        Timber.d(player.toString())
    }
}