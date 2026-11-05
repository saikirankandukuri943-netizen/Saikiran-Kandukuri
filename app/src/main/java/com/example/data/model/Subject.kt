package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String = "",
    val faculty: String = "",
    val targetPercentage: Int = 75,
    val colorHex: String = "#1976D2",
    val roomOrSlot: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
