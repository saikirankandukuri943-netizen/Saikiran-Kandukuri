package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.db.AppDatabase
import com.example.data.repository.AttendanceRepository
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.ui.viewmodel.AttendanceViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: AttendanceViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AttendanceRepository(
            subjectDao = database.subjectDao(),
            attendanceDao = database.attendanceDao(),
            profileDao = database.profileDao(),
            timetableDao = database.timetableDao()
        )
        AttendanceViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}
