package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TimetableSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_slots ORDER BY dayOfWeek ASC, periodIndex ASC")
    fun getAllSlots(): Flow<List<TimetableSlot>>

    @Query("SELECT * FROM timetable_slots WHERE dayOfWeek = :dayOfWeek ORDER BY periodIndex ASC")
    fun getSlotsForDay(dayOfWeek: Int): Flow<List<TimetableSlot>>

    @Query("SELECT COUNT(*) FROM timetable_slots")
    suspend fun getSlotCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: TimetableSlot): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<TimetableSlot>)

    @Update
    suspend fun updateSlot(slot: TimetableSlot)

    @Delete
    suspend fun deleteSlot(slot: TimetableSlot)

    @Query("DELETE FROM timetable_slots")
    suspend fun deleteAllSlots()
}
