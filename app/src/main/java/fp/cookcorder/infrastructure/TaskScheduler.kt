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
        val intent = createIntentForTaskScheduler(task)

        val pendingIntent =
                createPenndingIntentForAlarmManager(task, intent)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        if(SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, task.scheduleTime, pendingIntent)
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP, task.scheduleTime, pendingIntent)
        }
    }

    private fun createIntentForTaskScheduler(task: Task) =
            Intent(context, TaskBroadcastReceiver::class.java)
                    .apply { putExtra(KEY_INTENT_TASK_ID, task.id) }

    private fun createPenndingIntentForAlarmManager(task: Task, intent: Intent): PendingIntent? {
        return PendingIntent
                .getBroadcast(context, task.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}

class TaskBroadcastReceiver : DaggerBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Intent(context, PlayService::class.java)
                .apply { putExtra(KEY_INTENT_TASK_ID, intent.getLongExtra(KEY_INTENT_TASK_ID, -1)) }
                .let {
                    if (SDK_INT >= 26)
                        context.startForegroundService(it)
                    else context.startService(it)
                }
    }
}


const val KEY_NOTIFICATION_CHANNEL = "KEY_NOTIFICATION_CHANNEL"


class PlayService : DaggerService() {

    @Inject
    lateinit var schedulerProvider: SchedulerFactory

    @Inject
    lateinit var player: Player

    @Inject
    lateinit var taskRepo: TaskRepo

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notif = buildNotif()

        startForeground(intent.getLongExtra(KEY_INTENT_TASK_ID, -1).toInt(), notif.build())
        performWork(intent, startId)
        return Service.START_NOT_STICKY
    }


    private fun performWork(intent: Intent, startId: Int) {
        taskRepo.getTask(intent.getLongExtra(KEY_INTENT_TASK_ID, -1))
                .subscribeOn(schedulerProvider.io())
                .doAfterSuccess { taskRepo.saveTask(it) }
                .observeOn(schedulerProvider.ui())
                .flatMapObservable { task ->
                    player.play(task.name)
                            .doAfterNext {
                                showNotif(
                                        applicationContext,
                                        task.id.toInt(),
                                        task.title,
                                        getTimeFromEpoch(task.scheduleTime),
                                        it.first,
                                        it.second)
                            }
                            .doOnComplete {
                                stopForeground(false)
                                showNotif(
                                        applicationContext,
                                        task.id.toInt(),
                                        task.title,
                                        getTimeFromEpoch(task.scheduleTime),
                                        1,
                                        1)
                            }
                }
                .subscribe({}, {
                    Timber.d(it)
                })
    }


    private fun showNotif(context: Context,
                          id: Int,
                          title: String?,
                          time: String,
                          progres: Int = 0,
                          maxProgress: Int = 0) {
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notif = buildNotif(time, title).setProgress(maxProgress, progres, false)
        notificationManager.notify(id, notif.build())
    }

    private fun createContentIntent(context: Context): PendingIntent {
        val startAppIntent = Intent(context, MainActivity::class.java)
                .apply {
                    putExtra(MainActivity.KEY_LAUNCH_PAGE, 2)
                }
        return PendingIntent.getActivity(context, 200,
                startAppIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun buildNotif(time: String? = null, title: String? = null) =
            NotificationCompat.Builder(applicationContext, KEY_NOTIFICATION_CHANNEL)
                    .apply {
                        color = ContextCompat.getColor(applicationContext, R.color.colorAccent)
                        setSmallIcon(R.drawable.ic_tab_scheduled)
                        setContentTitle(if (time == null) "Your task from" else "Your task from $time")
                        title?.let { setContentText(it) }
                        setContentIntent(createContentIntent(applicationContext))
                        setAutoCancel(true)
                        setOnlyAlertOnce(true)
                    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
