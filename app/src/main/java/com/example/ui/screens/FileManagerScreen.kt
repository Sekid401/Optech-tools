package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WorkspaceFile
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.font.FontStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(viewModel: MainViewModel) {
    val currentPath by viewModel.fileManagerPath.collectAsState()
    val isSandboxMode by viewModel.isSandboxMode.collectAsState()
    val noSandboxPath by viewModel.noSandboxPath.collectAsState()
    val fileList by viewModel.fileList.collectAsState()
    val errorMsg by viewModel.fileError.collectAsState()
    val editorFileRelativePath by viewModel.editorFileRelativePath.collectAsState()
    val editorContent by viewModel.editorContent.collectAsState()
    val fileSystemMode by viewModel.fileSystemMode.collectAsState()

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderNameInput by remember { mutableStateOf("") }

    var showCreateFileDialog by remember { mutableStateOf(false) }
    var fileNameInput by remember { mutableStateOf("") }
    var fileContentInput by remember { mutableStateOf("") }

    var showRenameDialog by remember { mutableStateOf<WorkspaceFile?>(null) }
    var renameInput by remember { mutableStateOf("") }

    var activeMenuFile by remember { mutableStateOf<WorkspaceFile?>(null) }

    // Re-sync files list when directory or mode matches
    LaunchedEffect(currentPath, noSandboxPath, isSandboxMode, fileSystemMode) {
        viewModel.refreshFiles()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "File Manager",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isSandboxMode) {
                                if (currentPath.isEmpty()) "workspace://root" else "workspace:///$currentPath"
                            } else {
                                if (fileSystemMode == 2) {
                                    "app-fs://$noSandboxPath"
                                } else {
                                    "system://$noSandboxPath"
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    val canGoBack = if (isSandboxMode) {
                        currentPath.isNotEmpty()
                    } else {
                        if (fileSystemMode == 2) {
                            noSandboxPath != "/data/data/com.optech.sekid"
                        } else {
                            noSandboxPath != "/"
                        }
                    }
                    if (canGoBack) {
                        IconButton(onClick = { viewModel.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Go up a folder")
                        }
                    } else {
                        IconButton(onClick = { }, enabled = false) {
                            Icon(
                                imageVector = if (isSandboxMode) {
                                    Icons.Default.Computer
                                } else if (fileSystemMode == 2) {
                                    Icons.Default.Settings
                                } else {
                                    Icons.Default.Storage
                                },
                                contentDescription = "Root Workspace"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateFolderDialog = true }) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "Create Folder")
                    }
                    IconButton(onClick = { showCreateFileDialog = true }) {
                        Icon(Icons.Default.NoteAdd, contentDescription = "Create Text File")
                    }
                    IconButton(onClick = { viewModel.refreshFiles() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload folder")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Quad Mode ScrollableTabRow selector (Sandbox / No Sandbox / App Filesystem / Misc Connect)
                ScrollableTabRow(
                    selectedTabIndex = fileSystemMode,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = fileSystemMode == 0,
                        onClick = { viewModel.setFileSystemMode(0) },
                        text = { Text("Sandbox", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        icon = { Icon(Icons.Default.Lock, contentDescription = "Sandbox Workspace") }
                    )
                    Tab(
                        selected = fileSystemMode == 1,
                        onClick = { viewModel.setFileSystemMode(1) },
                        text = { Text("No Sandbox", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        icon = { Icon(Icons.Default.Storage, contentDescription = "OS Filesystem") }
                    )
                    Tab(
                        selected = fileSystemMode == 2,
                        onClick = { viewModel.setFileSystemMode(2) },
                        text = { Text("App Filesystem", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        icon = { Icon(Icons.Default.FolderSpecial, contentDescription = "App Filesystem") }
                    )
                    Tab(
                        selected = fileSystemMode == 3,
                        onClick = { viewModel.setFileSystemMode(3) },
                        text = { Text("Misc", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        icon = { Icon(Icons.Default.Cloud, contentDescription = "Cloud & LAN Storage") }
                    )
                }

                if (fileSystemMode == 3) {
                    MiscFileOptionsPanel(viewModel = viewModel)
                } else {
                    // Display current file alerts
                    errorMsg?.let { msg ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Error: $msg",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearFileError() }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close file alerts panel",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                if (fileList.isEmpty()) {
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
                                Icons.Default.FolderOpen,
                                contentDescription = "Directory is empty",
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No folders or files here.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Use the top icons to create new files or folders, " +
                                "or use 'mkdir' and 'touch' inside the Terminal section!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(fileList) { wfile ->
                            FileListItem(
                                wfile = wfile,
                                onClick = {
                                    if (wfile.isDirectory) {
                                        viewModel.enterDirectory(wfile.name)
                                    } else {
                                        viewModel.openFileInEditor(wfile.relativePath)
                                    }
                                },
                                onMenuClick = { activeMenuFile = wfile }
                            )
                        }
                    }
                }
                }
            }

            // Options Drawer pop-up triggered on three-dots menu click
            activeMenuFile?.let { selectedFile ->
                AlertDialog(
                    onDismissRequest = { activeMenuFile = null },
                    title = { Text(selectedFile.name, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Type: ${if (selectedFile.isDirectory) "Directory Folder" else "Text Document"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (!selectedFile.isDirectory) {
                                Text(
                                    "Size: ${selectedFile.size} Bytes",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                .format(Date(selectedFile.lastModified))
                            Text(
                                "Updated: $dateStr",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    confirmButton = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = {
                                    viewModel.deleteFile(selectedFile.relativePath)
                                    activeMenuFile = null
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete")
                                }
                            }

                            Row {
                                TextButton(
                                    onClick = {
                                        showRenameDialog = selectedFile
                                        renameInput = selectedFile.name
                                        activeMenuFile = null
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rename")
                                }
                                TextButton(onClick = { activeMenuFile = null }) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                )
            }

            // Create Folder Dialog
            if (showCreateFolderDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showCreateFolderDialog = false
                        folderNameInput = ""
                    },
                    title = { Text("New Folder", fontWeight = FontWeight.Bold) },
                    text = {
                        TextField(
                            value = folderNameInput,
                            onValueChange = { folderNameInput = it },
                            label = { Text("Folder Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val name = folderNameInput.trim()
                                if (name.isNotEmpty()) {
                                    viewModel.createFolder(name)
                                }
                                showCreateFolderDialog = false
                                folderNameInput = ""
                            }
                        ) {
                            Text("Create")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showCreateFolderDialog = false
                                folderNameInput = ""
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Create File Dialog
            if (showCreateFileDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showCreateFileDialog = false
                        fileNameInput = ""
                        fileContentInput = ""
                    },
                    title = { Text("New Text File", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextField(
                                value = fileNameInput,
                                onValueChange = { fileNameInput = it },
                                label = { Text("File Name (e.g. notes.txt)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            TextField(
                                value = fileContentInput,
                                onValueChange = { fileContentInput = it },
                                label = { Text("Initial Content (Optional)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val name = fileNameInput.trim()
                                if (name.isNotEmpty()) {
                                    viewModel.createFile(name, fileContentInput)
                                }
                                showCreateFileDialog = false
                                fileNameInput = ""
                                fileContentInput = ""
                            }
                        ) {
                            Text("Create")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showCreateFileDialog = false
                                fileNameInput = ""
                                fileContentInput = ""
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Rename Dialog
            showRenameDialog?.let { targetWFile ->
                AlertDialog(
                    onDismissRequest = { showRenameDialog = null },
                    title = { Text("Rename Folder/File", fontWeight = FontWeight.Bold) },
                    text = {
                        TextField(
                            value = renameInput,
                            onValueChange = { renameInput = it },
                            label = { Text("New Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val newName = renameInput.trim()
                                if (newName.isNotEmpty() && newName != targetWFile.name) {
                                    viewModel.renameFile(targetWFile.relativePath, newName)
                                }
                                showRenameDialog = null
                            }
                        ) {
                            Text("Apply")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRenameDialog = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // FULLSCREEN TEXT EDITOR OVERLAY DIALOG FOR DOCUMENT VIEWING/PREVIEWING
            editorFileRelativePath?.let { relativePath ->
                var editableContent by remember(editorContent) { mutableStateOf(editorContent) }

                AlertDialog(
                    onDismissRequest = { viewModel.closeEditor() },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = relativePath.split("/").last(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.closeEditor() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close preview editor")
                            }
                        }
                    },
                    text = {
                        TextField(
                            value = editableContent,
                            onValueChange = { editableContent = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clip(RoundedCornerShape(8.dp)),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            placeholder = { Text("Empty text content...") }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.saveEditorContent(editableContent) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Document")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.closeEditor() }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FileListItem(
    wfile: WorkspaceFile,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (wfile.isDirectory) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            }
        ),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Folder / File Visual distinction Indicators
                Icon(
                    imageVector = if (wfile.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                    contentDescription = null,
                    tint = if (wfile.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = wfile.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (wfile.isDirectory) {
                            "Folder"
                        } else {
                            val kbSize = String.format("%.1f", wfile.size.toDouble() / 1024.0)
                            "Text document • ${wfile.size} B (${kbSize} KB)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "File configuration menu"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiscFileOptionsPanel(viewModel: MainViewModel) {
    val cloudLogs by viewModel.cloudLogs.collectAsState()
    val gdConnected by viewModel.googleDriveConnected.collectAsState()
    val gdUser by viewModel.googleDriveUser.collectAsState()
    val dbConnected by viewModel.dropboxConnected.collectAsState()
    val dbUser by viewModel.dropboxUser.collectAsState()
    val odConnected by viewModel.oneDriveConnected.collectAsState()
    val odUser by viewModel.oneDriveUser.collectAsState()
    val lanConnected by viewModel.lanConnected.collectAsState()
    val lanPath by viewModel.lanPath.collectAsState()

    // Google Drive Inputs
    var gdEmail by remember { mutableStateOf("") }
    var gdAuthCode by remember { mutableStateOf("") }

    // Dropbox Inputs
    var dbAppKey by remember { mutableStateOf("") }
    var dbToken by remember { mutableStateOf("") }

    // OneDrive Inputs
    var odAccountType by remember { mutableStateOf("Corporate") }

    // LAN Inputs
    var lanIp by remember { mutableStateOf("") }
    var lanShareDir by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "MISC STORAGE & CLOUD CONNECTIONS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )

        // 1. Google Drive Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Cloud, contentDescription = null, tint = Color(0xFF34A853))
                        Text("Google Drive Secure Sync", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = if (gdConnected) "CONNECTED" else "DISCONNECTED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (gdConnected) Color(0xFF10B981) else Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (gdConnected) {
                    Text(
                        text = "Authorized Volume: $gdUser",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { viewModel.disconnectGoogleDrive() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Unmount Drive Account")
                    }
                } else {
                    OutlinedTextField(
                        value = gdEmail,
                        onValueChange = { gdEmail = it },
                        label = { Text("Google Email Account") },
                        placeholder = { Text("sensthesekid@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = gdAuthCode,
                        onValueChange = { gdAuthCode = it },
                        label = { Text("Secure OAuth Token Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (gdEmail.isNotBlank()) {
                                viewModel.connectGoogleDrive(gdEmail.trim(), gdAuthCode.trim())
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Authorize & Attach Volume")
                    }
                }
            }
        }

        // 2. Dropbox Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = Color(0xFF0061FE))
                        Text("Dropbox Cloud Sync", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = if (dbConnected) "CONNECTED" else "DISCONNECTED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dbConnected) Color(0xFF10B981) else Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (dbConnected) {
                    Text(
                        text = "Authorized Scope: $dbUser",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { viewModel.disconnectDropbox() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Disconnect Dropbox Link")
                    }
                } else {
                    OutlinedTextField(
                        value = dbAppKey,
                        onValueChange = { dbAppKey = it },
                        label = { Text("App Key ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dbToken,
                        onValueChange = { dbToken = it },
                        label = { Text("App Token Credentials") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (dbAppKey.isNotBlank()) {
                                viewModel.connectDropbox(dbAppKey.trim(), dbToken.trim())
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mount Dropbox Endpoint")
                    }
                }
            }
        }

        // 3. Microsoft OneDrive
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF0078D4))
                        Text("Microsoft OneDrive", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = if (odConnected) "ACTIVE" else "INACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (odConnected) Color(0xFF10B981) else Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (odConnected) {
                    Text(
                        text = "Authorized Scope: $odUser",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { viewModel.disconnectOneDrive() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Disconnect Office Backup")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Personal", "Corporate", "Development").forEach { type ->
                            val isSel = odAccountType == type
                            Button(
                                onClick = { odAccountType = type },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(type, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Button(
                        onClick = { viewModel.connectOneDrive(odAccountType) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pair OneDrive Storage Hub")
                    }
                }
            }
        }

        // 4. LAN Network Shares
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text("LAN Network Shares (SMBv3)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = if (lanConnected) "MOUNTED" else "UNMOUNTED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (lanConnected) Color(0xFF10B981) else Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (lanConnected) {
                    Text(
                        text = "Address Path: $lanPath",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { viewModel.disconnectLanShare() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Unmount SMB Directory Share")
                    }
                } else {
                    OutlinedTextField(
                        value = lanIp,
                        onValueChange = { lanIp = it },
                        label = { Text("Host Server IP (IPv4 Address)") },
                        placeholder = { Text("192.168.1.104") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lanShareDir,
                        onValueChange = { lanShareDir = it },
                        label = { Text("Shared Drive Directory Folder Name") },
                        placeholder = { Text("PublicStorage") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (lanIp.isNotBlank() && lanShareDir.isNotBlank()) {
                                viewModel.connectLanShare(lanIp.trim(), lanShareDir.trim())
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Connect SMB Link Network Path")
                    }
                }
            }
        }

        // 5. FTP / SFTP Client
        var ftpIp by remember { mutableStateOf("") }
        var ftpPort by remember { mutableStateOf("22") }
        var ftpUser by remember { mutableStateOf("") }
        var ftpPass by remember { mutableStateOf("") }
        var ftpConnectedState by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFFF59E0B))
                        Text("SFTP / FTP Remote Node", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = if (ftpConnectedState) "PEER ACTIVE" else "DISCONNECTED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ftpConnectedState) Color(0xFF10B981) else Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (ftpConnectedState) {
                    Text(
                        text = "Connected Peer: sftp://$ftpUser@$ftpIp:$ftpPort",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.LightGray
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("sftp://remote-root/", fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("📁 config_dump/", fontSize = 11.sp, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace)
                            Text("📁 backups/", fontSize = 11.sp, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace)
                            Text("📄 payload_log_2026.bin (104 KB)", fontSize = 11.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                            Text("📄 secure_keys.asc (4 KB)", fontSize = 11.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Button(
                        onClick = { 
                            ftpConnectedState = false 
                            viewModel.addAppLog("SFTP Connection destroyed to $ftpIp.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Disconnect Remote SFTP Socket")
                    }
                } else {
                    OutlinedTextField(
                        value = ftpIp,
                        onValueChange = { ftpIp = it },
                        label = { Text("FTP / SFTP Host Server IP") },
                        placeholder = { Text("192.168.1.15") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = ftpPort,
                            onValueChange = { ftpPort = it },
                            label = { Text("Port") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = ftpUser,
                            onValueChange = { ftpUser = it },
                            label = { Text("Username") },
                            placeholder = { Text("anonymous") },
                            singleLine = true,
                            modifier = Modifier.weight(2f)
                        )
                    }
                    OutlinedTextField(
                        value = ftpPass,
                        onValueChange = { ftpPass = it },
                        label = { Text("Password Credentials") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (ftpIp.isNotBlank()) {
                                ftpConnectedState = true
                                viewModel.addAppLog("Establishing SFTP secure login connection index to $ftpIp:$ftpPort")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Link and List Remote SFTP")
                    }
                }
            }
        }

        // 6. AWS S3 Bucket Connector
        var s3Url by remember { mutableStateOf("") }
        var s3AccessKey by remember { mutableStateOf("") }
        var s3SecretKey by remember { mutableStateOf("") }
        var s3BucketName by remember { mutableStateOf("") }
        var s3ConnectedState by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFFFF9900))
                        Text("S3 Object Bucket Explorer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = if (s3ConnectedState) "BUCKET ATTACHED" else "OFFLINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (s3ConnectedState) Color(0xFF10B981) else Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (s3ConnectedState) {
                    Text(
                        text = "Attached Object Store Bucket: s3://$s3BucketName",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.LightGray
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("s3://$s3BucketName/objects/", fontSize = 10.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("📦 datalake_shard_001.csv (1.2 MB)", fontSize = 11.sp, color = Color(0xFFFF9900), fontFamily = FontFamily.Monospace)
                            Text("📦 training_meta_vector.weights (87 KB)", fontSize = 11.sp, color = Color(0xFFFF9900), fontFamily = FontFamily.Monospace)
                            Text("📦 test_output_manifest.json (12 KB)", fontSize = 11.sp, color = Color(0xFFFF9900), fontFamily = FontFamily.Monospace)
                        }
                    }

                    Button(
                        onClick = {
                            s3ConnectedState = false
                            viewModel.addAppLog("Detached S3 Storage bucket $s3BucketName")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Detach Bucket Storage")
                    }
                } else {
                    OutlinedTextField(
                        value = s3BucketName,
                        onValueChange = { s3BucketName = it },
                        label = { Text("S3 Bucket Name") },
                        placeholder = { Text("my-cyber-assets") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = s3Url,
                        onValueChange = { s3Url = it },
                        label = { Text("Endpoint URL / URL Suffix") },
                        placeholder = { Text("https://s3.us-east-1.amazonaws.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = s3AccessKey,
                        onValueChange = { s3AccessKey = it },
                        label = { Text("Access Key ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = s3SecretKey,
                        onValueChange = { s3SecretKey = it },
                        label = { Text("Secret Access Credentials Key") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (s3BucketName.isNotBlank() && s3AccessKey.isNotBlank()) {
                                s3ConnectedState = true
                                viewModel.addAppLog("Connecting object bucket s3://$s3BucketName via Secure Client.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Initialize Endpoint Connection")
                    }
                }
            }
        }

        // 7. AES-256 Military Cryptographer
        val sandboxFileList by viewModel.fileList.collectAsState()
        val plainFilesOnly = sandboxFileList.filter { !it.isDirectory }
        
        var selectedFileRelPath by remember { mutableStateOf("") }
        var cryptoPasscode by remember { mutableStateOf("") }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981))
                        Text("AES-256 Crypto Safe Node", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                }

                Text(
                    "Select any file from your sandboxed workspace, enter a custom password, and encrypt or decrypt instantly! Files are encrypted using AES/CBC/PKCS5Padding with high entropy random IV.",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )

                if (plainFilesOnly.isEmpty()) {
                    Text(
                        "No workspace file nodes available to encrypt. Go to File Explorer tab to create files!",
                        fontSize = 11.sp,
                        color = Color.Yellow,
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    var expandedDropdown by remember { mutableStateOf(false) }
                    val currentSelectionLabel = if (selectedFileRelPath.isEmpty()) "--- SELECT FILE NODE ---" else selectedFileRelPath

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { expandedDropdown = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(currentSelectionLabel, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        
                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.fillMaxWidth().background(Color(0xFF0F172A))
                        ) {
                            plainFilesOnly.forEach { wsFile ->
                                DropdownMenuItem(
                                    text = { Text(wsFile.relativePath, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.White) },
                                    onClick = {
                                        selectedFileRelPath = wsFile.relativePath
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = cryptoPasscode,
                        onValueChange = { cryptoPasscode = it },
                        label = { Text("Encryption / Decryption Passphrase") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (selectedFileRelPath.isNotBlank() && cryptoPasscode.isNotBlank()) {
                                    viewModel.encryptWorkspaceFile(selectedFileRelPath, cryptoPasscode)
                                    cryptoPasscode = ""
                                    selectedFileRelPath = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            enabled = selectedFileRelPath.isNotBlank() && cryptoPasscode.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ENCRYPT FILE")
                        }

                        Button(
                            onClick = {
                                if (selectedFileRelPath.isNotBlank() && cryptoPasscode.isNotBlank()) {
                                    viewModel.decryptWorkspaceFile(selectedFileRelPath, cryptoPasscode)
                                    cryptoPasscode = ""
                                    selectedFileRelPath = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            enabled = selectedFileRelPath.isNotBlank() && cryptoPasscode.isNotBlank() && selectedFileRelPath.endsWith(".enc"),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("DECRYPT FILE")
                        }
                    }
                }
            }
        }

        // Live Cloud Logs Drawer
        if (cloudLogs.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ATTACHMENT ENGINE HANDSHAKES",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        IconButton(onClick = { viewModel.clearCloudLogs() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear console logs", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(cloudLogs) { log ->
                            Text(
                                log,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

