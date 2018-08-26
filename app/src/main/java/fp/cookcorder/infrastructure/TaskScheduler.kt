package fp.cookcorder.infrastructure

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import dagger.android.DaggerBroadcastReceiver
import dagger.android.DaggerService
import fp.cookcorder.R
import fp.cookcorder.app.SchedulerFactory
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import fp.cookcorder.screen.MainActivity
import fp.cookcorder.screen.utils.getTimeFromEpoch
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


class TaskBroadcastReceiver : DaggerBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Intent(context, PlayService::class.java)
                .apply { putExtra(KEY_INTENT_TASK_ID, intent.getLongExtra(KEY_INTENT_TASK_ID, -1)) }
                .let {
                    if(SDK_INT >= 26)
                    context.startForegroundService(it)
                    else context.startService(it)
                }
    }
}


private const val KEY_NOTIFICATION_CHANNEL = "KEY_NOTIFICATION_CHANNEL"


class PlayService : DaggerService() {

    @Inject
    lateinit var schedulerProvider: SchedulerFactory

    @Inject
    lateinit var player: Player

    @Inject
    lateinit var taskRepo: TaskRepo

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notif = buildNotif()

        startForeground(intent.getLongExtra(KEY_INTENT_TASK_ID, -1).toInt() ,notif )
        performWork(intent, startId)
        return Service.START_NOT_STICKY
    }


    private fun performWork(intent: Intent, startId: Int) {
        taskRepo.getTask(intent.getLongExtra(KEY_INTENT_TASK_ID, -1))
                .subscribeOn(schedulerProvider.io())
                .doAfterSuccess {  taskRepo.saveTask(it) }
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        {
                            Timber.d(it.toString())
                            player.play(it.name).doOnComplete {
                                stopForeground(false)
                                showNotif(applicationContext, it.id.toInt(), it.title, getTimeFromEpoch(it.scheduleTime))
                                Timber.d("Finished playing")
                            }.subscribe()
                        },
                        {
                            Timber.d(it)
                        })
    }


    private fun showNotif(context: Context, id: Int, title: String?, time: String) {
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

        notificationManager.notify(id, buildNotif(time, title))
    }

    private fun createContentIntent(context: Context): PendingIntent {
        val startAppIntent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(context, 200,
                startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun buildNotif(time: String? = null, title: String? = null): Notification? {
        return NotificationCompat.Builder(applicationContext, KEY_NOTIFICATION_CHANNEL)
                .apply {
                    color = ContextCompat.getColor(applicationContext, R.color.colorAccent)
                    setSmallIcon(R.drawable.ic_tab_scheduled)
                    setContentTitle(if(time == null)"Your task from" else "Your task from $time")
                    title?.let { setContentText(it) }
                    setContentIntent(createContentIntent(applicationContext))
                    setAutoCancel(true)
                    setOnlyAlertOnce(true)
                }.build()
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
