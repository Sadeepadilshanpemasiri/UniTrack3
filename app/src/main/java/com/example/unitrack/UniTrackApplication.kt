// app/src/main/java/com/example/unitrack/UniTrackApplication.kt
package com.example.unitrack

import android.app.Application
import android.content.Intent
import com.example.unitrack.services.LectureNotificationService

class UniTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Schedule lecture notifications
        scheduleLectureNotifications()
    }

    private fun scheduleLectureNotifications() {
        val intent = Intent(this, LectureNotificationService::class.java).apply {
            action = "SCHEDULE_LECTURE_NOTIFICATIONS"
        }
        sendBroadcast(intent)
    }
}