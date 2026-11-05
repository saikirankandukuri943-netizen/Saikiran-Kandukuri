package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.AttendanceStatus
import com.example.data.model.SlotType

class Converters {
    @TypeConverter
    fun fromStatus(status: AttendanceStatus?): String {
        return status?.name ?: AttendanceStatus.PRESENT.name
    }

    @TypeConverter
    fun toStatus(value: String?): AttendanceStatus {
        return value?.let {
            runCatching { AttendanceStatus.valueOf(it) }.getOrDefault(AttendanceStatus.PRESENT)
        } ?: AttendanceStatus.PRESENT
    }

    @TypeConverter
    fun fromSlotType(type: SlotType?): String {
        return type?.name ?: SlotType.CLASS.name
    }

    @TypeConverter
    fun toSlotType(value: String?): SlotType {
        return value?.let {
            runCatching { SlotType.valueOf(it) }.getOrDefault(SlotType.CLASS)
        } ?: SlotType.CLASS
    }
}
