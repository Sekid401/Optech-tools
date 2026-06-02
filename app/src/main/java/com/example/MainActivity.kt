package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.MainViewModel.ToolSection
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.FileManagerScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private var mainViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel()
                mainViewModel = viewModel
                androidx.compose.runtime.LaunchedEffect(intent) {
                    if (intent?.hasCategory(android.content.Intent.CATEGORY_HOME) == true) {
                        viewModel.navigateTo(ToolSection.APPS)
                    }
                }
                MainDashboardScreen(viewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasCategory(android.content.Intent.CATEGORY_HOME)) {
            mainViewModel?.navigateTo(ToolSection.APPS)
        }
    }
}

@Composable
fun MainDashboardScreen(viewModel: MainViewModel = viewModel()) {
    val activeSection by viewModel.currentSection.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_dashboard_scaffold"),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav_bar")
            ) {
                // Tab Item: FILE_MANAGER
                NavigationBarItem(
                    selected = activeSection == ToolSection.FILE_MANAGER,
                    onClick = { viewModel.navigateTo(ToolSection.FILE_MANAGER) },
                    icon = {
                        Icon(
                            imageVector = if (activeSection == ToolSection.FILE_MANAGER) Icons.Filled.FolderOpen else Icons.Outlined.FolderOpen,
                            contentDescription = "File Manager Section"
                        )
                    },
                    label = { Text("Files", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_btn_files")
                )

                // Tab Item: BROWSER
                NavigationBarItem(
                    selected = activeSection == ToolSection.BROWSER,
                    onClick = { viewModel.navigateTo(ToolSection.BROWSER) },
                    icon = {
                        Icon(
                            imageVector = if (activeSection == ToolSection.BROWSER) Icons.Filled.Language else Icons.Outlined.Language,
                            contentDescription = "Web Browser Section"
                        )
                    },
                    label = { Text("Browser", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_btn_browser")
                )

                // Tab Item: TERMINAL
                NavigationBarItem(
                    selected = activeSection == ToolSection.TERMINAL,
                    onClick = { viewModel.navigateTo(ToolSection.TERMINAL) },
                    icon = {
                        Icon(
                            imageVector = if (activeSection == ToolSection.TERMINAL) Icons.Filled.Terminal else Icons.Outlined.Terminal,
                            contentDescription = "Terminal Emulator Section"
                        )
                    },
                    label = { Text("Terminal", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_btn_terminal")
                )

                // Tab Item: CALENDAR
                NavigationBarItem(
                    selected = activeSection == ToolSection.CALENDAR,
                    onClick = { viewModel.navigateTo(ToolSection.CALENDAR) },
                    icon = {
                        Icon(
                            imageVector = if (activeSection == ToolSection.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "Interactive Calendar Section"
                        )
                    },
                    label = { Text("Calendar", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_btn_calendar")
                )

                // Tab Item: APPS
                NavigationBarItem(
                    selected = activeSection == ToolSection.APPS,
                    onClick = { viewModel.navigateTo(ToolSection.APPS) },
                    icon = {
                        Icon(
                            imageVector = if (activeSection == ToolSection.APPS) Icons.Filled.Apps else Icons.Outlined.Apps,
                            contentDescription = "Suite Apps Section"
                        )
                    },
                    label = { Text("Apps", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_btn_apps")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeSection) {
                ToolSection.FILE_MANAGER -> FileManagerScreen(viewModel)
                ToolSection.BROWSER -> BrowserScreen(viewModel)
                ToolSection.TERMINAL -> TerminalScreen(viewModel)
                ToolSection.CALENDAR -> CalendarScreen(viewModel)
                ToolSection.APPS -> AppsScreen(viewModel)
            }
        }
    }
}
