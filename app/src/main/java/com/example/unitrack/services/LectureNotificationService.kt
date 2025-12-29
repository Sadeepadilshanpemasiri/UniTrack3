// app/src/main/java/com/example/unitrack/services/LectureNotificationService.kt
package com.example.unitrack.services

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.unitrack.MainActivity
import com.example.unitrack.R
import com.example.unitrack.data.database.AppDatabase
import kotlinx.coroutines.runBlocking

class LectureNotificationService : BroadcastReceiver() {

    companion object {
        private const val DEFAULT_USER_ID = 1 // Default user ID

        fun startNotificationScheduler(context: Context, userId: Int = DEFAULT_USER_ID) {
            val intent = Intent(context, LectureNotificationService::class.java).apply {
                action = "SCHEDULE_LECTURE_NOTIFICATIONS"
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "SCHEDULE_LECTURE_NOTIFICATIONS" -> {
                scheduleDailyNotifications(context, DEFAULT_USER_ID)
            }
            "LECTURE_REMINDER" -> {
                val lectureId = intent.getIntExtra("lecture_id", -1)
                val title = intent.getStringExtra("title") ?: "Lecture"
                val message = intent.getStringExtra("message") ?: "is starting soon"

                showNotification(context, lectureId, title, message)
            }
        }
    }

    private fun scheduleDailyNotifications(context: Context, userId: Int) {
        // Cancel all existing notifications
        cancelAllNotifications(context, userId)

        // Get all lectures with notifications enabled for this user
        runBlocking {
            val database = AppDatabase.getDatabase(context)
            val lecturesFlow = database.lectureDao().getLecturesWithNotificationsForUser(userId)

            // Collect the flow
            lecturesFlow.collect { lectureList ->
                lectureList.forEach { lecture ->
                    scheduleLectureNotification(context, lecture)
                }
            }
        }

        // Schedule next day's notifications at midnight
        scheduleMidnightCheck(context)
    }

    private fun scheduleLectureNotification(context: Context, lecture: com.example.unitrack.data.models.Lecture) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val currentCalendar = java.util.Calendar.getInstance()
        val dayOfWeek = currentCalendar.get(java.util.Calendar.DAY_OF_WEEK)

        // Convert dayOfWeek from Calendar (Sunday=1) to our format (Monday=1)
        val lectureDay = when (lecture.dayOfWeek) {
            1 -> java.util.Calendar.MONDAY
            2 -> java.util.Calendar.TUESDAY
            3 -> java.util.Calendar.WEDNESDAY
            4 -> java.util.Calendar.THURSDAY
            5 -> java.util.Calendar.FRIDAY
            6 -> java.util.Calendar.SATURDAY
            7 -> java.util.Calendar.SUNDAY
            else -> java.util.Calendar.MONDAY
        }

        // Calculate notification time (start time - minutesBefore)
        val startTimeParts = lecture.startTime.split(":")
        val hour = startTimeParts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = startTimeParts.getOrNull(1)?.toIntOrNull() ?: 0

        val notificationCalendar = java.util.Calendar.getInstance().apply {
            // Set to next occurrence of this day
            if (dayOfWeek > lectureDay) {
                add(java.util.Calendar.DAY_OF_WEEK, 7 - (dayOfWeek - lectureDay))
            } else if (dayOfWeek < lectureDay) {
                add(java.util.Calendar.DAY_OF_WEEK, lectureDay - dayOfWeek)
            } else {
                // Same day, check if time has passed
                val currentHour = currentCalendar.get(java.util.Calendar.HOUR_OF_DAY)
                val currentMinute = currentCalendar.get(java.util.Calendar.MINUTE)
                if (currentHour > hour || (currentHour == hour && currentMinute >= minute)) {
                    add(java.util.Calendar.DAY_OF_WEEK, 7) // Next week
                }
            }

            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute - lecture.notificationMinutesBefore)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        val intent = Intent(context, LectureNotificationService::class.java).apply {
            action = "LECTURE_REMINDER"
            putExtra("lecture_id", lecture.id)
            putExtra("title", lecture.title)
            putExtra("message", "${lecture.getFormattedTime()} in ${lecture.room}")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            lecture.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notificationCalendar.timeInMillis,
            pendingIntent
        )
    }

    private fun showNotification(context: Context, lectureId: Int, title: String, message: String) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("fragment", "timetable")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            lectureId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use R.mipmap.ic_launcher if R.drawable.ic_school doesn't exist
        val smallIcon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            R.drawable.ic_school
        } else {
            R.mipmap.ic_launcher
        }

        val notification = NotificationCompat.Builder(context, "lecture_channel")
            .setSmallIcon(smallIcon)
            .setContentTitle("📚 $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(lectureId, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "lecture_channel",
                "Lecture Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming lectures"
                enableVibration(true)
                enableLights(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleMidnightCheck(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val midnightCalendar = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        val intent = Intent(context, LectureNotificationService::class.java).apply {
            action = "SCHEDULE_LECTURE_NOTIFICATIONS"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            midnightCalendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancelAllNotifications(context: Context, userId: Int = DEFAULT_USER_ID) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LectureNotificationService::class.java)

        // Cancel all lecture notifications for this user
        runBlocking {
            val database = AppDatabase.getDatabase(context)
            val lecturesFlow = database.lectureDao().getLecturesWithNotificationsForUser(userId)

            lecturesFlow.collect { lectureList ->
                lectureList.forEach { lecture ->
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        lecture.id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.cancel(pendingIntent)
                }
            }
        }

        // Cancel midnight check
        val midnightIntent = Intent(context, LectureNotificationService::class.java).apply {
            action = "SCHEDULE_LECTURE_NOTIFICATIONS"
        }
        val midnightPendingIntent = PendingIntent.getBroadcast(
            context,
            999,
            midnightIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(midnightPendingIntent)
    }
}