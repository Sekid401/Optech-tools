package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.data.Contact
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingSubScreen(
    viewModel: MainViewModel,
    presetPhoneNumber: String = "",
    onBack: () -> Unit,
    onCallContact: (String) -> Unit
) {
    val contacts by viewModel.allContacts.collectAsState()
    val allMessages by viewModel.allMessages.collectAsState()
    
    var activeThreadId by remember { mutableStateOf("") }
    var chatPartnerName by remember { mutableStateOf("Chat") }
    var showStartChatDialog by remember { mutableStateOf(false) }
    
    // Auto-select preset thread if coming from contacts Shortcut
    LaunchedEffect(presetPhoneNumber) {
        if (presetPhoneNumber.isNotEmpty()) {
            activeThreadId = presetPhoneNumber
            val contact = contacts.find { it.phoneNumber.replace(" ", "").contains(presetPhoneNumber.replace(" ", "")) }
            chatPartnerName = contact?.name ?: presetPhoneNumber
        }
    }

    // List of active conversations: Group messages by threadId
    val threads = remember(allMessages, contacts) {
        allMessages.groupBy { it.threadId }.map { (threadId, msgs) ->
            val contact = contacts.find { it.phoneNumber.replace(" ", "").contains(threadId.replace(" ", "")) }
            ThreadSummary(
                threadId = threadId,
                name = contact?.name ?: threadId,
                lastMessage = msgs.maxByOrNull { it.timestamp }?.text ?: "",
                timestamp = msgs.maxByOrNull { it.timestamp }?.timestamp ?: 0L
            )
        }.sortedByDescending { it.timestamp }
    }

    if (activeThreadId.isNotEmpty()) {
        // --- CHAT WINDOW VIEW ---
        ChatThreadWindow(
            viewModel = viewModel,
            threadId = activeThreadId,
            title = chatPartnerName,
            onBack = { activeThreadId = "" },
            onCall = { onCallContact(activeThreadId) }
        )
    } else {
        // --- CONVERSATIONS THREAD LIST ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Secure Messaging", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("msg_back")) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to Launchpad")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showStartChatDialog = true }, modifier = Modifier.testTag("msg_new_chat_btn")) {
                            Icon(Icons.Default.ChatBubble, contentDescription = "New Conversation")
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showStartChatDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New Message") },
                    modifier = Modifier.testTag("msg_new_thread_fab")
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (threads.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Chat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No active conversations",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Start a chat with a contact from directory.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(threads) { item ->
                            ThreadSummaryCard(
                                summary = item,
                                onClick = {
                                    activeThreadId = item.threadId
                                    chatPartnerName = item.name
                                },
                                onDelete = { viewModel.deleteConversation(item.threadId) }
                            )
                        }
                    }
                }
            }

            // Start New Conversation Select Contact Dialog
            if (showStartChatDialog) {
                AlertDialog(
                    onDismissRequest = { showStartChatDialog = false },
                    title = { Text("Choose Recipient", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                        ) {
                            if (contacts.isEmpty()) {
                                Text(
                                    "No contacts available. Please create contacts in Contacts subscreen first.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            } else {
                                Text("Select from Contacts list:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(contacts) { contact ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    activeThreadId = contact.phoneNumber
                                                    chatPartnerName = contact.name
                                                    showStartChatDialog = false
                                                }
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = contact.name.take(1).uppercase(),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(contact.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                Text(contact.phoneNumber, color = Color.Gray, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showStartChatDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThreadWindow(
    viewModel: MainViewModel,
    threadId: String,
    title: String,
    onBack: () -> Unit,
    onCall: () -> Unit
) {
    val messagesFlow = remember(threadId) { viewModel.getMessagesForThread(threadId) }
    val threadMessages by messagesFlow.collectAsState(initial = emptyList())
    var chatInputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto scroll list window to bottom on new messages
    LaunchedEffect(threadMessages.size) {
        if (threadMessages.isNotEmpty()) {
            listState.animateScrollToItem(threadMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(threadId, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to conversation threads")
                    }
                },
                actions = {
                    IconButton(onClick = onCall, modifier = Modifier.testTag("chat_call_shortcut")) {
                        Icon(Icons.Default.Call, contentDescription = "Trigger phone call")
                    }
                    IconButton(onClick = { viewModel.deleteConversation(threadId) }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear thread log history")
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
            // Chat balloon body container
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                items(threadMessages) { message ->
                    val isMe = message.sender == "Me"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (!isMe) {
                                    Text(
                                        text = title, 
                                        fontWeight = FontWeight.Bold, 
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                                Text(
                                    text = message.text,
                                    fontSize = 14.sp,
                                    color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                val formattedTime = remember(message.timestamp) {
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
                                }
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

            // Message text controller layout Bottom Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatInputText,
                    onValueChange = { chatInputText = it },
                    placeholder = { Text("Send secure message...") },
                    maxLines = 4,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_textfield"),
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        val trimmedText = chatInputText.trim()
                        if (trimmedText.isNotEmpty()) {
                            viewModel.sendMessage(
                                threadId = threadId,
                                sender = "Me",
                                text = trimmedText,
                                autoReply = true
                            )
                            chatInputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .testTag("chat_send_button"),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Chat Message",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ThreadSummaryCard(
    summary: ThreadSummary,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = summary.name.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = summary.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val formattedTime = remember(summary.timestamp) {
                        SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(summary.timestamp))
                    }
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = summary.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Delete Conversation Thread",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

data class ThreadSummary(
    val threadId: String,
    val name: String,
    val lastMessage: String,
    val timestamp: Long
)
