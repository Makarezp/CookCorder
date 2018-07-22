package fp.cookcorder.infrastructure

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import fp.cookcorder.model.Task
import timber.log.Timber
import javax.inject.Inject

interface TaskScheduler {

    fun scheduleTask(task: Task)
}


class TaskSchedulerImpl @Inject constructor(private val context: Context): TaskScheduler {

    override fun scheduleTask(task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, 0)
        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 500, pendingIntent)

        Timber.d("Set alarm")
    }
}