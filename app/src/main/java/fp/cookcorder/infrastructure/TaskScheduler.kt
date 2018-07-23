package fp.cookcorder.infrastructure

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import dagger.android.DaggerBroadcastReceiver
import fp.cookcorder.R
import fp.cookcorder.app.SchedulerFactory
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import fp.cookcorder.screen.MainActivity
import timber.log.Timber
import javax.inject.Inject

interface TaskScheduler {

    fun scheduleTask(task: Task)

}

private const val KEY_INTENT_TASK_ID = "KEY_INTENT_TASK_ID"

class TaskSchedulerImpl @Inject constructor(private val context: Context) : TaskScheduler {

    override fun scheduleTask(task: Task) {
        val intent = Intent(context, TaskBroadcastReceiver::class.java)
                .apply { putExtra(KEY_INTENT_TASK_ID, task.id) }

        val pendingIntent =
                PendingIntent
                        .getBroadcast(context, task.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP, task.scheduleTime, pendingIntent)
    }
}

private const val KEY_NOTIFICATION_CHANNEL = "KEY_NOTIFICATION_CHANNEL"
private const val KEY_NOTIFICATION_GROUP = "KEY_NOTIFICATION_GROUP"

class TaskBroadcastReceiver : DaggerBroadcastReceiver() {

    @Inject
    lateinit var schedulerProvider: SchedulerFactory

    @Inject
    lateinit var player: Player

    @Inject
    lateinit var taskRepo: TaskRepo

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        taskRepo.getTask(intent.getLongExtra(KEY_INTENT_TASK_ID, -1))
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                        {
                            Timber.d(it.toString())
                            showNotif(context, it.id.toInt())
                            player.startPlaying(it.name)
                        },
                        {
                            Timber.d(it)
                        })
    }

    private fun showNotif(context: Context, id: Int) {
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

        val notif = NotificationCompat.Builder(context, KEY_NOTIFICATION_CHANNEL)
                .apply {
                    color = ContextCompat.getColor(context, R.color.colorAccent)
                    setGroup(KEY_NOTIFICATION_GROUP)
                    setSmallIcon(R.drawable.ic_mic)
                    setContentTitle("palyer")
                    setAutoCancel(true)
                    setContentIntent(createContentIntent(context))
                }
        val notifSummary = NotificationCompat.Builder(context, KEY_NOTIFICATION_CHANNEL)
                .apply {
                    color = ContextCompat.getColor(context, R.color.colorAccent)
                    setGroup(KEY_NOTIFICATION_GROUP)
                    setGroupSummary(true)
                    setSmallIcon(R.drawable.ic_mic)
                    setAutoCancel(true)
                    setContentIntent(createContentIntent(context))
                }

        notificationManager.notify(1, notifSummary.build())
        notificationManager.notify(id, notif.build())
    }

    private fun createContentIntent(context: Context): PendingIntent {
        val startAppIntent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(context, 200,
                startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


}
