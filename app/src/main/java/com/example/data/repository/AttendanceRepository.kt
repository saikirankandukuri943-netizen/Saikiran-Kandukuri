package com.example.data.repository

import com.example.data.dao.AttendanceDao
import com.example.data.dao.ProfileDao
import com.example.data.dao.SubjectDao
import com.example.data.dao.TimetableDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.SlotType
import com.example.data.model.StudentProfile
import com.example.data.model.Subject
import com.example.data.model.TimetableSlot
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AttendanceRepository(
    private val subjectDao: SubjectDao,
    private val attendanceDao: AttendanceDao,
    private val profileDao: ProfileDao,
    private val timetableDao: TimetableDao
) {
    val allSubjects: Flow<List<Subject>> = subjectDao.getAllSubjects()
    val allRecords: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecords()
    val profile: Flow<StudentProfile?> = profileDao.getProfile()
    val allTimetableSlots: Flow<List<TimetableSlot>> = timetableDao.getAllSlots()

    fun getSlotsForDay(dayOfWeek: Int): Flow<List<TimetableSlot>> {
        return timetableDao.getSlotsForDay(dayOfWeek)
    }

    fun getRecordsForDate(dateString: String): Flow<List<AttendanceRecord>> {
        return attendanceDao.getRecordsForDate(dateString)
    }

    fun getRecordsBetweenDates(startDate: String, endDate: String): Flow<List<AttendanceRecord>> {
        return attendanceDao.getRecordsBetweenDates(startDate, endDate)
    }

    fun getRecordsForSubject(subjectId: Long): Flow<List<AttendanceRecord>> {
        return attendanceDao.getRecordsForSubject(subjectId)
    }

    suspend fun getRecordForSlot(subjectId: Long, dateString: String, timeSlot: String): AttendanceRecord? {
        return attendanceDao.getRecordForSlot(subjectId, dateString, timeSlot)
    }

    suspend fun markAttendance(
        subjectId: Long,
        dateString: String,
        status: AttendanceStatus,
        timeSlot: String = "",
        slotId: Long = 0,
        note: String = ""
    ) {
        val existing = if (timeSlot.isNotEmpty()) {
            attendanceDao.getRecordForSlot(subjectId, dateString, timeSlot)
        } else {
            attendanceDao.getRecordForSubjectAndDate(subjectId, dateString)
        }

        val record = if (existing != null) {
            existing.copy(
                status = status,
                timeSlot = if (timeSlot.isNotEmpty()) timeSlot else existing.timeSlot,
                slotId = if (slotId != 0L) slotId else existing.slotId,
                note = note,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            AttendanceRecord(
                subjectId = subjectId,
                dateString = dateString,
                status = status,
                timeSlot = timeSlot,
                slotId = slotId,
                note = note,
                updatedAt = System.currentTimeMillis()
            )
        }
        attendanceDao.insertOrUpdate(record)
    }

    suspend fun removeAttendanceRecord(subjectId: Long, dateString: String, timeSlot: String = "") {
        if (timeSlot.isNotEmpty()) {
            attendanceDao.deleteForSlot(subjectId, dateString, timeSlot)
        } else {
            attendanceDao.deleteForSubjectAndDate(subjectId, dateString)
        }
    }

    suspend fun deleteRecordById(id: Long) {
        attendanceDao.deleteById(id)
    }

    suspend fun insertSubject(subject: Subject): Long {
        return subjectDao.insertSubject(subject)
    }

    suspend fun updateSubject(subject: Subject) {
        subjectDao.updateSubject(subject)
    }

    suspend fun deleteSubject(subject: Subject) {
        subjectDao.deleteSubject(subject)
    }

    suspend fun updateTimetableSlot(slot: TimetableSlot) {
        timetableDao.updateSlot(slot)
    }

    suspend fun updateProfile(profile: StudentProfile) {
        profileDao.insertOrUpdateProfile(profile)
    }

    suspend fun ensureDefaultData() {
        if (subjectDao.getSubjectCount() == 0) {
            seedExactWeeklyTimetable()
        } else if (timetableDao.getSlotCount() == 0) {
            seedTimetableSlotsOnly()
        }
    }

    suspend fun seedExactWeeklyTimetable() {
        val defaultSubjects = listOf(
            Subject(
                name = "Global Operations & Tourism",
                code = "GOT",
                faculty = "MVR",
                targetPercentage = 75,
                colorHex = "#1E88E5", // Deep Blue
                roomOrSlot = "Room 101"
            ),
            Subject(
                name = "Management & Financial Tools",
                code = "MFT",
                faculty = "JSR",
                targetPercentage = 75,
                colorHex = "#0288D1", // Light Blue
                roomOrSlot = "Room 102"
            ),
            Subject(
                name = "Management of Organizations & Behavior",
                code = "MOB",
                faculty = "CJ",
                targetPercentage = 75,
                colorHex = "#00897B", // Teal
                roomOrSlot = "Room 103"
            ),
            Subject(
                name = "Economics & Financial Tools",
                code = "EFT",
                faculty = "VA",
                targetPercentage = 75,
                colorHex = "#3949AB", // Indigo
                roomOrSlot = "Room 104"
            ),
            Subject(
                name = "Principles & Practices of Tourism",
                code = "PPT",
                faculty = "PS / GLP",
                targetPercentage = 75,
                colorHex = "#5E35B1", // Deep Purple
                roomOrSlot = "Room 105"
            ),
            Subject(
                name = "Elective Course 106",
                code = "106 Elec",
                faculty = "AR",
                targetPercentage = 75,
                colorHex = "#00ACC1", // Cyan
                roomOrSlot = "Room 106"
            ),
            Subject(
                name = "Information Technology Lab",
                code = "IT LAB",
                faculty = "VB / CHP",
                targetPercentage = 75,
                colorHex = "#43A047", // Green
                roomOrSlot = "IT Lab"
            )
        )
        subjectDao.insertSubjects(defaultSubjects)

        profileDao.insertOrUpdateProfile(
            StudentProfile(
                id = 1,
                name = "Sai Kiran",
                rollNumber = "MBA-TTM-01",
                college = "School of Management & Tourism Studies",
                course = "MBA - Tourism and Travel Management (MBA TTM)",
                semester = "1st Semester",
                defaultTarget = 75,
                email = "saikiran@college.edu"
            )
        )

        seedTimetableSlotsOnly()
    }

    suspend fun seedTimetableSlotsOnly() {
        timetableDao.deleteAllSlots()
        val slots = mutableListOf<TimetableSlot>()

        // 1. MONDAY (Day 1)
        slots.add(TimetableSlot(dayOfWeek = 1, periodIndex = 1, startTime = "10:00 AM", endTime = "11:00 AM", timeSlotDisplay = "10:00 AM - 11:00 AM", subjectCode = "GOT", subjectName = "Global Operations & Tourism", teacherInitials = "MVR", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 1, periodIndex = 2, startTime = "11:00 AM", endTime = "12:00 PM", timeSlotDisplay = "11:00 AM - 12:00 PM", subjectCode = "MFT", subjectName = "Management & Financial Tools", teacherInitials = "JSR", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 1, periodIndex = 3, startTime = "12:00 PM", endTime = "1:00 PM", timeSlotDisplay = "12:00 PM - 1:00 PM", subjectCode = "MOB", subjectName = "Management of Organizations & Behavior", teacherInitials = "CJ", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 1, periodIndex = 4, startTime = "1:00 PM", endTime = "2:00 PM", timeSlotDisplay = "1:00 PM - 2:00 PM", subjectCode = "LUNCH", subjectName = "Lunch Break", teacherInitials = "", slotType = SlotType.LUNCH_BREAK))
        slots.add(TimetableSlot(dayOfWeek = 1, periodIndex = 5, startTime = "2:00 PM", endTime = "3:00 PM", timeSlotDisplay = "2:00 PM - 3:00 PM", subjectCode = "EFT", subjectName = "Economics & Financial Tools", teacherInitials = "VA", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 1, periodIndex = 6, startTime = "3:00 PM", endTime = "4:00 PM", timeSlotDisplay = "3:00 PM - 4:00 PM", subjectCode = "IT LAB", subjectName = "Information Technology Lab", teacherInitials = "VB", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 1, periodIndex = 7, startTime = "4:00 PM", endTime = "5:00 PM", timeSlotDisplay = "4:00 PM - 5:00 PM", subjectCode = "IT LAB", subjectName = "Information Technology Lab", teacherInitials = "VB", slotType = SlotType.CLASS))

        // 2. TUESDAY (Day 2)
        slots.add(TimetableSlot(dayOfWeek = 2, periodIndex = 1, startTime = "10:00 AM", endTime = "11:00 AM", timeSlotDisplay = "10:00 AM - 11:00 AM", subjectCode = "PPT", subjectName = "Principles & Practices of Tourism", teacherInitials = "PS", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 2, periodIndex = 2, startTime = "11:00 AM", endTime = "12:00 PM", timeSlotDisplay = "11:00 AM - 12:00 PM", subjectCode = "106 Elec", subjectName = "Elective Course 106", teacherInitials = "AR", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 2, periodIndex = 3, startTime = "12:00 PM", endTime = "1:00 PM", timeSlotDisplay = "12:00 PM - 1:00 PM", subjectCode = "MOB", subjectName = "Management of Organizations & Behavior", teacherInitials = "CJ", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 2, periodIndex = 4, startTime = "1:00 PM", endTime = "2:00 PM", timeSlotDisplay = "1:00 PM - 2:00 PM", subjectCode = "LUNCH", subjectName = "Lunch Break", teacherInitials = "", slotType = SlotType.LUNCH_BREAK))
        slots.add(TimetableSlot(dayOfWeek = 2, periodIndex = 5, startTime = "2:00 PM", endTime = "3:00 PM", timeSlotDisplay = "2:00 PM - 3:00 PM", subjectCode = "GOT", subjectName = "Global Operations & Tourism", teacherInitials = "MVR", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 2, periodIndex = 6, startTime = "3:00 PM", endTime = "4:00 PM", timeSlotDisplay = "3:00 PM - 4:00 PM", subjectCode = "Library", subjectName = "Library Period", teacherInitials = "", slotType = SlotType.ACTIVITY))
        slots.add(TimetableSlot(dayOfWeek = 2, periodIndex = 7, startTime = "4:00 PM", endTime = "5:00 PM", timeSlotDisplay = "4:00 PM - 5:00 PM", subjectCode = "Library", subjectName = "Library Period", teacherInitials = "", slotType = SlotType.ACTIVITY))

        // 3. WEDNESDAY (Day 3)
        // Note: Crossed-out 12-1 PM 106 Elec class excluded as No Class
        slots.add(TimetableSlot(dayOfWeek = 3, periodIndex = 1, startTime = "10:00 AM", endTime = "11:00 AM", timeSlotDisplay = "10:00 AM - 11:00 AM", subjectCode = "GOT", subjectName = "Global Operations & Tourism", teacherInitials = "MVR", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 3, periodIndex = 2, startTime = "11:00 AM", endTime = "12:00 PM", timeSlotDisplay = "11:00 AM - 12:00 PM", subjectCode = "EFT", subjectName = "Economics & Financial Tools", teacherInitials = "VA", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 3, periodIndex = 3, startTime = "12:00 PM", endTime = "1:00 PM", timeSlotDisplay = "12:00 PM - 1:00 PM", subjectCode = "No Class", subjectName = "No Class", teacherInitials = "", slotType = SlotType.NO_CLASS))
        slots.add(TimetableSlot(dayOfWeek = 3, periodIndex = 4, startTime = "1:00 PM", endTime = "2:00 PM", timeSlotDisplay = "1:00 PM - 2:00 PM", subjectCode = "LUNCH", subjectName = "Lunch Break", teacherInitials = "", slotType = SlotType.LUNCH_BREAK))
        slots.add(TimetableSlot(dayOfWeek = 3, periodIndex = 5, startTime = "2:00 PM", endTime = "3:00 PM", timeSlotDisplay = "2:00 PM - 3:00 PM", subjectCode = "IT LAB", subjectName = "Information Technology Lab", teacherInitials = "CHP", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 3, periodIndex = 6, startTime = "3:00 PM", endTime = "4:00 PM", timeSlotDisplay = "3:00 PM - 4:00 PM", subjectCode = "IT LAB", subjectName = "Information Technology Lab", teacherInitials = "CHP", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 3, periodIndex = 7, startTime = "4:00 PM", endTime = "5:00 PM", timeSlotDisplay = "4:00 PM - 5:00 PM", subjectCode = "No Class", subjectName = "No Class", teacherInitials = "", slotType = SlotType.NO_CLASS))

        // 4. THURSDAY (Day 4)
        slots.add(TimetableSlot(dayOfWeek = 4, periodIndex = 1, startTime = "10:00 AM", endTime = "11:00 AM", timeSlotDisplay = "10:00 AM - 11:00 AM", subjectCode = "MFT", subjectName = "Management & Financial Tools", teacherInitials = "JSR", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 4, periodIndex = 2, startTime = "11:00 AM", endTime = "12:00 PM", timeSlotDisplay = "11:00 AM - 12:00 PM", subjectCode = "GOT", subjectName = "Global Operations & Tourism", teacherInitials = "MVR", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 4, periodIndex = 3, startTime = "12:00 PM", endTime = "1:00 PM", timeSlotDisplay = "12:00 PM - 1:00 PM", subjectCode = "PPT", subjectName = "Principles & Practices of Tourism", teacherInitials = "GLP", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 4, periodIndex = 4, startTime = "1:00 PM", endTime = "2:00 PM", timeSlotDisplay = "1:00 PM - 2:00 PM", subjectCode = "LUNCH", subjectName = "Lunch Break", teacherInitials = "", slotType = SlotType.LUNCH_BREAK))
        slots.add(TimetableSlot(dayOfWeek = 4, periodIndex = 5, startTime = "2:00 PM", endTime = "3:00 PM", timeSlotDisplay = "2:00 PM - 3:00 PM", subjectCode = "106 Elec", subjectName = "Elective Course 106", teacherInitials = "AR", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 4, periodIndex = 6, startTime = "3:00 PM", endTime = "4:00 PM", timeSlotDisplay = "3:00 PM - 4:00 PM", subjectCode = "106 Elec", subjectName = "Elective Course 106", teacherInitials = "AR", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 4, periodIndex = 7, startTime = "4:00 PM", endTime = "5:00 PM", timeSlotDisplay = "4:00 PM - 5:00 PM", subjectCode = "No Class", subjectName = "No Class", teacherInitials = "", slotType = SlotType.NO_CLASS))

        // 5. FRIDAY (Day 5)
        slots.add(TimetableSlot(dayOfWeek = 5, periodIndex = 1, startTime = "10:00 AM", endTime = "11:00 AM", timeSlotDisplay = "10:00 AM - 11:00 AM", subjectCode = "PPT", subjectName = "Principles & Practices of Tourism", teacherInitials = "PS", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 5, periodIndex = 2, startTime = "11:00 AM", endTime = "12:00 PM", timeSlotDisplay = "11:00 AM - 12:00 PM", subjectCode = "EFT", subjectName = "Economics & Financial Tools", teacherInitials = "VA", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 5, periodIndex = 3, startTime = "12:00 PM", endTime = "1:00 PM", timeSlotDisplay = "12:00 PM - 1:00 PM", subjectCode = "MFT", subjectName = "Management & Financial Tools", teacherInitials = "JSR", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 5, periodIndex = 4, startTime = "1:00 PM", endTime = "2:00 PM", timeSlotDisplay = "1:00 PM - 2:00 PM", subjectCode = "LUNCH", subjectName = "Lunch Break", teacherInitials = "", slotType = SlotType.LUNCH_BREAK))
        slots.add(TimetableSlot(dayOfWeek = 5, periodIndex = 5, startTime = "2:00 PM", endTime = "3:00 PM", timeSlotDisplay = "2:00 PM - 3:00 PM", subjectCode = "MOB", subjectName = "Management of Organizations & Behavior", teacherInitials = "CJ", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 5, periodIndex = 6, startTime = "3:00 PM", endTime = "4:00 PM", timeSlotDisplay = "3:00 PM - 4:00 PM", subjectCode = "Mentoring", subjectName = "Mentoring Session", teacherInitials = "", slotType = SlotType.ACTIVITY))
        slots.add(TimetableSlot(dayOfWeek = 5, periodIndex = 7, startTime = "4:00 PM", endTime = "5:00 PM", timeSlotDisplay = "4:00 PM - 5:00 PM", subjectCode = "Mentoring", subjectName = "Mentoring Session", teacherInitials = "", slotType = SlotType.ACTIVITY))

        // 6. SATURDAY (Day 6)
        slots.add(TimetableSlot(dayOfWeek = 6, periodIndex = 1, startTime = "10:00 AM", endTime = "11:00 AM", timeSlotDisplay = "10:00 AM - 11:00 AM", subjectCode = "MFT", subjectName = "Management & Financial Tools", teacherInitials = "JSR", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 6, periodIndex = 2, startTime = "11:00 AM", endTime = "12:00 PM", timeSlotDisplay = "11:00 AM - 12:00 PM", subjectCode = "PPT", subjectName = "Principles & Practices of Tourism", teacherInitials = "GLP", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 6, periodIndex = 3, startTime = "12:00 PM", endTime = "1:00 PM", timeSlotDisplay = "12:00 PM - 1:00 PM", subjectCode = "EFT", subjectName = "Economics & Financial Tools", teacherInitials = "VA", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 6, periodIndex = 4, startTime = "1:00 PM", endTime = "2:00 PM", timeSlotDisplay = "1:00 PM - 2:00 PM", subjectCode = "LUNCH", subjectName = "Lunch Break", teacherInitials = "", slotType = SlotType.LUNCH_BREAK))
        slots.add(TimetableSlot(dayOfWeek = 6, periodIndex = 5, startTime = "2:00 PM", endTime = "3:00 PM", timeSlotDisplay = "2:00 PM - 3:00 PM", subjectCode = "106 Elec", subjectName = "Elective Course 106", teacherInitials = "AR", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 6, periodIndex = 6, startTime = "3:00 PM", endTime = "4:00 PM", timeSlotDisplay = "3:00 PM - 4:00 PM", subjectCode = "MOB", subjectName = "Management of Organizations & Behavior", teacherInitials = "CJ", slotType = SlotType.CLASS))
        slots.add(TimetableSlot(dayOfWeek = 6, periodIndex = 7, startTime = "4:00 PM", endTime = "5:00 PM", timeSlotDisplay = "4:00 PM - 5:00 PM", subjectCode = "No Class", subjectName = "No Class", teacherInitials = "", slotType = SlotType.NO_CLASS))

        timetableDao.insertSlots(slots)
    }

    suspend fun seedSampleRecordsForSubjects(subjects: List<Subject>) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val records = mutableListOf<AttendanceRecord>()

        for (dayOffset in 20 downTo 1) {
            val date = today.minusDays(dayOffset.toLong())
            val dayOfWeek = date.dayOfWeek.value
            if (dayOfWeek == 7) continue // Sunday off

            val dateStr = date.format(formatter)
            val isHoliday = (dayOffset == 10)

            for (subject in subjects) {
                val status = when {
                    isHoliday -> AttendanceStatus.HOLIDAY
                    (dayOffset + subject.id.toInt()) % 6 == 0 -> AttendanceStatus.ABSENT
                    else -> AttendanceStatus.PRESENT
                }
                records.add(
                    AttendanceRecord(
                        subjectId = subject.id,
                        dateString = dateStr,
                        timeSlot = "Slot 1",
                        status = status,
                        note = if (status == AttendanceStatus.ABSENT) "Sick leave / missed bus" else "",
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
        attendanceDao.insertOrUpdateAll(records)
    }

    suspend fun clearAllData() {
        attendanceDao.deleteAll()
        subjectDao.deleteAllSubjects()
        timetableDao.deleteAllSlots()
    }
}
