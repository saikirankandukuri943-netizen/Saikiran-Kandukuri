package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.StudentProfile
import com.example.data.model.Subject
import com.example.ui.theme.StatusAbsentRed
import com.example.util.DateUtils

@Composable
fun EditAttendanceDialog(
    record: AttendanceRecord,
    subjectName: String,
    onDismiss: () -> Unit,
    onSave: (AttendanceStatus, String) -> Unit,
    onDelete: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf(record.status) }
    var note by remember { mutableStateOf(record.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Edit Attendance",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "$subjectName • ${DateUtils.toDisplayString(record.dateString)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Attendance Status",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )

                // Selectable statuses
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AttendanceStatus.values().forEach { status ->
                        val isSelected = selectedStatus == status
                        Surface(
                            onClick = { selectedStatus = status },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = status.displayName.split(" ").first(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Reason (Optional)") },
                    placeholder = { Text("e.g. Sick leave, College fest, Exam") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("attendance_note_input"),
                    singleLine = false,
                    maxLines = 3
                )

                // Option to delete record
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAbsentRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delete_attendance_button")
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Attendance Entry",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete This Entry")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedStatus, note) },
                modifier = Modifier.testTag("save_attendance_button")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditSubjectDialog(
    initialSubject: Subject? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, code: String, faculty: String, target: Int, colorHex: String, room: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialSubject?.name ?: "") }
    var code by remember { mutableStateOf(initialSubject?.code ?: "") }
    var faculty by remember { mutableStateOf(initialSubject?.faculty ?: "") }
    var target by remember { mutableFloatStateOf((initialSubject?.targetPercentage ?: 75).toFloat()) }
    var room by remember { mutableStateOf(initialSubject?.roomOrSlot ?: "") }
    var notes by remember { mutableStateOf(initialSubject?.notes ?: "") }

    val presetColors = listOf(
        "#1E88E5", // Blue
        "#0288D1", // Light Blue
        "#00897B", // Teal
        "#43A047", // Green
        "#3949AB", // Indigo
        "#5E35B1", // Deep Purple
        "#D81B60", // Pink
        "#E65100"  // Orange
    )
    var selectedColor by remember { mutableStateOf(initialSubject?.colorHex ?: presetColors.first()) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialSubject == null) "Add New Subject" else "Edit Subject",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Subject Name *") },
                    placeholder = { Text("e.g. Global Operations & Tourism") },
                    isError = errorMessage != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_name_input"),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code / Short") },
                        placeholder = { Text("e.g. GOT") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subject_code_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("Room / Lab") },
                        placeholder = { Text("e.g. Room 101") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subject_room_input"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = faculty,
                    onValueChange = { faculty = it },
                    label = { Text("Faculty / Teacher") },
                    placeholder = { Text("e.g. Prof. MVR") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_faculty_input"),
                    singleLine = true
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Target Attendance",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "${target.toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Slider(
                        value = target,
                        onValueChange = { target = it },
                        valueRange = 50f..95f,
                        steps = 8
                    )
                }

                Text(
                    text = "Color Badge",
                    style = MaterialTheme.typography.labelMedium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetColors) { hex ->
                        val isSelected = hex == selectedColor
                        val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Blue)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex }
                                .then(
                                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Syllabus, timings, lecture schedule") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Subject name cannot be empty"
                    } else {
                        onSave(name, code, faculty, target.toInt(), selectedColor, room, notes)
                    }
                },
                modifier = Modifier.testTag("save_subject_button")
            ) {
                Text(if (initialSubject == null) "Add Subject" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditProfileDialog(
    profile: StudentProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, roll: String, college: String, course: String, semester: String, target: Int, email: String) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var roll by remember { mutableStateOf(profile.rollNumber) }
    var college by remember { mutableStateOf(profile.college) }
    var course by remember { mutableStateOf(profile.course) }
    var semester by remember { mutableStateOf(profile.semester) }
    var target by remember { mutableFloatStateOf(profile.defaultTarget.toFloat()) }
    var email by remember { mutableStateOf(profile.email) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Student Profile",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = roll,
                    onValueChange = { roll = it },
                    label = { Text("Roll Number / Student ID") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_roll_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = college,
                    onValueChange = { college = it },
                    label = { Text("College / University") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = course,
                        onValueChange = { course = it },
                        label = { Text("Course / Branch") },
                        placeholder = { Text("e.g. MBA-TTM") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = semester,
                        onValueChange = { semester = it },
                        label = { Text("Semester") },
                        placeholder = { Text("1st Sem") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("College Email (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "College Attendance Target",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "${target.toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Slider(
                        value = target,
                        onValueChange = { target = it },
                        valueRange = 60f..90f,
                        steps = 5
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, roll, college, course, semester, target.toInt(), email)
                },
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditTimetableSlotDialog(
    slot: com.example.data.model.TimetableSlot,
    onDismiss: () -> Unit,
    onSave: (com.example.data.model.TimetableSlot) -> Unit
) {
    var code by remember { mutableStateOf(slot.subjectCode) }
    var name by remember { mutableStateOf(slot.subjectName) }
    var teacher by remember { mutableStateOf(slot.teacherInitials) }
    var timeSlot by remember { mutableStateOf(slot.timeSlotDisplay) }
    var selectedType by remember { mutableStateOf(slot.slotType) }
    var room by remember { mutableStateOf(slot.room) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Timetable Period",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = timeSlot,
                    onValueChange = { timeSlot = it },
                    label = { Text("Class Timing") },
                    placeholder = { Text("e.g. 10:00 AM - 11:00 AM") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Subject Code") },
                        placeholder = { Text("e.g. GOT") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = teacher,
                        onValueChange = { teacher = it },
                        label = { Text("Teacher (Initials)") },
                        placeholder = { Text("e.g. MVR") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Subject / Activity Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room / Lab (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Period Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    com.example.data.model.SlotType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            onClick = { selectedType = type },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = when (type) {
                                        com.example.data.model.SlotType.CLASS -> "Class"
                                        com.example.data.model.SlotType.ACTIVITY -> "Activity"
                                        com.example.data.model.SlotType.LUNCH_BREAK -> "Lunch"
                                        com.example.data.model.SlotType.NO_CLASS -> "No Class"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        slot.copy(
                            subjectCode = code.trim(),
                            subjectName = name.trim(),
                            teacherInitials = teacher.trim(),
                            timeSlotDisplay = timeSlot.trim(),
                            slotType = selectedType,
                            room = room.trim()
                        )
                    )
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

