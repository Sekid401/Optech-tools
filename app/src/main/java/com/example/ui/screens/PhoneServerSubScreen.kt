package com.example.ui.screens

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit

data class ServerRequestLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val method: String,
    val path: String,
    val remoteAddress: String,
    val authStatus: String, // "Success", "Failed", "None"
    val statusCode: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneServerSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isServerRunning by rememberSaveable { mutableStateOf(false) }
    var serverPort by rememberSaveable { mutableStateOf(8080) }
    var serverApiKey by rememberSaveable { mutableStateOf("") }
    
    // Generate an initial random secure API Key if empty
    LaunchedEffect(Unit) {
        if (serverApiKey.isEmpty()) {
            serverApiKey = "optech_live_sk_" + UUID.randomUUID().toString().replace("-", "").take(16)
        }
    }

    val incomingLogs = remember { mutableStateListOf<ServerRequestLog>() }
    var activeIpAddress by remember { mutableStateOf("127.0.0.1") }
    var activeServerJob by remember { mutableStateOf<Job?>(null) }
    var serverStatusMessage by remember { mutableStateOf("Server Idle. Tap Start Server to begin.") }

    // Dynamic Wi-Fi IP address retrieval
    fun getLocalIpAddress() {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (netInterface in Collections.list(interfaces)) {
                val addresses = netInterface.inetAddresses
                for (address in Collections.list(addresses)) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        activeIpAddress = address.hostAddress ?: "127.0.0.1"
                        return
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        activeIpAddress = "127.0.0.1"
    }

    // Auto update IP info
    LaunchedEffect(isServerRunning) {
        getLocalIpAddress()
    }

    // Socket server background listener thread logic
    val startSocketServer: () -> Unit = {
        isServerRunning = true
        getLocalIpAddress()
        serverStatusMessage = "Server Live! Listening on http://$activeIpAddress:$serverPort"
        
        activeServerJob = scope.launch(Dispatchers.IO) {
            var ss: ServerSocket? = null
            try {
                ss = ServerSocket(serverPort)
                while (isServerRunning) {
                    val clientSocket: Socket = try {
                        ss.accept()
                    } catch (e: Exception) {
                        break // Quit on server close
                    }

                    scope.launch(Dispatchers.IO) {
                        handleClientConnection(clientSocket, serverApiKey, incomingLogs)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    serverStatusMessage = "Error starting server: ${e.localizedMessage ?: "Port already in use"}"
                    isServerRunning = false
                }
            } finally {
                try {
                    ss?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val stopSocketServer: () -> Unit = {
        isServerRunning = false
        activeServerJob?.cancel()
        activeServerJob = null
        serverStatusMessage = "Server Stopped."
    }

    // Client request visual simulator
    val runClientSimulation: (Boolean) -> Unit = { useCorrectKey ->
        scope.launch {
            val keyToUse = if (useCorrectKey) serverApiKey else "invalid_or_wrong_key_123"
            withContext(Dispatchers.IO) {
                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(3, TimeUnit.SECONDS)
                        .readTimeout(3, TimeUnit.SECONDS)
                        .build()
                    val request = Request.Builder()
                        .url("http://127.0.0.1:$serverPort/api/status")
                        .addHeader("Authorization", "Bearer $keyToUse")
                        .build()
                    client.newCall(request).execute().use { response ->
                        response.body?.string()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phone Web Server", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        stopSocketServer()
                        onBack()
                    }, modifier = Modifier.testTag("server_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Return home")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        incomingLogs.clear()
                    }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear incoming log lists")
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Status Canvas Block
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isServerRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isServerRunning) Color(0xFF10B981) else Color(0xFF6B7280)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isServerRunning) Icons.Default.Dns else Icons.Default.PortableWifiOff,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isServerRunning) "HTTP API Server Online" else "Server Offline",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isServerRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = serverStatusMessage,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = if (isServerRunning) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                // Server parameters controls list
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Server Settings:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        // Port configuration selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TCP Listen Port", fontSize = 14.sp)
                            OutlinedTextField(
                                value = serverPort.toString(),
                                onValueChange = { input ->
                                    val checked = input.filter { it.isDigit() }.toIntOrNull()
                                    if (checked != null && checked > 0 && checked <= 65535) {
                                        serverPort = checked
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(100.dp)
                                    .testTag("server_port_input"),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isServerRunning
                            )
                        }

                        // Generated Local Secret API Key field
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Server Secret API Key", fontSize = 14.sp)
                                if (!isServerRunning) {
                                    TextButton(onClick = {
                                        serverApiKey = "optech_live_sk_" + UUID.randomUUID().toString().replace("-", "").take(16)
                                    }) {
                                        Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Regenerate Key", fontSize = 10.sp)
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = serverApiKey,
                                onValueChange = {},
                                singleLine = true,
                                readOnly = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("server_api_field"),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            )
                        }

                        // Control toggle actions
                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                if (isServerRunning) stopSocketServer() else startSocketServer()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isServerRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("server_toggle_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isServerRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Text(if (isServerRunning) "STOP SERVER ENGINE" else "START SERVER SERVICE", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Interactive Internal Connection Simulator Panel
                if (isServerRunning) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Simulated Api Request Tests:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { runClientSimulation(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary),
                                    border = ButtonDefaults.outlinedButtonBorder,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Simulate Auth Success", fontSize = 10.sp)
                                }
                                Button(
                                    onClick = { runClientSimulation(false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.error),
                                    border = ButtonDefaults.outlinedButtonBorder,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Simulate Auth Fail", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // live log stream title
                Text(
                    text = "SERVER EVENT & REQUEST stream LOG",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                // Live event logger list
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(24.dp))
                        .background(Color(0xFF141613))
                        .padding(12.dp)
                ) {
                    if (incomingLogs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No requests received yet.\nConnect from browser or simulate a call above.",
                                textAlign = TextAlign.Center,
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            reverseLayout = true
                        ) {
                            items(incomingLogs.reversed()) { log ->
                                val dateStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                                val authColor = when (log.authStatus) {
                                    "Success" -> Color(0xFF10B981)
                                    "Failed" -> Color(0xFFEF4444)
                                    else -> Color(0xFFFBBF24)
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .testTag("server_log_row"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "[$dateStr] ${log.method} ${log.path}",
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(0.8f)) {
                                        Text(
                                            text = "HTTP ${log.statusCode}",
                                            color = if (log.statusCode == 200) Color(0xFF10B981) else Color(0xFFEF4444),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Key: ${log.authStatus}",
                                            color = authColor,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Actual incoming socket requests processing engine
private fun handleClientConnection(
    socket: Socket,
    serverApiKey: String,
    incomingLogs: MutableList<ServerRequestLog>
) {
    var reader: BufferedReader? = null
    var out: PrintWriter? = null
    try {
        reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        out = PrintWriter(socket.getOutputStream(), true)

        // Read and parse Request headers
        val firstLine = reader.readLine() ?: return
        val tokens = firstLine.split(" ")
        if (tokens.size < 3) return
        val method = tokens[0]
        val path = tokens[1].substringBefore("?")

        var line: String?
        var authHeader = ""
        while (reader.readLine().also { line = it } != null) {
            if (line!!.isEmpty()) break // Headers complete
            if (line!!.startsWith("Authorization:", ignoreCase = true)) {
                authHeader = line!!.substringAfter("Authorization:").trim()
            }
        }

        // Authentication validation helper logic
        val token = if (authHeader.startsWith("Bearer ", ignoreCase = true)) {
            authHeader.substring(7).trim()
        } else {
            ""
        }

        val serverKeyTrimmed = serverApiKey.trim()
        val authStatus = when {
            token.isEmpty() -> "None"
            token == serverKeyTrimmed -> "Success"
            else -> "Failed"
        }

        val jsonResponse = JSONObject()
        val httpCode: Int

        if (authStatus == "Success") {
            httpCode = 200
            when (path) {
                "/api/status" -> {
                    jsonResponse.put("status", "healthy")
                    jsonResponse.put("authorized", true)
                    jsonResponse.put("timestamp", System.currentTimeMillis())
                }
                "/api/info" -> {
                    jsonResponse.put("brand", Build.BRAND)
                    jsonResponse.put("model", Build.MODEL)
                    jsonResponse.put("hardware", Build.HARDWARE)
                    jsonResponse.put("sdk", Build.VERSION.SDK_INT)
                    jsonResponse.put("release", Build.VERSION.RELEASE)
                    jsonResponse.put("server", "Optech Socket Engine/1.0.0")
                }
                "/api/log" -> {
                    jsonResponse.put("message", "Web webhook event logged on mobile hosts successfully")
                    jsonResponse.put("sender_ip", socket.inetAddress.hostAddress)
                    jsonResponse.put("success", true)
                }
                else -> {
                    jsonResponse.put("message", "System diagnostic active. Endpoint not registered.")
                }
            }
        } else {
            httpCode = 401
            jsonResponse.put("error", "Unauthorized")
            jsonResponse.put("message", "Invalid or missing Bearer secure API key token headers")
        }

        val stringResponse = jsonResponse.toString(2)
        val rawResponseBytes = stringResponse.toByteArray(Charsets.UTF_8)

        // Standard compliant REST response payload
        out.print("HTTP/1.1 $httpCode ${if (httpCode == 200) "OK" else "Unauthorized"}\r\n")
        out.print("Content-Type: application/json; charset=utf-8\r\n")
        out.print("Content-Length: ${rawResponseBytes.size}\r\n")
        out.print("Access-Control-Allow-Origin: *\r\n") // Allow CORS requests
        out.print("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n")
        out.print("Access-Control-Allow-Headers: Authorization, Content-Type\r\n")
        out.print("Connection: close\r\n\r\n")
        out.flush()

        socket.getOutputStream().write(rawResponseBytes)
        socket.getOutputStream().flush()

        // Append log dynamically using Main context
        val logItem = ServerRequestLog(
            method = method,
            path = path,
            remoteAddress = socket.inetAddress.hostAddress ?: "unknown",
            authStatus = authStatus,
            statusCode = httpCode
        )
        
        // Android safe list updates
        synchronized(incomingLogs) {
            incomingLogs.add(logItem)
        }

    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            reader?.close()
            out?.close()
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
