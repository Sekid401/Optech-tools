package com.example.ui.screens

import java.util.Locale
import java.util.Date
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdbWirelessSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isPairingFinished by remember { mutableStateOf(false) }
    var logcatActive by remember { mutableStateOf(false) }

    // Pairing states
    var ipInput by remember { mutableStateOf("192.168.1.100") }
    var portInput by remember { mutableStateOf("38503") }
    var pairCodeInput by remember { mutableStateOf("512803") }

    // Mock ADB terminal log list
    val adbConsoleLines = remember {
        mutableStateListOf(
            "omni-adb-server: daemon initialized on port 5037",
            "Waiting for device transport..."
        )
    }
    val lazyListState = rememberLazyListState()

    // Logcat generation stream background thread
    LaunchedEffect(logcatActive) {
        if (logcatActive) {
            val logTags = listOf("I/ActivityManager", "D/ViewRootImpl", "W/AudioService", "E/AndroidRuntime", "I/WindowManager", "D/PackageManager")
            val logMsgs = listOf(
                "START u0 {act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] cmp=com.example/.MainActivity}",
                "Relayout window: window=Window{1a2b3c4 u0 com.example/com.example.MainActivity}",
                "Sound pool latency high: 140ms on stream STREAM_MUSIC",
                "FATAL EXCEPTION: main. Process: com.aistudio, Pid: 28312 (simulated stack)",
                "Focus gained by Window{1a2b3c4 u0 com.example/com.example.MainActivity}",
                "Querying package manager for services matching intent act=android.media.browse"
            )
            while (logcatActive) {
                delay(1200)
                val randTag = logTags.random()
                val randMsg = logMsgs.random()
                val timeClock = java.text.SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                adbConsoleLines.add("$timeClock  $randTag: $randMsg")
                
                // Keep inside limit 200 loglines
                if (adbConsoleLines.size > 200) adbConsoleLines.removeAt(0)
            }
        }
    }

    // AutoScroll terminal console to bottom
    LaunchedEffect(adbConsoleLines.size) {
        if (adbConsoleLines.isNotEmpty()) {
            lazyListState.animateScrollToItem(adbConsoleLines.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ADB Wireless Debugging", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("adb_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Launchpad")
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
            // Scrollable Settings & Config Guideline
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
            ) {
                // Intro guideline Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Adb Wireless Tutorial Guide", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Configure ADB on your personal terminal wirelessly without any USB cords. Run commands directly over local Wi-Fi pairing parameters.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("1. Turn on Developer Options on your device", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("2. Navigate to Wireless Debugging -> Activate Toggle", fontSize = 11.sp)
                            Text("3. Choose 'Pair device with pairing code' inside android sub-settings", fontSize = 11.sp)
                        }
                    }
                }

                // Interactive Simulator Fields card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Mock Wireless Terminal Pairing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = ipInput,
                                    onValueChange = { ipInput = it },
                                    label = { Text("IP Address") },
                                    placeholder = { Text("e.g. 192.168.1.100") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.5f).testTag("adb_ip_input")
                                )
                                OutlinedTextField(
                                    value = portInput,
                                    onValueChange = { portInput = it },
                                    label = { Text("Port") },
                                    placeholder = { Text("e.g. 5555") },
                                    singleLine = true,
                                    modifier = Modifier.weight(0.8f).testTag("adb_port_input")
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = pairCodeInput,
                                onValueChange = { pairCodeInput = it },
                                label = { Text("Pairing verification code") },
                                modifier = Modifier.fillMaxWidth().testTag("adb_code_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        adbConsoleLines.clear()
                                        adbConsoleLines.add("SYSTEM: Pairing process triggered to $ipInput:$portInput...")
                                        delay(800)
                                        adbConsoleLines.add("adb pairing handshake initiated. code: $pairCodeInput")
                                        delay(1000)
                                        adbConsoleLines.add("SUCCESS: Successfully paired device with ADB transport layer!")
                                        adbConsoleLines.add("Run 'adb connect $ipInput:$portInput' to start debugging logs.")
                                        isPairingFinished = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("adb_pairing_btn")
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Execute ADB Pair Link")
                            }
                        }
                    }
                }

                // Copyable Computer CLI Instruction snippet cards
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Command lines for your computer terminal:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Snippet box 1
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF232522))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "adb pair $ipInput:$portInput $pairCodeInput",
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFC2EFAD),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Terminal Shell logs controller title
                item {
                    Text(
                        "Interactive ADB Terminal Output Feed",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // --- MAIN INTEGRATED BLACK TERMINAL CONSOLE ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color(0xFF131512))
            ) {
                // Top control status actions bar inside console header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E211A))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Terminal, contentDescription = null, tint = Color(0xFF79D98F), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "omni-adb@workstation:~",
                            color = Color.LightGray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Logcat toggle button
                    TextButton(
                        onClick = { logcatActive = !logcatActive },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = if (logcatActive) "Pause Logcat" else "Live adb logcat",
                            color = if (logcatActive) Color(0xFFEF4444) else Color(0xFF79D98F),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Shell console output logs lines
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(adbConsoleLines) { line ->
                        Text(
                            text = line,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = if (line.contains("SUCCESS") || line.contains("Successfully")) Color(0xFF79D98F) else if (line.contains("ERROR") || line.contains("FATAL")) Color(0xFFFF5F56) else Color(0xFFC2EFAD)
                            )
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFF333333), thickness = 0.5.dp)

                // Simulated Fast Action Shortcut command triggers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F110E))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            adbConsoleLines.add("> adb devices")
                            adbConsoleLines.add("List of devices attached")
                            adbConsoleLines.add("$ipInput:$portInput    device")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262924)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("adb devices", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            adbConsoleLines.add("> adb shell getprop ro.product.model")
                            adbConsoleLines.add("Pixel 9 Pro XL Wireless VM")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262924)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("adb getprop", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            adbConsoleLines.clear()
                            adbConsoleLines.add("omni-adb-server: screen buffer logs cleared")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262924)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("clear", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
