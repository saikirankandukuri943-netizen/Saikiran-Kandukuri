package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.BunkAdvice
import com.example.data.model.Subject
import com.example.data.model.SubjectAttendanceStats
import com.example.ui.theme.DangerRedBg
import com.example.ui.theme.DangerRedText
import com.example.ui.theme.SafeGreenBg
import com.example.ui.theme.SafeGreenText
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusHolidayAmber
import com.example.ui.theme.StatusHolidayBg
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.WarningAmberBg
import com.example.ui.theme.WarningAmberText

@Composable
fun AttendanceRingGauge(
    percentage: Float,
    targetPercentage: Int = 75,
    size: Dp = 130.dp,
    strokeWidth: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 800),
        label = "percentageAnimation"
    )

    val progressColor = when {
        percentage >= targetPercentage -> StatusPresentGreen
        percentage >= targetPercentage - 10 -> StatusHolidayAmber
        else -> StatusAbsentRed
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Progress Arc
            val sweep = (animatedPercentage / 100f) * 360f
            if (sweep > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            )
            Text(
                text = "Target $targetPercentage%",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: AttendanceStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (status) {
        AttendanceStatus.PRESENT -> Triple(StatusPresentBg, StatusPresentGreen, Icons.Default.Check)
        AttendanceStatus.ABSENT -> Triple(StatusAbsentBg, StatusAbsentRed, Icons.Default.Close)
        AttendanceStatus.HOLIDAY -> Triple(StatusHolidayBg, StatusHolidayAmber, Icons.Default.EventBusy)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status.displayName,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status.displayName,
            color = textColor,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
fun BunkAdviceBanner(
    advice: BunkAdvice,
    target: Int,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = if (advice.isSafe) {
        Triple(SafeGreenBg, SafeGreenText, Icons.Default.CheckCircle)
    } else {
        Triple(DangerRedBg, DangerRedText, Icons.Default.Warning)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = if (advice.isSafe) "Safe" else "Warning",
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (advice.isSafe) "Attendance Safe" else "Attendance Alert",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )
                Text(
                    text = advice.message,
                    style = MaterialTheme.typography.bodySmall.copy(color = textColor),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun OverallMetricsRow(
    total: Int,
    present: Int,
    absent: Int,
    holiday: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricBox(
            label = "Total Held",
            value = total.toString(),
            color = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f)
        )
        MetricBox(
            label = "Attended",
            value = present.toString(),
            color = StatusPresentGreen,
            containerColor = StatusPresentBg,
            modifier = Modifier.weight(1f)
        )
        MetricBox(
            label = "Missed",
            value = absent.toString(),
            color = StatusAbsentRed,
            containerColor = StatusAbsentBg,
            modifier = Modifier.weight(1f)
        )
        MetricBox(
            label = "Holiday",
            value = holiday.toString(),
            color = StatusHolidayAmber,
            containerColor = StatusHolidayBg,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricBox(
    label: String,
    value: String,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SubjectQuickMarkRow(
    subject: Subject,
    currentRecord: AttendanceRecord?,
    onMark: (AttendanceStatus) -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject_quick_mark_${subject.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color badge / Code
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            runCatching {
                                Color(android.graphics.Color.parseColor(subject.colorHex))
                            }.getOrDefault(MaterialTheme.colorScheme.primary)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (subject.code.isNotEmpty()) subject.code.take(4) else subject.name.take(2).uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (subject.faculty.isNotEmpty()) {
                            Text(
                                text = subject.faculty,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        if (subject.roomOrSlot.isNotEmpty()) {
                            Text(
                                text = " • ${subject.roomOrSlot}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                if (currentRecord != null) {
                    StatusBadge(status = currentRecord.status)
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_quick_mark_${subject.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Attendance",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Present, Absent, Holiday
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    text = "Present",
                    isSelected = currentRecord?.status == AttendanceStatus.PRESENT,
                    activeColor = StatusPresentGreen,
                    activeBg = StatusPresentBg,
                    icon = Icons.Default.Check,
                    modifier = Modifier.weight(1f),
                    onClick = { onMark(AttendanceStatus.PRESENT) }
                )

                QuickActionButton(
                    text = "Absent",
                    isSelected = currentRecord?.status == AttendanceStatus.ABSENT,
                    activeColor = StatusAbsentRed,
                    activeBg = StatusAbsentBg,
                    icon = Icons.Default.Close,
                    modifier = Modifier.weight(1f),
                    onClick = { onMark(AttendanceStatus.ABSENT) }
                )

                QuickActionButton(
                    text = "Holiday",
                    isSelected = currentRecord?.status == AttendanceStatus.HOLIDAY,
                    activeColor = StatusHolidayAmber,
                    activeBg = StatusHolidayBg,
                    icon = Icons.Default.EventBusy,
                    modifier = Modifier.weight(1f),
                    onClick = { onMark(AttendanceStatus.HOLIDAY) }
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    isSelected: Boolean,
    activeColor: Color,
    activeBg: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) activeBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        label = "btnBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "btnContent"
    )

    Surface(
        onClick = onClick,
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, activeColor) else null,
        modifier = modifier.height(38.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
            )
        }
    }
}
