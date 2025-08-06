package com.example.cp3406_getitdone.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.cp3406_getitdone.domain.Task
import java.util.Calendar

fun scheduleReminder(context: Context, task: Task) {
    if (!task.notify) return // User selects notification or not

    val date = task.dueDate ?: return
    val (hour, minute) = task.dueTime ?: return

    val cal = Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
    }

    cal.add(Calendar.MINUTE, -task.reminderOffsetMinutes) // User selects offset

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("title", task.title)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        cal.timeInMillis.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    try {
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pendingIntent
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
        Toast.makeText(context, "Exact alarms not permitted", Toast.LENGTH_SHORT).show()
    }
}