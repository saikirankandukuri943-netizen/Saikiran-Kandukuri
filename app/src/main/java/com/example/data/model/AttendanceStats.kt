package com.example.data.model

import kotlin.math.ceil
import kotlin.math.floor

data class BunkAdvice(
    val isSafe: Boolean,
    val canBunkCount: Int,
    val mustAttendCount: Int,
    val message: String
)

data class SubjectAttendanceStats(
    val subject: Subject,
    val totalClasses: Int,
    val presentCount: Int,
    val absentCount: Int,
    val holidayCount: Int,
    val percentage: Float,
    val requiredToReach75: Int,
    val canBunkCount: Int,
    val bunkAdvice: BunkAdvice
)

data class OverallAttendanceStats(
    val totalClasses: Int,
    val presentCount: Int,
    val absentCount: Int,
    val holidayCount: Int,
    val percentage: Float,
    val targetPercentage: Int,
    val requiredToReach75: Int,
    val canBunkCount: Int,
    val bunkAdvice: BunkAdvice
)

object AttendanceCalculator {

    fun calculateRequiredToReachTarget(present: Int, total: Int, targetPercent: Int = 75): Int {
        if (total == 0) return 0
        val target = targetPercent.coerceIn(1, 99)
        val currentPercentage = (present.toDouble() / total.toDouble()) * 100.0
        if (currentPercentage >= target) return 0

        val targetRatio = target.toDouble() / 100.0
        val diff = (targetRatio * total - present) / (1.0 - targetRatio)
        return ceil(diff).toInt().coerceAtLeast(1)
    }

    fun calculateBunkMargin(present: Int, total: Int, targetPercent: Int = 75): Int {
        if (total == 0) return 0
        val target = targetPercent.coerceIn(1, 99)
        val currentPercentage = (present.toDouble() / total.toDouble()) * 100.0
        if (currentPercentage < target) return 0

        val margin = (100.0 * present - target * total) / target.toDouble()
        return floor(margin).toInt().coerceAtLeast(0)
    }

    fun calculateBunkAdvice(present: Int, total: Int, targetPercent: Int = 75): BunkAdvice {
        val target = targetPercent.coerceIn(1, 99)
        if (total == 0) {
            return BunkAdvice(
                isSafe = true,
                canBunkCount = 0,
                mustAttendCount = 0,
                message = "No classes conducted yet. Attend regularly to stay ahead!"
            )
        }

        val currentPercentage = (present.toDouble() / total.toDouble()) * 100.0

        return if (currentPercentage >= target) {
            val canBunk = calculateBunkMargin(present, total, target)
            BunkAdvice(
                isSafe = true,
                canBunkCount = canBunk,
                mustAttendCount = 0,
                message = when (canBunk) {
                    0 -> "On track at ${currentPercentage.toInt()}%! Don't miss the next class to stay above $target%."
                    1 -> "Safe: You can safely miss 1 class while keeping above $target%."
                    else -> "Safe: You can safely miss $canBunk classes while keeping above $target%."
                }
            )
        } else {
            val mustAttend = calculateRequiredToReachTarget(present, total, target)
            BunkAdvice(
                isSafe = false,
                canBunkCount = 0,
                mustAttendCount = mustAttend,
                message = when (mustAttend) {
                    1 -> "Warning: Attend the next 1 class to get back to $target%!"
                    else -> "Shortage Warning: Attend next $mustAttend classes consecutively to reach $target%!"
                }
            )
        }
    }

    fun computeSubjectStats(
        subject: Subject,
        records: List<AttendanceRecord>,
        fallbackTarget: Int = 75
    ): SubjectAttendanceStats {
        val subjectRecords = records.filter { it.subjectId == subject.id }
        var present = 0
        var absent = 0
        var holiday = 0

        for (rec in subjectRecords) {
            when (rec.status) {
                AttendanceStatus.PRESENT -> present++
                AttendanceStatus.ABSENT -> absent++
                AttendanceStatus.HOLIDAY -> holiday++
            }
        }

        val total = present + absent
        val percentage = if (total > 0) ((present.toFloat() / total.toFloat()) * 100f) else 100f
        val target = if (subject.targetPercentage > 0) subject.targetPercentage else fallbackTarget

        val advice = calculateBunkAdvice(present, total, target)
        val required = calculateRequiredToReachTarget(present, total, target)
        val canBunk = calculateBunkMargin(present, total, target)

        return SubjectAttendanceStats(
            subject = subject,
            totalClasses = total,
            presentCount = present,
            absentCount = absent,
            holidayCount = holiday,
            percentage = percentage,
            requiredToReach75 = required,
            canBunkCount = canBunk,
            bunkAdvice = advice
        )
    }

    fun computeOverallStats(
        records: List<AttendanceRecord>,
        targetPercent: Int = 75
    ): OverallAttendanceStats {
        var present = 0
        var absent = 0
        var holiday = 0

        for (rec in records) {
            when (rec.status) {
                AttendanceStatus.PRESENT -> present++
                AttendanceStatus.ABSENT -> absent++
                AttendanceStatus.HOLIDAY -> holiday++
            }
        }

        val total = present + absent
        val percentage = if (total > 0) ((present.toFloat() / total.toFloat()) * 100f) else 100f
        val advice = calculateBunkAdvice(present, total, targetPercent)
        val required = calculateRequiredToReachTarget(present, total, targetPercent)
        val canBunk = calculateBunkMargin(present, total, targetPercent)

        return OverallAttendanceStats(
            totalClasses = total,
            presentCount = present,
            absentCount = absent,
            holidayCount = holiday,
            percentage = percentage,
            targetPercentage = targetPercent,
            requiredToReach75 = required,
            canBunkCount = canBunk,
            bunkAdvice = advice
        )
    }
}
