package fp.cookcorder.infrastructure

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.MediaSessionCompat
import dagger.android.DaggerBroadcastReceiver
import dagger.android.DaggerService
import fp.cookcorder.R
import fp.cookcorder.app.SchedulerFactory
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import fp.cookcorder.screen.MainActivity
import fp.cookcorder.screen.utils.getTimeFromEpoch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface TaskScheduler {

    fun scheduleTask(task: Task)
}

private const val KEY_INTENT_TASK_ID = "KEY_INTENT_TASK_ID"

class TaskSchedulerImpl @Inject constructor(private val context: Context) : TaskScheduler {

    override fun scheduleTask(task: Task) {
        val intent = Intent(context, TaskBroadcastReceiver::class.java)
                .apply { putExtra(KEY_INTENT_TASK_ID, task.id) }

        val pendingIntent = PendingIntent
                .getBroadcast(context, task.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, task.scheduleTime, pendingIntent)
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP, task.scheduleTime, pendingIntent)
        }
    }

}

class TaskBroadcastReceiver : DaggerBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        PlayService.play(context, intent.getLongExtra(KEY_INTENT_TASK_ID, -1))
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


    val mediaSession: MediaSessionCompat by lazy {
        MediaSessionCompat(applicationContext, "TAG")
                .apply {
                    setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                            or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
                }
    }

    companion object {
        private const val KEY_TASK_TYPE = "KEY_TASK_TYPE"
        private const val PLAY_TASK = 1
        private const val STOP_TASK = 2

        fun play(context: Context, taskId: Long) {
            getPlayIntent(context, taskId)
                    .let {
                        if (SDK_INT >= 26)
                            context.startForegroundService(it)
                        else context.startService(it)
                    }
        }

        private fun getPlayIntent(context: Context, taskId: Long) =
                Intent(context, PlayService::class.java)
                        .apply {
                            putExtra(KEY_INTENT_TASK_ID, taskId)
                            putExtra(KEY_TASK_TYPE, PLAY_TASK)
                        }

        private fun getStopPlayingIntent(context: Context, taskId: Long): Intent =
                Intent(context, PlayService::class.java)
                        .apply {
                            putExtra(KEY_INTENT_TASK_ID, taskId)
                            putExtra(KEY_TASK_TYPE, STOP_TASK)
                        }

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val taskId = intent.getLongExtra(KEY_INTENT_TASK_ID, -1)
        when (intent.getIntExtra(KEY_TASK_TYPE, -1)) {

            PLAY_TASK -> {
                val notif = buildNotif(taskId,
                        null,
                        null,
                        true)

                startForeground(taskId.toInt(), notif.build())
                play(taskId)
            }
            STOP_TASK -> {
                stopPlayingTask(taskId)
                stopForeground(false)
            }
        }

        return Service.START_NOT_STICKY
    }


    @SuppressLint("CheckResult")
    private fun play(taskId: Long) {
        taskRepo.getTask(taskId)
                .subscribeOn(schedulerProvider.io())
                .doAfterSuccess { taskRepo.saveTask(it) }
                .observeOn(schedulerProvider.ui())
                .flatMapObservable { task ->
                    player.play(task.name, task.repeats)
                            .doOnSubscribe {
                                mediaSession.setCallback(createCallback())
                                mediaSession.isActive = true
                                showNotif(
                                        applicationContext,
                                        task.id,
                                        task.title,
                                        getTimeFromEpoch(task.scheduleTime))
                            }
                            .debounce(500, TimeUnit.MILLISECONDS)
                            .doAfterNext {
                                showNotif(
                                        applicationContext,
                                        task.id,
                                        task.title,
                                        getTimeFromEpoch(task.scheduleTime),
                                        it.first,
                                        it.second)
                            }
                            .doOnComplete {
                                showNotif(
                                        applicationContext,
                                        task.id,
                                        task.title,
                                        getTimeFromEpoch(task.scheduleTime),
                                        1,
                                        1,
                                        false)
                            }
                            .doFinally {
                                mediaSession.isActive = false

                            }
                            .subscribeOn(schedulerProvider.ui())
                }.doFinally {
                    stopForeground(false)
                }
                .subscribe({},Timber::d,{})
    }

    @SuppressLint("CheckResult")
    private fun stopPlayingTask(taskId: Long) {
        taskRepo.getTask(taskId)
                .subscribeOn(schedulerProvider.io())
                .flatMap {
                    player.stopPlaying(it.name)
                            .doAfterSuccess { _ ->
                                showNotif(
                                        applicationContext,
                                        it.id,
                                        it.title,
                                        getTimeFromEpoch(it.scheduleTime),
                                        1,
                                        1,
                                        false
                                )
                            }
                }.subscribe({
                    Timber.d("Success")
                }, {
                    Timber.d(it)
                })

    }

    private fun showNotif(context: Context,
                          id: Long,
                          title: String?,
                          time: String,
                          progress: Int = 0,
                          maxProgress: Int = 0,
                          play: Boolean = true) {
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notif = buildNotif(id, time, title, play)
        notificationManager.notify(id.toInt(), notif.build())
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

    private fun createStopIntent(context: Context, taskId: Long): PendingIntent {
        return PendingIntent.getService(context,
                taskId.toInt(),
                getStopPlayingIntent(context, taskId),
                PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun createPlayIntent(context: Context, taskId: Long): PendingIntent {
        return PendingIntent.getService(context, taskId.toInt(),
                getPlayIntent(context, taskId),
                PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun buildNotif(taskId: Long,
                           time: String? = null,
                           title: String? = null,
                           play: Boolean): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, KEY_NOTIFICATION_CHANNEL)
                .apply {
                    priority = NotificationCompat.PRIORITY_MAX
                    color = ContextCompat.getColor(applicationContext, R.color.colorAccent)
                    setSmallIcon(R.drawable.ic_tab_scheduled)
                    setContentTitle(if (time == null) "Your task from" else "Your task from $time")
                    title?.let { setContentText(it) }
                    setContentIntent(createContentIntent(applicationContext))
                    setAutoCancel(true)
                    setOnlyAlertOnce(true)

                    setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.sessionToken)
                            .setShowCancelButton(true)
                    )
                    if (play) {
                        addAction(android.R.drawable.ic_media_pause, "Stop", createStopIntent(applicationContext, taskId))
                    } else {
                        addAction(android.R.drawable.ic_media_play, "Play", createPlayIntent(applicationContext, taskId))
                    }
                }
    }


    private fun createCallback(): MediaSessionCompat.Callback {
        return object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                Timber.d("playing")
            }

            override fun onPause() {
                Timber.d("Stoping")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
