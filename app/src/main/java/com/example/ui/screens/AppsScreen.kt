package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalendarEvent
import com.example.data.Note
import com.example.data.WorkspaceFile
import com.example.ui.MainViewModel
import com.example.ui.RealNewsItem
import com.example.ui.RealWeather
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class SubApp {
    LAUNCHPAD, HOME_SCREEN, PROFILE_DASHBOARD, CALCULATOR, NOTES_PAD, MEDIA_VIEWER, AUDIO_LISTENER, ANDROID_APPS,
    CONTACTS, DIALER, MESSAGES, TASK_MANAGER, ADB_WIRELESS, AI_ASSISTANT, PHONE_SERVER, OFFLINE_TOOLS,
    VOICE_RECORDER, CAMERA_APP, MAIL, GAME_CENTER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(viewModel: MainViewModel) {
    var activeSubApp by remember { mutableStateOf(SubApp.HOME_SCREEN) }
    var currentDialerPreset by remember { mutableStateOf("") }
    var currentMessagesPreset by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Slide/fadeIn transition based on activeSubApp selection
    AnimatedContent(
        targetState = activeSubApp,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
        },
        label = "SubAppTransition"
    ) { subApp ->
        when (subApp) {
            SubApp.HOME_SCREEN -> HomeScreenSubScreen(
                viewModel = viewModel,
                onSelectSubApp = { activeSubApp = it },
                onNavigateToProfile = { activeSubApp = SubApp.PROFILE_DASHBOARD }
            )
            SubApp.PROFILE_DASHBOARD -> ProfileDashboardSubScreen(
                viewModel = viewModel,
                onBack = { activeSubApp = SubApp.HOME_SCREEN }
            )
            SubApp.LAUNCHPAD -> LaunchpadScreen(
                viewModel = viewModel,
                onSelectSubApp = { activeSubApp = it },
                onBackToHome = { activeSubApp = SubApp.HOME_SCREEN }
            )
            SubApp.CALCULATOR -> CalculatorScreen(
                onBack = { activeSubApp = SubApp.LAUNCHPAD }
            )
            SubApp.NOTES_PAD -> NotesPadSubScreen(
                viewModel = viewModel,
                onBack = { activeSubApp = SubApp.LAUNCHPAD }
            )
            SubApp.MEDIA_VIEWER -> MediaViewerSubScreen(
                viewModel = viewModel,
                onBack = { activeSubApp = SubApp.LAUNCHPAD }
            )
            SubApp.AUDIO_LISTENER -> AudioListenerSubScreen(
                viewModel = viewModel,
                onBack = { activeSubApp = SubApp.LAUNCHPAD }
            )
            SubApp.ANDROID_APPS -> AndroidAppsSubScreen(
                viewModel = viewModel,
                onBack = { activeSubApp = SubApp.LAUNCHPAD }
            )
            SubApp.CONTACTS -> ContactsSubScreen(
                viewModel = viewModel,
                onBack = { activeSubApp = SubApp.LAUNCHPAD },
                onCallContact = { number ->
                    currentDialerPreset = number
                    activeSubApp = SubApp.DIALER
                },
                onMessageContact = { number ->
                    currentMessagesPreset = number
                    activeSubApp = SubApp.MESSAGES
                }
            )
            SubApp.DIALER -> DialerSubScreen(
                viewModel = viewModel,
                presetPhoneNumber = currentDialerPreset,
                onBack = {
                    currentDialerPreset = ""
                    activeSubApp = SubApp.LAUNCHPAD
                }
            )
            SubApp.MESSAGES -> MessagingSubScreen(
                viewModel = viewModel,
                presetPhoneNumber = currentMessagesPreset,
                onBack = {
                    currentMessagesPreset = ""
                    activeSubApp = SubApp.LAUNCHPAD
                },
                onCallContact = { number ->
                    currentDialerPreset = number
                    activeSubApp = SubApp.DIALER
                }
            )
            SubApp.TASK_MANAGER -> TaskManagerSubScreen(
                viewModel = viewModel,
                onBack = { activeSubApp = SubApp.LAUNCHPAD }
            )
            SubApp.ADB_WIRELESS -> AdbWirelessSubScreen(
                viewModel = viewModel,
                onBack = { activeSubApp = SubApp.LAUNCHPAD }
            )
            SubApp.AI_ASSISTANT -> AiAssistantSubScreen(
                viewModel = viewModel,
                onBack = { activeSubApp = SubApp.LAUNCHPAD }
            )
            SubApp.PHONE_SERVER -> PhoneServerSubScreen(
                viewModel = viewModel,
                onBack = { activeSubApp = SubApp.LAUNCHPAD }
            )
            SubApp.OFFLINE_TOOLS -> OfflineToolsSubScreen(
                viewModel = viewModel,
                onBack = { activeSubApp = SubApp.LAUNCHPAD }
            )
            SubApp.VOICE_RECORDER -> VoiceRecorderSubScreen(
                viewModel = viewModel,
                onBack = { activeSubApp = SubApp.LAUNCHPAD }
            )
            SubApp.CAMERA_APP -> CameraSubScreen(
                viewModel = viewModel,
                onBack = { activeSubApp = SubApp.LAUNCHPAD }
            )
            SubApp.MAIL -> MailSubScreen(
                viewModel = viewModel,
                onBack = { activeSubApp = SubApp.LAUNCHPAD }
            )
            SubApp.GAME_CENTER -> GameCenterSubScreen(
                onBack = { activeSubApp = SubApp.LAUNCHPAD }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchpadScreen(
    viewModel: MainViewModel,
    onSelectSubApp: (SubApp) -> Unit,
    onBackToHome: () -> Unit
) {
    val hiddenApps by viewModel.hiddenApps.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar Area
        CenterAlignedTopAppBar(
            navigationIcon = {
                IconButton(onClick = onBackToHome, modifier = Modifier.testTag("launchpad_home_button")) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Back to home screen"
                    )
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "O",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "Optech Suite Apps",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!hiddenApps.contains(SubApp.NOTES_PAD.name)) {
                item {
                    AppSelectorCard(
                        title = "Interactive Notes",
                        description = "Take rich text notes and associate with files/calendar events.",
                        icon = Icons.Outlined.NoteAlt,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        onClick = { onSelectSubApp(SubApp.NOTES_PAD) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.CALCULATOR.name)) {
                item {
                    AppSelectorCard(
                        title = "Calculator & Terminal",
                        description = "Perform standard/scientific calculus with mathematical console terminal shell.",
                        icon = Icons.Outlined.Calculate,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        onClick = { onSelectSubApp(SubApp.CALCULATOR) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.MEDIA_VIEWER.name)) {
                item {
                    AppSelectorCard(
                        title = "Media Viewer",
                        description = "Browse, zoom, and inspect workspace photos & simulated video reels.",
                        icon = Icons.Outlined.PhotoLibrary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = { onSelectSubApp(SubApp.MEDIA_VIEWER) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.AUDIO_LISTENER.name)) {
                item {
                    AppSelectorCard(
                        title = "Audio Listener",
                        description = "Play local files or study music with live wave-equalizer visualization.",
                        icon = Icons.Outlined.MusicNote,
                        containerColor = Color(0xFFF3E0D8),
                        contentColor = Color(0xFF191C19),
                        onClick = { onSelectSubApp(SubApp.AUDIO_LISTENER) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.ANDROID_APPS.name)) {
                item {
                    AppSelectorCard(
                        title = "App drawer Android",
                        description = "Browse system packages and launch third-party apps directly.",
                        icon = Icons.Outlined.Apps,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        onClick = { onSelectSubApp(SubApp.ANDROID_APPS) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.DIALER.name)) {
                item {
                    AppSelectorCard(
                        title = "Phone Dialer",
                        description = "Interactive DTMF keypad, recents call log, and call simulators.",
                        icon = Icons.Outlined.Phone,
                        containerColor = Color(0xFFD1FAE5),
                        contentColor = Color(0xFF065F46),
                        onClick = { onSelectSubApp(SubApp.DIALER) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.MESSAGES.name)) {
                item {
                    AppSelectorCard(
                        title = "Secure Messaging",
                        description = "Real local database chat bubbles and simulated contact auto-responders.",
                        icon = Icons.Outlined.Chat,
                        containerColor = Color(0xFFDBEAFE),
                        contentColor = Color(0xFF1E40AF),
                        onClick = { onSelectSubApp(SubApp.MESSAGES) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.CONTACTS.name)) {
                item {
                    AppSelectorCard(
                        title = "Contacts Directory",
                        description = "Manage, filter, and categorise team members with quick dials.",
                        icon = Icons.Outlined.ContactPhone,
                        containerColor = Color(0xFFFAE8FF),
                        contentColor = Color(0xFF6B21A8),
                        onClick = { onSelectSubApp(SubApp.CONTACTS) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.TASK_MANAGER.name)) {
                item {
                    AppSelectorCard(
                        title = "Task Management Center",
                        description = "Dual personal todo checklist and real-time Windows Task Manager specs.",
                        icon = Icons.Outlined.PlaylistAddCheck,
                        containerColor = Color(0xFFFEF9C3),
                        contentColor = Color(0xFF854D0E),
                        onClick = { onSelectSubApp(SubApp.TASK_MANAGER) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.ADB_WIRELESS.name)) {
                item {
                    AppSelectorCard(
                        title = "ADB Wireless debugging",
                        description = "Interactive Wi-Fi pair terminal guides and simulated logcat feeds.",
                        icon = Icons.Outlined.BugReport,
                        containerColor = Color(0xFFFEE2E2),
                        contentColor = Color(0xFF991B1B),
                        onClick = { onSelectSubApp(SubApp.ADB_WIRELESS) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.AI_ASSISTANT.name)) {
                item {
                    AppSelectorCard(
                        title = "AI Assistant",
                        description = "Secure conversational Gemini assistant, prompt templates, and local overrides.",
                        icon = Icons.Outlined.Assistant,
                        containerColor = Color(0xFFE0F2FE),
                        contentColor = Color(0xFF0369A1),
                        onClick = { onSelectSubApp(SubApp.AI_ASSISTANT) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.PHONE_SERVER.name)) {
                item {
                    AppSelectorCard(
                        title = "Phone Web Server",
                        description = "Spawn a local API socket host on Wi-Fi, generate API keys, and log incoming requests.",
                        icon = Icons.Outlined.Dns,
                        containerColor = Color(0xFFECFDF5),
                        contentColor = Color(0xFF047857),
                        onClick = { onSelectSubApp(SubApp.PHONE_SERVER) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.OFFLINE_TOOLS.name)) {
                item {
                    AppSelectorCard(
                        title = "Modular Toolkit",
                        description = "Access 30+ interactive local utilities (calculators, timers, converters, widgets) entirely offline.",
                        icon = Icons.Outlined.Construction,
                        containerColor = Color(0xFFFAF5FF),
                        contentColor = Color(0xFF6B21A8),
                        onClick = { onSelectSubApp(SubApp.OFFLINE_TOOLS) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.VOICE_RECORDER.name)) {
                item {
                    AppSelectorCard(
                        title = "Voice Recorder",
                        description = "High fidelity audio recorder to log security indices, audio memos, and live speech feeds.",
                        icon = Icons.Outlined.Mic,
                        containerColor = Color(0xFFFFE4E6),
                        contentColor = Color(0xFF881337),
                        onClick = { onSelectSubApp(SubApp.VOICE_RECORDER) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.CAMERA_APP.name)) {
                item {
                    AppSelectorCard(
                        title = "Eyewitness Digi-Camera",
                        description = "Full terminal vision preview, high resolution capture logs, and flash configurations.",
                        icon = Icons.Outlined.PhotoCamera,
                        containerColor = Color(0xFFE0E7FF),
                        contentColor = Color(0xFF312E81),
                        onClick = { onSelectSubApp(SubApp.CAMERA_APP) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.MAIL.name)) {
                item {
                    AppSelectorCard(
                        title = "SMTP & POP3 Mail Hub",
                        description = "Setup connections to Gmail, Outlook, POP3/IMAP, read inbox databases, and dispatch attachments.",
                        icon = Icons.Outlined.Email,
                        containerColor = Color(0xFFE0F2FE),
                        contentColor = Color(0xFF0369A1),
                        onClick = { onSelectSubApp(SubApp.MAIL) }
                    )
                }
            }
            if (!hiddenApps.contains(SubApp.GAME_CENTER.name)) {
                item {
                    AppSelectorCard(
                        title = "Game Center Hub",
                        description = "Simulate and play over 200 retro arcade, puzzle, classic, and strategy games offline.",
                        icon = Icons.Outlined.SportsEsports,
                        containerColor = Color(0xFFFCE7F3),
                        contentColor = Color(0xFF9D174D),
                        onClick = { onSelectSubApp(SubApp.GAME_CENTER) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppSelectorCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .testTag("app_launch_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = contentColor.copy(alpha = 0.8f),
                    lineHeight = 15.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/* ==========================================================================
   ADVANCED CALCULATOR SUB-SCREEN (WITH MATH TERMINAL)
   ========================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    var formulaText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var consoleInput by remember { mutableStateOf("") }
    val consoleLines = remember { mutableStateListOf<String>() }

    if (consoleLines.isEmpty()) {
        consoleLines.add("============================================")
        consoleLines.add(" OPTECH MATHEMATICAL CONSOLE TERMINAL v1.0.0")
        consoleLines.add("============================================")
        consoleLines.add("Type algebraic expressions to evaluate safely.")
        consoleLines.add("Predefined: pi, e, sqrt(x), sin(x), cos(x), tan(x), ln(x), log(x).")
        consoleLines.add("Examples: (5 * 2) / sqrt(16) | sin(30 * pi / 180)")
        consoleLines.add("")
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val evaluateExpression: (String) -> Unit = { rawExpr ->
        if (rawExpr.trim().isNotEmpty()) {
            try {
                val expr = rawExpr
                    .replace("pi", "3.141592653589793")
                    .replace("e", "2.718281828459045")
                    .lowercase()
                val parser = MathParser(expr)
                val resultValue = parser.parse()
                // Format result beautifully
                val formattedResult = if (resultValue % 1.0 == 0.0) {
                    resultValue.toLong().toString()
                } else {
                    String.format("%.6f", resultValue).trimEnd('0').trimEnd('.')
                }
                resultText = formattedResult
                consoleLines.add("$ > $rawExpr")
                consoleLines.add("= $formattedResult")
                consoleLines.add("")
            } catch (e: Exception) {
                resultText = "Error"
                consoleLines.add("$ > $rawExpr")
                consoleLines.add("ERR: " + (e.message ?: "Invalid syntax"))
                consoleLines.add("")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App top control bar
        TopAppBar(
            title = { Text("Calculator & Console", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Return to suite menu")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // Main content row or column dividing keys from the mini terminal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            // Displays panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formulaText.ifEmpty { "0" },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 18.sp,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = resultText.ifEmpty { "" },
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // High Fidelity Key Button Deck
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val rowHeightModifier = Modifier.weight(1f)

                Row(modifier = rowHeightModifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalcKey("C", Color(0xFFFF5F56), MaterialTheme.colorScheme.surface, Modifier.weight(1f)) {
                        formulaText = ""
                        resultText = ""
                    }
                    CalcKey("(", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f)) { formulaText += "(" }
                    CalcKey(")", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f)) { formulaText += ")" }
                    CalcKey("/", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f)) { formulaText += "/" }
                }

                Row(modifier = rowHeightModifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalcKey("7", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "7" }
                    CalcKey("8", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "8" }
                    CalcKey("9", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "9" }
                    CalcKey("*", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f)) { formulaText += "*" }
                }

                Row(modifier = rowHeightModifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalcKey("4", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "4" }
                    CalcKey("5", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "5" }
                    CalcKey("6", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "6" }
                    CalcKey("-", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f)) { formulaText += "-" }
                }

                Row(modifier = rowHeightModifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalcKey("1", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "1" }
                    CalcKey("2", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "2" }
                    CalcKey("3", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "3" }
                    CalcKey("+", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f)) { formulaText += "+" }
                }

                Row(modifier = rowHeightModifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalcKey("0", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "0" }
                    CalcKey(".", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "." }
                    CalcKey("⌫", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f)) {
                        if (formulaText.isNotEmpty()) formulaText = formulaText.dropLast(1)
                    }
                    CalcKey("=", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary, Modifier.weight(1f)) {
                        evaluateExpression(formulaText)
                    }
                }

                Row(modifier = rowHeightModifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalcKey("sqrt", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "sqrt(" }
                    CalcKey("sin", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "sin(" }
                    CalcKey("cos", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "cos(" }
                    CalcKey("^", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f)) { formulaText += "^" }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mathematical Terminal Console
            Text(
                text = "Mathematical Shell CLI Console",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(24.dp))
                    .background(Color(0xFF1A1C18))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        reverseLayout = true
                    ) {
                        items(consoleLines.reversed()) { line ->
                            val textColor = when {
                                line.startsWith("$") -> Color(0xFF79D98F)
                                line.startsWith("=") -> Color(0xFFC2EFAD)
                                line.startsWith("ERR") -> Color(0xFFFF5F56)
                                else -> Color.White.copy(alpha = 0.8f)
                            }
                            Text(
                                text = line,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = textColor,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$ ",
                            color = Color(0xFF79D98F),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        BasicTextField(
                            value = consoleInput,
                            onValueChange = { consoleInput = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onAny = {
                                    evaluateExpression(consoleInput)
                                    consoleInput = ""
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calc_terminal_cli_input")
                        )
                        if (consoleInput.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    evaluateExpression(consoleInput)
                                    consoleInput = ""
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Evaluate console input",
                                    tint = Color(0xFF79D98F),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CalcKey(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxHeight()
            .testTag("calc_key_$text")
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}


/* ==========================================================================
   MATHEMATICAL PARSER CLASS (Token-Free Recursive Descent)
   ========================================================================== */

class MathParser(private val str: String) {
    private var pos = -1
    private var ch = 0

    private fun nextChar() {
        pos++
        ch = if (pos < str.length) str[pos].code else -1
    }

    private fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun parse(): Double {
        nextChar()
        val x = parseExpression()
        if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
        return x
    }

    private fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            if (eat('+'.code)) x += parseTerm()
            else if (eat('-'.code)) x -= parseTerm()
            else return x
        }
    }

    private fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            if (eat('*'.code)) x *= parseFactor()
            else if (eat('/'.code)) {
                val divisor = parseFactor()
                if (divisor == 0.0) throw ArithmeticException("Division by zero")
                x /= divisor
            } else return x
        }
    }

    private fun parseFactor(): Double {
        if (eat('+'.code)) return parseFactor()
        if (eat('-'.code)) return -parseFactor()

        var x: Double
        val startPos = pos
        if (eat('('.code)) {
            x = parseExpression()
            eat(')'.code)
        } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
            while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
            x = str.substring(startPos, pos).toDouble()
        } else if (ch >= 'a'.code && ch <= 'z'.code) {
            while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
            val func = str.substring(startPos, pos)
            if (func == "pi") {
                x = kotlin.math.PI
            } else if (func == "e") {
                x = kotlin.math.E
            } else {
                x = parseFactor()
                x = when (func) {
                    "sqrt" -> kotlin.math.sqrt(x)
                    "sin" -> kotlin.math.sin(x)
                    "cos" -> kotlin.math.cos(x)
                    "tan" -> kotlin.math.tan(x)
                    "log" -> kotlin.math.log10(x)
                    "ln" -> kotlin.math.ln(x)
                    else -> throw RuntimeException("Unknown function: $func")
                }
            }
        } else {
            throw RuntimeException("Unexpected character: " + ch.toChar())
        }

        if (eat('^'.code)) x = Math.pow(x, parseFactor())

        return x
    }
}


/* ==========================================================================
   NOTES PAD SUITE SUB-SCREEN (CREATE, EDIT, ASSOCIATE, DELETE NOTES)
   ========================================================================== */

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NotesPadSubScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val notes by viewModel.allNotes.collectAsState()
    val calendarEvents by viewModel.activeMonthEvents.collectAsState()
    val filesList by viewModel.fileList.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val filteredNotes = notes.filter {
        it.title.contains(searchQuery, true) || it.content.contains(searchQuery, true)
    }

    var showEditingDialog by remember { mutableStateOf(false) }
    var activeEditingNote by remember { mutableStateOf<Note?>(null) }

    // Form editing states
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var linkWithFile by remember { mutableStateOf<String?>(null) }
    var linkWithEventId by remember { mutableStateOf<Int?>(null) }

    // Dropdown pickers
    var isFileDropdownExpanded by remember { mutableStateOf(false) }
    var isEventDropdownExpanded by remember { mutableStateOf(false) }

    val openCreateForm: () -> Unit = {
        activeEditingNote = null
        noteTitle = ""
        noteContent = ""
        linkWithFile = null
        linkWithEventId = null
        showEditingDialog = true
    }

    val openEditForm: (Note) -> Unit = { note ->
        activeEditingNote = note
        noteTitle = note.title
        noteContent = note.content
        linkWithFile = note.associatedFileRelativePath
        linkWithEventId = note.associatedCalendarEventId
        showEditingDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App top control bar
        TopAppBar(
            title = { Text("Interactive Pad Notes", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Return to suite index")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            actions = {
                IconButton(onClick = openCreateForm) {
                    Icon(imageVector = Icons.Default.NoteAdd, contentDescription = "Make a fresh pad note", tint = MaterialTheme.colorScheme.primary)
                }
            }
        )

        // Search Bar container
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search note titles or content..."
        )

        if (filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.NoteAlt,
                        contentDescription = "No pad items found",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No notes match search query" else "Your Notes Pad is empty!",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Click the top page button to record standard text notes associated with active files or events.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredNotes) { note ->
                    val linkedEvent = calendarEvents.find { it.id == note.associatedCalendarEventId }
                    val linkedFile = note.associatedFileRelativePath

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openEditForm(note) }
                            .testTag("note_card_${note.id}"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = note.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.deleteNote(note.id) },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("delete_note_btn_${note.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Deletes selected note",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = note.content,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Association tags
                            if (linkedEvent != null || linkedFile != null) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (linkedFile != null) {
                                        SuggestionChip(
                                            onClick = {
                                                viewModel.navigateTo(MainViewModel.ToolSection.FILE_MANAGER)
                                                viewModel.openFileInEditor(linkedFile)
                                            },
                                            label = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(linkedFile.substringAfterLast('/'), fontSize = 10.sp)
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }

                                    if (linkedEvent != null) {
                                        SuggestionChip(
                                            onClick = {
                                                viewModel.navigateTo(MainViewModel.ToolSection.CALENDAR)
                                                viewModel.selectCalendarDate(linkedEvent.dateString)
                                            },
                                            label = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(linkedEvent.title, fontSize = 10.sp)
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp)
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

    // Modal Sheet or Dialog custom form for editing/creating notes
    if (showEditingDialog) {
        AlertDialog(
            onDismissRequest = { showEditingDialog = false },
            title = {
                Text(
                    text = if (activeEditingNote == null) "Create Pad Note" else "Edit Pad Note",
                    fontWeight = FontWeight.Bold
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
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Note Title") },
                        modifier = Modifier.fillMaxWidth().testTag("note_input_title"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Note Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("note_input_content"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Associated workspace file selector dropdown
                    Text(
                        text = "Associate with File",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { isFileDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = linkWithFile?.substringAfterLast('/') ?: "Select file (optional)",
                                    fontSize = 13.sp,
                                    color = if (linkWithFile != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Icon(
                                    imageVector = if (isFileDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isFileDropdownExpanded,
                            onDismissRequest = { isFileDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("(Unlinked/None)", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    linkWithFile = null
                                    isFileDropdownExpanded = false
                                }
                            )
                            filesList.filter { !it.isDirectory }.forEach { workspaceFile ->
                                DropdownMenuItem(
                                    text = { Text(workspaceFile.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        linkWithFile = workspaceFile.relativePath
                                        isFileDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Associated calendar events selector dropdown
                    Text(
                        text = "Associate with Calendar Event",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { isEventDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val assocEvent = calendarEvents.find { it.id == linkWithEventId }
                                Text(
                                    text = assocEvent?.title ?: "Select event (optional)",
                                    fontSize = 13.sp,
                                    color = if (assocEvent != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Icon(
                                    imageVector = if (isEventDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isEventDropdownExpanded,
                            onDismissRequest = { isEventDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("(Unlinked/None)", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    linkWithEventId = null
                                    isEventDropdownExpanded = false
                                }
                            )
                            calendarEvents.forEach { event ->
                                DropdownMenuItem(
                                    text = { Text("${event.title} (${event.dateString})", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        linkWithEventId = event.id
                                        isEventDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.trim().isNotEmpty()) {
                            val activeNote = activeEditingNote
                            if (activeNote == null) {
                                viewModel.insertNote(noteTitle, noteContent, linkWithFile, linkWithEventId)
                            } else {
                                viewModel.updateNote(activeNote.id, noteTitle, noteContent, linkWithFile, linkWithEventId)
                            }
                            showEditingDialog = false
                        }
                    },
                    modifier = Modifier.testTag("note_btn_save"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditingDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}


/* ==========================================================================
   PHOTO & VIDEO VIEWER SUB-SCREEN (Browse and Inspect Media Files)
   ========================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaViewerSubScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val fileList by viewModel.fileList.collectAsState()
    val scope = rememberCoroutineScope()

    // Filter media files or simulate standard photos/videos if none exist in files!
    val localMediaFiles = fileList.filter { file ->
        val ext = file.name.substringAfterLast('.').lowercase()
        ext in listOf("jpg", "jpeg", "png", "webp", "gif", "mp4")
    }

    // Mock catalog of beautiful, eye-friendly visual items (so media is always active)
    val simulatedCatalog = remember {
        listOf(
            SimulatedMedia(
                id = 1,
                title = "Study Space Workspace",
                type = MediaType.PHOTO,
                simulatedUrl = "workspace_photo"
            ),
            SimulatedMedia(
                id = 2,
                title = "Forest Canopy Ambient Loop",
                type = MediaType.VIDEO,
                simulatedUrl = "forest_video",
                creator = "Alex Sage",
                length = "0:15"
            ),
            SimulatedMedia(
                id = 3,
                title = "Pixel Art Cozy Fireplace",
                type = MediaType.PHOTO,
                simulatedUrl = "fireplace_pixel"
            ),
            SimulatedMedia(
                id = 4,
                title = "Midnight Coffee Synth Breeze",
                type = MediaType.VIDEO,
                simulatedUrl = "coffee_breeze",
                creator = "Jane Doe",
                length = "0:30"
            )
        )
    }

    var selectedSimulatedMedia by remember { mutableStateOf<SimulatedMedia?>(simulatedCatalog.firstOrNull()) }
    var mockPlayingState by remember { mutableStateOf(false) }
    var mockVideoSeconds by remember { mutableStateOf(0) }

    // Rhythmical simulated video ticking timer
    LaunchedEffect(mockPlayingState) {
        if (mockPlayingState) {
            while (mockPlayingState) {
                delay(1000)
                mockVideoSeconds = (mockVideoSeconds + 1) % 45
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App top control bar
        TopAppBar(
            title = { Text("Media Photo & Video Viewer", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Return to suite index")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Media selector List column (compact sidebar style)
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CATALOG SOURCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(simulatedCatalog) { item ->
                        val isSelected = selectedSimulatedMedia?.id == item.id
                        Card(
                            onClick = {
                                selectedSimulatedMedia = item
                                mockPlayingState = false
                                mockVideoSeconds = 0
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("media_item_${item.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (item.type == MediaType.VIDEO) Color(0xFFF3E0D8) else MaterialTheme.colorScheme.secondaryContainer
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (item.type == MediaType.VIDEO) Icons.Default.PlayCircleOutline else Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Color.Black.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (item.type == MediaType.VIDEO) "Video (${item.length})" else "Photo Web Display",
                                        fontSize = 10.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Big Screen Display
            Column(
                modifier = Modifier
                    .weight(1.8f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ACTIVE VIEWSCREEN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )

                selectedSimulatedMedia?.let { media ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E211D)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Fancy Canvas-Based Ambient Simulator (Draws beautiful gradient themes rather than missing raw local files)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawBehind {
                                        val brush = when (media.simulatedUrl) {
                                            "workspace_photo" -> Brush.radialGradient(
                                                colors = listOf(Color(0xFF386B41), Color(0xFF1A1C18)),
                                                radius = size.width * 0.9f
                                            )
                                            "forest_video" -> Brush.sweepGradient(
                                                colors = listOf(Color(0xFF222520), Color(0xFFC2EFAD), Color(0xFF222520))
                                            )
                                            "fireplace_pixel" -> Brush.verticalGradient(
                                                colors = listOf(Color(0xFFD35400), Color(0xFF2E322B))
                                            )
                                            else -> Brush.sweepGradient(
                                                colors = listOf(Color(0xFF475569), Color(0xFFF3E0D8), Color(0xFF386B41))
                                            )
                                        }
                                        drawRect(brush = brush)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (media.type == MediaType.VIDEO) {
                                    // Simulated motion graphics / active elements
                                    val pulseScale by animateFloatAsState(
                                        targetValue = if (mockPlayingState) 1.25f else 1.0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1500, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "VideoVisualPulse"
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size((75 * pulseScale).dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconButton(onClick = { mockPlayingState = !mockPlayingState }) {
                                                Icon(
                                                    imageVector = if (mockPlayingState) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = "Toggle play loop",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(18.dp))
                                        Text(
                                            text = if (mockPlayingState) "Playing Loop Simulation" else "Reel Paused",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "00:${String.format("%02d", mockVideoSeconds)} / ${media.length}",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                } else {
                                    // Photo Display Content
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Landscape,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.45f),
                                            modifier = Modifier.size(72.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = media.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Optech High Definition Asset Link",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Metadata Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text(
                                text = media.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Producer: ${media.creator ?: "System Sandbox"} | Resolution: 1920x1080",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class MediaType {
    PHOTO, VIDEO
}

data class SimulatedMedia(
    val id: Int,
    val title: String,
    val type: MediaType,
    val simulatedUrl: String,
    val creator: String? = "Suite Engine",
    val length: String = ""
)


/* ==========================================================================
   AUDIO LISTENER SUB-SCREEN (Music Player with Equalizer Visualization)
   ========================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioListenerSubScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val preloadedTracks = remember {
        listOf(
            SimulatedAudio(1, "Deep Focus Olive Beats", "Lofi Suite Master", "03:45"),
            SimulatedAudio(2, "Midnight Terminal Synth", "Retro Cyber Ambient", "04:12"),
            SimulatedAudio(3, "Clay Zen Cabin Breeze", "Acoustic Nature Breeze", "02:50")
        )
    }

    var selectedTrack by remember { mutableStateOf(preloadedTracks.first()) }
    var isPlaying by remember { mutableStateOf(false) }
    var trackProgressSeconds by remember { mutableStateOf(0) }

    // Rhythmical study track playback ticker
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                delay(1000)
                trackProgressSeconds = (trackProgressSeconds + 1) % 240
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App top control bar
        TopAppBar(
            title = { Text("Audio Listener & Studio", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Return to suite index")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Big Record Disk or Vinyl Cover
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E211D)),
                contentAlignment = Alignment.Center
            ) {
                // Disk visual rotation based on playing state
                val angleOffset by animateFloatAsState(
                    targetValue = if (isPlaying) 360f else 0f,
                    animationSpec = if (isPlaying) infiniteRepeatable(
                        animation = tween(6000, easing = LinearEasing)
                    ) else tween(300),
                    label = "DiskRotateAngle"
                )

                // Render aesthetic patterns
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    val brush = Brush.sweepGradient(
                        colors = listOf(Color(0xFF386B41), Color(0xFFD35400), Color(0xFF386B41)),
                    )
                    drawCircle(brush = brush)
                }

                // Inner sticker
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback progress controls
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = selectedTrack.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${selectedTrack.artist} • Classical Studio Link",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }

            // Beautiful animated waveform equalizer bars (Compose state animated!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 18) {
                        // Generate individual animators with offset starting points for bouncing rhythm
                        val targetScale = if (isPlaying) {
                            val pulse by rememberInfiniteTransition(label = "").animateFloat(
                                initialValue = 0.15f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(
                                        durationMillis = (400 + i * 50).coerceIn(300, 1000),
                                        easing = FastOutSlowInEasing
                                    ),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = ""
                            )
                            pulse
                        } else {
                            0.1f
                        }

                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(50.dp * targetScale)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (i % 2 == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                )
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                val progressFraction = trackProgressSeconds.toFloat() / 240f
                Slider(
                    value = progressFraction,
                    onValueChange = { trackProgressSeconds = (it * 240).toInt() },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${String.format("%02d", trackProgressSeconds / 60)}:${String.format("%02d", trackProgressSeconds % 60)}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Text(
                        text = selectedTrack.length,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            // Player control buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Previous track */ }) {
                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Skip back", modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.width(20.dp))
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle play pause",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                IconButton(onClick = { /* Next track */ }) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Skip forward", modifier = Modifier.size(36.dp))
                }
            }

            // Track list chooser
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(preloadedTracks) { track ->
                    val isSelected = selectedTrack.id == track.id
                    Card(
                        onClick = {
                            selectedTrack = track
                            trackProgressSeconds = 0
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.width(160.dp).testTag("audio_track_${track.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = track.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artist,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

data class SimulatedAudio(
    val id: Int,
    val title: String,
    val artist: String,
    val length: String
)


/* ==========================================================================
   SUPPORTING ATOM COMMON UI COMPONENTS (AESTHETIC MINIMAL OR OUTLINED)
   ========================================================================== */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("notes_search_input"),
        shape = RoundedCornerShape(26.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidAppsSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val androidApps by viewModel.androidApps.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val activeWallpaper by viewModel.desktopWallpaper.collectAsState()

    // Map wallpaper selections to beautiful Material 3 theme gradients matching Desktop home
    val wallpaperBrush = when (activeWallpaper) {
        "cosmic_void" -> Brush.radialGradient(
            colors = listOf(Color(0xFF2E1065), Color(0xFF030712)),
            radius = 1200f
        )
        "sunset_sky" -> Brush.linearGradient(
            colors = listOf(Color(0xFF312E81), Color(0xFF9D174D), Color(0xFF701A75))
        )
        "emerald_peak" -> Brush.linearGradient(
            colors = listOf(Color(0xFF064E3B), Color(0xFF111827))
        )
        "neon_twilight" -> Brush.linearGradient(
            colors = listOf(Color(0xFF1E1B4B), Color(0xFF581C87), Color(0xFF0D0630))
        )
        "ocean_deep" -> Brush.radialGradient(
            colors = listOf(Color(0xFF172554), Color(0xFF0B1329)),
            radius = 1000f
        )
        else -> Brush.radialGradient( // aurora_glow (default)
            colors = listOf(Color(0xFF0F172A), Color(0xFF020617), Color(0xFF1E293B)),
            radius = 1500f
        )
    }

    // Sort and filter launchable packages
    val filteredApps = remember(androidApps, searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            androidApps
        } else {
            androidApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) || 
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawRect(wallpaperBrush) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Frosted Header Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to home",
                        tint = Color.White
                    )
                }

                Text(
                    text = "App Drawer Android",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(
                    onClick = { viewModel.loadInstalledApps() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan new apps",
                        tint = Color.White
                    )
                }
            }

            // Sleek Rounded Search Bar (Glassmorphic)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search installed applications...", color = Color.White.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.8f)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.White)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .testTag("system_apps_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedContainerColor = Color.Black.copy(alpha = 0.35f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.25f)
                )
            )

            // Results Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${filteredApps.size} Launchable Apps Detected",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Apps,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No results matching target." else "Scanning loadable device packages...",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredApps) { app ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .clickable {
                                    try {
                                        val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                        if (launchIntent != null) {
                                            context.startActivity(launchIntent)
                                            viewModel.addAppLog("Launched external application: ${app.label}")
                                        } else {
                                            viewModel.addAppLog("Failed to launch ${app.label}: Package cannot be launched.")
                                        }
                                    } catch (e: Exception) {
                                        viewModel.addAppLog("Launch error layout: ${e.message}")
                                    }
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Circular Frosted Icon Frame
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (app.icon != null) {
                                    androidx.compose.ui.viewinterop.AndroidView(
                                        factory = { ctx ->
                                            android.widget.ImageView(ctx).apply {
                                                setImageDrawable(app.icon)
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Centered App label below the icon (just like a native home launcher!)
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            
                            // Tiny package indicator with system status badge
                            if (app.isSystem) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "SYS",
                                        fontSize = 8.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
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

// ==========================================
// LAUNCHER HOME SCREEN WORKSPACE (DESKTOP)
// ==========================================
@Composable
fun HomeScreenSubScreen(
    viewModel: MainViewModel,
    onSelectSubApp: (SubApp) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val activeWallpaper by viewModel.desktopWallpaper.collectAsState()
    val userName by viewModel.profileName.collectAsState()
    val profilePassword by viewModel.profilePassword.collectAsState()
    val hiddenApps by viewModel.hiddenApps.collectAsState()
    val context = LocalContext.current

    // High fidelity customization settings from ViewModel
    val gridColumns by viewModel.desktopGridColumns.collectAsState()
    val clockStyle by viewModel.desktopClockStyle.collectAsState()
    val iconSizeScale by viewModel.desktopIconSizeScale.collectAsState()
    val wallpaperOverlay by viewModel.desktopWallpaperOverlay.collectAsState()

    val pinnedApps by viewModel.pinnedApps.collectAsState()
    val enabledWidgets by viewModel.enabledWidgets.collectAsState()
    val widgetCardStyle by viewModel.widgetCardStyle.collectAsState()
    val iconRoundness by viewModel.iconRoundness.collectAsState()
    val quickMemoText by viewModel.quickMemoText.collectAsState()
    val wifiEnabled by viewModel.wifiEnabled.collectAsState()
    val bluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
    val dndEnabled by viewModel.dndEnabled.collectAsState()
    val airplaneModeEnabled by viewModel.airplaneModeEnabled.collectAsState()
    val locationEnabled by viewModel.locationEnabled.collectAsState()
    val androidApps by viewModel.androidApps.collectAsState()
    val appLogs by viewModel.appLogs.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 1) { 5 }

    var searchInputText by remember { mutableStateOf("") }
    var searchCategoryFilter by remember { mutableStateOf("ALL") }
    var showCustomizationSettings by remember { mutableStateOf(false) }

    // Map wallpaper selections to beautiful Material 3 theme gradients
    val wallpaperBrush = when (activeWallpaper) {
        "cosmic_void" -> Brush.radialGradient(
            colors = listOf(Color(0xFF2E1065), Color(0xFF030712)),
            radius = 1200f
        )
        "sunset_sky" -> Brush.linearGradient(
            colors = listOf(Color(0xFF312E81), Color(0xFF9D174D), Color(0xFF701A75))
        )
        "emerald_peak" -> Brush.linearGradient(
            colors = listOf(Color(0xFF064E3B), Color(0xFF111827))
        )
        "neon_twilight" -> Brush.linearGradient(
            colors = listOf(Color(0xFF1E1B4B), Color(0xFF581C87), Color(0xFF0D0630))
        )
        "ocean_deep" -> Brush.radialGradient(
            colors = listOf(Color(0xFF172554), Color(0xFF0B1329)),
            radius = 1000f
        )
        else -> Brush.radialGradient( // aurora_glow (default)
            colors = listOf(Color(0xFF0F172A), Color(0xFF020617), Color(0xFF1E293B)),
            radius = 1500f
        )
    }

    val customWallpaperFile = remember(activeWallpaper) { File(context.filesDir, "custom_wallpaper.jpg") }
    val isCustomWallpaperSelected = activeWallpaper == "custom_wallpaper" && customWallpaperFile.exists()

    // Outer workspace background container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (!isCustomWallpaperSelected) {
                    Modifier.drawBehind { drawRect(wallpaperBrush) }
                } else {
                    Modifier
                }
            )
            .testTag("launcher_home_desktop")
    ) {
        if (isCustomWallpaperSelected) {
            AsyncImage(
                model = customWallpaperFile,
                contentDescription = "Custom Desktop Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Flat Wallpaper Overlay Shading Layer (Tweakable in settings!)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = wallpaperOverlay))
        )

        val desktopTheme by viewModel.desktopTheme.collectAsState()

        if (desktopTheme == "metro") {
            MetroThemeLayout(
                viewModel = viewModel,
                onSelectSubApp = onSelectSubApp
            )
        } else if (desktopTheme == "search") {
            SearchThemeLayout(
                viewModel = viewModel,
                onSelectSubApp = onSelectSubApp,
                onNavigateToProfile = onNavigateToProfile
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // --- TOP HEADER (GREETINGS BAR) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.firstOrNull()?.uppercase() ?: "O",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (pagerState.currentPage) {
                                0 -> "FEED & TELEMETRY"
                                1 -> "SYSTEM WORKSPACE"
                                2 -> "PINNED APPLICATIONS"
                                3 -> "WIDGETS AREA"
                                else -> "UNIFIED SEARCH HUB"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray.copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Interactive Clock Speed & Status Badging
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        text = "NODE_01",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- NAVIGATION PAGE DOTS (SLIDING TABS) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "QUICK FEED" to Icons.Default.WbSunny,
                    "WORKSPACE" to Icons.Default.Apps,
                    "PINNED APPS" to Icons.Default.PushPin,
                    "WIDGETS" to Icons.Default.Widgets,
                    "UNIFIED SEARCH" to Icons.Default.Search
                ).forEachIndexed { index, (label, icon) ->
                    val isSelected = pagerState.currentPage == index
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- HORIZONTAL PAGER MAIN BODY ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    // ==========================================
                    // PAGE 0: "FOR YOU" INTEGRATED CORNER
                    // ==========================================
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // --- WEATHER STATION METRICS (WEATHER SYSTEM) ---
                            var selectedWeatherLoc by remember { mutableStateOf("New York") }
                            val realWeather by viewModel.realWeather.collectAsState()
                            val isWeatherLoading by viewModel.isWeatherLoading.collectAsState()
                            var customCityInput by remember { mutableStateOf("") }

                            LaunchedEffect(selectedWeatherLoc) {
                                viewModel.fetchRealWeather(selectedWeatherLoc)
                            }

                            ElevatedCard(
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = Color.Black.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Weather Title & Refresh
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(
                                                imageVector = Icons.Default.WbSunny,
                                                contentDescription = null,
                                                tint = Color(0xFFFBBF24),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "REAL-TIME WEATHER STATION",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.fetchRealWeather(selectedWeatherLoc)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            if (isWeatherLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Refresh Telemetry",
                                                    tint = Color.LightGray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Location Selector Row (Popular presets)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf(
                                            "New York",
                                            "London",
                                            "Tokyo",
                                            "Paris"
                                        ).forEach { loc ->
                                            val isSel = selectedWeatherLoc.equals(loc, ignoreCase = true)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSel) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                                    .clickable { selectedWeatherLoc = loc }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = loc,
                                                    color = if (isSel) Color.White else Color.Gray,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Custom City search input
                                    OutlinedTextField(
                                        value = customCityInput,
                                        onValueChange = { customCityInput = it },
                                        placeholder = { Text("Search any city worldwide...", color = Color.Gray, fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Search
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onSearch = {
                                                if (customCityInput.trim().isNotEmpty()) {
                                                    selectedWeatherLoc = customCityInput.trim()
                                                }
                                            }
                                        ),
                                        trailingIcon = {
                                            IconButton(
                                                onClick = {
                                                    if (customCityInput.trim().isNotEmpty()) {
                                                        selectedWeatherLoc = customCityInput.trim()
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color.White.copy(alpha = 0.3f),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedContainerColor = Color.Black.copy(alpha = 0.25f),
                                            unfocusedContainerColor = Color.Black.copy(alpha = 0.15f)
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (realWeather != null) {
                                        val w = realWeather!!
                                        // Primary Metrics display
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = w.temperature,
                                                    fontSize = 32.sp,
                                                    fontWeight = FontWeight.Light,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = w.cityName,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.LightGray,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Icon(Icons.Default.Air, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                                                    Text(w.windSpeed, style = MaterialTheme.typography.bodySmall, color = Color.White)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                                                    Text("HUMIDITY: ${w.humidity}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                                }
                                            }
                                        }

                                        // Advisory banner
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF312E81).copy(alpha = 0.3f))
                                                .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = "Conditions",
                                                    tint = Color(0xFFA5F3FC),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "${w.description}. Apparent feel is ${w.apparentTemp}.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFFA5F3FC)
                                                )
                                            }
                                        }

                                        // Hourly Mini Forecast
                                        if (w.hourlyForecast.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                w.hourlyForecast.forEach { (hr, t) ->
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(hr, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                                        Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp).padding(vertical = 2.dp))
                                                        Text(t, style = MaterialTheme.typography.labelSmall, color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                    }
                                }
                            }

                            // --- REAL-TIME NEWS FEED (LATEST CRITICAL UPDATES) ---
                            val realNewsList by viewModel.realNewsList.collectAsState()
                            val isNewsLoading by viewModel.isNewsLoading.collectAsState()

                            LaunchedEffect(Unit) {
                                if (realNewsList.isEmpty()) {
                                    viewModel.fetchRealNews()
                                }
                            }

                            // Interactive Filters and States
                            var filterCategory by remember { mutableStateOf("ALL") }
                            var newsSearchQuery by remember { mutableStateOf("") }
                            var bookmarkedIds by remember { mutableStateOf(setOf<Int>()) }
                            var activeReadingArticle by remember { mutableStateOf<RealNewsItem?>(null) }

                            // News Feed Title & Refresh
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SATELLITE NEWS BROADCASTS",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = { viewModel.fetchRealNews() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    if (isNewsLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh headlines", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Category selector Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("ALL", "TECH", "GENERAL", "★ SAVED").forEach { cat ->
                                    val isCurrent = filterCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isCurrent) Color(0xFF4F46E5) else Color.White.copy(alpha = 0.08f))
                                            .clickable { filterCategory = cat }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isCurrent) Color.White else Color.LightGray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Smart feed search input
                            OutlinedTextField(
                                value = newsSearchQuery,
                                onValueChange = { newsSearchQuery = it },
                                placeholder = { Text("Search briefings stream...", color = Color.Gray, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                                trailingIcon = {
                                    if (newsSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { newsSearchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedContainerColor = Color.Black.copy(alpha = 0.25f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.15f)
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Filtered list derived from Flow list
                            val filteredStories = remember(realNewsList, filterCategory, newsSearchQuery, bookmarkedIds) {
                                realNewsList.filter { item ->
                                    val catMatches = when (filterCategory) {
                                        "ALL" -> true
                                        "★ SAVED" -> bookmarkedIds.contains(item.id)
                                        "TECH" -> item.category.uppercase().contains("TECH") ||
                                                item.category.uppercase().contains("VERGE") ||
                                                item.category.uppercase().contains("WIRED") ||
                                                item.category.uppercase().contains("CRUNCH") ||
                                                item.category.uppercase().contains("ENGADGET")
                                        "GENERAL" -> !(item.category.uppercase().contains("TECH") ||
                                                item.category.uppercase().contains("VERGE") ||
                                                item.category.uppercase().contains("WIRED") ||
                                                item.category.uppercase().contains("CRUNCH") ||
                                                item.category.uppercase().contains("ENGADGET"))
                                        else -> false
                                    }
                                    val searchMatches = newsSearchQuery.trim().isEmpty() ||
                                            item.title.contains(newsSearchQuery, ignoreCase = true) ||
                                            item.description.contains(newsSearchQuery, ignoreCase = true)
                                    catMatches && searchMatches
                                }
                            }

                            if (isNewsLoading && realNewsList.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            } else if (filteredStories.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (filterCategory == "★ SAVED") "No saved briefings yet. Tap star on news items to bookmark." else "No live articles found matching filters.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                filteredStories.forEach { story ->
                                    val isSaved = bookmarkedIds.contains(story.id)
                                    ElevatedCard(
                                        colors = CardDefaults.elevatedCardColors(
                                            containerColor = Color.Black.copy(alpha = 0.25f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .clickable { activeReadingArticle = story }
                                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Category Badge
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.White.copy(alpha = 0.1f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text(story.icon, fontSize = 10.sp)
                                                        Text(story.category, style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(story.time, style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                                                    IconButton(
                                                        onClick = {
                                                            bookmarkedIds = if (isSaved) bookmarkedIds - story.id else bookmarkedIds + story.id
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isSaved) Icons.Filled.Star else Icons.Outlined.Star,
                                                            contentDescription = "Save Briefing",
                                                            tint = if (isSaved) Color(0xFFFFD700) else Color.LightGray,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = story.title,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )

                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    Text(
                                                        text = story.description,
                                                        color = Color.LightGray,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }

                                                if (story.urlToImage != null) {
                                                    AsyncImage(
                                                        model = story.urlToImage,
                                                        contentDescription = null,
                                                        modifier = Modifier
                                                            .size(60.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .align(Alignment.CenterVertically),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Full Screen article details popup reader view
                            activeReadingArticle?.let { activeStory ->
                                var readingThemeMode by remember { mutableStateOf("slate") } // slate, paper, matrix, cyber
                                var readingFontSize by remember { mutableStateOf(16.sp) }
                                val textModeColors = when (readingThemeMode) {
                                    "matrix" -> Pair(Color(0xFF0F172A), Color(0xFF10B981)) // Matrix green
                                    "paper" -> Pair(Color(0xFFFAFAF9), Color(0xFF1C1917)) // Clean white paper
                                    "slate" -> Pair(Color(0xFF1E293B), Color(0xFFF1F5F9)) // Dark Slate UI
                                    else -> Pair(Color(0xFF030712), Color(0xFFECEFEE)) // Cyber Midnight
                                }

                                AlertDialog(
                                    onDismissRequest = { activeReadingArticle = null },
                                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(textModeColors.first),
                                    title = null,
                                    text = {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .verticalScroll(rememberScrollState())
                                                .padding(horizontal = 4.dp, vertical = 24.dp)
                                        ) {
                                            // Navigation controls inside Reader modal
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(onClick = { activeReadingArticle = null }) {
                                                    Icon(Icons.Default.ArrowBack, contentDescription = "Close Reader", tint = textModeColors.second)
                                                }

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Size adjustment
                                                    IconButton(onClick = { if (readingFontSize.value > 12) readingFontSize = (readingFontSize.value - 2).sp }) {
                                                        Text("A-", style = MaterialTheme.typography.titleMedium, color = textModeColors.second, fontWeight = FontWeight.Bold)
                                                    }
                                                    IconButton(onClick = { if (readingFontSize.value < 26) readingFontSize = (readingFontSize.value + 2).sp }) {
                                                        Text("A+", style = MaterialTheme.typography.titleMedium, color = textModeColors.second, fontWeight = FontWeight.Bold)
                                                    }

                                                    // Reading mode swapper
                                                    listOf("cyber" to "🌌", "matrix" to "📟", "paper" to "📄", "slate" to "⚙️").forEach { (md, icon) ->
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                 .background(if (readingThemeMode == md) textModeColors.second.copy(alpha = 0.2f) else Color.Transparent)
                                                                .clickable { readingThemeMode = md }
                                                                .padding(6.dp)
                                                        ) {
                                                            Text(icon, fontSize = 14.sp)
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Optional article hero banner picture
                                            if (activeStory.urlToImage != null) {
                                                AsyncImage(
                                                    model = activeStory.urlToImage,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(180.dp)
                                                        .clip(RoundedCornerShape(12.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                            }

                                            // Article Header
                                            Text(
                                                text = activeStory.category.uppercase(),
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = textModeColors.second.copy(alpha = 0.7f),
                                                style = MaterialTheme.typography.labelMedium
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = activeStory.title,
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = textModeColors.second,
                                                lineHeight = 32.sp
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(textModeColors.second)
                                                )
                                                Text(
                                                    text = "${activeStory.category.uppercase()} BROADCAST • ${activeStory.time.uppercase()}",
                                                    fontFamily = FontFamily.Monospace,
                                                    color = textModeColors.second.copy(alpha = 0.6f),
                                                    fontSize = 10.sp
                                                )
                                            }

                                            Divider(modifier = Modifier.padding(vertical = 16.dp), color = textModeColors.second.copy(alpha = 0.15f))

                                            // Actual news Content
                                            Text(
                                                text = activeStory.content,
                                                fontSize = readingFontSize,
                                                color = textModeColors.second,
                                                lineHeight = (readingFontSize.value + 6).sp,
                                                textAlign = TextAlign.Justify
                                            )

                                            Spacer(modifier = Modifier.height(32.dp))

                                            // Bookmarking control inside detail view
                                            Button(
                                                onClick = {
                                                    bookmarkedIds = if (bookmarkedIds.contains(activeStory.id)) {
                                                        bookmarkedIds - activeStory.id
                                                    } else {
                                                        bookmarkedIds + activeStory.id
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = textModeColors.second.copy(alpha = 0.15f),
                                                    contentColor = textModeColors.second
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Icon(
                                                        imageVector = if (bookmarkedIds.contains(activeStory.id)) Icons.Filled.Star else Icons.Outlined.Star,
                                                        contentDescription = null,
                                                        tint = if (bookmarkedIds.contains(activeStory.id)) Color(0xFFFFD700) else textModeColors.second
                                                    )
                                                    Text(if (bookmarkedIds.contains(activeStory.id)) "SAVED TO SYSTEM STATION" else "BOOKMARK IN BRIEF HUB")
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {}
                                )
                            }

                            // --- ENABLED QUICK FEED WIDGETS SECTION ---
                            Spacer(modifier = Modifier.height(24.dp))
                            Divider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "QUICK FEED WIDGET HUD",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp)
                            )
                            Text(
                                text = "A dynamic custom display of active console nodes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                            )

                            if (enabledWidgets.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.03f))
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Widgets, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                                        Text("Quick Feed is Empty", color = Color.LightGray, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("Toggle telemetry widgets in the WIDGETS tab to view metrics dynamically.", color = Color.Gray, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                                    }
                                }
                            } else {
                                if (enabledWidgets.contains("battery")) {
                                    BatteryTelemetryCard(widgetCardStyle, viewModel)
                                }
                                if (enabledWidgets.contains("ram")) {
                                    RamCpuPerformanceCard(widgetCardStyle, viewModel)
                                }
                                if (enabledWidgets.contains("memo")) {
                                    PersistableMemoPadCard(widgetCardStyle, quickMemoText) { viewModel.saveQuickMemoText(it) }
                                }
                                if (enabledWidgets.contains("toggles")) {
                                    HardwareTogglesCard(widgetCardStyle, wifiEnabled, bluetoothEnabled, dndEnabled, airplaneModeEnabled, locationEnabled, viewModel)
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }

                    // ==========================================
                    // PAGE 1: CYBER DESK SCREEN (MAIN)
                    // ==========================================
                    1 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // High Fidelity Configured Clock Widget (LED, Terminal, Analog, Glow)
                            DesktopClockWidget(clockStyle)

                            Spacer(modifier = Modifier.height(16.dp))

                            // Shortcuts launcher Grid partitioned based on gridColumns count (4 vs 5)
                            val shortcutsList = remember(hiddenApps) {
                                listOf(
                                    LauncherShortcutItem("Files", Icons.Default.FolderOpen, Color(0xFFF59E0B)) { viewModel.navigateTo(MainViewModel.ToolSection.FILE_MANAGER) },
                                    LauncherShortcutItem("Chromium", Icons.Default.Language, Color(0xFF3B82F6)) { viewModel.navigateTo(MainViewModel.ToolSection.BROWSER) },
                                    LauncherShortcutItem("Terminal", Icons.Default.Terminal, Color(0xFF10B981)) { viewModel.navigateTo(MainViewModel.ToolSection.TERMINAL) },
                                    LauncherShortcutItem("Calendar", Icons.Default.CalendarMonth, Color(0xFFEC4899)) { viewModel.navigateTo(MainViewModel.ToolSection.CALENDAR) }
                                ).toMutableList().apply {
                                    if (!hiddenApps.contains(SubApp.NOTES_PAD.name)) {
                                        add(LauncherShortcutItem("Notes", Icons.Default.NoteAlt, Color(0xFF6366F1)) { onSelectSubApp(SubApp.NOTES_PAD) })
                                    }
                                    if (!hiddenApps.contains(SubApp.CALCULATOR.name)) {
                                        add(LauncherShortcutItem("Calculator", Icons.Default.Calculate, Color(0xFF8B5CF6)) { onSelectSubApp(SubApp.CALCULATOR) })
                                    }
                                    if (!hiddenApps.contains(SubApp.ANDROID_APPS.name)) {
                                        add(LauncherShortcutItem("App drawer", Icons.Default.Apps, Color(0xFF059669)) { onSelectSubApp(SubApp.ANDROID_APPS) })
                                    }
                                    add(LauncherShortcutItem("Launchpad", Icons.Default.Dashboard, Color(0xFF4B5563)) { onSelectSubApp(SubApp.LAUNCHPAD) })
                                }
                            }

                            // Dynamic Grid Layout chunker (custom columns)
                            val chunks = remember(shortcutsList, gridColumns) {
                                shortcutsList.chunked(gridColumns)
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                chunks.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        rowItems.forEach { item ->
                                            DesktopShortcutIcon(
                                                title = item.title,
                                                icon = item.icon,
                                                backgroundColor = item.color,
                                                iconSizeScale = iconSizeScale,
                                                onClick = item.onClick
                                            )
                                        }
                                        // Fill extra slots to maintain spacing in the Row
                                        if (rowItems.size < gridColumns) {
                                            repeat(gridColumns - rowItems.size) {
                                                Spacer(modifier = Modifier.width(72.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // --- METRICS / QUICK STATE PANEL (HARDWARE WIDGET) ---
                            ElevatedCard(
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = Color.Black.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "TELEMETRY METRICS HUD",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.LightGray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("NOMINAL", fontSize = 8.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Text("SYSTEM RESOURCE CPU", fontSize = 9.sp, color = Color.Gray)
                                            Text("38% WORKER STATE", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("VOLATILE RAM ALLOC", fontSize = 9.sp, color = Color.Gray)
                                            Text("2.4 GB ACTIVE", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("COMM LINK LATENCY", fontSize = 9.sp, color = Color.Gray)
                                            Text("14 MS STABLE", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Profile access widget
                            ElevatedCard(
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = Color.Black.copy(alpha = 0.45f)
                                ),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToProfile() }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF6366F1)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ManageAccounts,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "Profile & Security locks",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Configure user credentials (offline-only)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.LightGray.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Edit Profile Dashboard",
                                        tint = Color.LightGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }

                    // ==========================================
                    // PAGE 2: LAUNCHER CUSTOMIZATION CONTEXT
                    // ==========================================
                    // ==========================================
                    // PAGE 2: PINNED SYSTEM APPLICATIONS HUD
                    // ==========================================
                    2 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "PINNED APPLICATIONS SYSTEM",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Pin launchable system utilities and Android tools for high speed execution from this tab.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            val pm = LocalContext.current.packageManager
                            val pinnedList = androidApps.filter { pinnedApps.contains(it.packageName) }

                            if (pinnedList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.Black.copy(alpha = 0.25f))
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = "No Pins",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(
                                            text = "SECURED PIN GRID EMPTY",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.LightGray,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Utilize the App Pin Manager below to instantly register your favorite package keys onto this dashboard.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                val activeColsNum = gridColumns
                                val pinChunks = pinnedList.chunked(activeColsNum)

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    pinChunks.forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            rowItems.forEach { appInfo ->
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier
                                                        .width(76.dp)
                                                        .clickable {
                                                            try {
                                                                val intent = pm.getLaunchIntentForPackage(appInfo.packageName)
                                                                if (intent != null) {
                                                                    context.startActivity(intent)
                                                                    viewModel.addAppLog("Executed pinned app: ${appInfo.label}")
                                                               } else {
                                                                    viewModel.addAppLog("Unable to resolve launch intent for: ${appInfo.label}")
                                                                }
                                                            } catch (e: Exception) {
                                                                viewModel.addAppLog("Failed to boot app: ${e.message}")
                                                            }
                                                        }
                                                        .padding(vertical = 6.dp)
                                                ) {
                                                    val cornerRadius = when (iconRoundness) {
                                                        "square" -> 4.dp
                                                        "soft" -> 20.dp
                                                        "circle" -> 99.dp
                                                        else -> 14.dp
                                                    }
                                                    val boxSize = when (iconSizeScale) {
                                                        "compact" -> 40.dp
                                                        "large" -> 60.dp
                                                        else -> 50.dp
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(boxSize)
                                                            .clip(RoundedCornerShape(cornerRadius))
                                                            .background(Color.White.copy(alpha = 0.08f))
                                                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(cornerRadius)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (appInfo.icon != null) {
                                                            androidx.compose.ui.viewinterop.AndroidView(
                                                                factory = { ctx ->
                                                                    android.widget.ImageView(ctx).apply {
                                                                        setImageDrawable(appInfo.icon)
                                                                    }
                                                                },
                                                                modifier = Modifier.fillMaxSize().padding(if (iconSizeScale == "compact") 6.dp else if (iconSizeScale == "large") 12.dp else 9.dp)
                                                            )
                                                        } else {
                                                            Icon(Icons.Default.Android, appInfo.label, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = appInfo.label,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontSize = if (iconSizeScale == "compact") 10.sp else if (iconSizeScale == "large") 13.sp else 12.sp
                                                        ),
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            if (rowItems.size < activeColsNum) {
                                                repeat(activeColsNum - rowItems.size) {
                                                    Spacer(modifier = Modifier.width(76.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            Divider(color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "SECURED PACKAGE PIN MANAGER",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            var pinQuery by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = pinQuery,
                                onValueChange = { pinQuery = it },
                                placeholder = { Text("Filter package or name...", color = Color.Gray) },
                                textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6366F1),
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.1f)
                                ),
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) }
                            )

                            val filteredApps = remember(androidApps, pinQuery) {
                                androidApps.filter {
                                    it.label.contains(pinQuery, ignoreCase = true) || it.packageName.contains(pinQuery, ignoreCase = true)
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .padding(6.dp)
                            ) {
                                if (filteredApps.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        Text("No matching packages resolved.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth().heightIn(max = 230.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(filteredApps) { appInfo ->
                                            val isPinned = pinnedApps.contains(appInfo.packageName)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isPinned) Color(0xFF6366F1).copy(alpha = 0.08f) else Color.Transparent)
                                                    .clickable { viewModel.toggleAppPin(appInfo.packageName) }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color.White.copy(alpha = 0.05f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (appInfo.icon != null) {
                                                            androidx.compose.ui.viewinterop.AndroidView(
                                                                factory = { ctx ->
                                                                    android.widget.ImageView(ctx).apply {
                                                                        setImageDrawable(appInfo.icon)
                                                                    }
                                                                },
                                                                modifier = Modifier.fillMaxSize().padding(4.dp)
                                                            )
                                                        } else {
                                                            Icon(Icons.Default.Android, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                    Column {
                                                        Text(text = appInfo.label, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                                                        Text(text = appInfo.packageName, fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                                    }
                                                }

                                                IconButton(
                                                    onClick = { viewModel.toggleAppPin(appInfo.packageName) },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                                        contentDescription = "Pin Toggle",
                                                        tint = if (isPinned) Color(0xFF6366F1) else Color.DarkGray,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }

                    // ==========================================
                    // PAGE 3: TELEMETRY & WIDGETS CENTRAL CONTROLLER
                    // ==========================================
                    3 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "WIDGET SYSTEM CONTROLLER",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Activate active diagnostic grids and visual terminals to display instantly inside the Quick Feed (Page 0).",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Section A: Card Style Picker
                            Text(
                                text = "Widget Interface Theme Style",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf(
                                    "glass" to "Ambient Glass",
                                    "solid" to "Cyber Slate",
                                    "wireframe" to "Holo Wireframe"
                                ).forEach { (styleKey, styleLabel) ->
                                    val isSel = widgetCardStyle == styleKey
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSel) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.05f))
                                            .border(1.dp, if (isSel) Color.White else Color.Transparent, RoundedCornerShape(10.dp))
                                            .clickable { viewModel.setWidgetCardStyle(styleKey) }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = styleLabel,
                                            color = if (isSel) Color.Black else Color.LightGray,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Section B: Widget Checkboxes and Previews
                            val togglableWidgets = listOf(
                                "battery" to "Battery Energy Cells Terminals",
                                "ram" to "CPU & RAM Heavy Overclocker HUD",
                                "memo" to "Persisted Thoughts Notepad Memo Box",
                                "toggles" to "Simulated Hardware connectivity Console"
                            )

                            togglableWidgets.forEach { (widgetKey, widgetTitle) ->
                                val isActive = enabledWidgets.contains(widgetKey)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isActive) Color.White.copy(alpha = 0.04f) else Color.Transparent)
                                        .clickable { viewModel.toggleWidgetEnabled(widgetKey) }
                                        .padding(vertical = 8.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = widgetTitle, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = if (isActive) "ACTIVE NODE • Streaming Telemetry" else "NODE SLEEPING",
                                            fontSize = 9.sp,
                                            color = if (isActive) Color(0xFF10B981) else Color.Gray,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Checkbox(
                                        checked = isActive,
                                        onCheckedChange = { viewModel.toggleWidgetEnabled(widgetKey) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFFF59E0B),
                                            uncheckedColor = Color.Gray
                                        )
                                    )
                                }

                                // Interactive LIVE Preview Card!
                                if (isActive) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 12.dp, end = 12.dp, bottom = 16.dp, top = 4.dp)
                                    ) {
                                        when (widgetKey) {
                                            "battery" -> BatteryTelemetryCard(widgetCardStyle, viewModel)
                                            "ram" -> RamCpuPerformanceCard(widgetCardStyle, viewModel)
                                            "memo" -> PersistableMemoPadCard(widgetCardStyle, quickMemoText) { viewModel.saveQuickMemoText(it) }
                                            "toggles" -> HardwareTogglesCard(widgetCardStyle, wifiEnabled, bluetoothEnabled, dndEnabled, airplaneModeEnabled, locationEnabled, viewModel)
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }

                    // ==========================================
                    // PAGE 4: UNIFIED SEARCH & SYSTEM SETTINGS PANEL
                    // ==========================================
                    4 -> {
                        UnifiedSearchAndSettingsPanel(
                            viewModel = viewModel,
                            searchInputText = searchInputText,
                            onSearchInputTextChange = { searchInputText = it },
                            searchCategoryFilter = searchCategoryFilter,
                            onSearchCategoryFilterChange = { searchCategoryFilter = it },
                            showCustomizationSettings = showCustomizationSettings,
                            onShowCustomizationSettingsChange = { showCustomizationSettings = it },
                            onSelectSubApp = onSelectSubApp
                        )
                    }

                    // Original page settings drawer is now relocated and embedded in search expandable drawer
                    44 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "SYSTEM CUSTOMIZATION & SETTINGS",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Overclock your visual elements, custom clock layouts, icon structures, look and diagnostic outputs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // --- DESKTOP LAYOUT THEME ---
                            Text(
                                text = "Desktop Layout Theme",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select dynamic aesthetic style grids or import an external theme profile.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            val dTheme by viewModel.desktopTheme.collectAsState()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "normal" to "Normal Theme",
                                    "metro" to "Metro WP10",
                                    "search" to "Minimal Search"
                                ).forEach { (themeKey, label) ->
                                    val isSelected = dTheme == themeKey
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.05f))
                                            .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(10.dp))
                                            .clickable { viewModel.setDesktopTheme(themeKey) }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else Color.LightGray,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val themeImporterLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.GetContent()
                            ) { uri: android.net.Uri? ->
                                if (uri != null) {
                                    try {
                                        context.contentResolver.openInputStream(uri)?.use { stream ->
                                            val tContent = stream.bufferedReader().use { it.readText() }
                                            val msg = viewModel.importOpTheme(tContent)
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: java.lang.Exception) {
                                        Toast.makeText(context, "Theme import failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }

                            Button(
                                onClick = { themeImporterLauncher.launch("*/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("IMPORT .OPTHEME THEME FILE", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Theme Wallpaperpresets
                            Text(
                                text = "Desktop Wallpaper presets",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                WallpaperThumbCircle("aurora_glow", Brush.radialGradient(listOf(Color(0xFF0F172A), Color(0xFF020617))), activeWallpaper == "aurora_glow") { viewModel.setDesktopWallpaper("aurora_glow") }
                                WallpaperThumbCircle("cosmic_void", Brush.radialGradient(listOf(Color(0xFF2E1065), Color(0xFF030712))), activeWallpaper == "cosmic_void") { viewModel.setDesktopWallpaper("cosmic_void") }
                                WallpaperThumbCircle("sunset_sky", Brush.linearGradient(listOf(Color(0xFF312E81), Color(0xFF9D174D))), activeWallpaper == "sunset_sky") { viewModel.setDesktopWallpaper("sunset_sky") }
                                WallpaperThumbCircle("emerald_peak", Brush.linearGradient(listOf(Color(0xFF064E3B), Color(0xFF111827))), activeWallpaper == "emerald_peak") { viewModel.setDesktopWallpaper("emerald_peak") }
                                WallpaperThumbCircle("neon_twilight", Brush.linearGradient(listOf(Color(0xFF1E1B4B), Color(0xFF581C87))), activeWallpaper == "neon_twilight") { viewModel.setDesktopWallpaper("neon_twilight") }
                                WallpaperThumbCircle("ocean_deep", Brush.radialGradient(listOf(Color(0xFF172554), Color(0xFF0B1329))), activeWallpaper == "ocean_deep") { viewModel.setDesktopWallpaper("ocean_deep") }
                            }

                            // Custom Wallpaper Selector Section
                            Text(
                                text = "Or use your own wallpaper",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val customWallpaperFile = remember(activeWallpaper) { File(context.filesDir, "custom_wallpaper.jpg") }
                                val customWallpaperExists = customWallpaperFile.exists()

                                val wallpaperGalleryLauncher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.GetContent()
                                ) { uri: android.net.Uri? ->
                                    if (uri != null) {
                                        try {
                                            context.contentResolver.openInputStream(uri)?.use { input ->
                                                val dstFile = File(context.filesDir, "custom_wallpaper.jpg")
                                                dstFile.outputStream().use { output ->
                                                    input.copyTo(output)
                                                }
                                                viewModel.setDesktopWallpaper("custom_wallpaper")
                                                viewModel.addAppLog("Custom wallpaper loaded from storage.")
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error reading image: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }

                                Button(
                                    onClick = { wallpaperGalleryLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("UPLOAD IMAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }

                                if (customWallpaperExists) {
                                    // Small preview of current custom wallpaper
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, if (activeWallpaper == "custom_wallpaper") Color.Green else Color.DarkGray, CircleShape)
                                            .clickable { viewModel.setDesktopWallpaper("custom_wallpaper") }
                                    ) {
                                        AsyncImage(
                                            model = customWallpaperFile,
                                            contentDescription = "Custom preview",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.setDesktopWallpaper("aurora_glow")
                                            try {
                                                customWallpaperFile.delete()
                                                viewModel.addAppLog("Cleared custom wallpaper.")
                                            } catch (e: Exception) {}
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("DELETE", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.White)
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                            // Wallpaper Dim Overlay Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Wallpaper overlay shading",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${(wallpaperOverlay * 100).toInt()}% Shade",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Slider(
                                value = wallpaperOverlay,
                                onValueChange = { viewModel.setDesktopWallpaperOverlay(it) },
                                valueRange = 0f..0.85f,
                                steps = 17,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF6366F1),
                                    activeTrackColor = Color(0xFF6366F1),
                                    inactiveTrackColor = Color.DarkGray
                                )
                            )

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                            // Icon size config
                            Text(
                                text = "App Desktop layout scale",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "compact" to "Compact Grid",
                                    "normal" to "Nominal Scale",
                                    "large" to "Enlarged Panel"
                                ).forEach { (scaleKey, label) ->
                                    val isSelected = iconSizeScale == scaleKey
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(0xFF6366F1) else Color.White.copy(alpha = 0.05f))
                                            .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(10.dp))
                                            .clickable { viewModel.setDesktopIconSizeScale(scaleKey) }
                                            .padding(vertical = 12.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else Color.LightGray,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                            // Icon CORNER ROUNDNESS config
                            Text(
                                text = "App icon corners design",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "square" to "Square",
                                    "medium" to "Medium",
                                    "soft" to "Soft corner",
                                    "circle" to "Full Circle"
                                ).forEach { (roundKey, label) ->
                                    val isSelected = iconRoundness == roundKey
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.05f))
                                            .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(10.dp))
                                            .clickable { viewModel.setIconRoundness(roundKey) }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else Color.LightGray,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                            // Desktop column count config
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Desktop shortcuts grid columns",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { if (gridColumns > 3) viewModel.setDesktopGridColumns(gridColumns - 1) },
                                        enabled = gridColumns > 3,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.08f))
                                    ) {
                                        Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        text = "$gridColumns Cols",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    IconButton(
                                        onClick = { if (gridColumns < 6) viewModel.setDesktopGridColumns(gridColumns + 1) },
                                        enabled = gridColumns < 6,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.08f))
                                    ) {
                                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                            // Clock Style Config
                            Text(
                                text = "Clock Style Layout Theme",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "digital_glow" to "Digital Neon Glow",
                                    "cyber_terminal" to "Cyber Monotty",
                                    "led_retro" to "LED Industrial",
                                    "none" to "Minimal modern"
                                ).forEach { (clkTheme, clkLabel) ->
                                    val isCurrent = clockStyle == clkTheme
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isCurrent) Color(0xFFEC4899) else Color.White.copy(alpha = 0.05f))
                                            .border(1.dp, if (isCurrent) Color.White else Color.Transparent, RoundedCornerShape(10.dp))
                                            .clickable { viewModel.setDesktopClockStyle(clkTheme) }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(clkLabel, color = if (isCurrent) Color.White else Color.LightGray, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                            // USER PROFILE IDENTITY MANAGEMENT (Settings menu)
                            Text(
                                text = "System Operator Identity",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Modify active profile credentials saved locally on this client node.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            var internalUserName by remember(userName) { mutableStateOf(userName) }
                            OutlinedTextField(
                                value = internalUserName,
                                onValueChange = {
                                    internalUserName = it
                                    if (it.isNotBlank()) viewModel.updateProfile(it, profilePassword)
                                },
                                label = { Text("Operator Username Signature", color = Color.Gray) },
                                textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.1f)
                                )
                            )

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                            // --- DIAGNOSTIC TELEMETRY EVENT LOG TERMINAL HUD ---
                            Text(
                                text = "Active Systems Diagnostics Terminal",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Monitors live workspace events, overclocks, toggles, and memory terminals.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black)
                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("SHELL@NODE_01:~", fontSize = 9.sp, color = Color(0xFF10B981), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                }

                                Divider(color = Color(0xFF10B981).copy(alpha = 0.15f))

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(top = 4.dp),
                                    reverseLayout = true
                                ) {
                                    items(appLogs.reversed()) { logText ->
                                        Text(
                                            text = "> $logText",
                                            fontSize = 9.sp,
                                            color = Color(0xFF34D399),
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    if (appLogs.isEmpty()) {
                                        item {
                                            Text("> No telemetry logging events received yet. Nominals.", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                            // App Hiding checkbox management
                            Text("Launcher Apps Manager", color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Instantly hide/unhide target system utilities from desktop widgets & Launchpad flows.", color = Color.Gray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))

                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .padding(8.dp)
                            ) {
                                val allToggableApps = listOf(
                                    SubApp.NOTES_PAD to "Interactive Notes Pad",
                                    SubApp.CALCULATOR to "Scientific Calculator",
                                    SubApp.MEDIA_VIEWER to "Media Library Viewer",
                                    SubApp.AUDIO_LISTENER to "Wave Synthesizer Player",
                                    SubApp.ANDROID_APPS to "App drawer Android",
                                    SubApp.DIALER to "Phone Dialer simulation",
                                    SubApp.MESSAGES to "Secure Local messaging chat",
                                    SubApp.CONTACTS to "Contacts directory list",
                                    SubApp.TASK_MANAGER to "Performance Core monitors",
                                    SubApp.ADB_WIRELESS to "Wireless Debugging utilities",
                                    SubApp.AI_ASSISTANT to "Gemini AI interactive terminal",
                                    SubApp.PHONE_SERVER to "Node client API server host",
                                    SubApp.OFFLINE_TOOLS to "Offline system toolboxes"
                                )

                                allToggableApps.forEach { (subApp, label) ->
                                    val isHidden = hiddenApps.contains(subApp.name)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.toggleAppVisibility(subApp.name) }
                                            .padding(vertical = 4.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(label, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                                        Checkbox(
                                            checked = !isHidden,
                                            onCheckedChange = { viewModel.toggleAppVisibility(subApp.name) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFF6366F1),
                                                uncheckedColor = Color.Gray
                                            )
                                        )
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                            // --- DANGER CRITICAL RESET CONSOLE ACTION ---
                            var showResetConfirmOption by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "CRITICAL COLD RESET SEGMENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFCA5A5),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Cleans SharedPreferences, cache keys, pins lists, enablements and reverts visual overclocks to standard templates.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = { showResetConfirmOption = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("RESET WORKSPACE INSTANTLY", style = MaterialTheme.typography.labelSmall, color = Color.White, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }

                            if (showResetConfirmOption) {
                                AlertDialog(
                                    onDismissRequest = { showResetConfirmOption = false },
                                    title = { Text("Wipe Settings Cache?", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
                                    text = { Text("This will revert all widget selections, clock themes, app pins, roundness styles, and the active system operator username signature to original defaults. Continue?", color = Color.LightGray) },
                                    containerColor = Color(0xFF1E1B4B),
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                viewModel.resetWorkspaceSettings()
                                                showResetConfirmOption = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                        ) {
                                            Text("YES, WIPE", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showResetConfirmOption = false }) {
                                            Text("CANCEL", color = Color.Gray)
                                        }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    } // Closes else block for normal layout
    }
}

data class LauncherShortcutItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun DesktopClockWidget(selectedStyle: String) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US).format(Date())
    
    when (selectedStyle) {
        "digital_glow" -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Text(
                    text = timeFormat,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.W900,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = dateFormat,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
        "cyber_terminal" -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, Color(0xFF34D399), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "> [SYS_CLKINFO: ACTIVE_UPTIME]",
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF34D399),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SYS_TIME: $timeFormat",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF10B981),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "SYS_DATE: ${dateFormat.uppercase()}",
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF6EE7B7),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        "led_retro" -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2C1616)) // Deep dark reddish led display back
                    .border(2.dp, Color(0xFF452B2B), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timeFormat,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEF4444), // Glowy Red LED
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "- SECURE CORE NODE SYSTEM -",
                    color = Color(0xFF991B1B),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        else -> { // "none" or default/minimal
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = timeFormat,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DesktopShortcutIcon(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    iconSizeScale: String = "normal",
    onClick: () -> Unit
) {
    val boxSize = when (iconSizeScale) {
        "compact" -> 40.dp
        "large" -> 60.dp
        else -> 50.dp
    }
    val iconSize = when (iconSizeScale) {
        "compact" -> 20.dp
        "large" -> 28.dp
        else -> 24.dp
    }
    val fontSize = when (iconSizeScale) {
        "compact" -> 10.sp
        "large" -> 13.sp
        else -> 12.sp
    }
    val spacingHeight = when (iconSizeScale) {
        "compact" -> 4.dp
        "large" -> 8.dp
        else -> 6.dp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(boxSize)
                .clip(RoundedCornerShape(14.dp))
                .background(backgroundColor)
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
        Spacer(modifier = Modifier.height(spacingHeight))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = fontSize),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun WallpaperThumbCircle(
    themeKey: String,
    gradient: Brush,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(gradient)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color.White else Color.DarkGray,
                shape = CircleShape
            )
    )
}

// ==========================================
// USER PROFILE & PASSWORD LOCK DASHBOARD (USER REQUEST 1)
// ==========================================
@Composable
fun ProfileDashboardSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val currentUserName by viewModel.profileName.collectAsState()
    val currentPassword by viewModel.profilePassword.collectAsState()

    var editedName by remember { mutableStateOf(currentUserName) }
    var editedPassword by remember { mutableStateOf(currentPassword) }
    var showPasswordPlain by remember { mutableStateOf(false) }

    var saveConfirmationMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_btn")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to Workspace Launcher")
            }
            Text(
                text = "Dashboard & Profile",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Box(modifier = Modifier.size(48.dp)) // Spacer target
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Profile Display Badge
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentUserName.take(2).uppercase(),
                fontSize = 32.sp,
                fontWeight = FontWeight.W900,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Operator identity token",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Field (Always allowed)
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Operator profile Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name Icon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Password Field Check (Secure, never email!)
                OutlinedTextField(
                    value = editedPassword,
                    onValueChange = { editedPassword = it },
                    label = { Text("Dashboard Password lock code") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                    trailingIcon = {
                        IconButton(onClick = { showPasswordPlain = !showPasswordPlain }) {
                            Icon(
                                imageVector = if (showPasswordPlain) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password view"
                            )
                        }
                    },
                    visualTransformation = if (showPasswordPlain) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // High Contrast warning explaining why email is missing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = "Security Note Icon",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "OFFLINE SECURITY INSTRUCTION: Email bindings are strictly disabled to protect telemetry logs from cloud leakage.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = {
                        if (editedName.isNotBlank() && editedPassword.isNotBlank()) {
                            viewModel.updateProfile(editedName, editedPassword)
                            saveConfirmationMessage = "Identity profile saved securely to SharedPreferences!"
                        } else {
                            saveConfirmationMessage = "Fields cannot be blank."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_profile_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }

                if (saveConfirmationMessage.isNotEmpty()) {
                    Text(
                        text = saveConfirmationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (saveConfirmationMessage.contains("SharedPreferences")) Color(0xFF81C784) else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// =========================================================================
// TELEMETRY & SYSTEM WIDGET COMPOSABLES FOR INTERACTIVE HOME & HUDS
// =========================================================================

@Composable
fun BatteryTelemetryCard(style: String, viewModel: MainViewModel) {
    val level = 84
    val temperature = "29.4°C"
    val status = "CHARGING (SECURE)"
    val outlineColor = when (style) {
        "glass" -> Color.White.copy(alpha = 0.15f)
        "solid" -> Color.Transparent
        else -> Color(0xFFF59E0B).copy(alpha = 0.4f)
    }
    val containerBg = when (style) {
        "glass" -> Color.Black.copy(alpha = 0.35f)
        "solid" -> Color(0xFF1E293B)
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = if (style != "solid") BorderStroke(1.dp, outlineColor) else null,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.BatteryChargingFull, null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    Text("BATTERY CELLS", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(status, fontSize = 8.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("NODE POWER RATIO", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                    Text("$level%", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("CELL TEMP", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                    Text(temperature, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = level / 100f,
                color = Color(0xFF10B981),
                trackColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun RamCpuPerformanceCard(style: String, viewModel: MainViewModel) {
    val outlineColor = when (style) {
        "glass" -> Color.White.copy(alpha = 0.15f)
        "solid" -> Color.Transparent
        else -> Color(0xFF3B82F6).copy(alpha = 0.4f)
    }
    val containerBg = when (style) {
        "glass" -> Color.Black.copy(alpha = 0.35f)
        "solid" -> Color(0xFF1E293B)
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = if (style != "solid") BorderStroke(1.dp, outlineColor) else null,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Memory, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                    Text("OVERCLOCK MONITORS", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Text("CORES NOMINAL", fontSize = 8.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("CPU RESOURCE LEVEL", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                    Text("42.8% active", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("ALLOTTED VOLATILE RAM", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                    Text("2.8 GB / 6.0 GB", style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = 0.428f,
                color = Color(0xFF3B82F6),
                trackColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun PersistableMemoPadCard(style: String, memoText: String, onTextSaved: (String) -> Unit) {
    val outlineColor = when (style) {
        "glass" -> Color.White.copy(alpha = 0.15f)
        "solid" -> Color.Transparent
        else -> Color(0xFFF59E0B).copy(alpha = 0.4f)
    }
    val containerBg = when (style) {
        "glass" -> Color.Black.copy(alpha = 0.35f)
        "solid" -> Color(0xFF1E293B)
        else -> Color.Transparent
    }

    var localTextState by remember(memoText) { mutableStateOf(memoText) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = if (style != "solid") BorderStroke(1.dp, outlineColor) else null,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Edit, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                    Text("SECURED CLIENT MEMOPAD", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Icon(Icons.Default.Save, "Saved Status indication", tint = Color.Gray, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = localTextState,
                onValueChange = {
                    localTextState = it
                    onTextSaved(it)
                },
                placeholder = { Text("Write secured workspace notes or ideas here...", color = Color.Gray, fontSize = 11.sp) },
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                modifier = Modifier.fillMaxWidth().heightIn(min = 70.dp, max = 130.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFF59E0B).copy(alpha = 0.4f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                    focusedContainerColor = Color.Black.copy(alpha = 0.25f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
fun HardwareTogglesCard(
    style: String,
    wifi: Boolean,
    bluetooth: Boolean,
    dnd: Boolean,
    airplane: Boolean,
    location: Boolean,
    viewModel: MainViewModel
) {
    val outlineColor = when (style) {
        "glass" -> Color.White.copy(alpha = 0.15f)
        "solid" -> Color.Transparent
        else -> Color(0xFFEC4899).copy(alpha = 0.4f)
    }
    val containerBg = when (style) {
        "glass" -> Color.Black.copy(alpha = 0.35f)
        "solid" -> Color(0xFF1E293B)
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = if (style != "solid") BorderStroke(1.dp, outlineColor) else null,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.SettingsInputAntenna, null, tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                    Text("HARDWARE SWITCHES", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Text("INTEGRATED CONNECTIVITY", fontSize = 8.sp, color = Color(0xFFEC4899), fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(12.dp))

            val toggleItems = listOf(
                ToggleItemHelper(Icons.Default.Wifi, wifi, "Wi-Fi") { viewModel.toggleWifi() },
                ToggleItemHelper(Icons.Default.Bluetooth, bluetooth, "Bluetooth") { viewModel.toggleBluetooth() },
                ToggleItemHelper(Icons.Default.DoNotDisturb, dnd, "DND") { viewModel.toggleDnd() },
                ToggleItemHelper(Icons.Default.AirplanemodeActive, airplane, "Airplane") { viewModel.toggleAirplaneMode() },
                ToggleItemHelper(Icons.Default.PinDrop, location, "Location") { viewModel.toggleLocation() }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                toggleItems.forEach { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { item.action() }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (item.value) Color(0xFFEC4899) else Color.White.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, item.label, tint = if (item.value) Color.White else Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.label, fontSize = 8.sp, color = Color.LightGray, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

data class ToggleItemHelper(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val value: Boolean,
    val label: String,
    val action: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedSearchAndSettingsPanel(
    viewModel: MainViewModel,
    searchInputText: String,
    onSearchInputTextChange: (String) -> Unit,
    searchCategoryFilter: String,
    onSearchCategoryFilterChange: (String) -> Unit,
    showCustomizationSettings: Boolean,
    onShowCustomizationSettingsChange: (Boolean) -> Unit,
    onSelectSubApp: (SubApp) -> Unit
) {
    val context = LocalContext.current
    val activeWallpaper by viewModel.desktopWallpaper.collectAsState()
    val desktopTheme by viewModel.desktopTheme.collectAsState()
    val userName by viewModel.profileName.collectAsState()
    val profilePassword by viewModel.profilePassword.collectAsState()
    val hiddenApps by viewModel.hiddenApps.collectAsState()
    val gridColumns by viewModel.desktopGridColumns.collectAsState()
    val clockStyle by viewModel.desktopClockStyle.collectAsState()
    val iconSizeScale by viewModel.desktopIconSizeScale.collectAsState()
    val wallpaperOverlay by viewModel.desktopWallpaperOverlay.collectAsState()
    val appLogs by viewModel.appLogs.collectAsState()
    val iconRoundness by viewModel.iconRoundness.collectAsState()
    val wifiEnabled by viewModel.wifiEnabled.collectAsState()
    val bluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
    val dndEnabled by viewModel.dndEnabled.collectAsState()
    val airplaneModeEnabled by viewModel.airplaneModeEnabled.collectAsState()
    val locationEnabled by viewModel.locationEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "OPTECH UNIFIED SEARCH",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = "Search across sandboxed files, local contact rosters, built-in apps, offline notes, tasks, and calendar events in real-time.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Unified Search Bar input
        OutlinedTextField(
            value = searchInputText,
            onValueChange = onSearchInputTextChange,
            placeholder = { Text("Search files, folders, contacts, apps...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (searchInputText.isNotEmpty()) {
                    IconButton(onClick = { onSearchInputTextChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear input", tint = Color.LightGray)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color(0xFF312E81)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal category filters for search
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "FILES", "CONTACTS", "APPS", "NOTES", "TASKS", "EVENTS").forEach { filter ->
                val isSelected = searchCategoryFilter == filter
                Surface(
                    color = if (isSelected) Color(0xFF10B981) else Color(0xFF312E81).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.clickable { onSearchCategoryFilterChange(filter) }
                ) {
                    Text(
                        text = filter,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // REAL-TIME SEARCH MATCH ENGINE
        val workspaceRoot = remember { File(context.filesDir, "workspace") }
        val filesInWorkspace = remember(searchInputText) {
            if (workspaceRoot.exists()) {
                try {
                    workspaceRoot.walkTopDown().maxDepth(6).map { file ->
                        val relPath = file.relativeTo(workspaceRoot).path
                        WorkspaceFile(
                            name = file.name,
                            isDirectory = file.isDirectory,
                            size = if (file.isDirectory) 0 else file.length(),
                            lastModified = file.lastModified(),
                            relativePath = relPath,
                            absoluteFile = file
                        )
                    }.filter { it.name.isNotBlank() && it.name != "workspace" }.toList()
                } catch (e: Exception) {
                    emptyList()
                }
            } else emptyList()
        }

        val filteredFiles = remember(searchInputText, searchCategoryFilter, filesInWorkspace) {
            if (searchInputText.length >= 2 && (searchCategoryFilter == "ALL" || searchCategoryFilter == "FILES")) {
                filesInWorkspace.filter {
                    it.name.contains(searchInputText, ignoreCase = true) ||
                    it.relativePath.contains(searchInputText, ignoreCase = true)
                }
            } else emptyList()
        }

        val allContactsVal by viewModel.allContacts.collectAsState()
        val filteredContacts = remember(searchInputText, searchCategoryFilter, allContactsVal) {
            if (searchInputText.length >= 2 && (searchCategoryFilter == "ALL" || searchCategoryFilter == "CONTACTS")) {
                allContactsVal.filter {
                    it.name.contains(searchInputText, ignoreCase = true) ||
                    it.phoneNumber.contains(searchInputText) ||
                    it.email.contains(searchInputText, ignoreCase = true)
                }
            } else emptyList()
        }

        val androidAppsVal by viewModel.androidApps.collectAsState()
        val filteredAndroidApps = remember(searchInputText, searchCategoryFilter, androidAppsVal) {
            if (searchInputText.length >= 1 && (searchCategoryFilter == "ALL" || searchCategoryFilter == "APPS")) {
                androidAppsVal.filter {
                    it.label.contains(searchInputText, ignoreCase = true) ||
                    it.packageName.contains(searchInputText, ignoreCase = true)
                }
            } else emptyList()
        }

        val filteredSuiteApps = remember(searchInputText, searchCategoryFilter) {
            if (searchInputText.length >= 1 && (searchCategoryFilter == "ALL" || searchCategoryFilter == "APPS")) {
                SubApp.values().filter {
                    it.name.contains(searchInputText, ignoreCase = true) &&
                    it != SubApp.HOME_SCREEN &&
                    it != SubApp.LAUNCHPAD &&
                    it != SubApp.PROFILE_DASHBOARD
                }
            } else emptyList()
        }

        val allNotesVal by viewModel.allNotes.collectAsState()
        val filteredNotes = remember(searchInputText, searchCategoryFilter, allNotesVal) {
            if (searchInputText.length >= 2 && (searchCategoryFilter == "ALL" || searchCategoryFilter == "NOTES")) {
                allNotesVal.filter {
                    it.title.contains(searchInputText, ignoreCase = true) ||
                    it.content.contains(searchInputText, ignoreCase = true)
                }
            } else emptyList()
        }

        val allTasksVal by viewModel.allTasks.collectAsState()
        val filteredTasks = remember(searchInputText, searchCategoryFilter, allTasksVal) {
            if (searchInputText.length >= 2 && (searchCategoryFilter == "ALL" || searchCategoryFilter == "TASKS")) {
                allTasksVal.filter {
                    it.title.contains(searchInputText, ignoreCase = true)
                }
            } else emptyList()
        }

        val allEventsVal by viewModel.activeMonthEvents.collectAsState()
        val filteredEvents = remember(searchInputText, searchCategoryFilter, allEventsVal) {
            if (searchInputText.length >= 2 && (searchCategoryFilter == "ALL" || searchCategoryFilter == "EVENTS")) {
                allEventsVal.filter {
                    it.title.contains(searchInputText, ignoreCase = true) ||
                    it.description.contains(searchInputText, ignoreCase = true)
                }
            } else emptyList()
        }

        val totalResults = filteredFiles.size + filteredContacts.size + 
                           filteredAndroidApps.size + filteredSuiteApps.size + 
                           filteredNotes.size + filteredTasks.size + filteredEvents.size

        // Search Output List Render Cards
        if (searchInputText.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FindInPage, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(42.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Type 2+ characters to query the node", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        } else if (totalResults == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOff, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(42.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No matches found for '$searchInputText'", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Text(
                text = "$totalResults SEARCH RESULTS FOUND",
                color = Color(0xFF10B981),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Files & Folders matches
                filteredFiles.forEach { file ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp),
                        onClick = { viewModel.navigateTo(MainViewModel.ToolSection.FILE_MANAGER) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(file.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = if (file.isDirectory) "Folder // workspace/${file.relativePath}" else "File // ${file.size / 1024} KB // workspace/${file.relativePath}",
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // 2. Contacts matches
                filteredContacts.forEach { contact ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp),
                        onClick = { 
                            onSelectSubApp(SubApp.DIALER)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ContactPage,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contact.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "Phone: ${contact.phoneNumber} // Email: ${contact.email}",
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(Icons.Default.Call, contentDescription = "Call contact", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // 3. Suite Apps matches
                filteredSuiteApps.forEach { subApp ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp),
                        onClick = { onSelectSubApp(subApp) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Launch,
                                contentDescription = null,
                                tint = Color(0xFFEC4899),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(subApp.name.replace("_", " "), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "Suite Utility App // Tap to launch instantly",
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // 4. Android System Apps matches
                filteredAndroidApps.forEach { app ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp),
                        onClick = {
                            try {
                                val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                if (intent != null) context.startActivity(intent)
                            } catch (e: Exception) {}
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Android,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "Android System App // Pkg: ${app.packageName}",
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(Icons.Default.PlayArrow, contentDescription = "Launch Android app", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // 5. Notes matches
                filteredNotes.forEach { note ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp),
                        onClick = { onSelectSubApp(SubApp.NOTES_PAD) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Note,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(note.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "Note // Content: ${note.content.take(60)}...",
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // 6. Tasks matches
                filteredTasks.forEach { task ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp),
                        onClick = { onSelectSubApp(SubApp.TASK_MANAGER) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PlaylistAddCheck,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(task.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "Todo Task // Status: ${if (task.isCompleted) "COMPLETED" else "ACTIVE"}",
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // 7. Calendar Events matches
                filteredEvents.forEach { event ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp),
                        onClick = { viewModel.navigateTo(MainViewModel.ToolSection.CALENDAR) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "Calendar Event // Date: ${event.dateString} // Detail: ${event.description}",
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color.DarkGray)

        // COLLAPSED DESKTOP CUSTOMIZATION & SETTINGS DRAW PANEL
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().testTag("customizer_setting_expander")
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .clickable { onShowCustomizationSettingsChange(!showCustomizationSettings) }
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF6366F1))
                        Text(
                            text = "DESKTOP SETTINGS",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = if (showCustomizationSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                if (showCustomizationSettings) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp).padding(bottom = 16.dp)
                    ) {
                        // --- DESKTOP LAYOUT THEME ---
                        Text(
                            text = "Desktop Layout Theme",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select dynamic aesthetic style grids or import an external theme profile.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "normal" to "Normal Theme",
                                "metro" to "Metro WP10",
                                "search" to "Minimal Search"
                            ).forEach { (themeKey, label) ->
                                val isSelected = desktopTheme == themeKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(10.dp))
                                        .clickable { viewModel.setDesktopTheme(themeKey) }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val themeImporterLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri: android.net.Uri? ->
                            if (uri != null) {
                                try {
                                    context.contentResolver.openInputStream(uri)?.use { stream ->
                                        val tContent = stream.bufferedReader().use { it.readText() }
                                        val msg = viewModel.importOpTheme(tContent)
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: java.lang.Exception) {
                                    Toast.makeText(context, "Theme import failed: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        Button(
                            onClick = { themeImporterLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("IMPORT .OPTHEME THEME FILE", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Wallpaper Presets
                        Text(
                            text = "Desktop Wallpaper presets",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            WallpaperThumbCircle("aurora_glow", Brush.radialGradient(listOf(Color(0xFF0F172A), Color(0xFF020617))), activeWallpaper == "aurora_glow") { viewModel.setDesktopWallpaper("aurora_glow") }
                            WallpaperThumbCircle("cosmic_void", Brush.radialGradient(listOf(Color(0xFF2E1065), Color(0xFF030712))), activeWallpaper == "cosmic_void") { viewModel.setDesktopWallpaper("cosmic_void") }
                            WallpaperThumbCircle("sunset_sky", Brush.linearGradient(listOf(Color(0xFF312E81), Color(0xFF9D174D))), activeWallpaper == "sunset_sky") { viewModel.setDesktopWallpaper("sunset_sky") }
                            WallpaperThumbCircle("emerald_peak", Brush.linearGradient(listOf(Color(0xFF064E3B), Color(0xFF111827))), activeWallpaper == "emerald_peak") { viewModel.setDesktopWallpaper("emerald_peak") }
                            WallpaperThumbCircle("neon_twilight", Brush.linearGradient(listOf(Color(0xFF1E1B4B), Color(0xFF581C87))), activeWallpaper == "neon_twilight") { viewModel.setDesktopWallpaper("neon_twilight") }
                            WallpaperThumbCircle("ocean_deep", Brush.radialGradient(listOf(Color(0xFF172554), Color(0xFF0B1329))), activeWallpaper == "ocean_deep") { viewModel.setDesktopWallpaper("ocean_deep") }
                        }

                        // Custom wallpaper selector
                        Text(
                            text = "Or use your own wallpaper",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val customWallpaperFile = remember(activeWallpaper) { File(context.filesDir, "custom_wallpaper.jpg") }
                            val customWallpaperExists = customWallpaperFile.exists()

                            val wallpaperGalleryLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.GetContent()
                            ) { uri: android.net.Uri? ->
                                if (uri != null) {
                                    try {
                                        context.contentResolver.openInputStream(uri)?.use { input ->
                                            val dstFile = File(context.filesDir, "custom_wallpaper.jpg")
                                            dstFile.outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                            viewModel.setDesktopWallpaper("custom_wallpaper")
                                            viewModel.addAppLog("Custom wallpaper loaded from storage.")
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error reading image: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }

                            Button(
                                onClick = { wallpaperGalleryLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("UPLOAD IMAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }

                            if (customWallpaperExists) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, if (activeWallpaper == "custom_wallpaper") Color.Green else Color.DarkGray, CircleShape)
                                        .clickable { viewModel.setDesktopWallpaper("custom_wallpaper") }
                                ) {
                                    AsyncImage(
                                        model = customWallpaperFile,
                                        contentDescription = "Custom preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.setDesktopWallpaper("aurora_glow")
                                        try {
                                            customWallpaperFile.delete()
                                            viewModel.addAppLog("Cleared custom wallpaper.")
                                        } catch (e: Exception) {}
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("DELETE", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.White)
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                        // Dim Overlay slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Wallpaper overlay shading",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(wallpaperOverlay * 100).toInt()}% Shade",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = wallpaperOverlay,
                            onValueChange = { viewModel.setDesktopWallpaperOverlay(it) },
                            valueRange = 0f..0.85f,
                            steps = 17,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF6366F1),
                                activeTrackColor = Color(0xFF6366F1),
                                inactiveTrackColor = Color.DarkGray
                            )
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                        // Mini config details of corners, sizes, grid system columns
                        Text(
                            text = "App Desktop layout scale",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "compact" to "Compact Grid",
                                "normal" to "Nominal Scale",
                                "large" to "Enlarged Panel"
                            ).forEach { (scaleKey, label) ->
                                val isSelected = iconSizeScale == scaleKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFF6366F1) else Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(10.dp))
                                        .clickable { viewModel.setDesktopIconSizeScale(scaleKey) }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                        // Corner roundings
                        Text(
                            text = "App icon corners design",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "square" to "Square",
                                "medium" to "Medium",
                                "soft" to "Soft corner",
                                "circle" to "Full Circle"
                            ).forEach { (roundKey, label) ->
                                val isSelected = iconRoundness == roundKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(10.dp))
                                        .clickable { viewModel.setIconRoundness(roundKey) }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else Color.LightGray,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                        // Grid columns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Desktop shortcuts columns",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { if (gridColumns > 3) viewModel.setDesktopGridColumns(gridColumns - 1) },
                                    enabled = gridColumns > 3,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                ) {
                                    Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "$gridColumns Cols",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                IconButton(
                                    onClick = { if (gridColumns < 6) viewModel.setDesktopGridColumns(gridColumns + 1) },
                                    enabled = gridColumns < 6,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                ) {
                                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                        // Clock theme style
                        Text(
                            text = "Clock Style Layout Theme",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "digital_glow" to "Digital Neon Glow",
                                "cyber_terminal" to "Cyber Monotty",
                                "led_retro" to "LED Industrial",
                                "none" to "Minimal modern"
                            ).forEach { (clkTheme, clkLabel) ->
                                val isCurrent = clockStyle == clkTheme
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isCurrent) Color(0xFFEC4899) else Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, if (isCurrent) Color.White else Color.Transparent, RoundedCornerShape(10.dp))
                                        .clickable { viewModel.setDesktopClockStyle(clkTheme) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(clkLabel, color = if (isCurrent) Color.White else Color.LightGray, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                        // Systems profile identity name
                        Text(
                            text = "System Operator Identity",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Modify active profile credentials saved locally on this client node.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        var internalUserName by remember(userName) { mutableStateOf(userName) }
                        OutlinedTextField(
                            value = internalUserName,
                            onValueChange = {
                                internalUserName = it
                                if (it.isNotBlank()) viewModel.updateProfile(it, profilePassword)
                            },
                            label = { Text("Operator Username Signature", color = Color.Gray) },
                            textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color.DarkGray,
                                focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.1f)
                            )
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                        // Diagnostics logging box
                        Text(
                            text = "Active Systems Diagnostics Terminal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Monitors live workspace events, overclocks, toggles, and memory terminals.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black)
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MainDiagnosticsShellHeaderLabelOption()
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                            }

                            Divider(color = Color(0xFF10B981).copy(alpha = 0.15f))

                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(top = 4.dp),
                                reverseLayout = true
                            ) {
                                items(appLogs.reversed()) { logText ->
                                    Text(
                                        text = "> $logText",
                                        fontSize = 9.sp,
                                        color = Color(0xFF34D399),
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                if (appLogs.isEmpty()) {
                                    item {
                                        Text("> No telemetry logging events received yet. Nominals.", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                        // Hide app toggler
                        Text("Launcher Apps Manager", color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Instantly hide/unhide target system utilities from desktop widgets & Launchpad flows.", color = Color.Gray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))

                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.3f))
                                .padding(8.dp)
                        ) {
                            val allToggableApps = listOf(
                                SubApp.NOTES_PAD to "Interactive Notes Pad",
                                SubApp.CALCULATOR to "Scientific Calculator",
                                SubApp.MEDIA_VIEWER to "Media Library Viewer",
                                SubApp.AUDIO_LISTENER to "Wave Synthesizer Player",
                                SubApp.ANDROID_APPS to "App drawer Android",
                                SubApp.DIALER to "Phone Dialer simulation",
                                SubApp.MESSAGES to "Secure Local messaging chat",
                                SubApp.CONTACTS to "Contacts directory list",
                                SubApp.TASK_MANAGER to "Performance Core monitors",
                                SubApp.ADB_WIRELESS to "Wireless Debugging utilities",
                                SubApp.AI_ASSISTANT to "Gemini AI interactive terminal",
                                SubApp.PHONE_SERVER to "Node client API server host",
                                SubApp.OFFLINE_TOOLS to "Offline system toolboxes",
                                SubApp.GAME_CENTER to "Game Center arcade hub"
                            )

                            allToggableApps.forEach { (subApp, label) ->
                                val isHidden = hiddenApps.contains(subApp.name)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleAppVisibility(subApp.name) }
                                        .padding(vertical = 4.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(label, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                                    Checkbox(
                                        checked = !isHidden,
                                        onCheckedChange = { viewModel.toggleAppVisibility(subApp.name) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF6366F1),
                                            uncheckedColor = Color.Gray
                                        )
                                    )
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                        // Danger zone cold reset
                        var showResetConfirmOption by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "CRITICAL COLD RESET SEGMENT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Cleans SharedPreferences, cache keys, pins lists, enablements and reverts visual overclocks to standard templates.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { showResetConfirmOption = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("RESET WORKSPACE INSTANTLY", style = MaterialTheme.typography.labelSmall, color = Color.White, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        if (showResetConfirmOption) {
                            AlertDialog(
                                onDismissRequest = { showResetConfirmOption = false },
                                title = { Text("Wipe Settings Cache?", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
                                text = { Text("This will revert all widget selections, clock themes, app pins, roundness styles, and the active system operator username signature to original defaults. Continue?", color = Color.LightGray) },
                                containerColor = Color(0xFF1E1B4B),
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.resetWorkspaceSettings()
                                            showResetConfirmOption = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                    ) {
                                        Text("YES, WIPE", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showResetConfirmOption = false }) {
                                        Text("CANCEL", color = Color.Gray)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MainDiagnosticsShellHeaderLabelOption() {
    Text("SHELL@NODE_01:~", fontSize = 9.sp, color = Color(0xFF10B981), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
}

@Composable
fun MetroThemeLayout(
    viewModel: MainViewModel,
    onSelectSubApp: (SubApp) -> Unit
) {
    val context = LocalContext.current
    val quickMemoText by viewModel.quickMemoText.collectAsState()
    val realWeather by viewModel.realWeather.collectAsState()
    val hiddenApps by viewModel.hiddenApps.collectAsState()
    
    val cal = Calendar.getInstance()
    val todayDay = cal.get(Calendar.DAY_OF_MONTH).toString()
    val todayMonth = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)?.uppercase() ?: "JUN"

    var showCustSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Metro Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Start",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Light,
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
            IconButton(onClick = { showCustSettings = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }

        // Live Tile 1: Weather (Wide tile)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFF0078D7))
                .clickable { viewModel.navigateTo(MainViewModel.ToolSection.CALENDAR) }
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Text(
                        text = "${realWeather?.temperature ?: 22}°C",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = realWeather?.description?.uppercase() ?: "SUNNY OUTLOOK",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "WEATHER • ${realWeather?.cityName?.uppercase() ?: "NEW YORK"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Tiles Row 2: Files & Browser (Medium Squares)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1.5f)
                    .background(Color(0xFF107C41))
                    .clickable { viewModel.navigateTo(MainViewModel.ToolSection.FILE_MANAGER) }
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Text("Files Manager", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1.5f)
                    .background(Color(0xFF008272))
                    .clickable { viewModel.navigateTo(MainViewModel.ToolSection.BROWSER) }
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Text("Chromium", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Live Tile 3: Memo Live Board (Wide Tile)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFF744DA9))
                .clickable { showCustSettings = true }
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.Default.PushPin, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Text("QUICK MEMO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
                }
                Text(
                    text = quickMemoText.ifEmpty { "Tap to configure quick memo clip details." },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    maxLines = 2,
                    fontFamily = FontFamily.Monospace,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Tiles Row 4: Terminal & Calendar (Medium Squares)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1.5f)
                    .background(Color(0xFF00188F))
                    .clickable { viewModel.navigateTo(MainViewModel.ToolSection.TERMINAL) }
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Text("Terminal", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1.5f)
                    .background(Color(0xFFD13438))
                    .clickable { viewModel.navigateTo(MainViewModel.ToolSection.CALENDAR) }
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = todayDay,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Calendar ($todayMonth)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Row 5: Notes & Calculator (Medium Squares)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!hiddenApps.contains(SubApp.NOTES_PAD.name)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.5f)
                        .background(Color(0xFF002050))
                        .clickable { onSelectSubApp(SubApp.NOTES_PAD) }
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Icon(Icons.Default.NoteAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Text("Notes Pad", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            if (!hiddenApps.contains(SubApp.CALCULATOR.name)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.5f)
                        .background(Color(0xFF512BD4))
                        .clickable { onSelectSubApp(SubApp.CALCULATOR) }
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Text("Calculator", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Footer customization setup
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { showCustSettings = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Style, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Customize Theme Layout Specs", color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showCustSettings) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showCustSettings = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    UnifiedThemeCustomizerSettingsSection(viewModel = viewModel, onDismiss = { showCustSettings = false })
                }
            }
        }
    }
}

@Composable
fun SearchThemeLayout(
    viewModel: MainViewModel,
    onSelectSubApp: (SubApp) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    var searchInputText by remember { mutableStateOf("") }
    
    val allNotes by viewModel.allNotes.collectAsState()
    val allContacts by viewModel.allContacts.collectAsState()
    val hiddenApps by viewModel.hiddenApps.collectAsState()

    var showCustSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Center Digital Clock
        DesktopClockWidget(selectedStyle = "terminal")

        Spacer(modifier = Modifier.height(30.dp))

        // Main customized search bar
        OutlinedTextField(
            value = searchInputText,
            onValueChange = { searchInputText = it },
            placeholder = { Text("Search files, contacts, apps, notes...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray) },
            trailingIcon = {
                if (searchInputText.isNotEmpty()) {
                    IconButton(onClick = { searchInputText = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.White)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedContainerColor = Color.Black.copy(alpha = 0.4f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.25f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Real-Time Search Results
        if (searchInputText.isNotEmpty()) {
            val q = searchInputText.lowercase()

            // 1. Match Apps
            val matchedApps = remember(q, hiddenApps) {
                listOf(
                    "Files" to { viewModel.navigateTo(MainViewModel.ToolSection.FILE_MANAGER) },
                    "Chromium Browser" to { viewModel.navigateTo(MainViewModel.ToolSection.BROWSER) },
                    "Terminal Emulator" to { viewModel.navigateTo(MainViewModel.ToolSection.TERMINAL) },
                    "Calendar Telemetry" to { viewModel.navigateTo(MainViewModel.ToolSection.CALENDAR) },
                    "Notes Pad" to { onSelectSubApp(SubApp.NOTES_PAD) },
                    "Calculator" to { onSelectSubApp(SubApp.CALCULATOR) }
                ).filter { it.first.lowercase().contains(q) }
            }

            // 2. Match Contacts
            val matchedContacts = remember(q, allContacts) {
                allContacts.filter { it.name.lowercase().contains(q) || it.phoneNumber.contains(q) }
            }

            // 3. Match Notes
            val matchedNotes = remember(q, allNotes) {
                allNotes.filter { it.title.lowercase().contains(q) || it.content.lowercase().contains(q) }
            }

            // 4. Match Files in Sandbox Workspace
            var matchedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
            LaunchedEffect(q) {
                val root = viewModel.fileHelper.resolveRelativePath("")
                val matches = root.walkTopDown()
                    .filter { it.isFile && it.name.lowercase().contains(q) }
                    .take(15)
                    .toList()
                matchedFiles = matches
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Apps header
                if (matchedApps.isNotEmpty()) {
                    Text("APPLICATIONS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    matchedApps.forEach { (name, click) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable { click() }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Apps, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text(name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Contacts header
                if (matchedContacts.isNotEmpty()) {
                    Text("CONTACTS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                    matchedContacts.forEach { contact ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable { onSelectSubApp(SubApp.CONTACTS) }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(contact.name, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("${contact.phoneNumber} (${contact.category})", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Notes header
                if (matchedNotes.isNotEmpty()) {
                    Text("SAVED NOTES", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEC4899), fontWeight = FontWeight.Bold)
                    matchedNotes.forEach { note ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable { onSelectSubApp(SubApp.NOTES_PAD) }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(note.title, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(note.content, color = Color.LightGray, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }

                // Files header
                if (matchedFiles.isNotEmpty()) {
                    Text("SANDBOX FILES", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
                    matchedFiles.forEach { file ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable { viewModel.navigateTo(MainViewModel.ToolSection.FILE_MANAGER) }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                Text(file.name, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        } else {
            // Recommendation pins
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "FREQUENT SHORTCUTS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    "Files" to Icons.Default.FolderOpen,
                    "Browser" to Icons.Default.Language,
                    "Terminal" to Icons.Default.Terminal
                ).forEach { (lbl, icon) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .clickable {
                                when(lbl) {
                                    "Files" -> viewModel.navigateTo(MainViewModel.ToolSection.FILE_MANAGER)
                                    "Browser" -> viewModel.navigateTo(MainViewModel.ToolSection.BROWSER)
                                    "Terminal" -> viewModel.navigateTo(MainViewModel.ToolSection.TERMINAL)
                                }
                            }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text(lbl, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(50.dp))
            Button(
                onClick = { showCustSettings = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Style, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Customize Search Widget Layout Theme", color = Color.White)
            }
        }
    }

    if (showCustSettings) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showCustSettings = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    UnifiedThemeCustomizerSettingsSection(viewModel = viewModel, onDismiss = { showCustSettings = false })
                }
            }
        }
    }
}

@Composable
fun UnifiedThemeCustomizerSettingsSection(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activeWallpaper by viewModel.desktopWallpaper.collectAsState()
    val wallpaperOverlay by viewModel.desktopWallpaperOverlay.collectAsState()
    val desktopTheme by viewModel.desktopTheme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "COSMIC THEMING & PRESETS",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Layout Theme selector
        Text("Desktop Layout Theme", color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "normal" to "Normal",
                "metro" to "Metro WP10",
                "search" to "Search Hub"
            ).forEach { (themeKey, label) ->
                val isSelected = desktopTheme == themeKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.05f))
                        .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setDesktopTheme(themeKey) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = if (isSelected) Color.White else Color.Gray, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // .optheme Importer
        val opthemeFileLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: android.net.Uri? ->
            if (uri != null) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val fileContent = stream.bufferedReader().use { it.readText() }
                        val resultMessage = viewModel.importOpTheme(fileContent)
                        Toast.makeText(context, resultMessage, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error importing: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        Button(
            onClick = { opthemeFileLauncher.launch("*/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("IMPORT CUSTOM .OPTHEME FILE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Wallpaper Presets", color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WallpaperThumbCircle("aurora_glow", Brush.radialGradient(listOf(Color(0xFF0F172A), Color(0xFF020617))), activeWallpaper == "aurora_glow") { viewModel.setDesktopWallpaper("aurora_glow") }
            WallpaperThumbCircle("cosmic_void", Brush.radialGradient(listOf(Color(0xFF2E1065), Color(0xFF030712))), activeWallpaper == "cosmic_void") { viewModel.setDesktopWallpaper("cosmic_void") }
            WallpaperThumbCircle("sunset_sky", Brush.linearGradient(listOf(Color(0xFF312E81), Color(0xFF9D174D))), activeWallpaper == "sunset_sky") { viewModel.setDesktopWallpaper("sunset_sky") }
            WallpaperThumbCircle("emerald_peak", Brush.linearGradient(listOf(Color(0xFF064E3B), Color(0xFF111827))), activeWallpaper == "emerald_peak") { viewModel.setDesktopWallpaper("emerald_peak") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Wallpaper Overlay Shader", color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Slider(
            value = wallpaperOverlay,
            onValueChange = { viewModel.setDesktopWallpaperOverlay(it) },
            valueRange = 0f..0.85f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF10B981),
                activeTrackColor = Color(0xFF10B981)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("DONE & CLOSE")
        }
    }
}

