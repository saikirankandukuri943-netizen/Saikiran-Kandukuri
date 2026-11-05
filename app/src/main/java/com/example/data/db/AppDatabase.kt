package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.AttendanceDao
import com.example.data.dao.ProfileDao
import com.example.data.dao.SubjectDao
import com.example.data.dao.TimetableDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.StudentProfile
import com.example.data.model.Subject
import com.example.data.model.TimetableSlot

@Database(
    entities = [
        Subject::class,
        AttendanceRecord::class,
        StudentProfile::class,
        TimetableSlot::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun profileDao(): ProfileDao
    abstract fun timetableDao(): TimetableDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "college_attendance_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
