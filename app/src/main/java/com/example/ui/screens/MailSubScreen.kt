package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.OptechMail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MailSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val inbox by viewModel.mailInbox.collectAsState()
    val logs by viewModel.mailLogs.collectAsState()
    val isConnecting by viewModel.isMailConnecting.collectAsState()
    val accountEmail by viewModel.mailAccount.collectAsState()
    val accountType by viewModel.mailAccountType.collectAsState()
    val fileList by viewModel.fileList.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }
    var showComposeDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Config Form Inputs
    var emailInput by remember { mutableStateOf("") }
    var providerInput by remember { mutableStateOf("Gmail") }
    var hostInput by remember { mutableStateOf("imap.gmail.com") }
    var incomingPortInput by remember { mutableStateOf("993") }
    var outgoingHostInput by remember { mutableStateOf("smtp.gmail.com") }
    var outgoingPortInput by remember { mutableStateOf("465") }
    var passwordInput by remember { mutableStateOf("") }

    // Active reading mail state
    var activeMailDetail by remember { mutableStateOf<OptechMail?>(null) }

    // Compose Inputs
    var composeTo by remember { mutableStateOf("") }
    var composeSubject by remember { mutableStateOf("") }
    var composeBody by remember { mutableStateOf("") }
    var selectedAttachments by remember { mutableStateOf(setOf<String>()) }
    var showAttachmentSelector by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Optech Mail Hub",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = accountEmail?.let { "SECURE: $it ($accountType)" } ?: "No secure mail account configured",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (accountEmail != null) Color(0xFF10B981) else Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("mail_app_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return to Launchpad")
                    }
                },
                actions = {
                    if (accountEmail == null) {
                        Button(
                            onClick = { showConfigDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CONNECT SECURE", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        IconButton(onClick = { viewModel.clearMailAccount() }) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = "Logout account", tint = Color.Red)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                )
            )
        },
        floatingActionButton = {
            if (accountEmail != null) {
                FloatingActionButton(
                    onClick = { showComposeDialog = true },
                    containerColor = Color(0xFF4F46E5),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("mail_compose_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Compose mail")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0F172A)) // Custom space Slate Midnight background
        ) {
            // Live Handshake Console Drawer (Shows protocol sync logs dynamically!)
            if (logs.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp)
                        .background(Color.Black)
                        .border(1.dp, Color(0xFF1E293B))
                        .padding(8.dp)
                ) {
                    val scrollState = rememberScrollState()
                    LaunchedEffect(logs.size) {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "DEEP PACKET MAIL EXCHANGE LOGS",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Bold
                            )
                            if (isConnecting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(10.dp),
                                    strokeWidth = 1.dp,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                        logs.forEach { logLine ->
                            Text(
                                text = logLine,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = when {
                                    logLine.startsWith(">>>") -> Color(0xFF38BDF8)   // outgoing
                                    logLine.startsWith("<<<") -> Color(0xFF34D399)   // incoming
                                    else -> Color.Gray
                                }
                            )
                        }
                    }
                }
            }

            // Search Header area
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter inbox items...", color = Color.Gray, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF4F46E5),
                    unfocusedBorderColor = Color(0xFF1E293B)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filtered Inbox message items list
            val filteredMails = remember(inbox, searchQuery) {
                inbox.filter {
                    it.sender.contains(searchQuery, ignoreCase = true) ||
                            it.senderEmail.contains(searchQuery, ignoreCase = true) ||
                            it.subject.contains(searchQuery, ignoreCase = true) ||
                            it.body.contains(searchQuery, ignoreCase = true)
                }
            }

            if (filteredMails.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AllInbox,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (accountEmail == null) "Connect an account to load mail grids." else "Operational inbox is empty.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMails) { mail ->
                        Card(
                            onClick = {
                                viewModel.markMailRead(mail.id)
                                activeMailDetail = mail
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (mail.isRead) Color(0xFF1E293B) else Color(0xFF334155).copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (mail.isRead) 0.dp else 1.dp,
                                    color = Color(0xFF4F46E5).copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (mail.isSentDraft) Color(0xFF4F46E5) else Color(0xFF0F766E)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = mail.sender.take(1).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = mail.sender,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (mail.isRead) FontWeight.Medium else FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = mail.senderEmail,
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    Text(
                                        text = mail.timestamp,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = mail.subject,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (mail.isRead) FontWeight.Normal else FontWeight.Bold,
                                    color = if (mail.isRead) Color.LightGray else Color.White
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = mail.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (mail.attachments.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Attachment, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF38BDF8))
                                        Text(
                                            text = "${mail.attachments.size} ATTACHMENT(S)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF38BDF8)
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

    // Secure Mail Configuration Dialog
    if (showConfigDialog) {
        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = {
                Text(
                    "Secure Account Handshake",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Configures imap-pop3 network client relays. Handshake certificates automatically validated.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Account Name") },
                        singleLine = true,
                        placeholder = { Text("Omni User") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("App Password / Access Token") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        placeholder = { Text("••••••••••••••••") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Client type buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Gmail", "Yahoo", "Outlook", "Custom POP3").forEach { prov ->
                            val isSel = providerInput == prov
                            Button(
                                onClick = {
                                    providerInput = prov
                                    when (prov) {
                                        "Gmail" -> {
                                            hostInput = "imap.gmail.com"
                                            incomingPortInput = "993"
                                            outgoingHostInput = "smtp.gmail.com"
                                            outgoingPortInput = "465"
                                        }
                                        "Yahoo" -> {
                                            hostInput = "pop.mail.yahoo.com"
                                            incomingPortInput = "995"
                                            outgoingHostInput = "smtp.mail.yahoo.com"
                                            outgoingPortInput = "465"
                                        }
                                        "Outlook" -> {
                                            hostInput = "outlook.office365.com"
                                            incomingPortInput = "993"
                                            outgoingHostInput = "smtp-mail.outlook.com"
                                            outgoingPortInput = "587"
                                        }
                                        else -> {
                                            hostInput = "pop3.securegrid.lan"
                                            incomingPortInput = "110"
                                            outgoingHostInput = "smtp.securegrid.lan"
                                            outgoingPortInput = "25"
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) Color(0xFF4F46E5) else Color(0xFF1E293B)
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(prov, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = hostInput,
                        onValueChange = { hostInput = it },
                        label = { Text("Incoming Mail Server Host") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = incomingPortInput,
                        onValueChange = { incomingPortInput = it },
                        label = { Text("Port (IMAP/POP3)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = outgoingHostInput,
                        onValueChange = { outgoingHostInput = it },
                        label = { Text("Outgoing SMTP Host") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = outgoingPortInput,
                        onValueChange = { outgoingPortInput = it },
                        label = { Text("SMTP Port") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val email = emailInput.trim()
                        if (email.isNotEmpty()) {
                            viewModel.configureMailAccount(
                                provider = providerInput,
                                email = email,
                                host = hostInput.trim(),
                                incomingPort = incomingPortInput.trim().toIntOrNull() ?: 993,
                                outgoingHost = outgoingHostInput.trim(),
                                outgoingPort = outgoingPortInput.trim().toIntOrNull() ?: 465,
                                password = passwordInput.trim()
                            )
                        }
                        showConfigDialog = false
                    }
                ) {
                    Text("START HANDSHAKE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    // Mail Compose Dialog Panel
    if (showComposeDialog) {
        AlertDialog(
            onDismissRequest = { showComposeDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("New Outgoing Broadcast", fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showComposeDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close composer")
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = composeTo,
                        onValueChange = { composeTo = it },
                        label = { Text("Recipient (To:) ") },
                        singleLine = true,
                        placeholder = { Text("dispatch@hq.optech.sekid") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.AccountBox, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = composeSubject,
                        onValueChange = { composeSubject = it },
                        label = { Text("Subject (Headers:) ") },
                        singleLine = true,
                        placeholder = { Text("Project telemetry logs") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Subject, contentDescription = null) }
                    )

                    // Attachments trigger selection
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .clickable { showAttachmentSelector = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Attachment, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedAttachments.isEmpty()) "ATTACH REGISTRY FILE" else "${selectedAttachments.size} FILE(S) SEEDING",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.LightGray)
                        }
                    }

                    // Selected files listing tags
                    if (selectedAttachments.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            selectedAttachments.forEach { path ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF334155))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(path.split("/").last(), fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.LightGray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clickable { selectedAttachments = selectedAttachments - path },
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = composeBody,
                        onValueChange = { composeBody = it },
                        label = { Text("Message Body") },
                        placeholder = { Text("Write encrypted lines here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 15
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val to = composeTo.trim()
                        if (to.isNotEmpty()) {
                            viewModel.dispatchComposeMail(
                                to = to,
                                subject = composeSubject.trim().ifEmpty { "No Subject" },
                                body = composeBody,
                                attachments = selectedAttachments.toList()
                            )
                        }
                        showComposeDialog = false
                        composeTo = ""
                        composeSubject = ""
                        composeBody = ""
                        selectedAttachments = emptySet()
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DISPATCH SMTP")
                }
            },
            dismissButton = {
                TextButton(onClick = { showComposeDialog = false }) {
                    Text("DISMISS")
                }
            }
        )
    }

    // Attachment Selections Dialog Listing Current Workspace Files
    if (showAttachmentSelector) {
        AlertDialog(
            onDismissRequest = { showAttachmentSelector = false },
            title = { Text("Attach Workspace Resource", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)
                ) {
                    if (fileList.isEmpty()) {
                        Text("No file entries registered in local editor directories.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(fileList) { wfile ->
                                if (!wfile.isDirectory) {
                                    val isTagged = selectedAttachments.contains(wfile.relativePath)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isTagged) Color(0xFF1E293B) else Color.Transparent)
                                            .clickable {
                                                selectedAttachments = if (isTagged) {
                                                    selectedAttachments - wfile.relativePath
                                                } else {
                                                    selectedAttachments + wfile.relativePath
                                                }
                                            }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isTagged) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                            contentDescription = null,
                                            tint = if (isTagged) Color(0xFF10B981) else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(wfile.name, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                            Text("workspace:///${wfile.relativePath}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAttachmentSelector = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Detailed Mail Reader Dialog Overlay Panel
    activeMailDetail?.let { mail ->
        AlertDialog(
            onDismissRequest = { activeMailDetail = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Secure Cryptographic Message", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { activeMailDetail = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close reader")
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row {
                                Text("FROM: ", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("${mail.sender} <${mail.senderEmail}>", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row {
                                Text("TO:   ", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("<${mail.recipient}>", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row {
                                Text("DATE: ", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text(mail.timestamp, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row {
                                Text("SUBJ: ", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text(mail.subject, color = Color(0xFFF1F5F9), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Attachments banner
                    if (mail.attachments.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("ATTACHED ARTIFACTS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            mail.attachments.forEach { path ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    border = BorderStroke(1.dp, Color(0xFF334155))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.FilePresent, contentDescription = null, tint = Color(0xFF38BDF8))
                                        Column {
                                            Text(path.split("/").last(), style = MaterialTheme.typography.bodySmall, color = Color.White)
                                            Text("workspace:///$path", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Body
                    Divider(color = Color(0xFF1E293B))
                    Text(
                        text = mail.body,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = Color(0xFFCBD5E1),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { activeMailDetail = null }) {
                    Text("OK")
                }
            }
        )
    }
}
