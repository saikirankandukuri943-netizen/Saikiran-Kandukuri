package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Sai Kiran",
    val rollNumber: String = "MBA-TTM-01",
    val college: String = "School of Management & Tourism Studies",
    val course: String = "MBA - Tourism and Travel Management (MBA TTM)",
    val semester: String = "1st Semester",
    val defaultTarget: Int = 75,
    val email: String = "saikiran@college.edu"
)
