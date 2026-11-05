package com.example

import com.example.data.model.AttendanceCalculator
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Subject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AttendanceCalculatorTest {

    @Test
    fun testBunkAdviceWhenAttendanceIsAboveTarget() {
        // Attended 8 out of 10 classes -> 80% with 75% target
        // (8) / (10 + X) >= 0.75 -> 8 >= 7.5 + 0.75X -> 0.75X <= 0.5 -> X <= 0.66 -> 0 bunk
        val advice0 = AttendanceCalculator.calculateBunkAdvice(present = 8, total = 10, targetPercent = 75)
        assertTrue(advice0.isSafe)
        assertEquals(0, advice0.canBunkCount)

        // Attended 16 out of 20 classes -> 80%
        // (16) / (20 + X) >= 0.75 -> 16 >= 15 + 0.75X -> 0.75X <= 1.0 -> X <= 1.33 -> 1 can bunk
        val advice1 = AttendanceCalculator.calculateBunkAdvice(present = 16, total = 20, targetPercent = 75)
        assertTrue(advice1.isSafe)
        assertEquals(1, advice1.canBunkCount)
    }

    @Test
    fun testBunkAdviceWhenAttendanceIsBelowTarget() {
        // Attended 5 out of 10 classes -> 50% with 75% target
        // (5 + Y) / (10 + Y) >= 0.75 -> 5 + Y >= 7.5 + 0.75Y -> 0.25Y >= 2.5 -> Y >= 10
        val advice = AttendanceCalculator.calculateBunkAdvice(present = 5, total = 10, targetPercent = 75)
        assertFalse(advice.isSafe)
        assertEquals(10, advice.mustAttendCount)
    }

    @Test
    fun testSubjectStatsWithHolidays() {
        val subject = Subject(id = 1, name = "Management of Organizations & Behavior", code = "MOB", targetPercentage = 75)
        val records = listOf(
            AttendanceRecord(id = 1, subjectId = 1, dateString = "2026-11-01", status = AttendanceStatus.PRESENT),
            AttendanceRecord(id = 2, subjectId = 1, dateString = "2026-11-02", status = AttendanceStatus.PRESENT),
            AttendanceRecord(id = 3, subjectId = 1, dateString = "2026-11-03", status = AttendanceStatus.ABSENT),
            AttendanceRecord(id = 4, subjectId = 1, dateString = "2026-11-04", status = AttendanceStatus.HOLIDAY)
        )

        val stats = AttendanceCalculator.computeSubjectStats(subject, records, fallbackTarget = 75)

        assertEquals(2, stats.presentCount)
        assertEquals(1, stats.absentCount)
        assertEquals(1, stats.holidayCount)
        assertEquals(3, stats.totalClasses) // Holiday does not penalize conducted classes
        assertEquals(66.66667f, stats.percentage, 0.01f)
        assertFalse(stats.bunkAdvice.isSafe)
    }

    @Test
    fun testZeroClassesHandledGracefully() {
        val overall = AttendanceCalculator.computeOverallStats(emptyList(), targetPercent = 75)
        assertEquals(0, overall.totalClasses)
        assertEquals(0, overall.presentCount)
        assertEquals(0, overall.absentCount)
        assertEquals(100f, overall.percentage, 0.01f)
        assertTrue(overall.bunkAdvice.isSafe)
    }

    @Test
    fun testRequiredClassesToReach75Target() {
        // 5 out of 10 attended = 50%. (5+Y)/(10+Y) >= 0.75 -> Y >= 10
        val required = AttendanceCalculator.calculateRequiredToReachTarget(present = 5, total = 10, targetPercent = 75)
        assertEquals(10, required)

        // 75% target met (e.g. 15/20 = 75%) -> requires 0 additional
        val requiredMet = AttendanceCalculator.calculateRequiredToReachTarget(present = 15, total = 20, targetPercent = 75)
        assertEquals(0, requiredMet)
    }

    @Test
    fun testBunkMarginCalculation() {
        // 16 out of 20 attended = 80%. Target 75%. (16)/(20+1) = 16/21 = 76.19% >= 75%.
        val margin = AttendanceCalculator.calculateBunkMargin(present = 16, total = 20, targetPercent = 75)
        assertEquals(1, margin)
    }
}
