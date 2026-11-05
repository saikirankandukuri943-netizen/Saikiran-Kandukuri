package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY dateString DESC, updatedAt DESC")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE dateString = :dateString ORDER BY timeSlot ASC")
    fun getRecordsForDate(dateString: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE dateString BETWEEN :startDate AND :endDate ORDER BY dateString ASC")
    fun getRecordsBetweenDates(startDate: String, endDate: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE subjectId = :subjectId ORDER BY dateString DESC")
    fun getRecordsForSubject(subjectId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE subjectId = :subjectId AND dateString = :dateString AND timeSlot = :timeSlot LIMIT 1")
    suspend fun getRecordForSlot(subjectId: Long, dateString: String, timeSlot: String): AttendanceRecord?

    @Query("SELECT * FROM attendance_records WHERE subjectId = :subjectId AND dateString = :dateString LIMIT 1")
    suspend fun getRecordForSubjectAndDate(subjectId: Long, dateString: String): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(records: List<AttendanceRecord>)

    @Update
    suspend fun update(record: AttendanceRecord)

    @Delete
    suspend fun delete(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM attendance_records WHERE subjectId = :subjectId AND dateString = :dateString AND timeSlot = :timeSlot")
    suspend fun deleteForSlot(subjectId: Long, dateString: String, timeSlot: String)

    @Query("DELETE FROM attendance_records WHERE subjectId = :subjectId AND dateString = :dateString")
    suspend fun deleteForSubjectAndDate(subjectId: Long, dateString: String)

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAll()
}
