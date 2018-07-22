package fp.cookcorder.infrastructure

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.android.DaggerBroadcastReceiver
import fp.cookcorder.app.SchedulerFactory
import fp.cookcorder.model.Task
import fp.cookcorder.repo.TaskRepo
import timber.log.Timber
import javax.inject.Inject

interface TaskScheduler {

    fun scheduleTask(task: Task)

}

private const val KEY_INTENT_TASK_ID = "key_intent_task_id"

class TaskSchedulerImpl @Inject constructor(private val context: Context) : TaskScheduler {

    override fun scheduleTask(task: Task) {
        val intent = Intent(context, TaskBroadcastReceiver::class.java)
                .apply { putExtra(KEY_INTENT_TASK_ID, task.id) }

        val pendingIntent = PendingIntent
                .getBroadcast(context, task.id.toInt(), intent,  PendingIntent.FLAG_ONE_SHOT)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 500, pendingIntent)
    }
}

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
                            player.startPlaying(it.name)
                        },
                        {
                            Timber.d(it)
                        })
    }
}