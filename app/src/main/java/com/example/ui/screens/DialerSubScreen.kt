package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecentCall
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialerSubScreen(
    viewModel: MainViewModel,
    presetPhoneNumber: String = "",
    onBack: () -> Unit
) {
    var dialInput by remember { mutableStateOf(presetPhoneNumber) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Dialer Keypad, 1 = Call History
    val recentCalls by viewModel.recentCalls.collectAsState()
    val contacts by viewModel.allContacts.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Active Call Simulation overlay state
    var isCallActive by remember { mutableStateOf(false) }
    var activeCallDuration by remember { mutableStateOf(0) }
    var activeCallContactName by remember { mutableStateOf<String?>(null) }
    var speakerOn by remember { mutableStateOf(false) }
    var muteOn by remember { mutableStateOf(false) }

    // Audio DTMF sound generator
    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (_: Exception) {
            null
        }
    }

    // Load recent calls and keep dialInput synced if preset changes
    LaunchedEffect(presetPhoneNumber) {
        if (presetPhoneNumber.isNotEmpty()) {
            dialInput = presetPhoneNumber
            selectedTab = 0
        }
    }

    // Call duration timer
    LaunchedEffect(isCallActive) {
        if (isCallActive) {
            activeCallDuration = 0
            while (isCallActive) {
                delay(1000)
                activeCallDuration++
            }
        }
    }

    fun playDtmfTone(char: Char) {
        scope.launch {
            try {
                val tone = when (char) {
                    '1' -> ToneGenerator.TONE_DTMF_1
                    '2' -> ToneGenerator.TONE_DTMF_2
                    '3' -> ToneGenerator.TONE_DTMF_3
                    '4' -> ToneGenerator.TONE_DTMF_4
                    '5' -> ToneGenerator.TONE_DTMF_5
                    '6' -> ToneGenerator.TONE_DTMF_6
                    '7' -> ToneGenerator.TONE_DTMF_7
                    '8' -> ToneGenerator.TONE_DTMF_8
                    '9' -> ToneGenerator.TONE_DTMF_9
                    '0' -> ToneGenerator.TONE_DTMF_0
                    '*' -> ToneGenerator.TONE_DTMF_S
                    '#' -> ToneGenerator.TONE_DTMF_P
                    else -> -1
                }
                if (tone != -1) {
                    toneGenerator?.startTone(tone, 120)
                }
            } catch (_: Exception) {}
        }
    }

    fun startCallSimulation(numberToCall: String) {
        if (numberToCall.isBlank()) return
        
        // Find matching contact name in database
        val matchedContact = contacts.find { it.phoneNumber.replace(" ", "").contains(numberToCall.replace(" ", "")) }
        activeCallContactName = matchedContact?.name ?: "Unknown Receiver"
        isCallActive = true
    }

    fun endCallSimulation(hangupType: String) {
        isCallActive = false
        // Insert into database recent calls
        viewModel.insertRecentCall(
            phoneNumber = dialInput,
            contactName = if (activeCallContactName == "Unknown Receiver") null else activeCallContactName,
            callType = hangupType,
            durationSeconds = activeCallDuration
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Phone Dialer & Log", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("dialer_back")) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to Launchpad")
                        }
                    },
                    actions = {
                        if (selectedTab == 1 && recentCalls.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearCallHistory() }) {
                                Text("Clear Log")
                            }
                        }
                    }
                )
            },
            bottomBar = {
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Outlined.Dialpad, contentDescription = null) },
                        text = { Text("Dialpad") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Outlined.History, contentDescription = null) },
                        text = { Text("Recents") }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (selectedTab == 0) {
                    // --- KEYPAD INTERFACE ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Phone Number input displays
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = dialInput.ifEmpty { "Enter Number" },
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = if (dialInput.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (dialInput.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Save contact",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clickable {
                                                // Quick action to trigger save intent if needed (or navigate to Contacts)
                                                // Let's implement contact save preset navigations or actions
                                            }
                                            .padding(6.dp)
                                    )
                                    Text(
                                        text = "Real SIM Call",
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clickable {
                                                // Trigger actual phone intent!
                                                try {
                                                    val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$dialInput"))
                                                    context.startActivity(callIntent)
                                                    viewModel.insertRecentCall(dialInput, null, "Outgoing", 0)
                                                } catch (_: Exception) {}
                                            }
                                            .padding(6.dp)
                                    )
                                }
                            }
                        }

                        // Dialer Rows
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val keys = listOf(
                                listOf("1" to "", "2" to "ABC", "3" to "DEF"),
                                listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
                                listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
                                listOf("*" to "", "0" to "+", "#" to "")
                            )

                            keys.forEach { row ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    row.forEach { (number, letters) ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1.2f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                                .clickable {
                                                    dialInput += number
                                                    playDtmfTone(number[0])
                                                }
                                                .testTag("dialkey_$number"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = number,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 24.sp
                                                )
                                                if (letters.isNotEmpty()) {
                                                    Text(
                                                        text = letters,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Footer calling actions row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left element: Empty or Quick Preset (like add icon)
                                Box(modifier = Modifier.size(56.dp))

                                // Main Green Dial FAB
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(if (dialInput.isEmpty()) Color.Gray else Color(0xFF4CAF50))
                                        .clickable(enabled = dialInput.isNotEmpty()) {
                                            startCallSimulation(dialInput)
                                        }
                                        .testTag("dialer_call_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Simulate Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                // Delete Keypad Input Backspace
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .clickable(enabled = dialInput.isNotEmpty()) {
                                            if (dialInput.isNotEmpty()) {
                                                dialInput = dialInput.dropLast(1)
                                            }
                                        }
                                        .testTag("dialer_backspace"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Backspace number entry",
                                        tint = if (dialInput.isNotEmpty()) MaterialTheme.colorScheme.error else Color.Gray.copy(alpha = 0.4f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // --- CALL HISTORY RECENT LOGS ---
                    if (recentCalls.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No call history found",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(recentCalls, key = { it.id }) { call ->
                                RecentCallCard(
                                    call = call,
                                    onRedial = {
                                        dialInput = call.phoneNumber
                                        selectedTab = 0
                                        startCallSimulation(call.phoneNumber)
                                    },
                                    onDelete = { viewModel.deleteRecentCall(call.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Simulated Active Call Full Screen Animation Overlay ---
        AnimatedVisibility(
            visible = isCallActive,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF131512))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Call status top info
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SIMULATED CELLULAR CALL",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C312C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = activeCallContactName ?: dialInput,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dialInput,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Format count: e.g. "00:08"
                    val minutes = activeCallDuration / 60
                    val seconds = activeCallDuration % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Wave pulse simulation graphic representation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0..7) {
                            val pulseLinesHeight = remember(activeCallDuration) { (15..65).random() }
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(pulseLinesHeight.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF79D98F))
                            )
                        }
                    }
                }

                // Active Controls buttons & Hangup button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = { speakerOn = !speakerOn },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (speakerOn) Color(0xFF2563EB) else Color.DarkGray)
                        ) {
                            Icon(
                                imageVector = if (speakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = "Speaker audio option",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = { muteOn = !muteOn },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (muteOn) Color(0xFFDC2626) else Color.DarkGray)
                        ) {
                            Icon(
                                imageVector = if (muteOn) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute mic selection",
                                tint = Color.White
                            )
                        }
                    }

                    // Hang up button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .clickable {
                                endCallSimulation("Outgoing")
                            }
                            .testTag("dialer_hangup_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CallEnd,
                            contentDescription = "Hang Up / End Call",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentCallCard(
    call: RecentCall,
    onRedial: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Outgoing / Incoming / Missed status Icon Indicators
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (call.callType) {
                            "Outgoing" -> Color(0xFFE0F2FE)
                            "Incoming" -> Color(0xFFDCFCE7)
                            else -> Color(0xFFFEE2E2)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (call.callType) {
                        "Outgoing" -> Icons.Default.CallMade
                        "Incoming" -> Icons.Default.CallReceived
                        else -> Icons.Default.CallMissed
                    },
                    contentDescription = call.callType,
                    tint = when (call.callType) {
                        "Outgoing" -> Color(0xFF0369A1)
                        "Incoming" -> Color(0xFF15803D)
                        else -> Color(0xFFB91C1C)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = call.contactName ?: call.phoneNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formattedTime = remember(call.timestamp) {
                        SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(call.timestamp))
                    }
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    if (call.durationSeconds > 0) {
                        val durationText = if (call.durationSeconds >= 60) {
                            "${call.durationSeconds / 60}m ${call.durationSeconds % 60}s"
                        } else {
                            "${call.durationSeconds}s"
                        }
                        Text(
                            text = "· $durationText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            IconButton(onClick = onRedial, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Redial Number",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete call Log",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
