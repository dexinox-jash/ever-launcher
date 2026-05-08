package com.everlauncher.ui.launcher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.dp

@Composable
fun ModeIndicator(
    modeName: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = modeName.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}
