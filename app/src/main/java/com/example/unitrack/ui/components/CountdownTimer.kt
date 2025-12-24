// app/src/main/java/com/example/unitrack/ui/components/CountdownTimer.kt
package com.example.unitrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.*
import kotlin.time.Duration.Companion.seconds

@Composable
fun CountdownTimer(
    dueDate: Long,
    modifier: Modifier = Modifier,
    showDays: Boolean = true,
    showHours: Boolean = true,
    showMinutes: Boolean = true
) {
    var remainingTime by remember { mutableStateOf(calculateRemainingTime(dueDate)) }

    LaunchedEffect(key1 = dueDate) {
        while (true) {
            remainingTime = calculateRemainingTime(dueDate)
            delay(1.seconds) // Update every second
        }
    }

    val isOverdue = remainingTime.days < 0 ||
            (remainingTime.days == 0L &&
                    (remainingTime.hours < 0 ||
                            remainingTime.minutes < 0 ||
                            remainingTime.seconds < 0))

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isOverdue) "OVERDUE" else "TIME REMAINING",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isOverdue) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showDays && Math.abs(remainingTime.days) > 0) {
                    TimeUnit(
                        value = Math.abs(remainingTime.days),
                        unit = "Days",
                        isOverdue = isOverdue
                    )
                }

                if (showHours) {
                    TimeUnit(
                        value = Math.abs(remainingTime.hours),
                        unit = "Hours",
                        isOverdue = isOverdue
                    )
                }

                if (showMinutes) {
                    TimeUnit(
                        value = Math.abs(remainingTime.minutes),
                        unit = "Minutes",
                        isOverdue = isOverdue
                    )
                }

                TimeUnit(
                    value = Math.abs(remainingTime.seconds),
                    unit = "Seconds",
                    isOverdue = isOverdue
                )
            }

            if (isOverdue) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Submit as soon as possible!",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun TimeUnit(value: Long, unit: String, isOverdue: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = if (isOverdue) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = if (isOverdue) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

data class RemainingTime(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long
)

private fun calculateRemainingTime(dueDate: Long): RemainingTime {
    val now = Calendar.getInstance().timeInMillis
    val diff = dueDate - now

    return if (diff <= 0) {
        // Already overdue, show negative time
        val absDiff = Math.abs(diff)
        val days = -(absDiff / (1000 * 60 * 60 * 24))
        val hours = -((absDiff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
        val minutes = -((absDiff % (1000 * 60 * 60)) / (1000 * 60))
        val seconds = -((absDiff % (1000 * 60)) / 1000)
        RemainingTime(days, hours, minutes, seconds)
    } else {
        // Still time remaining
        val days = diff / (1000 * 60 * 60 * 24)
        val hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (diff % (1000 * 60)) / 1000
        RemainingTime(days, hours, minutes, seconds)
    }
}