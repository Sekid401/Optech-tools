package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

enum class ToolCategory {
    ALL, MATH, TIME, TEXT, LIFESTYLE
}

data class OfflineTool(
    val id: String,
    val title: String,
    val textDescription: String,
    val category: ToolCategory,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val bgColor: Color,
    val fgColor: Color
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OfflineToolsSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf(ToolCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var activeToolId by remember { mutableStateOf<String?>(null) }

    // 30 offline tools declaration
    val toolsList = remember {
        listOf(
            // MATCH & NUMBERS
            OfflineTool("stopwatch", "Stopwatch Timer", "High precision stopwatch with split laps.", ToolCategory.TIME, Icons.Default.Timer, Color(0xFFEFF6FF), Color(0xFF1D4ED8)),
            OfflineTool("pomodoro", "Pomodoro Clock", "25/5 cycle focus timer for tasks.", ToolCategory.TIME, Icons.Default.HourglassEmpty, Color(0xFFFEF2F2), Color(0xFFDC2626)),
            OfflineTool("unit_conv", "Unit Converter", "Instant conversion for distance, weights, temps.", ToolCategory.MATH, Icons.Default.SwapHoriz, Color(0xFFECFDF5), Color(0xFF047857)),
            OfflineTool("base_conv", "Number Base", "Translate Dec, Hex, Oct, Bin bases.", ToolCategory.MATH, Icons.Default.Hexagon, Color(0xFFF5F3FF), Color(0xFF7C3AED)),
            OfflineTool("tip_calc", "Tip Calculator", "Split bills and calculate tips easily.", ToolCategory.MATH, Icons.Default.ReceiptLong, Color(0xFFFFFBEB), Color(0xFFD97706)),
            OfflineTool("dice_flip", "Dice & Coin", "Roll multiple dice or flip a coin.", ToolCategory.MATH, Icons.Default.Casino, Color(0xFFF0FDF4), Color(0xFF16A34A)),
            OfflineTool("roman_conv", "Roman Numerals", "Integers to ancient Roman letters.", ToolCategory.MATH, Icons.Default.WorkspacePremium, Color(0xFFFFF7ED), Color(0xFFEA580C)),
            OfflineTool("gpa_calc", "GPA Calculator", "Academics terms average calculator.", ToolCategory.MATH, Icons.Default.School, Color(0xFFEFF6FF), Color(0xFF2563EB)),
            OfflineTool("fibo_gen", "Fibonacci Gen", "First n terms of mathematical sequence.", ToolCategory.MATH, Icons.Default.Timeline, Color(0xFFECEFF1), Color(0xFF37474F)),
            
            // TIME & PRODUCTIVITY
            OfflineTool("days_diff", "Date Difference", "Find days count between calendar days.", ToolCategory.TIME, Icons.Default.CalendarMonth, Color(0xFFFEF3C7), Color(0xFFD97706)),
            OfflineTool("metronome", "BPM Metronome", "A visual metric rhythmic ticker slider.", ToolCategory.TIME, Icons.Default.SlowMotionVideo, Color(0xFFFAF5FF), Color(0xFF9333EA)),
            OfflineTool("exact_age", "Life Calculator", "Exact total years, hours, seconds lived.", ToolCategory.TIME, Icons.Default.Cake, Color(0xFFFFF1F2), Color(0xFFE11D48)),
            OfflineTool("world_time", "TZ Reference", "Major capital city time offsets.", ToolCategory.TIME, Icons.Default.Language, Color(0xFFEFF6FF), Color(0xFF0284C7)),
            OfflineTool("countdown", "Countdown Board", "Local milliseconds event count timers.", ToolCategory.TIME, Icons.Default.NotificationImportant, Color(0xFFFFF7ED), Color(0xFFC2410C)),
            OfflineTool("binary_clock", "Binary Clock", "Hours and minutes as active bit grids.", ToolCategory.TIME, Icons.Default.Watch, Color(0xFFF1F5F9), Color(0xFF334155)),

            // TEXT & DEV TOOLS
            OfflineTool("pwd_gen", "Password Maker", "Generate random high entropy keys.", ToolCategory.TEXT, Icons.Default.Password, Color(0xFFF3F4F6), Color(0xFF1F2937)),
            OfflineTool("case_conv", "Case Rotator", "Uppercase, lowercase, camelCase, snake_case.", ToolCategory.TEXT, Icons.Default.TextFields, Color(0xFFEDF2F7), Color(0xFF2D3748)),
            OfflineTool("b64_conv", "Base64 Encoder", "Encode or decode raw base64 texts.", ToolCategory.TEXT, Icons.Default.Code, Color(0xFFF0FDF4), Color(0xFF15803D)),
            OfflineTool("word_count", "Word Counters", "Sum text items, lines, characters.", ToolCategory.TEXT, Icons.Default.Numbers, Color(0xFFECFDF5), Color(0xFF097969)),
            OfflineTool("url_conv", "URL Converter", "Web safe address encoder & decoder.", ToolCategory.TEXT, Icons.Default.Link, Color(0xFFFFFBEB), Color(0xFFB45309)),
            OfflineTool("pwd_strength", "Key Strength", "Inspect key text entropy metrics.", ToolCategory.TEXT, Icons.Default.LockReset, Color(0xFFFEF2F2), Color(0xFF991B1B)),
            OfflineTool("morse_code", "Morse Code", "Translates typing to dot-dash symbols.", ToolCategory.TEXT, Icons.Default.Keyboard, Color(0xFFEEF2F6), Color(0xFF475569)),
            OfflineTool("hash_gen", "Hash Generator", "Local MD5 and SHA-256 calculators.", ToolCategory.TEXT, Icons.Default.Fingerprint, Color(0xFFEDF2F7), Color(0xFF1A365D)),

            // LIFESTYLE & FOCUS
            OfflineTool("bmi_calc", "Fitness BMI", "Find Body Mass classification metrics.", ToolCategory.LIFESTYLE, Icons.Default.Favorite, Color(0xFFFFF1F2), Color(0xFFBE123C)),
            OfflineTool("color_picker", "Hex Palette", "Random gorgeous graphic designer hexes.", ToolCategory.LIFESTYLE, Icons.Default.Palette, Color(0xFFFDF2F8), Color(0xFFDB2777)),
            OfflineTool("tic_tac", "TicTacToe Bot", "Tactical grid matches versus offline bot.", ToolCategory.LIFESTYLE, Icons.Default.SportsEsports, Color(0xFFECFDF5), Color(0xFF047857)),
            OfflineTool("vis_ruler", "Screen Ruler", "10cm/4inch graphic calibrated lines.", ToolCategory.LIFESTYLE, Icons.Default.Straighten, Color(0xFFFFFBEB), Color(0xFF78350F)),
            OfflineTool("sketchpad", "Sketch Board", "Touch draw panel brush canvas.", ToolCategory.LIFESTYLE, Icons.Default.Brush, Color(0xFFEFF6FF), Color(0xFF1D4ED8)),
            OfflineTool("decision_wheel", "Chooser Wheel", "Pick interactive random entry results.", ToolCategory.LIFESTYLE, Icons.Default.RotateRight, Color(0xFFF0FDF4), Color(0xFF0F766E)),
            OfflineTool("clinometer", "Angle Clinometer", "Emulated bubble clinometer gauges.", ToolCategory.LIFESTYLE, Icons.Default.Explore, Color(0xFFF5F3FF), Color(0xFF6D28D9))
        )
    }

    val filteredTools = remember(selectedCategory, searchQuery) {
        toolsList.filter {
            (selectedCategory == ToolCategory.ALL || it.category == selectedCategory) &&
                    (it.title.contains(searchQuery, true) || it.textDescription.contains(searchQuery, true))
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("Modular Utilities Deck", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("30 fully local multi-tool suite applications", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_util_index")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to primary menu")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // High fidelity search and filtering tabs
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search 30 modular tools...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("tool_search_input"),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                }
            )

            // Dynamic Categories Selector Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(ToolCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    val displayLabel = remember(category) {
                        when (category) {
                            ToolCategory.ALL -> "All Tools (30)"
                            ToolCategory.MATH -> "Math & Numbers"
                            ToolCategory.TIME -> "Time & Focus"
                            ToolCategory.TEXT -> "Text & Coding"
                            ToolCategory.LIFESTYLE -> "Lifestyle / Custom"
                        }
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(displayLabel, fontSize = 12.sp) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main items layout grid
            if (filteredTools.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No matching tools found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredTools, key = { it.id }) { tool ->
                        Card(
                            onClick = { activeToolId = tool.id },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("launch_micro_${tool.id}"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = tool.bgColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(tool.fgColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = tool.icon,
                                            contentDescription = null,
                                            tint = tool.fgColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ArrowOutward,
                                        contentDescription = null,
                                        tint = tool.fgColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = tool.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = tool.fgColor,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tool.textDescription,
                                        fontSize = 10.sp,
                                        color = tool.fgColor.copy(alpha = 0.8f),
                                        maxLines = 2,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Active Utility Render Overlay dialog mapping 30 tools
    activeToolId?.let { toolId ->
        AlertDialog(
            onDismissRequest = { activeToolId = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp)),
            content = {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Title header bar
                        val currentTool = remember(toolId) { toolsList.first { it.id == toolId } }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .background(currentTool.bgColor, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(currentTool.fgColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(currentTool.icon, contentDescription = null, tint = currentTool.fgColor)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(currentTool.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = currentTool.fgColor)
                                    Text(currentTool.textDescription, fontSize = 10.sp, color = currentTool.fgColor.copy(alpha = 0.8f))
                                }
                            }

                            IconButton(onClick = { activeToolId = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close toolkit", tint = currentTool.fgColor)
                            }
                        }

                        // Specific active content container
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            when (toolId) {
                                // Time category
                                "stopwatch" -> StopwatchToolContent()
                                "pomodoro" -> PomodoroToolContent()
                                "days_diff" -> DaysDiffToolContent()
                                "metronome" -> MetronomeToolContent()
                                "exact_age" -> ExactAgeToolContent()
                                "countdown" -> CountdownToolContent()
                                "world_time" -> WorldTimeToolContent()
                                "binary_clock" -> BinaryClockToolContent()

                                // Math category
                                "unit_conv" -> UnitConvToolContent()
                                "base_conv" -> BaseConvToolContent()
                                "tip_calc" -> TipCalcToolContent()
                                "dice_flip" -> DiceFlipToolContent()
                                "roman_conv" -> RomanConvToolContent()
                                "gpa_calc" -> GpaCalcToolContent()
                                "fibo_gen" -> FiboGenToolContent()

                                // Text category
                                "pwd_gen" -> PasswordGenToolContent()
                                "case_conv" -> CaseConvToolContent()
                                "b64_conv" -> Base64ToolContent()
                                "word_count" -> WordCountToolContent()
                                "url_conv" -> UrlConvToolContent()
                                "pwd_strength" -> PwdStrengthToolContent()
                                "morse_code" -> MorseCodeToolContent()
                                "hash_gen" -> HashGenToolContent()

                                // Lifestyle category
                                "bmi_calc" -> BmiCalcToolContent()
                                "color_picker" -> ColorPickerToolContent()
                                "tic_tac" -> TicTacToeToolContent()
                                "vis_ruler" -> VisRulerToolContent()
                                "sketchpad" -> PaintSketchpadToolContent()
                                "decision_wheel" -> DecisionWheelToolContent()
                                "clinometer" -> ClinometerToolContent()
                            }
                        }
                    }
                }
            }
        )
    }
}



/* ==========================================================================
   TIME CATEGORY MINI MODULES
   ========================================================================== */

@Composable
fun StopwatchToolContent() {
    var elapsedMillis by remember { mutableStateOf(0L) }
    var running by remember { mutableStateOf(false) }
    val laps = remember { mutableStateListOf<String>() }

    LaunchedEffect(running) {
        if (running) {
            val start = System.currentTimeMillis() - elapsedMillis
            while (running) {
                elapsedMillis = System.currentTimeMillis() - start
                delay(10)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val min = (elapsedMillis / 60000) % 60
        val sec = (elapsedMillis / 1000) % 60
        val ms = (elapsedMillis % 1000) / 10
        val formattedTime = String.format("%02d:%02d.%02d", min, sec, ms)

        Text(
            text = formattedTime,
            fontFamily = FontFamily.Monospace,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { running = !running },
                colors = ButtonDefaults.buttonColors(containerColor = if (running) Color(0xFFEF4444) else Color(0xFF10B981))
            ) {
                Text(if (running) "Pause" else "Start")
            }
            if (running) {
                Button(onClick = { laps.add(formattedTime) }) {
                    Text("Lap")
                }
            }
            Button(onClick = {
                running = false
                elapsedMillis = 0L
                laps.clear()
            }) {
                Text("Reset")
            }
        }

        if (laps.isNotEmpty()) {
            Text("Laps:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(laps.reversed()) { lap ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Lap ${laps.indexOf(lap) + 1}", fontSize = 13.sp)
                            Text(lap, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PomodoroToolContent() {
    var totalSeconds by remember { mutableStateOf(1500) } // 25 Min default
    var isRunning by remember { mutableStateOf(false) }
    var isBreak by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (totalSeconds > 0 && isRunning) {
                delay(1000)
                totalSeconds -= 1
            }
            if (totalSeconds == 0) {
                isBreak = !isBreak
                totalSeconds = if (isBreak) 300 else 1500
                isRunning = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isBreak) "Break Time! ☕" else "Work Focus Session ⚡",
            fontWeight = FontWeight.Bold,
            color = if (isBreak) Color(0xFF10B981) else Color(0xFFDC2626)
        )

        val minutes = totalSeconds / 60
        val remainingSecs = totalSeconds % 60
        Text(
            text = String.format("%02d:%02d", minutes, remainingSecs),
            fontFamily = FontFamily.Monospace,
            fontSize = 54.sp,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { isRunning = !isRunning }) {
                Text(if (isRunning) "Pause" else "Start Cycle")
            }
            Button(onClick = {
                isRunning = false
                isBreak = false
                totalSeconds = 1500
            }) {
                Text("Reset")
            }
        }
    }
}

@Composable
fun DaysDiffToolContent() {
    var dateStartStr by remember { mutableStateOf("2026-05-31") }
    var dateEndStr by remember { mutableStateOf("2026-12-25") }
    var resultText by remember { mutableStateOf("Calculates Days difference...") }

    fun compute() {
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date1 = formatter.parse(dateStartStr)
            val date2 = formatter.parse(dateEndStr)
            if (date1 != null && date2 != null) {
                val diff = Math.abs(date2.time - date1.time)
                val days = java.util.concurrent.TimeUnit.DAYS.convert(diff, java.util.concurrent.TimeUnit.MILLISECONDS)
                resultText = "Duration counts: $days days"
            }
        } catch (e: Exception) {
            resultText = "Invalid Date Input! Use YYYY-MM-DD format."
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = dateStartStr,
            onValueChange = { dateStartStr = it },
            label = { Text("Start Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = dateEndStr,
            onValueChange = { dateEndStr = it },
            label = { Text("End Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { compute() }, modifier = Modifier.fillMaxWidth()) {
            Text("Compute Days Duration")
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = resultText,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MetronomeToolContent() {
    var bpm by remember { mutableStateOf(100f) }
    var activeTick by remember { mutableStateOf(false) }
    var isStarted by remember { mutableStateOf(false) }

    LaunchedEffect(isStarted, bpm) {
        if (isStarted) {
            val interval = (60000 / bpm).toLong()
            while (isStarted) {
                activeTick = true
                delay(80)
                activeTick = false
                delay(interval - 80)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(if (activeTick) Color(0xFF9333EA) else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "TICK",
                fontWeight = FontWeight.Bold,
                color = if (activeTick) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text("${bpm.toInt()} BPM", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Slider(
            value = bpm,
            onValueChange = { bpm = it },
            valueRange = 40f..240f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Button(onClick = { isStarted = !isStarted }) {
            Text(if (isStarted) "STOP RHYTHM" else "START METRONOME")
        }
    }
}

@Composable
fun ExactAgeToolContent() {
    var birthYear by remember { mutableStateOf("2000") }
    var resultRows by remember { mutableStateOf<List<String>>(emptyList()) }

    fun computeAge() {
        val y = birthYear.toIntOrNull() ?: return
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        if (y > currentYear) return

        val yearsLived = currentYear - y
        val monthsLived = yearsLived * 12
        val daysLived = yearsLived * 365
        val hoursLived = daysLived * 24L
        val secondsLived = hoursLived * 3600L

        resultRows = listOf(
            "$yearsLived full calendar Years lived",
            "$monthsLived approximate Months lived",
            "$daysLived approx Days lived",
            "$hoursLived Hours survived",
            "$secondsLived Seconds of precious life!"
        )
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = birthYear,
            onValueChange = { birthYear = it },
            label = { Text("Birth Year (Four digits)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { computeAge() }, modifier = Modifier.fillMaxWidth()) {
            Text("Calculate Time Traveled")
        }

        resultRows.forEach { row ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(row, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun CountdownToolContent() {
    var label by remember { mutableStateOf("Next New Year") }
    var targetTime by remember { mutableStateOf("2027-01-01 00:00:00") }
    var countdownText by remember { mutableStateOf("00d : 00h : 00m : 00s") }

    LaunchedEffect(targetTime) {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        while (true) {
            try {
                val date = format.parse(targetTime)
                if (date != null) {
                    val diff = date.time - System.currentTimeMillis()
                    if (diff > 0) {
                        val d = diff / (1000 * 60 * 60 * 24)
                        val h = (diff / (1000 * 60 * 60)) % 24
                        val m = (diff / (1000 * 60)) % 60
                        val s = (diff / 1000) % 60
                        countdownText = String.format("%02dd : %02dh : %02dm : %02ds", d, h, m, s)
                    } else {
                        countdownText = "Target reached!"
                    }
                }
            } catch (e: Exception) {
                countdownText = "Format: YYYY-MM-DD HH:mm:ss"
            }
            delay(1000)
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Event Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = targetTime, onValueChange = { targetTime = it }, label = { Text("Target Date/Time (YYYY-MM-DD HH:mm:ss)") }, modifier = Modifier.fillMaxWidth())

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(modifier = Modifier.height(10.dp))
                Text(countdownText, fontFamily = FontFamily.Monospace, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        }
    }
}

@Composable
fun WorldTimeToolContent() {
    val capitals = remember {
        listOf(
            "London (GMT)" to 0,
            "Paris (CET)" to 1,
            "Tokyo (JST)" to 9,
            "New York (EST)" to -5,
            "Los Angeles (PST)" to -8,
            "Sydney (AEDT)" to 11,
            "Mumbai (IST)" to 5
        )
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        capitals.forEach { (city, offset) ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.add(Calendar.HOUR_OF_DAY, offset)
            val timeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(calendar.time)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(city, fontWeight = FontWeight.Bold)
                    Text(
                        timeString,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BinaryClockToolContent() {
    val calendar = Calendar.getInstance()
    var h by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var m by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    var s by remember { mutableStateOf(calendar.get(Calendar.SECOND)) }

    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            h = cal.get(Calendar.HOUR_OF_DAY)
            m = cal.get(Calendar.MINUTE)
            s = cal.get(Calendar.SECOND)
            delay(1000)
        }
    }

    @Composable
    fun BinaryRow(label: String, valNum: Int) {
        val binaryStr = String.format("%6s", Integer.toBinaryString(valNum)).replace(' ', '0')
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
            binaryStr.forEach { bit ->
                val isActive = bit == '1'
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isActive) Color(0xFF10B981) else Color(0xFF6B7280))
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                )
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text("Active Binary Rows (32 16 8 4 2 1):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        BinaryRow("Hr", h)
        BinaryRow("Min", m)
        BinaryRow("Sec", s)
    }
}

/* ==========================================================================
   MATH CATEGORY MINI MODULES
   ========================================================================== */

@Composable
fun UnitConvToolContent() {
    var sourceVal by remember { mutableStateOf("1.0") }
    var currentUnitType by remember { mutableStateOf("Length") } // Lenght, Weight, Temp
    var outputText by remember { mutableStateOf("Output:") }

    fun calculate() {
        val v = sourceVal.toDoubleOrNull() ?: return
        when (currentUnitType) {
            "Length" -> {
                // meters to feet/inches
                val feet = v * 3.28084
                val inches = v * 39.3701
                outputText = "Feet: ${String.format("%.2f", feet)}\nInches: ${String.format("%.2f", inches)}"
            }
            "Weight" -> {
                // kg to lbs
                val lbs = v * 2.20462
                outputText = "Pounds (lbs): ${String.format("%.2f", lbs)}"
            }
            "Temp" -> {
                // Cel to Fah
                val fah = (v * 9 / 5) + 32
                outputText = "Fahrenheit: ${String.format("%.1f", fah)} °F"
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Length", "Weight", "Temp").forEach { unit ->
                val checked = currentUnitType == unit
                Button(
                    onClick = { currentUnitType = unit },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(unit, fontSize = 11.sp, color = if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        OutlinedTextField(
            value = sourceVal,
            onValueChange = { sourceVal = it },
            label = { Text("Source Value (${if (currentUnitType == "Length") "Meters" else if (currentUnitType == "Weight") "KG" else "Celcius"})") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = { calculate() }, modifier = Modifier.fillMaxWidth()) {
            Text("Convert Unit")
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Text(outputText, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun BaseConvToolContent() {
    var inputDecimal by remember { mutableStateOf("255") }
    var binVal by remember { mutableStateOf("0") }
    var hexVal by remember { mutableStateOf("0") }
    var octVal by remember { mutableStateOf("0") }

    fun translate() {
        val v = inputDecimal.toIntOrNull()
        if (v != null) {
            binVal = Integer.toBinaryString(v)
            hexVal = Integer.toHexString(v).uppercase()
            octVal = Integer.toOctalString(v)
        } else {
            binVal = "None"
            hexVal = "None"
            octVal = "None"
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = inputDecimal,
            onValueChange = { inputDecimal = it },
            label = { Text("Base Decimal Integer") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { translate() }, modifier = Modifier.fillMaxWidth()) {
            Text("Translate Base Values")
        }

        listOf("Binary" to binVal, "Hexadecimal" to hexVal, "Octal" to octVal).forEach { (base, valStr) ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(base, fontWeight = FontWeight.Bold)
                    Text(valStr, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun TipCalcToolContent() {
    var billTotal by remember { mutableStateOf("80.0") }
    var people by remember { mutableStateOf("4") }
    var tipPercent by remember { mutableStateOf("15") }
    var resultText by remember { mutableStateOf("") }

    fun compute() {
        val bill = billTotal.toDoubleOrNull() ?: return
        val count = people.toDoubleOrNull() ?: return
        val tip = tipPercent.toDoubleOrNull() ?: return

        if (count > 0) {
            val totalTip = bill * (tip / 100.0)
            val fullAmt = bill + totalTip
            val share = fullAmt / count
            resultText = String.format("Full Tip: $%.2f\nCombined Bill: $%.2f\nEach Person owes: $%.2f", totalTip, fullAmt, share)
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = billTotal, onValueChange = { billTotal = it }, label = { Text("Bill Total ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = people, onValueChange = { people = it }, label = { Text("Number of People") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = tipPercent, onValueChange = { tipPercent = it }, label = { Text("Tip %") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

        Button(onClick = { compute() }, modifier = Modifier.fillMaxWidth()) {
            Text("Calculate tip splits")
        }

        if (resultText.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text(resultText, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun DiceFlipToolContent() {
    var rolledVal by remember { mutableStateOf("Result Idle") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(rolledVal, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                scope.launch {
                    rolledVal = "Rolling..."
                    delay(300)
                    rolledVal = "Rolled: ${(1..6).random()}"
                }
            }) {
                Text("Roll Dice D6")
            }
            Button(onClick = {
                scope.launch {
                    rolledVal = "Flipping..."
                    delay(300)
                    rolledVal = if (Math.random() > 0.5) "HEADS" else "TAILS"
                }
            }) {
                Text("Flip Coin")
            }
        }
    }
}

@Composable
fun RomanConvToolContent() {
    var romanInput by remember { mutableStateOf("1994") }
    var romanOutput by remember { mutableStateOf("MCMXCIV") }

    fun build() {
        val num = romanInput.toIntOrNull() ?: return
        if (num <= 0 || num > 3999) {
            romanOutput = "Range: 1 to 3999"
            return
        }
        val thousands = listOf("", "M", "MM", "MMM")
        val hundreds = listOf("", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM")
        val tens = listOf("", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC")
        val units = listOf("", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX")

        romanOutput = thousands[num / 1000] +
                hundreds[(num % 1000) / 100] +
                tens[(num % 100) / 10] +
                units[num % 10]
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = romanInput,
            onValueChange = { romanInput = it },
            label = { Text("Standard Integer (1 - 3999)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { build() }, modifier = Modifier.fillMaxWidth()) {
            Text("Translate to Roman Numeral")
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(romanOutput, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GpaCalcToolContent() {
    var subject1 by remember { mutableStateOf("A") }
    var subject2 by remember { mutableStateOf("B") }
    var credit1 by remember { mutableStateOf("4") }
    var credit2 by remember { mutableStateOf("3") }
    var gpaResult by remember { mutableStateOf("GPA Idle") }

    fun compute() {
        fun convertGrade(g: String) = when (g.uppercase().trim()) {
            "A" -> 4.0
            "B" -> 3.0
            "C" -> 2.0
            "D" -> 1.0
            else -> 0.0
        }
        val cr1 = credit1.toDoubleOrNull() ?: 0.0
        val cr2 = credit2.toDoubleOrNull() ?: 0.0
        val sumCredits = cr1 + cr2
        if (sumCredits > 0) {
            val fullPts = (convertGrade(subject1) * cr1) + (convertGrade(subject2) * cr2)
            gpaResult = String.format("Current Term GPA: %.2f", fullPts / sumCredits)
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = subject1, onValueChange = { subject1 = it }, label = { Text("Grade 1 (A-F)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = credit1, onValueChange = { credit1 = it }, label = { Text("Credits 1") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = subject2, onValueChange = { subject2 = it }, label = { Text("Grade 2 (A-F)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = credit2, onValueChange = { credit2 = it }, label = { Text("Credits 2") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
        }

        Button(onClick = { compute() }, modifier = Modifier.fillMaxWidth()) {
            Text("Calculate average GPA")
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Text(gpaResult, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun FiboGenToolContent() {
    var termsCount by remember { mutableStateOf("10") }
    var resultingSeq by remember { mutableStateOf("") }

    fun gen() {
        val t = termsCount.toIntOrNull() ?: return
        if (t <= 0 || t > 50) {
            resultingSeq = "Terms limited to ranges 1 - 50"
            return
        }
        val list = mutableListOf<Long>()
        var a = 0L
        var b = 1L
        for (i in 0 until t) {
            list.add(a)
            val next = a + b
            a = b
            b = next
        }
        resultingSeq = list.joinToString(", ")
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = termsCount,
            onValueChange = { termsCount = it },
            label = { Text("Generate Fibonacci Terms") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { gen() }, modifier = Modifier.fillMaxWidth()) {
            Text("Generate Sequence terms")
        }

        if (resultingSeq.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(resultingSeq, modifier = Modifier.padding(16.dp), fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            }
        }
    }
}

/* ==========================================================================
   TEXT CATEGORY MINI MODULES
   ========================================================================== */

@Composable
fun PasswordGenToolContent() {
    var isCheckedSymbols by remember { mutableStateOf(true) }
    var isCheckedDigits by remember { mutableStateOf(true) }
    var targetLength by remember { mutableStateOf(16f) }
    var generatedPwd by remember { mutableStateOf("") }

    fun evaluate() {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;':,./<>"
        var pool = chars
        if (isCheckedDigits) pool += numbers
        if (isCheckedSymbols) pool += symbols

        val r = Random()
        val builder = java.lang.StringBuilder()
        for (i in 0 until targetLength.toInt()) {
            val offset = r.nextInt(pool.length)
            builder.append(pool[offset])
        }
        generatedPwd = builder.toString()
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Password Size: ${targetLength.toInt()} characters", fontWeight = FontWeight.Bold)
        Slider(value = targetLength, onValueChange = { targetLength = it }, valueRange = 8f..32f)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isCheckedSymbols, onCheckedChange = { isCheckedSymbols = it })
            Text("Include special character Symbols")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isCheckedDigits, onCheckedChange = { isCheckedDigits = it })
            Text("Include Numbers digits")
        }

        Button(onClick = { evaluate() }, modifier = Modifier.fillMaxWidth()) {
            Text("Assemble Random Keys")
        }

        if (generatedPwd.isNotEmpty()) {
            OutlinedTextField(
                value = generatedPwd,
                onValueChange = {},
                readOnly = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun CaseConvToolContent() {
    var textInput by remember { mutableStateOf("hello optech developer") }
    var textOutput by remember { mutableStateOf("HELLO OPTECH DEVELOPER") }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Standard text payload input") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = { textOutput = textInput.uppercase() }, modifier = Modifier.weight(1f)) { Text("UPPER") }
            Button(onClick = { textOutput = textInput.lowercase() }, modifier = Modifier.weight(1f)) { Text("lower") }
            Button(onClick = {
                val words = textInput.split(" ")
                val camelCase = words.mapIndexed { index, w ->
                    val clean = w.lowercase(Locale.getDefault())
                    if (index == 0) clean else clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                }.joinToString("")
                textOutput = camelCase
            }, modifier = Modifier.weight(1f)) { Text("camel") }
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Text(textOutput, modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun Base64ToolContent() {
    var rawText by remember { mutableStateOf("Sample raw string payload") }
    var baseResult by remember { mutableStateOf("") }

    fun encode() {
        try {
            baseResult = android.util.Base64.encodeToString(rawText.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            baseResult = "Encoding err"
        }
    }

    fun decode() {
        try {
            baseResult = String(android.util.Base64.decode(rawText, android.util.Base64.NO_WRAP), Charsets.UTF_8)
        } catch (e: Exception) {
            baseResult = "Invalid Base64 formats"
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = rawText, onValueChange = { rawText = it }, label = { Text("Target payload string") }, modifier = Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { encode() }, modifier = Modifier.weight(1f)) { Text("Encode Base64") }
            Button(onClick = { decode() }, modifier = Modifier.weight(1f)) { Text("Decode Base64") }
        }

        if (baseResult.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(baseResult, modifier = Modifier.padding(14.dp), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun WordCountToolContent() {
    var textValue by remember { mutableStateOf("Analyze word lengths inside this prompt container.") }
    var counts by remember { mutableStateOf("Words: 7 | Characters: 52 | Lines: 1") }

    fun compute() {
        val chars = textValue.length
        val words = if (textValue.trim().isEmpty()) 0 else textValue.split("\\s+".toRegex()).size
        val lines = textValue.split("\n").size
        counts = "Words count: $words | Characters combined: $chars | Total Lines: $lines"
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = textValue, onValueChange = { textValue = it }, label = { Text("Analyze text limits") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { compute() }, modifier = Modifier.fillMaxWidth()) { Text("Analyze string") }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Text(counts, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun UrlConvToolContent() {
    var textInput by remember { mutableStateOf("https://google.com/search?q=optech tools") }
    var textOutput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = textInput, onValueChange = { textInput = it }, label = { Text("URL Address Input") }, modifier = Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                textOutput = java.net.URLEncoder.encode(textInput, "UTF-8")
            }, modifier = Modifier.weight(1f)) { Text("Url Encode") }
            Button(onClick = {
                textOutput = java.net.URLDecoder.decode(textInput, "UTF-8")
            }, modifier = Modifier.weight(1f)) { Text("Url Decode") }
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Text(textOutput, modifier = Modifier.padding(14.dp), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
    }
}

@Composable
fun PwdStrengthToolContent() {
    var pwdInput by remember { mutableStateOf("") }
    var strengthText by remember { mutableStateOf("Status: Enter keys") }

    fun evaluate() {
        val s = pwdInput
        if (s.isEmpty()) {
            strengthText = "Length empty."
            return
        }
        var score = 0
        if (s.length >= 8) score++
        if (s.length >= 12) score++
        if (s.any { it.isDigit() }) score++
        if (s.any { !it.isLetterOrDigit() }) score++
        if (s.any { it.isUpperCase() }) score++

        strengthText = when {
            score >= 5 -> "Extremely Strong Entropy ✅ (5/5)"
            score >= 3 -> "Moderate Strength (3/5)"
            else -> "Weak Security Warning ❌"
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = pwdInput, onValueChange = { pwdInput = it }, label = { Text("Analyze Password Security") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { evaluate() }, modifier = Modifier.fillMaxWidth()) { Text("Analyze key entropy") }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Text(strengthText, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun MorseCodeToolContent() {
    var textValue by remember { mutableStateOf("TELEGRAPH") }
    var resultingMorse by remember { mutableStateOf("- . .-.. . --. .-. .- .--. ....") }

    fun translate() {
        val alphabet = mapOf(
            'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".", 'F' to "..-.", 'G' to "--.",
            'H' to "....", 'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.",
            'O' to "---", 'P' to ".--.", 'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-", 'U' to "..-",
            'V' to "...-", 'W' to ".--", 'X' to "-..-", 'Y' to "-.--", 'Z' to "--..", ' ' to " "
        )
        resultingMorse = textValue.uppercase().map { alphabet[it] ?: "" }.joinToString(" ")
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = textValue, onValueChange = { textValue = it }, label = { Text("Natural Alphabet Input") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { translate() }, modifier = Modifier.fillMaxWidth()) { Text("Telegraph Morse Output") }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Text(resultingMorse, modifier = Modifier.padding(18.dp), fontFamily = FontFamily.Monospace, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HashGenToolContent() {
    var rawText by remember { mutableStateOf("Optech") }
    var selectedHashType by remember { mutableStateOf("SHA-256") }
    var resultHash by remember { mutableStateOf("") }

    fun calculate() {
        try {
            val digest = MessageDigest.getInstance(selectedHashType)
            val bytes = digest.digest(rawText.toByteArray(Charsets.UTF_8))
            resultHash = bytes.joinToString("") { String.format("%02x", it) }
        } catch (e: Exception) {
            resultHash = "Err calculations"
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = rawText, onValueChange = { rawText = it }, label = { Text("Source Raw Payload String") }, modifier = Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("SHA-256", "MD5").forEach { type ->
                val active = selectedHashType == type
                Button(
                    onClick = { selectedHashType = type },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(type, color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Button(onClick = { calculate() }, modifier = Modifier.fillMaxWidth()) { Text("Compute Cryptographic digest") }

        if (resultHash.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(resultHash, modifier = Modifier.padding(14.dp), fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* ==========================================================================
   LIFESTYLE CATEGORY MINI MODULES
   ========================================================================== */

@Composable
fun BmiCalcToolContent() {
    var heightCm by remember { mutableStateOf("175") }
    var weightKg by remember { mutableStateOf("70") }
    var resultingMetrics by remember { mutableStateOf("") }

    fun compute() {
        val h = heightCm.toDoubleOrNull() ?: return
        val w = weightKg.toDoubleOrNull() ?: return
        if (h > 0) {
            val m = h / 100.0
            val bmi = w / (m * m)
            val desc = when {
                bmi < 18.5 -> "Underweight status (Eat healthy balanced meals)"
                bmi < 24.9 -> "Healthy normal Weight status ✅ Keep it up!"
                bmi < 29.9 -> "Overweight category ranges"
                else -> "Obese category indicators"
            }
            resultingMetrics = String.format("Body Mass BMI: %.1f\nIndicates: %s", bmi, desc)
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = heightCm, onValueChange = { heightCm = it }, label = { Text("Height (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = weightKg, onValueChange = { weightKg = it }, label = { Text("Weight (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

        Button(onClick = { compute() }, modifier = Modifier.fillMaxWidth()) { Text("Calculate Fitness Ratio") }

        if (resultingMetrics.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text(resultingMetrics, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun ColorPickerToolContent() {
    var colorPalette by remember { mutableStateOf(listOf("#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6")) }

    fun refreshColors() {
        val chars = "0123456789ABCDEF"
        val r = Random()
        colorPalette = List(5) {
            val s = java.lang.StringBuilder("#")
            for (i in 0 until 6) {
                s.append(chars[r.nextInt(16)])
            }
            s.toString()
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Material UI hex designers colors palette:", fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colorPalette.forEach { hex ->
                val realColor = try {
                    Color(android.graphics.Color.parseColor(hex))
                } catch (e: Exception) {
                    Color.Gray
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(realColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        hex,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(2.dp)
                    )
                }
            }
        }

        Button(onClick = { refreshColors() }, modifier = Modifier.fillMaxWidth()) {
            Text("Assemble Random Harmonious Colors")
        }
    }
}

@Composable
fun TicTacToeToolContent() {
    var tilesState by remember { mutableStateOf(List(9) { "" }) }
    var statusText by remember { mutableStateOf("Player X (Me) move") }
    var matchDone by remember { mutableStateOf(false) }

    fun checkMatches(board: List<String>): String {
        val rules = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // horizontal
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // vertical
            listOf(0, 4, 8), listOf(2, 4, 6) // diagonal
        )
        for (row in rules) {
            if (board[row[0]].isNotEmpty() && board[row[0]] == board[row[1]] && board[row[0]] == board[row[2]]) {
                return board[row[0]]
            }
        }
        if (board.none { it.isEmpty() }) return "D"
        return ""
    }

    fun makeBotMove(currentBoard: MutableList<String>) {
        val empties = currentBoard.mapIndexed { idx, s -> if (s.isEmpty()) idx else -1 }.filter { it != -1 }
        if (empties.isNotEmpty()) {
            val pick = empties.random()
            currentBoard[pick] = "O"
        }
    }

    fun handlePress(idx: Int) {
        if (tilesState[idx].isNotEmpty() || matchDone) return
        val currentBoard = tilesState.toMutableList()
        currentBoard[idx] = "X"

        var outcome = checkMatches(currentBoard)
        if (outcome.isNotEmpty()) {
            tilesState = currentBoard
            statusText = if (outcome == "D") "Draw match!" else "Player $outcome Wins!"
            matchDone = true
            return
        }

        makeBotMove(currentBoard)
        outcome = checkMatches(currentBoard)
        tilesState = currentBoard

        if (outcome.isNotEmpty()) {
            statusText = if (outcome == "D") "Draw match!" else "Player $outcome Wins!"
            matchDone = true
        } else {
            statusText = "Player X turn"
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(statusText, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        Box(
            modifier = Modifier
                .size(240.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (r in 0 until 3) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (c in 0 until 3) {
                            val idx = (r * 3) + c
                            val value = tilesState[idx]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { handlePress(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    value,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (value == "X") Color(0xFF1D4ED8) else Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(onClick = {
            tilesState = List(9) { "" }
            statusText = "Player X (Me) move"
            matchDone = false
        }) {
            Text("Restart Chess Match")
        }
    }
}

@Composable
fun VisRulerToolContent() {
    var isCmUnit by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { isCmUnit = true }) { Text("Centimeters Ruler Calibration") }
            Button(onClick = { isCmUnit = false }) { Text("Inches Ruler Calibration") }
        }

        Text("Lay object directly on your display glass screen:", fontSize = 11.sp, fontWeight = FontWeight.Medium)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val calFactor = if (isCmUnit) 37.79f else 96.0f // Standard pixels approx adjustments
                val count = if (isCmUnit) 10 else 4
                for (i in 0..count) {
                    val x = i * calFactor
                    drawLine(
                        color = Color.Black,
                        start = Offset(x, 0f),
                        end = Offset(x, if (i % 5 == 0) 60f else 30f),
                        strokeWidth = 3f
                    )
                }
            }
        }
    }
}

@Composable
fun PaintSketchpadToolContent() {
    val points = remember { mutableStateListOf<Offset>() }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        points.add(change.position)
                    }
                }
        ) {
            for (i in 0 until points.size - 1) {
                if (points[i] != Offset.Zero && points[i+1] != Offset.Zero) {
                    drawLine(
                        color = Color.Black,
                        start = points[i],
                        end = points[i+1],
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { points.clear() }) { Text("Clear Canvas") }
        }
    }
}

@Composable
fun DecisionWheelToolContent() {
    var rawWheelInput by remember { mutableStateOf("Pizza, Burgers, Salad, Sushi") }
    var selectedWinner by remember { mutableStateOf("Ready to Spin") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = rawWheelInput, onValueChange = { rawWheelInput = it }, label = { Text("List choices (comma separated)") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                scope.launch {
                    val options = rawWheelInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    if (options.isNotEmpty()) {
                        selectedWinner = "Spinning selection..."
                        delay(500)
                        selectedWinner = "Resulting Choice: " + options.random()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Spin Decision Wheel")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(selectedWinner, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
fun ClinometerToolContent() {
    var pitchSlider by remember { mutableStateOf(0f) }
    var rollSlider by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Emulated Clinometer Degrees Angular Pitch/Roll", fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val offsetMultiplierX = rollSlider * 60f
            val offsetMultiplierY = pitchSlider * 60f

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .offset(x = offsetMultiplierX.dp, y = offsetMultiplierY.dp)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Pitch Tilt: $pitchSlider")
                Slider(value = pitchSlider, onValueChange = { pitchSlider = it }, valueRange = -1f..1f)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Roll Tilt: $rollSlider")
                Slider(value = rollSlider, onValueChange = { rollSlider = it }, valueRange = -1f..1f)
            }
        }
    }
}
