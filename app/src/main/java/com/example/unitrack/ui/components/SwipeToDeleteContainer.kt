// Create a new file: SwipeToDeleteContainer.kt
package com.example.unitrack.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    var swipeState by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Delete background (appears when swiping)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.error)
                .zIndex(1f),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.padding(end = 24.dp)
            )
        }

        // Content card (swipeable)
        Box(
            modifier = Modifier
                .offset(x = swipeState.dp)
                .zIndex(2f)
        ) {
            content()
        }
    }
}