package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    HOLIDAY;

    val displayName: String
        get() = when (this) {
            PRESENT -> "Present"
            ABSENT -> "Absent"
            HOLIDAY -> "Holiday / Cancelled"
        }
}

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["subjectId", "dateString", "timeSlot"], unique = true),
        Index(value = ["dateString"]),
        Index(value = ["subjectId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val dateString: String, // Format: YYYY-MM-DD
    val status: AttendanceStatus,
    val timeSlot: String = "", // Format: "10:00 AM - 11:00 AM" to support separate periods
    val slotId: Long = 0,
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
