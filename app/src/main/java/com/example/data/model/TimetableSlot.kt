package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SlotType {
    CLASS,          // Counts towards subject attendance
    ACTIVITY,       // Library, Mentoring (separate activity, not counted in subject attendance by default)
    LUNCH_BREAK,    // 1:00 PM - 2:00 PM Lunch break (does not count as a class)
    NO_CLASS;       // Free periods (does not count as class or absence)

    val isClass: Boolean
        get() = this == CLASS

    val isMarkable: Boolean
        get() = this == CLASS || this == ACTIVITY
}

@Entity(tableName = "timetable_slots")
data class TimetableSlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int, // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val periodIndex: Int, // 1 to 7
    val startTime: String, // "10:00 AM"
    val endTime: String,   // "11:00 AM"
    val timeSlotDisplay: String, // "10:00 AM - 11:00 AM"
    val subjectCode: String, // "GOT", "MFT", "MOB", "EFT", "PPT", "106 Elec", "IT LAB", "Library", "Mentoring", "Lunch Break", "No Class"
    val subjectName: String,
    val teacherInitials: String = "", // "MVR", "JSR", "CJ", "VA", "VB", "CHP", "PS", "GLP", "AR"
    val slotType: SlotType = SlotType.CLASS,
    val room: String = ""
)
