package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalendarEvent
import com.example.ui.MainViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: MainViewModel) {
    val year by viewModel.calendarYear.collectAsState()
    val month by viewModel.calendarMonth.collectAsState()
    val selectedDateStr by viewModel.calendarSelectedDate.collectAsState()
    val allEvents by viewModel.activeMonthEvents.collectAsState()
    val selectedDayEvents by viewModel.selectedDayEvents.collectAsState()

    var showAddEventDialog by remember { mutableStateOf(false) }
    var eventTitleInput by remember { mutableStateOf("") }
    var eventDescInput by remember { mutableStateOf("") }

    // Constants for Calendar
    val monthsNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val dayOfWeekLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    // Dynamic month days and grid calculation using java.util.Calendar
    val javaCal = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val daysInActiveMonth = javaCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startDayOffset = javaCal.get(Calendar.DAY_OF_WEEK) - 1 // 0 (Sun) to 6 (Sat)

    // Helper: format YYYY-MM-DD for comparing and querying events
    fun getFormattedDateString(day: Int): String {
        return String.format("%04d-%02d-%02d", year, month + 1, day)
    }

    // Identify current system date today to badge highlight it
    val systemTodayStr = remember {
        val today = Calendar.getInstance()
        String.format("%04d-%02d-%02d", today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Omni Calendar", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Calendar Event")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // --- Month and Year Header Controller Row ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.stepCalendarMonth(-1) }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Prev Month")
                    }

                    Text(
                        text = "${monthsNames[month]} $year",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    IconButton(onClick = { viewModel.stepCalendarMonth(1) }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month")
                    }
                }
            }

            // --- Weeks Header labels ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayOfWeekLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- Month Grid Display deck ---
            val totalCells = daysInActiveMonth + startDayOffset
            val rowsCount = (totalCells + 6) / 7

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (row in 0 until rowsCount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - startDayOffset + 1

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.1f)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNumber in 1..daysInActiveMonth) {
                                    val dateKey = getFormattedDateString(dayNumber)
                                    val isSelected = dateKey == selectedDateStr
                                    val isToday = dateKey == systemTodayStr

                                    // Count active events on this specific date to show dot badges
                                    val dayEventsCount = allEvents.filter { it.dateString == dateKey }.size

                                    // Layout style of the active cell
                                    val dayBgColor = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else -> Color.Transparent
                                    }
                                    val dayTextColor = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(dayBgColor)
                                            .clickable { viewModel.selectCalendarDate(dateKey) }
                                            .border(
                                                width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                                color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = dayTextColor
                                        )

                                        if (dayEventsCount > 0) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                // Small calendar dots depending on events count
                                                val maxDots = minOf(3, dayEventsCount)
                                                for (i in 0 until maxDots) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .background(
                                                                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                                shape = CircleShape
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Divider and Selected Date Day Header ---
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val readableDate = formatReadableDate(selectedDateStr)
                    Text(
                        text = "Agenda: $readableDate",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val evSize = selectedDayEvents.size
                    Text(
                        text = "$evSize ${if (evSize == 1) "Event" else "Events"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- Selected Day Agenda Events View List ---
            if (selectedDayEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.EventNote,
                            contentDescription = "No events today",
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Nothing scheduled for this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tap the floating action button to schedule deep reminder events!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(selectedDayEvents) { event ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Left status bar colored tag
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(38.dp)
                                            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = event.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (event.description.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = event.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                IconButton(onClick = { viewModel.deleteCalendarEvent(event.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Event")
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Dialog View for Adding New Schedule Events ---
        if (showAddEventDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddEventDialog = false
                    eventTitleInput = ""
                    eventDescInput = ""
                },
                title = { Text("Schedule Event", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Date: ${formatReadableDate(selectedDateStr)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextField(
                            value = eventTitleInput,
                            onValueChange = { eventTitleInput = it },
                            label = { Text("Event Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = eventDescInput,
                            onValueChange = { eventDescInput = it },
                            label = { Text("Notes / Descriptions") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val title = eventTitleInput.trim()
                            if (title.isNotEmpty()) {
                                viewModel.addCalendarEvent(title, eventDescInput)
                            }
                            showAddEventDialog = false
                            eventTitleInput = ""
                            eventDescInput = ""
                        },
                        enabled = eventTitleInput.trim().isNotEmpty()
                    ) {
                        Text("Add Event")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddEventDialog = false
                            eventTitleInput = ""
                            eventDescInput = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * Format string e.g. "YYYY-MM-DD" to human readable title e.g. "June 05, 2026"
 */
fun formatReadableDate(dateString: String): String {
    if (dateString.isEmpty()) return ""
    return try {
        val parts = dateString.split("-")
        val year = parts[0]
        val monthCode = parts[1].toInt() - 1
        val day = parts[2].toInt()

        val monthsNames = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        "${monthsNames[monthCode]} $day, $year"
    } catch (e: Exception) {
        dateString
    }
}
