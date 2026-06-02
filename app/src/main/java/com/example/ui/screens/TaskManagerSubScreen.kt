package com.example.ui.screens

import android.app.ActivityManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.PlaylistAddCheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TodoTask
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagerSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var selectedMainTab by remember { mutableStateOf(0) } // 0 = Todo checklist, 1 = Windows Task Manager

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Management Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("task_mgr_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Launchpad")
                    }
                }
            )
        },
        bottomBar = {
            TabRow(selectedTabIndex = selectedMainTab) {
                Tab(
                    selected = selectedMainTab == 0,
                    onClick = { selectedMainTab = 0 },
                    icon = { Icon(Icons.Outlined.PlaylistAddCheck, contentDescription = null) },
                    text = { Text("Personal Todo List") }
                )
                Tab(
                    selected = selectedMainTab == 1,
                    onClick = { selectedMainTab = 1 },
                    icon = { Icon(Icons.Outlined.DesktopWindows, contentDescription = null) },
                    text = { Text("Windows Task Manager") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (selectedMainTab == 0) {
                TodoChecklistSection(viewModel)
            } else {
                WindowsTaskManagerSection()
            }
        }
    }
}

// ==========================================
// 1. TODO CHECKLIST COMPONENT
// ==========================================
@Composable
fun TodoChecklistSection(viewModel: MainViewModel) {
    val tasks by viewModel.allTasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Add task form states
    var taskTitle by remember { mutableStateOf("") }
    var taskDesc by remember { mutableStateOf("") }
    var taskPriority by remember { mutableStateOf("Medium") } // High, Medium, Low

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("My Scheduled Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${tasks.count { !it.isCompleted }} pending, ${tasks.count { it.isCompleted }} finished", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Button(onClick = { showAddDialog = true }, modifier = Modifier.testTag("todo_add_btn")) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Task")
            }
        }

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("All caught up!", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Create a new task checklist to get started.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Incomplete group
                val incomplete = tasks.filter { !it.isCompleted }
                if (incomplete.isNotEmpty()) {
                    item {
                        Text("Active Tasks", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    items(incomplete, key = { it.id }) { task ->
                        TodoTaskRow(
                            task = task,
                            onToggle = { viewModel.updateTaskStatus(task.id, true) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }

                // Completed group
                val completed = tasks.filter { it.isCompleted }
                if (completed.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Completed", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                    items(completed, key = { it.id }) { task ->
                        TodoTaskRow(
                            task = task,
                            onToggle = { viewModel.updateTaskStatus(task.id, false) },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create Checklist Item", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("todo_title_input")
                    )
                    OutlinedTextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().testTag("todo_desc_input")
                    )

                    Text("Task Urgency/Priority", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("High", "Medium", "Low").forEach { selection ->
                            val active = taskPriority == selection
                            FilterChip(
                                selected = active,
                                onClick = { taskPriority = selection },
                                label = { Text(selection) },
                                modifier = Modifier.testTag("priority_tag_$selection")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            viewModel.insertTask(taskTitle, taskDesc, taskPriority)
                            taskTitle = ""
                            taskDesc = ""
                            taskPriority = "Medium"
                            showAddDialog = false
                        }
                    },
                    enabled = taskTitle.isNotBlank(),
                    modifier = Modifier.testTag("todo_dialog_save_btn")
                ) {
                    Text("Add Checklist")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TodoTaskRow(
    task: TodoTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("todo_check_${task.id}")
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    ),
                    color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (task.isCompleted) Color.Gray.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Priority label badge
            SuggestionChip(
                onClick = {},
                label = { Text(task.priority, fontSize = 10.sp, color = Color.White) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = when (task.priority) {
                        "High" -> Color(0xFFEF4444)
                        "Medium" -> Color(0xFFEAB308)
                        else -> Color(0xFF3B82F6)
                    }
                ),
                modifier = Modifier.height(26.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


// ==========================================
// 2. WINDOWS TASK MANAGER COMPONENT (AUTHENTIC REPLICA!)
// ==========================================
@Composable
fun WindowsTaskManagerSection() {
    var subTabSelection by remember { mutableStateOf(0) } // 0 = Performance, 1 = Processes, 2 = Services
    val context = LocalContext.current

    // Live performance counts
    var cpuLiveValue by remember { mutableStateOf(12f) }
    var memoryUsedMb by remember { mutableStateOf(120L) }
    var memoryTotalMb by remember { mutableStateOf(512L) }
    val cpuHistory = remember { mutableStateListOf<Float>() }
    val ramHistory = remember { mutableStateListOf<Float>() }

    // Load running mock processes states
    val processes = remember {
        mutableStateListOf(
            TaskProcess("System Idle Process", 0, "Running", 82f, 1),
            TaskProcess("System Kernel Core", 4, "System", 3.2f, 12),
            TaskProcess("optech-host-daemon", 72, "Running", 4.1f, 85),
            TaskProcess("omni-secure-shell", 102, "Running", 0.5f, 22),
            TaskProcess("chromium-webserver", 280, "Running", 6.8f, 142),
            TaskProcess("studymusic-equalizer", 312, "Suspended", 0.0f, 48),
            TaskProcess("android-logcat-stream", 442, "Running", 2.1f, 32),
            TaskProcess("sqlite-database-engine", 590, "Running", 1.4f, 19)
        )
    }

    val services = remember {
        mutableStateListOf(
            TaskService("omni-adb-daemon", "Android Debug Wireless Host", "Running"),
            TaskService("media-server-host", "Study Music Audio Service", "Stopped"),
            TaskService("calendar-notifier", "Calendar Events Watchdog", "Running"),
            TaskService("file-monitor-service", "Files Observer Daemon", "Running"),
            TaskService("dtmf-dial-service", "Keypad Synthesizer Router", "Running"),
            TaskService("cache-cleaner-daemon", "Browser Temp File Purger", "Stopped")
        )
    }

    var selectedProcessForEnd by remember { mutableStateOf<TaskProcess?>(null) }
    var showEndAlertConfirm by remember { mutableStateOf(false) }

    // Query periodic resource utilisation stats
    LaunchedEffect(Unit) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val runtime = Runtime.getRuntime()

        while (true) {
            // Memory Usage calculations
            val totalMemoryBytes = runtime.totalMemory()
            val freeMemoryBytes = runtime.freeMemory()
            val denominator = 1024 * 1024
            memoryUsedMb = (totalMemoryBytes - freeMemoryBytes) / denominator
            memoryTotalMb = runtime.maxMemory() / denominator

            // Set dynamic raw cpu
            cpuLiveValue = (5..45).random().toFloat()
            
            // Randomly jitter process stats slightly to look hyper realistic
            processes.forEachIndexed { idx, p ->
                if (p.name == "System Idle Process") {
                    p.cpuUsage = 95f - cpuLiveValue
                } else if (p.status == "Running") {
                    p.cpuUsage = (0..80).random() / 10f
                }
            }

            // Record data inside History queue limits
            cpuHistory.add(cpuLiveValue)
            if (cpuHistory.size > 50) cpuHistory.removeAt(0)

            val ramPercent = (memoryUsedMb.toFloat() / memoryTotalMb.toFloat()) * 100f
            ramHistory.add(ramPercent)
            if (ramHistory.size > 50) ramHistory.removeAt(0)

            delay(1000)
        }
    }

    // Windows classic dialog retro styles
    val shellBackground = Color(0xFFF1F3F9)
    val shellMenuBg = Color(0xFFE1E5F0)
    val taskmgrGridLineColor = Color(0xFF79D98F).copy(alpha = 0.25f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3F3))
    ) {
        // --- Retro Windows File/Options Menu Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(shellBackground)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("File", "Options", "View", "Tools", "Help").forEach { menu ->
                Text(
                    text = menu,
                    style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 11.sp, fontWeight = FontWeight.Normal),
                    color = Color.Black,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }

        // --- Tabs Selection bar mimics Windows 11 Task Manager sidebar/topbar ---
        TabRow(
            selectedTabIndex = subTabSelection,
            containerColor = Color(0xFFE5E7EB),
            contentColor = Color.Black
        ) {
            Tab(
                selected = subTabSelection == 0,
                onClick = { subTabSelection = 0 },
                text = { Text("Performance", style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, fontWeight = FontWeight.Bold)) }
            )
            Tab(
                selected = subTabSelection == 1,
                onClick = { subTabSelection = 1 },
                text = { Text("Processes", style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, fontWeight = FontWeight.Bold)) }
            )
            Tab(
                selected = subTabSelection == 2,
                onClick = { subTabSelection = 2 },
                text = { Text("Services", style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, fontWeight = FontWeight.Bold)) }
            )
        }

        // --- ACTIVE WINDOW BODY DEPENDS ON SELECTION ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(8.dp)
        ) {
            when (subTabSelection) {
                0 -> {
                    // --- TAB: PERFORMANCE CHARTS ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Graph 1: CPU Utilisation
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("CPU Utilisation Graph", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 13.sp)
                                Text(
                                    text = String.format("%.1f %% Live", cpuLiveValue),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D),
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Color(0xFF0C100D))
                            ) {
                                // Draw horizontal & vertical tech gridlines
                                val horizontalGrids = 4
                                val verticalGrids = 8
                                for (i in 1 until horizontalGrids) {
                                    val y = size.height * (i.toFloat() / horizontalGrids)
                                    drawLine(taskmgrGridLineColor, Offset(0f, y), Offset(size.width, y), 1f)
                                }
                                for (i in 1 until verticalGrids) {
                                    val x = size.width * (i.toFloat() / verticalGrids)
                                    drawLine(taskmgrGridLineColor, Offset(x, 0f), Offset(x, size.height), 1f)
                                }

                                // Drawing CPU percentage paths
                                if (cpuHistory.size > 1) {
                                    val path = Path()
                                    val stepX = size.width / 50f
                                    cpuHistory.forEachIndexed { idx, value ->
                                        val x = idx * stepX
                                        val percentFactor = 1f - (value / 100f) // 100% at the top of canvas
                                        val y = size.height * percentFactor
                                        if (idx == 0) {
                                            path.moveTo(x, y)
                                        } else {
                                            path.lineTo(x, y)
                                        }
                                    }
                                    drawPath(path, Color(0xFF63D37E), style = Stroke(width = 3f))
                                }
                            }
                        }

                        // Graph 2: JVM Allocation Memory
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Memory Allocations (RAM)", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 13.sp)
                                Text(
                                    text = "$memoryUsedMb / $memoryTotalMb MB",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB),
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Color(0xFF0C100D))
                            ) {
                                val horizontalGrids = 4
                                val verticalGrids = 8
                                for (i in 1 until horizontalGrids) {
                                    val y = size.height * (i.toFloat() / horizontalGrids)
                                    drawLine(taskmgrGridLineColor.copy(alpha = 0.15f), Offset(0f, y), Offset(size.width, y), 1f)
                                }
                                for (i in 1 until verticalGrids) {
                                    val x = size.width * (i.toFloat() / verticalGrids)
                                    drawLine(taskmgrGridLineColor.copy(alpha = 0.15f), Offset(x, 0f), Offset(x, size.height), 1f)
                                }

                                if (ramHistory.size > 1) {
                                    val path = Path()
                                    val stepX = size.width / 50f
                                    ramHistory.forEachIndexed { idx, value ->
                                        val x = idx * stepX
                                        val percentFactor = 1f - (value / 100f)
                                        val y = size.height * percentFactor
                                        if (idx == 0) {
                                            path.moveTo(x, y)
                                        } else {
                                            path.lineTo(x, y)
                                        }
                                    }
                                    drawPath(path, Color(0xFF5F9FFF), style = Stroke(width = 3f))
                                }
                            }
                        }

                        // Core info spec stats
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Technical Hardware Configuration", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Virtual Machine Cores", fontSize = 11.sp, color = Color.Black)
                                        Text("${Runtime.getRuntime().availableProcessors()} Units", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)
                                    }
                                    Column {
                                        Text("Host System Status", fontSize = 11.sp, color = Color.Black)
                                        Text("Online / Stable", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF15803D))
                                    }
                                    Column {
                                        Text("VM Up-Time Clock", fontSize = 11.sp, color = Color.Black)
                                        val upClock = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()) }
                                        Text(upClock, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // --- TAB: PROCESSES LIST ---
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header category row labels
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE5E7EB))
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Process Name", Modifier.weight(2f), style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.Black)
                            Text("PID", Modifier.weight(0.5f), style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.Black)
                            Text("Status", Modifier.weight(0.8f), style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.Black)
                            Text("CPU %", Modifier.weight(0.6f), style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.Black)
                            Text("Memory", Modifier.weight(0.8f), style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.Black)
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color.White)
                        ) {
                            items(processes) { process ->
                                val isSelected = selectedProcessForEnd == process
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) Color(0xFFCBE5FF) else Color.Transparent)
                                        .clickable { selectedProcessForEnd = process }
                                        .padding(vertical = 8.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(process.name, Modifier.weight(2f), style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp), maxLines = 1, color = Color.Black)
                                    Text("${process.pid}", Modifier.weight(0.5f), style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp), color = Color.Black)
                                    Text(process.status, Modifier.weight(0.8f), style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp), color = Color.Black)
                                    
                                    // Custom color based on cpu usage (amber heat map)
                                    val cpuColor = if (process.cpuUsage > 10f) Color(0xFFFFECCC) else Color.Transparent
                                    Box(
                                        modifier = Modifier
                                            .weight(0.6f)
                                            .background(cpuColor)
                                            .padding(2.dp)
                                    ) {
                                        Text(
                                            text = String.format("%.1f %%", process.cpuUsage),
                                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                            color = Color.Black
                                        )
                                    }

                                    // Mem Allocations Jitter MBs
                                    Text("${process.memoryUsageMb} MB", Modifier.weight(0.8f), style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp), color = Color.Black)
                                }
                                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                            }
                        }

                        // Bottom command actions bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { showEndAlertConfirm = true },
                                enabled = selectedProcessForEnd != null && selectedProcessForEnd?.name != "System Idle Process" && selectedProcessForEnd?.name != "System Kernel Core",
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.testTag("end_task_retro_btn")
                            ) {
                                Text("End Task Processes", style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
                2 -> {
                    // --- TAB: SERVICES OVERVIEW ---
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE5E7EB))
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Service Name", Modifier.weight(1.5f), style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.Black)
                            Text("Description", Modifier.weight(2f), style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.Black)
                            Text("Status", Modifier.weight(0.8f), style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.Black)
                            Text("Action Toggle", Modifier.weight(1f), style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.Black)
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color.White)
                        ) {
                            items(services) { service ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(service.name, Modifier.weight(1.5f), style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold), color = Color.Black)
                                    Text(service.description, Modifier.weight(2f), style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 11.sp), color = Color.Black)
                                    Text(
                                        text = service.status,
                                        modifier = Modifier.weight(0.8f),
                                        style = TextStyle(
                                            fontFamily = FontFamily.SansSerif,
                                            fontSize = 11.sp,
                                            color = if (service.status == "Running") Color(0xFF16A34A) else Color(0xFFEF4444),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    
                                    // Switch Toggle start/stop service
                                    Box(modifier = Modifier.weight(1f)) {
                                        TextButton(
                                            onClick = {
                                                service.status = if (service.status == "Running") "Stopped" else "Running"
                                            },
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(
                                                text = if (service.status == "Running") "Stop Daemon" else "Start Daemon",
                                                style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Process Force Termination confirmations retro dialog
    if (showEndAlertConfirm && selectedProcessForEnd != null) {
        val dyingProcess = selectedProcessForEnd!!
        AlertDialog(
            onDismissRequest = { showEndAlertConfirm = false },
            title = { Text("Task Manager Diagnostics Alert", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to terminate ${dyingProcess.name} (PID: ${dyingProcess.pid})?\n\nIf you terminate this application process, unsaved data associated with this thread will be wiped instantly.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        processes.remove(dyingProcess)
                        selectedProcessForEnd = null
                        showEndAlertConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Kill Process Tree")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndAlertConfirm = false }) {
                    Text("Cancel Call")
                }
            }
        )
    }
}

data class TaskProcess(
    val name: String,
    val pid: Int,
    var status: String,
    var cpuUsage: Float,
    val memoryUsageMb: Int
)

class TaskService(
    val name: String,
    val description: String,
    initialStatus: String
) {
    var status by mutableStateOf(initialStatus)
}
