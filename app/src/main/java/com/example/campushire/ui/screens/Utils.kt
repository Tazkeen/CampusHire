package com.example.campushire.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Clickable text without the ripple (used for small text links). */
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
}

fun formatDate(millis: Long): String =
    if (millis <= 0L) "-" else SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

