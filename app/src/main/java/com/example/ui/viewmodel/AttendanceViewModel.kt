package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceCalculator
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.OverallAttendanceStats
import com.example.data.model.SlotType
import com.example.data.model.StudentProfile
import com.example.data.model.Subject
import com.example.data.model.SubjectAttendanceStats
import com.example.data.model.TimetableSlot
import com.example.data.repository.AttendanceRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

enum class ReportPeriod(val label: String) {
    THIS_WEEK("This Week"),
    LAST_WEEK("Last Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    ALL_TIME("All Time")
}

data class ReportSummary(
    val period: ReportPeriod,
    val periodLabel: String,
    val overallStats: OverallAttendanceStats,
    val subjectStats: List<SubjectAttendanceStats>,
    val presentDatesCount: Int,
    val absentDatesCount: Int,
    val holidayDatesCount: Int
)

data class DayTimetableStats(
    val totalClasses: Int,
    val completedClasses: Int,
    val pendingClasses: Int
)

class AttendanceViewModel(
    private val repository: AttendanceRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureDefaultData()
        }
    }

    val subjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val records: StateFlow<List<AttendanceRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<StudentProfile> = repository.profile
        .combine(MutableStateFlow(StudentProfile())) { prof, defaultProf ->
            prof ?: defaultProf
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudentProfile())

    val allTimetableSlots: StateFlow<List<TimetableSlot>> = repository.allTimetableSlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected date for dashboard today's attendance marking
    private val _selectedDate = MutableStateFlow(DateUtils.today())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Slots for the selected date's day of week (1 = Monday, 6 = Saturday, 7 = Sunday)
    val selectedDateSlots: StateFlow<List<TimetableSlot>> = combine(
        _selectedDate,
        allTimetableSlots
    ) { date, slots ->
        val dayOfWeek = date.dayOfWeek.value
        slots.filter { it.dayOfWeek == dayOfWeek }.sortedBy { it.periodIndex }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Records for the currently selected date (Key: "$subjectId-$timeSlot")
    val selectedDateSlotRecords: StateFlow<Map<String, AttendanceRecord>> = combine(
        _selectedDate,
        records
    ) { date, allRecs ->
        val dateStr = DateUtils.toIsoString(date)
        allRecs.filter { it.dateString == dateStr }
            .associateBy { rec ->
                if (rec.timeSlot.isNotEmpty()) "${rec.subjectId}-${rec.timeSlot}"
                else rec.subjectId.toString()
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Summary of completed vs pending classes for selected date
    val dayTimetableStats: StateFlow<DayTimetableStats> = combine(
        selectedDateSlots,
        selectedDateSlotRecords,
        subjects
    ) { slots, recordsMap, subjs ->
        val codeToSubject = subjs.associateBy { it.code.uppercase() }
        val classSlots = slots.filter { it.slotType == SlotType.CLASS }
        var completed = 0

        for (slot in classSlots) {
            val subj = codeToSubject[slot.subjectCode.uppercase()]
            if (subj != null) {
                val record = recordsMap["${subj.id}-${slot.timeSlotDisplay}"] ?: recordsMap[subj.id.toString()]
                if (record != null) {
                    completed++
                }
            }
        }

        DayTimetableStats(
            totalClasses = classSlots.size,
            completedClasses = completed,
            pendingClasses = (classSlots.size - completed).coerceAtLeast(0)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DayTimetableStats(0, 0, 0))

    // Subject attendance stats (computed reactively)
    val subjectStatsList: StateFlow<List<SubjectAttendanceStats>> = combine(
        subjects,
        records,
        profile
    ) { subjs, recs, prof ->
        val target = prof.defaultTarget
        subjs.map { subj ->
            AttendanceCalculator.computeSubjectStats(subj, recs, target)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overall attendance stats
    val overallStats: StateFlow<OverallAttendanceStats> = combine(
        records,
        profile
    ) { recs, prof ->
        AttendanceCalculator.computeOverallStats(recs, prof.defaultTarget)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        OverallAttendanceStats(
            totalClasses = 0,
            presentCount = 0,
            absentCount = 0,
            holidayCount = 0,
            percentage = 100f,
            targetPercentage = 75,
            requiredToReach75 = 0,
            canBunkCount = 0,
            bunkAdvice = AttendanceCalculator.calculateBunkAdvice(0, 0, 75)
        )
    )

    // Calendar state
    private val _calendarYearMonth = MutableStateFlow(YearMonth.now())
    val calendarYearMonth: StateFlow<YearMonth> = _calendarYearMonth.asStateFlow()

    private val _selectedCalendarDate = MutableStateFlow(DateUtils.today())
    val selectedCalendarDate: StateFlow<LocalDate> = _selectedCalendarDate.asStateFlow()

    val dateToRecordsMap: StateFlow<Map<String, List<AttendanceRecord>>> = records
        .combine(MutableStateFlow(Unit)) { recs, _ ->
            recs.groupBy { it.dateString }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val selectedCalendarRecords: StateFlow<List<AttendanceRecord>> = combine(
        _selectedCalendarDate,
        records
    ) { date, recs ->
        val dateStr = DateUtils.toIsoString(date)
        recs.filter { it.dateString == dateStr }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reports state
    private val _reportPeriod = MutableStateFlow(ReportPeriod.THIS_MONTH)
    val reportPeriod: StateFlow<ReportPeriod> = _reportPeriod.asStateFlow()

    val reportSummary: StateFlow<ReportSummary> = combine(
        _reportPeriod,
        subjects,
        records,
        profile
    ) { period, subjs, recs, prof ->
        val today = DateUtils.today()
        val (startDate, endDate, label) = when (period) {
            ReportPeriod.THIS_WEEK -> {
                val week = DateUtils.getWeekDates(today)
                Triple(week.first(), week.last(), "This Week (${DateUtils.toShortDisplayString(DateUtils.toIsoString(week.first()))} - ${DateUtils.toShortDisplayString(DateUtils.toIsoString(week.last()))})")
            }
            ReportPeriod.LAST_WEEK -> {
                val prevWeek = DateUtils.getWeekDates(today.minusWeeks(1))
                Triple(prevWeek.first(), prevWeek.last(), "Last Week (${DateUtils.toShortDisplayString(DateUtils.toIsoString(prevWeek.first()))} - ${DateUtils.toShortDisplayString(DateUtils.toIsoString(prevWeek.last()))})")
            }
            ReportPeriod.THIS_MONTH -> {
                val ym = YearMonth.from(today)
                Triple(ym.atDay(1), ym.atEndOfMonth(), DateUtils.toMonthYearString(ym))
            }
            ReportPeriod.LAST_MONTH -> {
                val ym = YearMonth.from(today).minusMonths(1)
                Triple(ym.atDay(1), ym.atEndOfMonth(), DateUtils.toMonthYearString(ym))
            }
            ReportPeriod.ALL_TIME -> {
                Triple(today.minusYears(2), today.plusYears(1), "All Time History")
            }
        }

        val startStr = DateUtils.toIsoString(startDate)
        val endStr = DateUtils.toIsoString(endDate)

        val filteredRecs = recs.filter { it.dateString in startStr..endStr }
        val overall = AttendanceCalculator.computeOverallStats(filteredRecs, prof.defaultTarget)
        val subStats = subjs.map { subj ->
            AttendanceCalculator.computeSubjectStats(subj, filteredRecs, prof.defaultTarget)
        }

        val distinctPresentDates = filteredRecs.filter { it.status == AttendanceStatus.PRESENT }.map { it.dateString }.distinct().size
        val distinctAbsentDates = filteredRecs.filter { it.status == AttendanceStatus.ABSENT }.map { it.dateString }.distinct().size
        val distinctHolidayDates = filteredRecs.filter { it.status == AttendanceStatus.HOLIDAY }.map { it.dateString }.distinct().size

        ReportSummary(
            period = period,
            periodLabel = label,
            overallStats = overall,
            subjectStats = subStats,
            presentDatesCount = distinctPresentDates,
            absentDatesCount = distinctAbsentDates,
            holidayDatesCount = distinctHolidayDates
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReportSummary(
            period = ReportPeriod.THIS_MONTH,
            periodLabel = "This Month",
            overallStats = AttendanceCalculator.computeOverallStats(emptyList(), 75),
            subjectStats = emptyList(),
            presentDatesCount = 0,
            absentDatesCount = 0,
            holidayDatesCount = 0
        )
    )

    // UI Feedback Message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun nextDate() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun prevDate() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun setCalendarMonth(ym: YearMonth) {
        _calendarYearMonth.value = ym
    }

    fun nextCalendarMonth() {
        _calendarYearMonth.value = _calendarYearMonth.value.plusMonths(1)
    }

    fun prevCalendarMonth() {
        _calendarYearMonth.value = _calendarYearMonth.value.minusMonths(1)
    }

    fun setSelectedCalendarDate(date: LocalDate) {
        _selectedCalendarDate.value = date
    }

    fun setReportPeriod(period: ReportPeriod) {
        _reportPeriod.value = period
    }

    // Attendance Actions
    fun markSlotAttendance(
        slot: TimetableSlot,
        status: AttendanceStatus,
        date: LocalDate = _selectedDate.value,
        note: String = ""
    ) {
        viewModelScope.launch {
            val dateStr = DateUtils.toIsoString(date)
            val subjs = subjects.value
            val targetSubject = subjs.find { it.code.equals(slot.subjectCode, ignoreCase = true) }
                ?: subjs.firstOrNull()

            if (targetSubject != null) {
                repository.markAttendance(
                    subjectId = targetSubject.id,
                    dateString = dateStr,
                    status = status,
                    timeSlot = slot.timeSlotDisplay,
                    slotId = slot.id,
                    note = note
                )
                _userMessage.value = "${slot.subjectCode} (${slot.timeSlotDisplay}) marked as ${status.displayName}"
            } else {
                _userMessage.value = "Subject ${slot.subjectCode} not found in database"
            }
        }
    }

    fun markAttendance(
        subjectId: Long,
        status: AttendanceStatus,
        date: LocalDate = _selectedDate.value,
        timeSlot: String = "",
        slotId: Long = 0,
        note: String = ""
    ) {
        viewModelScope.launch {
            val dateStr = DateUtils.toIsoString(date)
            repository.markAttendance(
                subjectId = subjectId,
                dateString = dateStr,
                status = status,
                timeSlot = timeSlot,
                slotId = slotId,
                note = note
            )
            _userMessage.value = "Attendance marked as ${status.displayName}"
        }
    }

    fun editAttendance(
        subjectId: Long,
        dateString: String,
        timeSlot: String,
        status: AttendanceStatus,
        note: String
    ) {
        viewModelScope.launch {
            repository.markAttendance(
                subjectId = subjectId,
                dateString = dateString,
                status = status,
                timeSlot = timeSlot,
                note = note
            )
            _userMessage.value = "Attendance entry updated successfully"
        }
    }

    fun removeAttendance(subjectId: Long, dateString: String, timeSlot: String = "") {
        viewModelScope.launch {
            repository.removeAttendanceRecord(subjectId, dateString, timeSlot)
            _userMessage.value = "Attendance entry removed"
        }
    }

    fun deleteRecordById(id: Long) {
        viewModelScope.launch {
            repository.deleteRecordById(id)
            _userMessage.value = "Entry removed"
        }
    }

    // Timetable slot editing (Allow timetable editing if college changes the schedule)
    fun updateTimetableSlot(slot: TimetableSlot) {
        viewModelScope.launch {
            repository.updateTimetableSlot(slot)
            _userMessage.value = "Timetable slot updated"
        }
    }

    // Subject Management
    fun addSubject(
        name: String,
        code: String,
        faculty: String,
        targetPercentage: Int = 75,
        colorHex: String = "#1E88E5",
        room: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val subject = Subject(
                name = name.trim(),
                code = code.trim().uppercase(),
                faculty = faculty.trim(),
                targetPercentage = targetPercentage,
                colorHex = colorHex,
                roomOrSlot = room.trim(),
                notes = notes.trim()
            )
            repository.insertSubject(subject)
            _userMessage.value = "Subject \"$name\" added successfully"
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            repository.updateSubject(subject)
            _userMessage.value = "Subject \"${subject.name}\" updated"
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            _userMessage.value = "Subject \"${subject.name}\" deleted"
        }
    }

    // Profile & Settings
    fun updateProfile(
        name: String,
        rollNumber: String,
        college: String,
        course: String,
        semester: String,
        target: Int,
        email: String
    ) {
        viewModelScope.launch {
            val current = profile.value
            val updated = current.copy(
                name = name.trim(),
                rollNumber = rollNumber.trim(),
                college = college.trim(),
                course = course.trim(),
                semester = semester.trim(),
                defaultTarget = target,
                email = email.trim()
            )
            repository.updateProfile(updated)
            _userMessage.value = "Profile updated successfully"
        }
    }

    fun updateDefaultTarget(target: Int) {
        viewModelScope.launch {
            val current = profile.value
            repository.updateProfile(current.copy(defaultTarget = target))
            _userMessage.value = "Attendance target updated to $target%"
        }
    }

    fun reloadMbaTtmTimetable() {
        viewModelScope.launch {
            repository.seedExactWeeklyTimetable()
            _userMessage.value = "Weekly timetable and subjects reloaded!"
        }
    }

    fun seedSampleAttendanceData() {
        viewModelScope.launch {
            val currentSubjects = subjects.value
            if (currentSubjects.isNotEmpty()) {
                repository.seedSampleRecordsForSubjects(currentSubjects)
                _userMessage.value = "Sample attendance history generated!"
            } else {
                repository.seedExactWeeklyTimetable()
                val updated = repository.allSubjects
                _userMessage.value = "Subjects and sample data generated!"
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _userMessage.value = "All subjects and attendance records cleared"
        }
    }
}

class AttendanceViewModelFactory(
    private val repository: AttendanceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AttendanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
