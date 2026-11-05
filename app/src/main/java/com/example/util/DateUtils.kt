package com.example.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")
    private val shortDisplayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM")
    private val monthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    fun today(): LocalDate = LocalDate.now()

    fun todayString(): String = today().format(isoFormatter)

    fun toIsoString(date: LocalDate): String = date.format(isoFormatter)

    fun fromIsoString(str: String): LocalDate {
        return runCatching { LocalDate.parse(str, isoFormatter) }.getOrDefault(LocalDate.now())
    }

    fun toDisplayString(date: LocalDate): String = date.format(displayFormatter)

    fun toDisplayString(isoStr: String): String = fromIsoString(isoStr).format(displayFormatter)

    fun toShortDisplayString(isoStr: String): String = fromIsoString(isoStr).format(shortDisplayFormatter)

    fun toMonthYearString(yearMonth: YearMonth): String = yearMonth.format(monthYearFormatter)

    fun getDayOfWeekShort(dayOfWeek: DayOfWeek): String {
        return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    /**
     * Generates calendar cells for a month starting on Monday (Mon=1 ... Sun=7)
     * Leading nulls represent empty cells before the 1st of the month.
     */
    fun getCalendarGrid(yearMonth: YearMonth): List<LocalDate?> {
        val firstDayOfMonth = yearMonth.atDay(1)
        val daysInMonth = yearMonth.lengthOfMonth()

        // 1 for Monday, 7 for Sunday
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1..7
        val leadingEmptyCells = startDayOfWeek - 1

        val cells = mutableListOf<LocalDate?>()
        for (i in 0 until leadingEmptyCells) {
            cells.add(null)
        }
        for (day in 1..daysInMonth) {
            cells.add(yearMonth.atDay(day))
        }

        // Fill trailing cells to complete the last row (multiple of 7)
        val remainder = cells.size % 7
        if (remainder != 0) {
            for (i in 0 until (7 - remainder)) {
                cells.add(null)
            }
        }
        return cells
    }

    /**
     * Gets the Monday through Sunday dates for the week containing [date]
     */
    fun getWeekDates(date: LocalDate): List<LocalDate> {
        val dayOfWeekValue = date.dayOfWeek.value // 1 = Monday
        val monday = date.minusDays((dayOfWeekValue - 1).toLong())
        return (0..6).map { monday.plusDays(it.toLong()) }
    }
}
