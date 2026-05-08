package com.everlauncher.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.everlauncher.domain.model.DailyAnalytics
import java.time.format.TextStyle
import java.util.Locale

/**
 * 7-day bar chart showing focus scores.
 * Bars are proportional to daily score (max 100).
 */
@Composable
fun WeeklyChart(
    days: List<DailyAnalytics>,
    modifier: Modifier = Modifier
) {
    val sorted = days.sortedBy { it.date }
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    val chartDesc = "7-day focus scores: " + sorted.joinToString { "${it.date.dayOfWeek.name.take(3)} ${it.focusScore}" }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .semantics { contentDescription = chartDesc },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        sorted.forEach { day ->
            val fraction = (day.focusScore / 100f).coerceIn(0f, 1f)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Track
                    drawRect(color = trackColor)
                    // Bar
                    if (fraction > 0f) {
                        drawRect(
                            color = primaryColor,
                            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height * (1f - fraction)),
                            size = androidx.compose.ui.geometry.Size(size.width, size.height * fraction)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
