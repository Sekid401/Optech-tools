package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Assistant
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class AssistantMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "Me" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val messages = remember {
        mutableStateListOf(
            AssistantMessage(
                sender = "AI",
                text = "Hello! I am your Omni-AI supervisor. Ask me anything about this device, code architecture, or local services, and I will assist you instantly."
            )
        )
    }

    var userInput by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var showKeyOverrideDialog by remember { mutableStateOf(false) }

    // Read initial API key from BuildConfig, and support override.
    var customApiKey by rememberSaveable { mutableStateOf("") }
    val effectiveApiKey = remember(customApiKey) {
        if (customApiKey.isNotBlank()) customApiKey else BuildConfig.GEMINI_API_KEY
    }

    // Auto scroll chat to bottom when new messages arrive.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    val systemInstruction = "You are Omni-AI, an expert mobile database supervisor and suite assistant. " +
            "You are smart, precise, friendly, and speak with clear, confident display logic. " +
            "Avoid unnecessarily long output; focus on direct, elegant, actionable steps."

    // API network logic
    val sendMessageToGemini: (String) -> Unit = { userMessage ->
        if (userMessage.isNotBlank() && !isThinking) {
            messages.add(AssistantMessage(sender = "Me", text = userMessage))
            userInput = ""
            isThinking = true
            keyboardController?.hide()

            scope.launch {
                val responseText = withContext(Dispatchers.IO) {
                    try {
                        val apiKey = effectiveApiKey.trim()
                        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                            return@withContext "API KEY WARNING: No valid Gemini API Key was found inside this app. Please click the key icon in the upper-right corner to supply your Gemini API key directly!"
                        }

                        val client = OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build()

                        // Build raw contents hierarchy manually for conversation continuity
                        val contentsArray = JSONArray()
                        messages.forEach { msg ->
                            val role = if (msg.sender == "Me") "user" else "model"
                            contentsArray.put(
                                JSONObject()
                                    .put("role", role)
                                    .put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                            )
                        }

                        // Top level configuration using org.json
                        val jsonBody = JSONObject().apply {
                            put("contents", contentsArray)
                            put("systemInstruction", JSONObject().apply {
                                put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                            })
                            put("generationConfig", JSONObject().apply {
                                put("temperature", 0.7)
                                put("maxOutputTokens", 1024)
                            })
                        }

                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        val requestBody = jsonBody.toString().toRequestBody(mediaType)

                        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                        val request = Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build()

                        client.newCall(request).execute().use { response ->
                            if (!response.isSuccessful) {
                                return@withContext "Network Error (HTTP ${response.code}): ${response.message}\nEnsure your API key is correct and you have internet access."
                            }
                            val responseBodyString = response.body?.string() ?: ""
                            val resJson = JSONObject(responseBodyString)
                            val candidatesArray = resJson.optJSONArray("candidates")
                            if (candidatesArray != null && candidatesArray.length() > 0) {
                                val firstCandidate = candidatesArray.getJSONObject(0)
                                val partsArray = firstCandidate.getJSONObject("content").getJSONArray("parts")
                                if (partsArray.length() > 0) {
                                    return@withContext partsArray.getJSONObject(0).getString("text")
                                }
                            }
                            "Empty response received. Please try again."
                        }
                    } catch (e: Exception) {
                        "Connection Error: ${e.localizedMessage ?: "Unknown network failure"}"
                    }
                }

                delay(300) // Aesthetic human-like delay
                isThinking = false
                messages.add(AssistantMessage(sender = "AI", text = responseText))
            }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("AI Assistant", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Powered by gemini-3.5-flash", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("ai_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to launchpad")
                    }
                },
                actions = {
                    val keyStatusTint = if (effectiveApiKey.isNotBlank() && effectiveApiKey != "MY_GEMINI_API_KEY") Color(0xFF10B981) else Color(0xFFEF4444)
                    IconButton(onClick = { showKeyOverrideDialog = true }, modifier = Modifier.testTag("ai_key_override_btn")) {
                        Icon(
                            imageVector = Icons.Outlined.Key,
                            contentDescription = "Configuration API Key Settings",
                            tint = keyStatusTint
                        )
                    }
                    IconButton(onClick = {
                        messages.clear()
                        messages.add(AssistantMessage(sender = "AI", text = "Alright! Let's start fresh. How can I help you?"))
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart Chat Log")
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
            // Main conversation canvas
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 12.dp)
            ) {
                // Warning Banner if API Key is placeholder
                if (effectiveApiKey.isBlank() || effectiveApiKey == "MY_GEMINI_API_KEY") {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("API Key Missing", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                    Text("Configure raw API key in AI Studio Secrets panel or tap the Key icon in the top right to paste one.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }
                }

                // Chat Messages List
                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.sender == "Me"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        if (!isMe) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Assistant,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Card(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    fontSize = 14.sp,
                                    color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val formattedTime = remember(msg.timestamp) {
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                                }
                                Text(
                                    text = formattedTime,
                                    fontSize = 10.sp,
                                    color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }

                // Live Typing Thinking Indicator
                if (isThinking) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 40.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "AI is analyzing response stream...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Suggested chips if only start model greeting is visible
                if (messages.size <= 1 && !isThinking) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Suggested Queries & Guides:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val prompts = listOf(
                                "Explain mobile adb wireless link",
                                "Explain how a local phone-to-computer web server works",
                                "Draft a daily schedule to optimize productivity"
                            )

                            prompts.forEach { item ->
                                Card(
                                    onClick = { sendMessageToGemini(item) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.ArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(item, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

            // Text Input layout bar at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { Text("Ask secure AI assistant_") },
                    maxLines = 4,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_input_text"),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (userInput.isNotBlank() && !isThinking) {
                            sendMessageToGemini(userInput)
                        }
                    })
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (userInput.isNotBlank() && !isThinking) {
                            sendMessageToGemini(userInput)
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .testTag("ai_send_btn"),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send API Question Prompt",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Custom API Key Local Input Settings Dialog
    if (showKeyOverrideDialog) {
        AlertDialog(
            onDismissRequest = { showKeyOverrideDialog = false },
            title = { Text("Gemini API Configuration", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Configure API key dynamically. Key is memorized locally during applet operations.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = customApiKey,
                        onValueChange = { customApiKey = it },
                        label = { Text("Custom API Key (Optional)") },
                        placeholder = { Text("Paste AI Studio API Key") },
                        modifier = Modifier.fillMaxWidth().testTag("ai_key_input_field"),
                        singleLine = true
                    )
                    if (BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") {
                        Text(
                            "Injected platform key: Active / Detected ✅",
                            color = Color(0xFF10B981),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            "No injected platform key detected.",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showKeyOverrideDialog = false }) {
                    Text("Save configuration")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    customApiKey = ""
                    showKeyOverrideDialog = false
                }) {
                    Text("Clear override")
                }
            }
        )
    }
}
