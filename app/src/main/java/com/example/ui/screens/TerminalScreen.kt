package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@Composable
fun TerminalScreen(viewModel: MainViewModel) {
    val terminalLines by viewModel.terminalLines.collectAsState()
    val terminalDir by viewModel.terminalDirectory.collectAsState()
    val appLogs by viewModel.appLogs.collectAsState()

    var commandInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Shell, 1 = App Logs

    // Auto-scroll terminal output list to bottom on any new line insertion
    LaunchedEffect(terminalLines.size, appLogs.size, selectedTab) {
        val size = if (selectedTab == 0) terminalLines.size else appLogs.size
        if (size > 0) {
            lazyListState.animateScrollToItem(size - 1)
        }
    }

    // High Tech Terminal Color Theme Deck (Professional Polish Custom Terminal colors)
    val consoleBackground = Color(0xFF131512)
    val consoleText = Color(0xFFC2EFAD)
    val consolePrompt = Color(0xFF79D98F)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(consoleBackground)
    ) {
        // --- Tab Selection bar at the top ---
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1E211A),
            contentColor = consolePrompt,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Command Shell", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("App Logs Console", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        // --- Custom Console Top Status bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF222520))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFFFF5F56), shape = RoundedCornerShape(5.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFFFFBD2E), shape = RoundedCornerShape(5.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF27C93F), shape = RoundedCornerShape(5.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (selectedTab == 0) "omnishell: /${terminalDir}" else "optech-console: active-logs",
                    color = Color.LightGray,
                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                )
            }

            // Quick Shortcut buttons
            if (selectedTab == 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.executeTerminalCommand("help") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.HelpOutline, 
                            contentDescription = "Quick help command", 
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.executeTerminalCommand("neofetch") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Info, 
                            contentDescription = "Quick info system spec stats", 
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.executeTerminalCommand("clear") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.ClearAll, 
                            contentDescription = "Clear output logs", 
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "[Max 10 Logs]",
                    color = Color.Gray,
                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                )
            }
        }

        if (selectedTab == 0) {
            // --- Terminal Active Screen Outputs ---
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(terminalLines) { line ->
                    val color = if (line.contains("omni@sandbox")) {
                        Color(0xFF6DA4FF)
                    } else if (line.lowercase().contains("error") || line.lowercase().contains("not found")) {
                        Color(0xFFFF5F56)
                    } else if (line.startsWith("==") || line.startsWith(" OMNI")) {
                        Color(0xFFFFBD2E)
                    } else {
                        consoleText
                    }

                    Text(
                        text = line,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = color
                        )
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF424940), thickness = 1.dp)

            // --- Active Command Inputs Line ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141512))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prompt prefix label
                Text(
                    text = ">_",
                    color = consolePrompt,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )

                BasicTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = consoleText,
                        fontWeight = FontWeight.Normal
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            val input = commandInput.trim()
                            if (input.isNotEmpty()) {
                                viewModel.executeTerminalCommand(input)
                                commandInput = ""
                            }
                        }
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        val input = commandInput.trim()
                        if (input.isNotEmpty()) {
                            viewModel.executeTerminalCommand(input)
                            commandInput = ""
                        }
                    },
                    enabled = commandInput.trim().isNotEmpty()
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Execute Command",
                        tint = if (commandInput.trim().isNotEmpty()) consoleText else Color.DarkGray
                    )
                }
            }
        } else {
            // --- Console Live Logs Outputs ---
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(appLogs) { log ->
                    Text(
                        text = log,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = Color(0xFFFFF0B8)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}
