// app/src/main/java/com/example/unitrack/data/models/Lecture.kt
package com.example.unitrack.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "lectures",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
//    indices = [
//        Index(value = ["userId"]),
//        Index(value = ["dayOfWeek"]),
//        Index(value = ["subjectId"]),
//        Index(value = ["notificationEnabled"])
//    ]
)
data class Lecture(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int = 1, // ADD THIS FIELD - Default to user ID 1
    val subjectId: Int,
    val title: String,
    val dayOfWeek: Int, // 1-7 (Monday=1, Sunday=7)
    val startTime: String, // Format: "09:00"
    val endTime: String,   // Format: "10:30"
    val room: String = "",
    val lecturer: String = "",
    val notificationEnabled: Boolean = true,
    val notificationMinutesBefore: Int = 15, // Default 15 minutes before
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getDayName(): String {
        return when (dayOfWeek) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Unknown"
        }
    }

    fun getFormattedTime(): String {
        return "$startTime - $endTime"
    }
}