// app/src/main/java/com/example/unitrack/data/models/Assignment.kt
package com.example.unitrack.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = "assignments",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class Assignment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subjectId: Int,
    val title: String,
    val description: String = "",
    val dueDate: Long,            // Unix timestamp
    val priority: Int = 1,        // 1=Low, 2=Medium, 3=High, 4=Critical
    val estimatedTimeHours: Int = 2,
    val status: String = "pending", // pending, in_progress, completed, overdue
    val completionDate: Long? = null,
    val obtainedMarks: Float? = null,
    val totalMarks: Float = 100f,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val reminderEnabled: Boolean = true,
    val reminderTime: Long = 0 // hours before due date
) {
    fun daysRemaining(): Long {
        val now = System.currentTimeMillis()
        return (dueDate - now) / (1000 * 60 * 60 * 24)
    }

    fun hoursRemaining(): Long {
        val now = System.currentTimeMillis()
        return (dueDate - now) / (1000 * 60 * 60)
    }

    fun isOverdue(): Boolean {
        val now = System.currentTimeMillis()
        return dueDate < now && status != "completed"
    }

    fun isDueToday(): Boolean {
        val now = System.currentTimeMillis()
        val todayStart = getStartOfDay(now)
        val todayEnd = todayStart + (24 * 60 * 60 * 1000)
        return dueDate in todayStart..todayEnd
    }

    fun isDueTomorrow(): Boolean {
        val now = System.currentTimeMillis()
        val tomorrowStart = getStartOfDay(now) + (24 * 60 * 60 * 1000)
        val tomorrowEnd = tomorrowStart + (24 * 60 * 60 * 1000)
        return dueDate in tomorrowStart..tomorrowEnd
    }

    fun getDueDateFormatted(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(Date(dueDate))
    }

    fun getTimeRemainingText(): String {
        return when {
            isOverdue() -> "Overdue by ${-daysRemaining()} days"
            daysRemaining() == 0L -> "Due today"
            daysRemaining() == 1L -> "Due tomorrow"
            daysRemaining() < 7 -> "Due in ${daysRemaining()} days"
            else -> "Due on ${getDueDateFormatted()}"
        }
    }

    fun getPriorityText(): String {
        return when (priority) {
            1 -> "Low"
            2 -> "Medium"
            3 -> "High"
            4 -> "Critical"
            else -> "Low"
        }
    }

    fun getPriorityColor(): String {
        return when (priority) {
            1 -> "#4CAF50" // Green
            2 -> "#FFC107" // Yellow/Amber
            3 -> "#FF9800" // Orange
            4 -> "#F44336" // Red
            else -> "#4CAF50"
        }
    }

    fun calculatePercentage(): Float {
        return if (obtainedMarks != null && totalMarks > 0) {
            (obtainedMarks!! / totalMarks) * 100
        } else {
            0f
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}