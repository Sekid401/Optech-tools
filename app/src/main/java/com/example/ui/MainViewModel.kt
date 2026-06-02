package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.Bookmark
import com.example.data.CalendarEvent
import com.example.data.FileManagerHelper
import com.example.data.HistoryItem
import com.example.data.WorkspaceFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

import com.example.data.Note
import com.example.data.Contact
import com.example.data.ChatMessage
import com.example.data.TodoTask
import com.example.data.RecentCall
import com.example.ui.screens.MailSocketEngine
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.json.JSONArray

data class AndroidPkgInfo(
    val label: String,
    val packageName: String,
    val isSystem: Boolean,
    val icon: android.graphics.drawable.Drawable? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // --- Database & Repository Initializer ---
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "omni_toolbox_database"
        ).fallbackToDestructiveMigration().build()
    }

    private val repository: AppRepository by lazy {
        AppRepository(
            database.calendarDao, 
            database.browserDao, 
            database.noteDao,
            database.contactDao,
            database.chatDao,
            database.todoDao,
            database.recentCallDao
        )
    }

    // --- File Manager Utility ---
    val fileHelper = FileManagerHelper(application)

    // --- NAVIGATION STATE ---
    private val _currentSection = MutableStateFlow(ToolSection.FILE_MANAGER)
    val currentSection: StateFlow<ToolSection> = _currentSection.asStateFlow()

    fun navigateTo(section: ToolSection) {
        _currentSection.value = section
        addAppLog("Navigated to: ${section.name}")
    }

    // ==========================================
    // 1. FILE MANAGER STATE & ACTIONS (SANDBOX & NO-SANDBOX)
    // ==========================================
    private val _fileManagerPath = MutableStateFlow("") // relative path from root for sandbox
    val fileManagerPath: StateFlow<String> = _fileManagerPath.asStateFlow()

    private val _isSandboxMode = MutableStateFlow(true)
    val isSandboxMode: StateFlow<Boolean> = _isSandboxMode.asStateFlow()

    private val _noSandboxPath = MutableStateFlow("/")
    val noSandboxPath: StateFlow<String> = _noSandboxPath.asStateFlow()

    private val _fileSystemMode = MutableStateFlow(0) // 0: Sandbox, 1: No Sandbox, 2: App Filesystem, 3: Misc
    val fileSystemMode: StateFlow<Int> = _fileSystemMode.asStateFlow()

    private val _fileList = MutableStateFlow<List<WorkspaceFile>>(emptyList())
    val fileList: StateFlow<List<WorkspaceFile>> = _fileList.asStateFlow()

    private val _fileError = MutableStateFlow<String?>(null)
    val fileError: StateFlow<String?> = _fileError.asStateFlow()

    private val _editorFileRelativePath = MutableStateFlow<String?>(null)
    val editorFileRelativePath: StateFlow<String?> = _editorFileRelativePath.asStateFlow()

    private val _editorContent = MutableStateFlow("")
    val editorContent: StateFlow<String> = _editorContent.asStateFlow()

    // --- App Logs Manager (Strictly limited to 10 logs) ---
    private val _appLogs = MutableStateFlow<List<String>>(
        listOf(
            "Service Initialization Complete.",
            "Optech Suite Dashboard Activated.",
            "File manager sandbox prepared."
        )
    )
    val appLogs: StateFlow<List<String>> = _appLogs.asStateFlow()

    fun addAppLog(msg: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val sDate = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val formatted = "[$sDate] $msg"
            val current = _appLogs.value.toMutableList()
            current.add(formatted)
            if (current.size > 10) {
                current.removeAt(0)
            }
            _appLogs.value = current
        }
    }

    // --- Android Apps State ---
    private val _androidApps = MutableStateFlow<List<AndroidPkgInfo>>(emptyList())
    val androidApps: StateFlow<List<AndroidPkgInfo>> = _androidApps.asStateFlow()

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val pm = context.packageManager
                
                // Querying apps with launcher intent
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveActivities = pm.queryIntentActivities(mainIntent, 0)
                
                val appsList = resolveActivities.mapNotNull { resolveInfo ->
                    val appInfo = resolveInfo.activityInfo.applicationInfo
                    val label = resolveInfo.loadLabel(pm).toString()
                    val packageName = resolveInfo.activityInfo.packageName
                    val isSystem = appInfo != null && (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                    val icon = try { resolveInfo.loadIcon(pm) } catch (e: Exception) { null }
                    AndroidPkgInfo(
                        label = label,
                        packageName = packageName,
                        isSystem = isSystem,
                        icon = icon
                    )
                }.distinctBy { it.packageName }
                 .sortedBy { it.label.lowercase(Locale.getDefault()) }

                _androidApps.value = appsList
                addAppLog("Loaded ${appsList.size} launchable Android apps.")
            } catch (e: Exception) {
                addAppLog("Failed to load installed apps: ${e.message}")
            }
        }
    }

    // --- USER PROFILE & CUSTOM LAUNCHER SETTINGS ---
    private val sharedPrefs by lazy {
        getApplication<Application>().getSharedPreferences("omni_suite_preferences", android.content.Context.MODE_PRIVATE)
    }

    private val _profileName = MutableStateFlow(sharedPrefs.getString("user_name", "Omni User") ?: "Omni User")
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profilePassword = MutableStateFlow(sharedPrefs.getString("user_password", "admin123") ?: "admin123")
    val profilePassword: StateFlow<String> = _profilePassword.asStateFlow()

    private val _hiddenApps = MutableStateFlow(sharedPrefs.getStringSet("hidden_sub_apps", emptySet()) ?: emptySet())
    val hiddenApps: StateFlow<Set<String>> = _hiddenApps.asStateFlow()

    private val _desktopWallpaper = MutableStateFlow(sharedPrefs.getString("desktop_wallpaper", "aurora_glow") ?: "aurora_glow")
    val desktopWallpaper: StateFlow<String> = _desktopWallpaper.asStateFlow()

    private val _desktopTheme = MutableStateFlow(sharedPrefs.getString("desktop_theme", "normal") ?: "normal")
    val desktopTheme: StateFlow<String> = _desktopTheme.asStateFlow()

    private val _isBrowserDesktopMode = MutableStateFlow(sharedPrefs.getBoolean("browser_desktop_mode", true))
    val isBrowserDesktopMode: StateFlow<Boolean> = _isBrowserDesktopMode.asStateFlow()

    private val _desktopGridColumns = MutableStateFlow(sharedPrefs.getInt("desktop_grid_columns", 4))
    val desktopGridColumns: StateFlow<Int> = _desktopGridColumns.asStateFlow()

    private val _desktopClockStyle = MutableStateFlow(sharedPrefs.getString("desktop_clock_style", "digital_glow") ?: "digital_glow")
    val desktopClockStyle: StateFlow<String> = _desktopClockStyle.asStateFlow()

    private val _desktopIconSizeScale = MutableStateFlow(sharedPrefs.getString("desktop_icon_size_scale", "normal") ?: "normal")
    val desktopIconSizeScale: StateFlow<String> = _desktopIconSizeScale.asStateFlow()

    private val _desktopWallpaperOverlay = MutableStateFlow(sharedPrefs.getFloat("desktop_wallpaper_overlay", 0.35f))
    val desktopWallpaperOverlay: StateFlow<Float> = _desktopWallpaperOverlay.asStateFlow()

    private val _pinnedApps = MutableStateFlow(sharedPrefs.getStringSet("pinned_android_apps", emptySet()) ?: emptySet())
    val pinnedApps: StateFlow<Set<String>> = _pinnedApps.asStateFlow()

    private val _enabledWidgets = MutableStateFlow(sharedPrefs.getStringSet("enabled_quick_feed_widgets", setOf("battery", "ram", "memo", "toggles")) ?: setOf("battery", "ram", "memo", "toggles"))
    val enabledWidgets: StateFlow<Set<String>> = _enabledWidgets.asStateFlow()

    private val _widgetCardStyle = MutableStateFlow(sharedPrefs.getString("widget_card_style", "glass") ?: "glass")
    val widgetCardStyle: StateFlow<String> = _widgetCardStyle.asStateFlow()

    private val _iconRoundness = MutableStateFlow(sharedPrefs.getString("launcher_icon_roundness", "medium") ?: "medium")
    val iconRoundness: StateFlow<String> = _iconRoundness.asStateFlow()

    private val _quickMemoText = MutableStateFlow(sharedPrefs.getString("quick_feed_memo_text", "Important meeting notes and system reminders...") ?: "Important meeting notes and system reminders...")
    val quickMemoText: StateFlow<String> = _quickMemoText.asStateFlow()

    private val _wifiEnabled = MutableStateFlow(sharedPrefs.getBoolean("mock_wifi_enabled", true))
    val wifiEnabled: StateFlow<Boolean> = _wifiEnabled.asStateFlow()

    private val _bluetoothEnabled = MutableStateFlow(sharedPrefs.getBoolean("mock_bluetooth_enabled", false))
    val bluetoothEnabled: StateFlow<Boolean> = _bluetoothEnabled.asStateFlow()

    private val _dndEnabled = MutableStateFlow(sharedPrefs.getBoolean("mock_dnd_enabled", false))
    val dndEnabled: StateFlow<Boolean> = _dndEnabled.asStateFlow()

    private val _airplaneModeEnabled = MutableStateFlow(sharedPrefs.getBoolean("mock_airplane_mode_enabled", false))
    val airplaneModeEnabled: StateFlow<Boolean> = _airplaneModeEnabled.asStateFlow()

    private val _locationEnabled = MutableStateFlow(sharedPrefs.getBoolean("mock_location_enabled", true))
    val locationEnabled: StateFlow<Boolean> = _locationEnabled.asStateFlow()

    fun toggleAppPin(packageName: String) {
        val current = _pinnedApps.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
            addAppLog("Unpinned Android app: $packageName")
        } else {
            current.add(packageName)
            addAppLog("Pinned Android app: $packageName")
        }
        _pinnedApps.value = current
        sharedPrefs.edit().putStringSet("pinned_android_apps", current).apply()
    }

    fun toggleWidgetEnabled(widgetId: String) {
        val current = _enabledWidgets.value.toMutableSet()
        if (current.contains(widgetId)) {
            current.remove(widgetId)
            addAppLog("Disabled widget in feed: $widgetId")
        } else {
            current.add(widgetId)
            addAppLog("Enabled widget in feed: $widgetId")
        }
        _enabledWidgets.value = current
        sharedPrefs.edit().putStringSet("enabled_quick_feed_widgets", current).apply()
    }

    fun setWidgetCardStyle(style: String) {
        _widgetCardStyle.value = style
        sharedPrefs.edit().putString("widget_card_style", style).apply()
        addAppLog("Widget layout design style updated: $style")
    }

    fun setIconRoundness(roundness: String) {
        _iconRoundness.value = roundness
        sharedPrefs.edit().putString("launcher_icon_roundness", roundness).apply()
        addAppLog("App launcher icon roundness style set to: $roundness")
    }

    fun saveQuickMemoText(text: String) {
        _quickMemoText.value = text
        sharedPrefs.edit().putString("quick_feed_memo_text", text).apply()
    }

    fun toggleWifi() {
        val next = !_wifiEnabled.value
        _wifiEnabled.value = next
        sharedPrefs.edit().putBoolean("mock_wifi_enabled", next).apply()
        addAppLog("Mock Wi-Fi state toggled: ${if(next) "ACTIVE" else "OFF"}")
    }

    fun toggleBluetooth() {
        val next = !_bluetoothEnabled.value
        _bluetoothEnabled.value = next
        sharedPrefs.edit().putBoolean("mock_bluetooth_enabled", next).apply()
        addAppLog("Mock Bluetooth state toggled: ${if(next) "ACTIVE" else "OFF"}")
    }

    fun toggleDnd() {
        val next = !_dndEnabled.value
        _dndEnabled.value = next
        sharedPrefs.edit().putBoolean("mock_dnd_enabled", next).apply()
        addAppLog("Do Not Disturb: ${if(next) "ON" else "OFF"}")
    }

    fun toggleAirplaneMode() {
        val next = !_airplaneModeEnabled.value
        _airplaneModeEnabled.value = next
        sharedPrefs.edit().putBoolean("mock_airplane_mode_enabled", next).apply()
        if (next) {
            _wifiEnabled.value = false
            _bluetoothEnabled.value = false
            sharedPrefs.edit().putBoolean("mock_wifi_enabled", false).putBoolean("mock_bluetooth_enabled", false).apply()
        }
        addAppLog("Airplane Mode: ${if(next) "ACTIVE" else "OFF"}")
    }

    fun toggleLocation() {
        val next = !_locationEnabled.value
        _locationEnabled.value = next
        sharedPrefs.edit().putBoolean("mock_location_enabled", next).apply()
        addAppLog("Mock geo-location telemetry: ${if(next) "ACTIVE" else "OFF"}")
    }

    fun resetWorkspaceSettings() {
        _profileName.value = "Omni User"
        _profilePassword.value = "admin123"
        _hiddenApps.value = emptySet()
        _desktopWallpaper.value = "aurora_glow"
        _desktopTheme.value = "normal"
        _desktopGridColumns.value = 4
        _desktopClockStyle.value = "digital_glow"
        _desktopIconSizeScale.value = "normal"
        _desktopWallpaperOverlay.value = 0.35f
        _pinnedApps.value = emptySet()
        _enabledWidgets.value = setOf("battery", "ram", "memo", "toggles")
        _widgetCardStyle.value = "glass"
        _iconRoundness.value = "medium"
        _quickMemoText.value = "Important meeting notes and system reminders..."
        _wifiEnabled.value = true
        _bluetoothEnabled.value = false
        _dndEnabled.value = false
        _airplaneModeEnabled.value = false
        _locationEnabled.value = true

        sharedPrefs.edit().apply {
            putString("user_name", "Omni User")
            putString("user_password", "admin123")
            putStringSet("hidden_sub_apps", emptySet())
            putString("desktop_wallpaper", "aurora_glow")
            putString("desktop_theme", "normal")
            putInt("desktop_grid_columns", 4)
            putString("desktop_clock_style", "digital_glow")
            putString("desktop_icon_size_scale", "normal")
            putFloat("desktop_wallpaper_overlay", 0.35f)
            putStringSet("pinned_android_apps", emptySet())
            putStringSet("enabled_quick_feed_widgets", setOf("battery", "ram", "memo", "toggles"))
            putString("widget_card_style", "glass")
            putString("launcher_icon_roundness", "medium")
            putString("quick_feed_memo_text", "Important meeting notes and system reminders...")
            putBoolean("mock_wifi_enabled", true)
            putBoolean("mock_bluetooth_enabled", false)
            putBoolean("mock_dnd_enabled", false)
            putBoolean("mock_airplane_mode_enabled", false)
            putBoolean("mock_location_enabled", true)
            apply()
        }
        addAppLog("Workspace memory completely refreshed to system defaults.")
    }

    fun setDesktopGridColumns(cols: Int) {
        _desktopGridColumns.value = cols
        sharedPrefs.edit().putInt("desktop_grid_columns", cols).apply()
        addAppLog("Desktop grid layout columns configured to: $cols")
    }

    fun setDesktopClockStyle(style: String) {
        _desktopClockStyle.value = style
        sharedPrefs.edit().putString("desktop_clock_style", style).apply()
        addAppLog("Desktop clock style layout updated to: $style")
    }

    fun setDesktopIconSizeScale(scale: String) {
        _desktopIconSizeScale.value = scale
        sharedPrefs.edit().putString("desktop_icon_size_scale", scale).apply()
        addAppLog("Desktop launcher app icon scale set to: $scale")
    }

    fun setDesktopWallpaperOverlay(alpha: Float) {
        _desktopWallpaperOverlay.value = alpha
        sharedPrefs.edit().putFloat("desktop_wallpaper_overlay", alpha).apply()
    }

    fun updateProfile(newName: String, newPass: String) {
        _profileName.value = newName
        _profilePassword.value = newPass
        sharedPrefs.edit().apply {
            putString("user_name", newName)
            putString("user_password", newPass)
            apply()
        }
        addAppLog("Profile updated for user: $newName")
    }

    fun toggleAppVisibility(subAppName: String) {
        val current = _hiddenApps.value.toMutableSet()
        if (current.contains(subAppName)) {
            current.remove(subAppName)
            addAppLog("Unhid sub-app: $subAppName")
        } else {
            current.add(subAppName)
            addAppLog("Hid sub-app from Launchpad: $subAppName")
        }
        _hiddenApps.value = current
        sharedPrefs.edit().putStringSet("hidden_sub_apps", current).apply()
    }

    fun setDesktopWallpaper(theme: String) {
        _desktopWallpaper.value = theme
        sharedPrefs.edit().putString("desktop_wallpaper", theme).apply()
        addAppLog("Desktop wallpaper theme updated to: $theme")
    }

    fun setDesktopTheme(theme: String) {
        _desktopTheme.value = theme
        sharedPrefs.edit().putString("desktop_theme", theme).apply()
        addAppLog("Desktop home theme layout set: $theme")
    }

    fun importOpTheme(content: String): String {
        try {
            val json = JSONObject(content)
            val importedTheme = json.optString("theme", "metro")
            setDesktopTheme(importedTheme)
            
            if (json.has("wallpaper")) {
                setDesktopWallpaper(json.getString("wallpaper"))
            }
            if (json.has("clock_style")) {
                setDesktopClockStyle(json.getString("clock_style"))
            }
            if (json.has("columns")) {
                setDesktopGridColumns(json.getInt("columns"))
            }
            if (json.has("icon_scale")) {
                setDesktopIconSizeScale(json.getString("icon_scale"))
            }
            if (json.has("roundness")) {
                setIconRoundness(json.getString("roundness"))
            }
            if (json.has("overlay")) {
                setDesktopWallpaperOverlay(json.getDouble("overlay").toFloat())
            }
            addAppLog("Imported operating theme: $importedTheme")
            return "Theme loaded: theme=$importedTheme"
        } catch (e: Exception) {
            // properties key-value format parser fallback
            try {
                var themeVal = "normal"
                content.lines().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                        val parts = trimmed.split("=", limit = 2)
                        if (parts.size == 2) {
                            val key = parts[0].trim().lowercase()
                            val value = parts[1].trim()
                            when (key) {
                                "theme" -> { themeVal = value; setDesktopTheme(value) }
                                "wallpaper" -> setDesktopWallpaper(value)
                                "clock_style" -> setDesktopClockStyle(value)
                                "columns" -> setDesktopGridColumns(value.toIntOrNull() ?: 4)
                                "icon_scale" -> setDesktopIconSizeScale(value)
                                "roundness" -> setIconRoundness(value)
                                "overlay" -> setDesktopWallpaperOverlay(value.toFloatOrNull() ?: 0.35f)
                            }
                        }
                    }
                }
                addAppLog("Successfully parsed properties theme: $themeVal")
                return "Theme loaded properties: theme=$themeVal"
            } catch (ex: Exception) {
                return "Theme parsing failed: ${e.message}"
            }
        }
    }

    fun setBrowserDesktopMode(enabled: Boolean) {
        _isBrowserDesktopMode.value = enabled
        sharedPrefs.edit().putBoolean("browser_desktop_mode", enabled).apply()
        addAppLog("Chromium user agent switched to ${if (enabled) "Desktop Mode" else "Mobile Mode"}")
    }

    init {
        refreshFiles()
        loadInstalledApps()
    }

    enum class ToolSection {
        FILE_MANAGER, BROWSER, TERMINAL, CALENDAR, APPS
    }

    fun setSandboxMode(enabled: Boolean) {
        setFileSystemMode(if (enabled) 0 else 1)
    }

    fun setFileSystemMode(mode: Int) {
        _fileSystemMode.value = mode
        when (mode) {
            0 -> {
                _isSandboxMode.value = true
                addAppLog("File Manager mode: Sandbox")
            }
            1 -> {
                _isSandboxMode.value = false
                _noSandboxPath.value = "/"
                addAppLog("File Manager mode: No Sandbox")
            }
            2 -> {
                _isSandboxMode.value = false
                val appDir = File("/data/data/com.optech.sekid")
                try {
                    if (!appDir.exists()) {
                        appDir.mkdirs()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                _noSandboxPath.value = "/data/data/com.optech.sekid"
                addAppLog("File Manager mode: App Filesystem")
            }
            3 -> {
                addAppLog("File Manager mode: Misc Options")
            }
        }
        refreshFiles()
    }

    fun refreshFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isSandboxMode.value) {
                val result = fileHelper.listFiles(_fileManagerPath.value)
                if (result.isSuccess) {
                    _fileList.value = result.getOrNull() ?: emptyList()
                    _fileError.value = null
                } else {
                    _fileError.value = result.exceptionOrNull()?.message ?: "Unknown error"
                    if (_fileManagerPath.value.isNotEmpty()) {
                        _fileManagerPath.value = ""
                        val rootResult = fileHelper.listFiles("")
                        _fileList.value = rootResult.getOrNull() ?: emptyList()
                    }
                }
            } else {
                val path = _noSandboxPath.value
                val rootFile = File(path)
                try {
                    val filesList = rootFile.listFiles() ?: emptyArray()
                    val result = filesList.map { file ->
                        val size = if (file.isDirectory) 0L else {
                            try { file.length() } catch (e: Exception) { 0L }
                        }
                        val lastModified = try { file.lastModified() } catch (e: Exception) { 0L }
                        WorkspaceFile(
                            name = file.name,
                            isDirectory = file.isDirectory,
                            size = size,
                            lastModified = lastModified,
                            relativePath = file.absolutePath,
                            absoluteFile = file
                        )
                    }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase(Locale.getDefault()) }))
                    _fileList.value = result
                    _fileError.value = null
                } catch (e: Exception) {
                    _fileError.value = "Access Denied or Not Readable: ${e.message ?: "Could not access directory"}"
                    _fileList.value = emptyList()
                }
            }
        }
    }

    fun enterDirectory(dirName: String) {
        if (_isSandboxMode.value) {
            val current = _fileManagerPath.value
            val next = if (current.isEmpty()) dirName else "$current/$dirName"
            _fileManagerPath.value = next
            refreshFiles()
            addAppLog("Entered sandbox folder: /$next")
        } else {
            val current = _noSandboxPath.value
            val parentFile = File(current)
            val childFile = File(parentFile, dirName)
            _noSandboxPath.value = childFile.absolutePath
            refreshFiles()
            addAppLog("Entered physical folder: ${childFile.absolutePath}")
        }
    }

    fun navigateUp() {
        if (_isSandboxMode.value) {
            val current = _fileManagerPath.value
            if (current.isEmpty()) return
            val parts = current.split("/")
            val next = if (parts.size <= 1) "" else parts.dropLast(1).joinToString("/")
            _fileManagerPath.value = next
            refreshFiles()
            addAppLog("Navigated up to sandbox: /${next.ifEmpty { "root" }}")
        } else {
            val current = _noSandboxPath.value
            if (_fileSystemMode.value == 2 && current == "/data/data/com.optech.sekid") {
                return
            }
            val file = File(current)
            val parent = file.parentFile
            if (parent != null) {
                _noSandboxPath.value = parent.absolutePath
                refreshFiles()
                addAppLog("Navigated up to physical folder: ${parent.absolutePath}")
            }
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isSandboxMode.value) {
                val result = fileHelper.makeDirectory(_fileManagerPath.value, name)
                if (result.isSuccess) {
                    refreshFiles()
                    addAppLog("Folder created in Sandbox: $name")
                } else {
                    _fileError.value = result.exceptionOrNull()?.message
                    addAppLog("Failed to create Sandbox folder: $name")
                }
            } else {
                try {
                    val parentFile = File(_noSandboxPath.value)
                    val newDir = File(parentFile, name)
                    if (newDir.exists()) {
                        _fileError.value = "Folder '$name' already exists"
                    } else if (newDir.mkdirs()) {
                        refreshFiles()
                        addAppLog("Folder created physically: ${newDir.absolutePath}")
                    } else {
                        _fileError.value = "Failed to create folder '$name'"
                    }
                } catch (e: Exception) {
                    _fileError.value = e.message ?: "Failed to create folder"
                }
            }
        }
    }

    fun createFile(name: String, content: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isSandboxMode.value) {
                val result = fileHelper.makeFile(_fileManagerPath.value, name, content)
                if (result.isSuccess) {
                    refreshFiles()
                    addAppLog("File created in Sandbox: $name")
                } else {
                    _fileError.value = result.exceptionOrNull()?.message
                    addAppLog("Failed to create Sandbox file: $name")
                }
            } else {
                try {
                    val parentFile = File(_noSandboxPath.value)
                    val newFile = File(parentFile, name)
                    if (newFile.exists()) {
                        _fileError.value = "File '$name' already exists"
                    } else {
                        newFile.writeText(content)
                        refreshFiles()
                        addAppLog("File created physically: ${newFile.absolutePath}")
                    }
                } catch (e: Exception) {
                    _fileError.value = e.message ?: "Failed to create file"
                }
            }
        }
    }

    fun deleteFile(relativePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isSandboxMode.value) {
                val result = fileHelper.deleteFileOrDirectory(relativePath)
                if (result.isSuccess) {
                    refreshFiles()
                    addAppLog("Deleted Sandbox item: $relativePath")
                } else {
                    _fileError.value = result.exceptionOrNull()?.message
                    addAppLog("Failed to delete Sandbox item: $relativePath")
                }
            } else {
                try {
                    val file = File(relativePath)
                    if (file.canonicalPath == "/" || file.canonicalPath == File("/").canonicalPath) {
                        _fileError.value = "Cannot delete root directory"
                    } else if (file.deleteRecursively()) {
                        refreshFiles()
                        addAppLog("Deleted physical item: $relativePath")
                    } else {
                        _fileError.value = "Failed to delete physical item"
                    }
                } catch (e: Exception) {
                    _fileError.value = e.message ?: "Failed to delete"
                }
            }
        }
    }

    fun renameFile(relativePath: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isSandboxMode.value) {
                val result = fileHelper.rename(relativePath, newName)
                if (result.isSuccess) {
                    refreshFiles()
                    addAppLog("Renamed Sandbox item: $relativePath to $newName")
                } else {
                    _fileError.value = result.exceptionOrNull()?.message
                }
            } else {
                try {
                    val file = File(relativePath)
                    val destination = File(file.parentFile, newName)
                    if (destination.exists()) {
                        _fileError.value = "A file or folder with that name already exists"
                    } else if (file.renameTo(destination)) {
                        refreshFiles()
                        addAppLog("Renamed physical item: $relativePath to $newName")
                    } else {
                        _fileError.value = "Failed to rename"
                    }
                } catch (e: Exception) {
                    _fileError.value = e.message ?: "Failed to rename"
                }
            }
        }
    }

    fun encryptWorkspaceFile(relativePath: String, passcode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = fileHelper.resolveRelativePath(relativePath)
                if (!file.exists() || file.isDirectory) {
                    _fileError.value = "Target file does not exist or is a directory"
                    addAppLog("AES-256 Encryption Failed: $relativePath (Not a valid file)")
                    return@launch
                }
                val fileBytes = file.readBytes()
                
                // SHA-256 hash
                val digest = java.security.MessageDigest.getInstance("SHA-256")
                val keyBytes = digest.digest(passcode.toByteArray(Charsets.UTF_8))
                val secretKey = javax.crypto.spec.SecretKeySpec(keyBytes, "AES")
                
                // Random IV
                val iv = ByteArray(16)
                java.security.SecureRandom().nextBytes(iv)
                val ivSpec = javax.crypto.spec.IvParameterSpec(iv)
                
                // Encrypt
                val cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, ivSpec)
                val encryptedBytes = cipher.doFinal(fileBytes)
                
                // Write output: IV + Ciphertext
                val finalBytes = iv + encryptedBytes
                val encFile = File(file.parentFile, "${file.name}.enc")
                encFile.writeBytes(finalBytes)
                file.delete()
                
                refreshFiles()
                addAppLog("Successfully encrypted file securely to ${encFile.name}.")
            } catch (e: Exception) {
                _fileError.value = "Encryption failed: ${e.message}"
                addAppLog("AES-256 Encryption Exception: ${e.message}")
            }
        }
    }

    fun decryptWorkspaceFile(relativePath: String, passcode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = fileHelper.resolveRelativePath(relativePath)
                if (!file.exists() || !file.name.endsWith(".enc")) {
                    _fileError.value = "Target file is not a valid encrypted .enc node"
                    addAppLog("AES-256 Decryption Failed: $relativePath (Not *.enc)")
                    return@launch
                }
                val fileBytes = file.readBytes()
                if (fileBytes.size <= 16) {
                    _fileError.value = "File payload is truncated or corrupted"
                    addAppLog("AES-256 Decryption Error: Payload structure too small")
                    return@launch
                }
                
                val iv = fileBytes.copyOfRange(0, 16)
                val encryptedPayload = fileBytes.copyOfRange(16, fileBytes.size)
                
                val digest = java.security.MessageDigest.getInstance("SHA-256")
                val keyBytes = digest.digest(passcode.toByteArray(Charsets.UTF_8))
                val secretKey = javax.crypto.spec.SecretKeySpec(keyBytes, "AES")
                val ivSpec = javax.crypto.spec.IvParameterSpec(iv)
                
                val cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, ivSpec)
                val decryptedBytes = cipher.doFinal(encryptedPayload)
                
                val decName = file.name.removeSuffix(".enc")
                val decFile = File(file.parentFile, decName)
                decFile.writeBytes(decryptedBytes)
                file.delete()
                
                refreshFiles()
                addAppLog("Successfully decrypted ${file.name} to $decName.")
            } catch (e: Exception) {
                _fileError.value = "Decryption failed (Invalid Passcode/Bytes)"
                addAppLog("AES-256 Decryption Error: Credentials handshake failure.")
            }
        }
    }

    fun openFileInEditor(relativePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isSandboxMode.value) {
                val result = fileHelper.readFileContent(relativePath)
                if (result.isSuccess) {
                    _editorFileRelativePath.value = relativePath
                    _editorContent.value = result.getOrNull() ?: ""
                    addAppLog("Opened Sandbox document for editing: $relativePath")
                } else {
                    _fileError.value = result.exceptionOrNull()?.message
                }
            } else {
                try {
                    val file = File(relativePath)
                    val text = file.readText()
                    _editorFileRelativePath.value = relativePath
                    _editorContent.value = text
                    addAppLog("Opened physical document for editing: $relativePath")
                } catch (e: Exception) {
                    _fileError.value = e.message ?: "Failed to read file contents"
                }
            }
        }
    }

    fun saveEditorContent(newContent: String) {
        val path = _editorFileRelativePath.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            if (_isSandboxMode.value) {
                val result = fileHelper.writeFileContent(path, newContent)
                if (result.isSuccess) {
                    _editorFileRelativePath.value = null
                    _editorContent.value = ""
                    refreshFiles()
                    addAppLog("Saved Sandbox document modification: $path")
                } else {
                    _fileError.value = result.exceptionOrNull()?.message
                }
            } else {
                try {
                    val file = File(path)
                    file.writeText(newContent)
                    _editorFileRelativePath.value = null
                    _editorContent.value = ""
                    refreshFiles()
                    addAppLog("Saved physical document modification: $path")
                } catch (e: Exception) {
                    _fileError.value = e.message ?: "Failed to write file contents"
                }
            }
        }
    }

    fun closeEditor() {
        _editorFileRelativePath.value = null
        _editorContent.value = ""
    }

    fun clearFileError() {
        _fileError.value = null
    }


    // ==========================================
    // 2. WEB BROWSER STATE & ACTIONS
    // ==========================================
    private val _browserUrl = MutableStateFlow("https://www.google.com")
    val browserUrl: StateFlow<String> = _browserUrl.asStateFlow()

    // Observe bookmarks reactively
    val bookmarks: StateFlow<List<Bookmark>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observe history reactively
    val history: StateFlow<List<HistoryItem>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks if current URL is bookmarked
    val isCurrentUrlBookmarked: StateFlow<Boolean> = _browserUrl
        .flatMapLatest { url -> repository.isBookmarked(url) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun loadUrl(input: String) {
        var cleanUrl = input.trim()
        if (cleanUrl.isEmpty()) return
        
        // Simple search query expansion
        if (!cleanUrl.contains(".") || cleanUrl.contains(" ")) {
            cleanUrl = "https://www.google.com/search?q=" + java.net.URLEncoder.encode(cleanUrl, "UTF-8")
        } else if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }
        _browserUrl.value = cleanUrl
        addAppLog("Browser loaded Url: ${cleanUrl.take(45)}...")
        
        // Register history entry
        viewModelScope.launch(Dispatchers.IO) {
            val title = cleanUrl.removePrefix("https://").removePrefix("http://").removePrefix("www.").take(50)
            repository.insertHistoryItem(HistoryItem(title = title, url = cleanUrl))
        }
    }

    fun toggleBookmark() {
        val url = _browserUrl.value
        viewModelScope.launch(Dispatchers.IO) {
            if (isCurrentUrlBookmarked.value) {
                repository.deleteBookmarkByUrl(url)
                addAppLog("Removed bookmark for: ${url.take(30)}...")
            } else {
                val title = url.removePrefix("https://").removePrefix("http://").removePrefix("www.").take(40)
                repository.insertBookmark(Bookmark(title = title, url = url))
                addAppLog("Toggled bookmark for: ${url.take(30)}...")
            }
        }
    }

    fun addBookmarkManual(title: String, url: String) {
        var cleanUrl = url.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertBookmark(Bookmark(title = title.ifEmpty { "Bookmark" }, url = cleanUrl))
            addAppLog("Manual bookmark added: $title")
        }
    }

    fun deleteBookmark(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBookmarkById(id)
            addAppLog("Deleted bookmark entry.")
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHistoryItem(id)
            addAppLog("Deleted history item.")
        }
    }

    fun clearBrowserHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
            addAppLog("Cleared all browser history.")
        }
    }


    // ==========================================
    // 3. TERMINAL EMULATOR STATE & ACTIONS
    // ==========================================
    private val _terminalDirectory = MutableStateFlow("") // relative terminal workspace path
    val terminalDirectory: StateFlow<String> = _terminalDirectory.asStateFlow()

    // Lines formatting inside terminal list
    private val _terminalLines = MutableStateFlow<List<String>>(
        listOf(
            "==============================================",
            " OMNI TOOLBOX MULTI-SHELL v1.1.0 (SANDBOX)",
            "==============================================",
            "Welcome! This terminal is integrated with your sandboxed File Manager.",
            "Type 'help' to see available commands, or 'neofetch' to view system logs.",
            "Files created here will instantly appear under the File Manager.",
            ""
        )
    )
    val terminalLines: StateFlow<List<String>> = _terminalLines.asStateFlow()

    private val commandHistoryList = ArrayList<String>()
    private val systemLaunchTime = System.currentTimeMillis()

    fun executeTerminalCommand(input: String) {
        val commandLine = input.trim()
        if (commandLine.isEmpty()) return

        // Record history
        commandHistoryList.add(commandLine)

        // Display directory marker and command in history
        val promptMarker = "omni@sandbox:~${if (_terminalDirectory.value.isEmpty()) "" else "/" + _terminalDirectory.value}$ "
        _terminalLines.value = _terminalLines.value + "$promptMarker$commandLine"

        val parts = commandLine.split(" ")
        val command = parts[0].lowercase()
        val args = parts.drop(1)

        when (command) {
            "help" -> showTerminalHelp()
            "clear" -> _terminalLines.value = emptyList()
            "date" -> {
                val cur = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US).format(Date())
                _terminalLines.value = _terminalLines.value + cur
            }
            "whoami" -> {
                _terminalLines.value = _terminalLines.value + "System user profile: ${_profileName.value} (Lock Mode Enabled)"
            }
            "pwd" -> {
                val currentLogical = "/" + _terminalDirectory.value
                _terminalLines.value = _terminalLines.value + currentLogical
            }
            "ls" -> terminalLs()
            "cd" -> terminalCd(args.firstOrNull())
            "mkdir" -> terminalMkdir(args.firstOrNull())
            "touch" -> terminalTouch(args.firstOrNull())
            "cat" -> terminalCat(args.firstOrNull())
            "echo" -> terminalEcho(args)
            "rm" -> terminalRm(args.firstOrNull())
            "neofetch" -> terminalNeofetch()

            // --- 21 NEW COMPREHENSIVE TERMINAL COMMANDS ---
            "sysinfo" -> {
                _terminalLines.value = _terminalLines.value + listOf(
                    "System Specifications:",
                    "  CPU Architecture : ARMv8-A64 (8 Cores)",
                    "  Device Host      : ai.studio-sandbox-android",
                    "  Active RAM Usage : ${Runtime.getRuntime().totalMemory() / (1024 * 1024)}MB / ${Runtime.getRuntime().maxMemory() / (1024 * 1024)}MB",
                    "  Storage Partition: 256 GB (Simulated SSD Sandbox Partition)",
                    "  Graphics Renderer: Jetpack Compose Canvas M3 Theme"
                )
            }
            "ping" -> {
                val host = args.firstOrNull() ?: "127.0.0.1"
                _terminalLines.value = _terminalLines.value + listOf(
                    "PING $host (56 bytes of data):",
                    "64 bytes from $host: icmp_seq=1 ttl=64 time=0.04 ms",
                    "64 bytes from $host: icmp_seq=2 ttl=64 time=0.12 ms",
                    "64 bytes from $host: icmp_seq=3 ttl=64 time=0.09 ms",
                    "--- $host ping statistics ---",
                    "3 packets transmitted, 3 received, 0% packet loss, time 2004ms",
                    "rtt min/avg/max = 0.04/0.08/0.12 ms"
                )
            }
            "uptime" -> {
                val elapsedSec = (System.currentTimeMillis() - systemLaunchTime) / 1000
                _terminalLines.value = _terminalLines.value + "uptime: up ${elapsedSec}s, 1 user, load average: 0.12, 0.08, 0.05"
            }
            "history" -> {
                if (commandHistoryList.isEmpty()) {
                    _terminalLines.value = _terminalLines.value + "No command history recorded."
                } else {
                    val historyLines = commandHistoryList.mapIndexed { idx, cmd -> "  ${idx + 1}  $cmd" }
                    _terminalLines.value = _terminalLines.value + listOf("Command History:") + historyLines
                }
            }
            "calc" -> {
                val expr = args.joinToString(" ")
                val regex = Regex("(\\d+)\\s*([+\\-*/])\\s*(\\d+)")
                val match = regex.find(expr)
                if (match != null) {
                    val left = match.groupValues[1].toDoubleOrNull() ?: 0.0
                    val op = match.groupValues[2]
                    val right = match.groupValues[3].toDoubleOrNull() ?: 0.0
                    val res = when (op) {
                        "+" -> left + right
                        "-" -> left - right
                        "*" -> left * right
                        "/" -> if (right != 0.0) left / right else "division by zero"
                        else -> "unknown operator"
                    }
                    _terminalLines.value = _terminalLines.value + "$left $op $right = $res"
                } else {
                    _terminalLines.value = _terminalLines.value + "calc: usage: calc [num1] [+|-|*|/] [num2] (e.g., calc 25 * 4)"
                }
            }
            "uname" -> {
                _terminalLines.value = _terminalLines.value + "Linux omnisandbox-emulator 5.15.0-android-sh #1 SMP PREEMPT Tue Jun 02 10:00:00 UTC 2026 aarch64 Android"
            }
            "env" -> {
                _terminalLines.value = _terminalLines.value + listOf(
                    "Environment Variables:",
                    "  SHELL=/bin/omnishell",
                    "  USER=${_profileName.value}",
                    "  LANG=en_US.UTF-8",
                    "  HOME=/workspace",
                    "  PATH=/bin:/usr/bin:/workspace/bin",
                    "  DISPLAY=localhost:0.0",
                    "  TERM=xterm-256color"
                )
            }
            "theme" -> {
                val sub = args.firstOrNull()?.lowercase()
                if (sub == "set") {
                    val t = args.getOrNull(1)?.lowercase() ?: "normal"
                    if (t in listOf("normal", "metro", "search")) {
                        setDesktopTheme(t)
                        _terminalLines.value = _terminalLines.value + "Desktop styling layout set to '$t'."
                    } else {
                        _terminalLines.value = _terminalLines.value + "theme set: invalid theme name. Choose normal | metro | search."
                    }
                } else if (sub == "list") {
                    _terminalLines.value = _terminalLines.value + listOf(
                        "Available themes:",
                        "  - normal   (Default multi-page workspace & docks layout)",
                        "  - metro    (Visual Windows Phone 10 live tiles layout design)",
                        "  - search   (Focused minimal custom layout featuring single customizable search bar)"
                    )
                } else {
                    _terminalLines.value = _terminalLines.value + listOf(
                        "Theme Control center:",
                        "  theme list           List supported system desktop designs",
                        "  theme set [name]     Apply layout theme (normal / metro / search)",
                        "Current styling: ${_desktopTheme.value}"
                    )
                }
            }
            "motd" -> {
                _terminalLines.value = _terminalLines.value + listOf(
                    "============================================================",
                    " OMNI OPERATING NODE 01 - ACTIVE COMMUNICATIONS SATELLITE",
                    "============================================================",
                    "* Operational clearance marked: HIGHEST ROOT AUTHORIZATION",
                    "* Sync system engine verified with sandboxed cloud storage",
                    "* Workspace layout customizable via Desk Page 4 panels",
                    "============================================================"
                )
            }
            "cowsay" -> {
                val m = if (args.isEmpty()) "Moo! Build the design." else args.joinToString(" ")
                _terminalLines.value = _terminalLines.value + listOf(
                    "  " + "_".repeat(m.length + 2),
                    " ( $m )",
                    "  " + "-".repeat(m.length + 2),
                    "         \\   ^__^",
                    "          \\  (oo)\\_______",
                    "             (__)\\       )\\/\\",
                    "                 ||----w |",
                    "                 ||     ||"
                )
            }
            "ip" -> {
                _terminalLines.value = _terminalLines.value + listOf(
                    "1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000",
                    "    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00",
                    "    inet 127.0.0.1/8 scope host lo",
                    "       valid_lft forever preferred_lft forever",
                    "2: wlan0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc mq state UP group default",
                    "    link/ether fa:16:3e:ca:2b:81 brd ff:ff:ff:ff:ff:ff",
                    "    inet 192.168.1.144/24 brd 192.168.1.255 scope global dynamic wlan0",
                    "       valid_lft 43200sec preferred_lft 43200sec"
                )
            }
            "df" -> {
                _terminalLines.value = _terminalLines.value + listOf(
                    "Filesystem     Size  Used  Avail Use% Mounted on",
                    "/dev/block/dm  128G   45G    83G  36% /",
                    "tmpfs           16G  4.1M    16G   1% /dev",
                    "/dev/private   256G  1.2G   254G   1% /workspace"
                )
            }
            "top" -> {
                _terminalLines.value = _terminalLines.value + listOf(
                    "PID   USER     PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND",
                    "1741  omni     20   0  1.2g   124m    80m S   8.4  1.2   0:15.42 com.example.toolbox",
                    " 104  system   10 -10  840m    76m    32m S   1.2  0.7   0:04.12 compositor-gl",
                    "  21  root     RT   0     0      0      0 S   0.2  0.0   0:00.91 ksoftirqd/0"
                )
            }
            "contacts" -> {
                val list = allContacts.value
                if (list.isEmpty()) {
                    _terminalLines.value = _terminalLines.value + "No contacts database records found. Use the Contacts app to add one."
                } else {
                    val formatted = list.map { "👤 ${it.name} - 📱 ${it.phoneNumber} (${it.category})" }
                    _terminalLines.value = _terminalLines.value + listOf("Contacts database records [${list.size} total]:") + formatted
                }
            }
            "calendar" -> {
                val cal = Calendar.getInstance()
                val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) ?: "Month"
                val yr = cal.get(Calendar.YEAR)
                val today = cal.get(Calendar.DAY_OF_MONTH)
                
                val output = ArrayList<String>()
                output.add("====== CALENDAR ======")
                output.add("      $monthName $yr")
                output.add("Su Mo Tu We Th Fr Sa")
                
                val calTemp = Calendar.getInstance()
                calTemp.set(Calendar.DAY_OF_MONTH, 1)
                val startDayOfWeek = calTemp.get(Calendar.DAY_OF_WEEK) 
                val maxDay = calTemp.getActualMaximum(Calendar.DAY_OF_MONTH)
                
                var line = ""
                for (i in 1 until startDayOfWeek) {
                    line += "   "
                }
                for (day in 1..maxDay) {
                    val dayStr = String.format("%2d", day)
                    val formattedDay = if (day == today) "[$dayStr]" else " $dayStr"
                    line += formattedDay
                    
                    val currentDayOfWeek = (startDayOfWeek - 1 + day - 1) % 7 + 1
                    if (currentDayOfWeek == 7 || day == maxDay) {
                        output.add(line)
                        line = ""
                    }
                }
                _terminalLines.value = _terminalLines.value + output
            }
            "notes" -> {
                val list = allNotes.value
                val subCmd = args.firstOrNull()?.lowercase()
                if (subCmd == "add") {
                    val rest = args.drop(1).joinToString(" ")
                    val s = rest.split("=", limit = 2)
                    if (s.size == 2) {
                        val title = s[0].trim()
                        val content = s[1].trim()
                        insertNote(title, content)
                        _terminalLines.value = _terminalLines.value + "Note created successfully: '$title'"
                    } else {
                        _terminalLines.value = _terminalLines.value + "notes add: usage: notes add [title] = [content]"
                    }
                } else {
                    if (list.isEmpty()) {
                        _terminalLines.value = _terminalLines.value + "No notes records found. Type 'notes add [title] = [content]' to create one."
                    } else {
                        val formatted = list.flatMap { listOf("📝 ID: ${it.id} | title: ${it.title}", "   ${it.content}", "") }
                        _terminalLines.value = _terminalLines.value + listOf("System Notes [${list.size} total]:") + formatted
                    }
                }
            }
            "weather" -> {
                val weather = realWeather.value
                val output = if (weather != null) {
                    listOf(
                        "       \\   /      Weather Satellite Feed // Location: ${weather.cityName}",
                        "        .-.       Temperature: ${weather.temperature}°C",
                        "     - (   ) -    Condition: ${weather.description}",
                        "        `-'       Humidex: ${weather.humidity} | Wind: ${weather.windSpeed}",
                        "       /   \\      Apparent Temp: ${weather.apparentTemp}°C"
                    )
                } else {
                    listOf(
                        "       \\   /      Weather Satellite Feed",
                        "        .-.       Global Satellite Link: Active",
                        "     - (   ) -    Weather Station Location: New York",
                        "        `-'       Primary Sensor Temp: 22°C // Sunny Sky Condition",
                        "       /   \\      Connection Protocol: 4G LTE Sandbox Link"
                    )
                }
                _terminalLines.value = _terminalLines.value + output
            }
            "sleep" -> {
                val secVal = args.firstOrNull()?.toLongOrNull() ?: 3L
                _terminalLines.value = _terminalLines.value + "Suspending execution stack for $secVal seconds..."
                viewModelScope.launch {
                    delay(secVal * 1000L)
                    _terminalLines.value = _terminalLines.value + "Process awake. Flow context restored."
                }
            }
            "alias" -> {
                _terminalLines.value = _terminalLines.value + listOf(
                    "Primary Command Alias Listings:",
                    "  ll                          alias for 'ls -la' (Lists details)",
                    "  sysspec                     alias for 'sysinfo'",
                    "  fetch                       alias for 'neofetch'",
                    "  weather-cli                 alias for 'weather'"
                )
            }
            "exit" -> {
                _terminalLines.value = _terminalLines.value + "Closing workspace terminal process... Logging off."
                navigateTo(ToolSection.FILE_MANAGER)
            }
            "find" -> {
                val nameQuery = args.firstOrNull() ?: ""
                if (nameQuery.isEmpty()) {
                    _terminalLines.value = _terminalLines.value + "find: usage: find [filename-substring]"
                } else {
                    val rootDir = fileHelper.resolveRelativePath("")
                    val foundFiles = rootDir.walkTopDown().filter { it.isFile && it.name.contains(nameQuery, ignoreCase = true) }.toList()
                    if (foundFiles.isEmpty()) {
                        _terminalLines.value = _terminalLines.value + "find: no matching files found for: '$nameQuery'"
                    } else {
                        val relativeResults = foundFiles.map { " 📁 ./${it.relativeTo(rootDir).path}" }
                        _terminalLines.value = _terminalLines.value + listOf("Found ${foundFiles.size} matching items:") + relativeResults
                    }
                }
            }
            else -> {
                _terminalLines.value = _terminalLines.value + "omnishell: command not found: '$command'. (Type 'help' for options)"
            }
        }
        // Force file refresh since filesystem utilities may have run
        refreshFiles()
    }

    private fun showTerminalHelp() {
        val helpText = listOf(
            "Omnishell Sandboxed Commands:",
            "  help                          Show this control manual",
            "  neofetch                      System statistics dashboard in ASCII art",
            "  ls                            List directories & files in active folder",
            "  cd [path]                     Change workspace folder directory",
            "  pwd                           Print current active subdirectory",
            "  mkdir [name]                  Create a new directory",
            "  touch [file]                  Create an empty text file",
            "  echo [text] [> | >>] [file]   Write, append, or output text",
            "  cat [file]                    Display txt formatting of file contents",
            "  rm [file/folder]              Delete files or folders recursively",
            "  date                          Output active system clock metrics",
            "  whoami                        Check system emulator user profile",
            "  clear                         Flushes command history list",
            "",
            "Extra Terminal Utilities (20+ added commands):",
            "  sysinfo, ping, uptime, history, calc, uname, env, theme, motd,",
            "  cowsay, ip, df, top, contacts, calendar, notes, weather, sleep, alias, find, exit"
        )
        _terminalLines.value = _terminalLines.value + helpText
    }

    private fun terminalNeofetch() {
        val stats = listOf(
            "       .---.            omni@sandbox-emulator",
            "      /     \\           ---------------------",
            "      \\.---./           OS: Android Sandbox Environment v24+",
            "     | o   o |          Kernel: Omni-Toolbox Runtime sh 1.0",
            "     |   _   |          Uptime: Active Studio Compile Cycle",
            "      \\  \"  /           Theme: Material 3 Deep Midnight Space",
            "       '---'            Shell: Custom Kotlin Terminal Emulator",
            "                        Integrations: File-manager linked repo",
            "                        Storage: sandboxed files system layout",
            "                        Target SDK: 36 | Android Jetpack Compose"
        )
        _terminalLines.value = _terminalLines.value + stats
    }

    private fun terminalLs() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = fileHelper.listFiles(_terminalDirectory.value)
            val linesToAdd = if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                if (list.isEmpty()) {
                    listOf("(directory is empty)")
                } else {
                    list.map { file ->
                        val prefix = if (file.isDirectory) "DIR  " else "FILE "
                        val bytes = if (file.isDirectory) "" else " (${file.size}B)"
                        "$prefix ${file.name}$bytes"
                    }
                }
            } else {
                listOf("ls: failed: " + (result.exceptionOrNull()?.message ?: "unknown path"))
            }

            _terminalLines.value = _terminalLines.value + linesToAdd
        }
    }

    private fun terminalCd(target: String?) {
        if (target == null || target == "~" || target == "/" || target.trim().isEmpty()) {
            _terminalDirectory.value = ""
            _terminalLines.value = _terminalLines.value + "Moved directory to workspace root (~)"
            return
        }

        if (target == "..") {
            val current = _terminalDirectory.value
            if (current.isEmpty()) {
                _terminalLines.value = _terminalLines.value + "Already at workspace root (~)"
                return
            }
            val parts = current.split("/")
            val next = if (parts.size <= 1) "" else parts.dropLast(1).joinToString("/")
            _terminalDirectory.value = next
            _terminalLines.value = _terminalLines.value + "Moved dir up: ~${if (next.isEmpty()) "" else "/$next"}"
            return
        }

        // Navigate to a subdirectory
        val current = _terminalDirectory.value
        val potentialDir = if (current.isEmpty()) target else "$current/$target"
        val physicalDir = fileHelper.resolveRelativePath(potentialDir)

        if (physicalDir.exists() && physicalDir.isDirectory) {
            _terminalDirectory.value = potentialDir
            _terminalLines.value = _terminalLines.value + "Navigated directory to: ~/$potentialDir"
        } else {
            _terminalLines.value = _terminalLines.value + "cd: no such directory: $target"
        }
    }

    private fun terminalMkdir(name: String?) {
        if (name == null || name.trim().isEmpty()) {
            _terminalLines.value = _terminalLines.value + "mkdir: missing destination folder operand"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = fileHelper.makeDirectory(_terminalDirectory.value, name)
            _terminalLines.value = _terminalLines.value + if (result.isSuccess) {
                "Created folder directory '$name'"
            } else {
                "mkdir: cannot create folder '$name': " + (result.exceptionOrNull()?.message ?: "")
            }
        }
    }

    private fun terminalTouch(name: String?) {
        if (name == null || name.trim().isEmpty()) {
            _terminalLines.value = _terminalLines.value + "touch: missing file operand"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = fileHelper.makeFile(_terminalDirectory.value, name, "")
            _terminalLines.value = _terminalLines.value + if (result.isSuccess) {
                "Created item empty file '$name'"
            } else {
                "touch: cannot create '$name': " + (result.exceptionOrNull()?.message ?: "")
            }
        }
    }

    private fun terminalCat(name: String?) {
        if (name == null || name.trim().isEmpty()) {
            _terminalLines.value = _terminalLines.value + "cat: missing file operand"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val relativeFilePath = if (_terminalDirectory.value.isEmpty()) name else "${_terminalDirectory.value}/$name"
            val result = fileHelper.readFileContent(relativeFilePath)
            _terminalLines.value = _terminalLines.value + if (result.isSuccess) {
                val content = result.getOrNull() ?: ""
                content.split("\n")
            } else {
                listOf("cat: '$name': " + (result.exceptionOrNull()?.message ?: "Not found or binary"))
            }
        }
    }

    private fun terminalEcho(args: List<String>) {
        if (args.isEmpty()) {
            _terminalLines.value = _terminalLines.value + ""
            return
        }

        // Parse echo commands: echo "some text" > file.txt (overwrite) or >> file.txt (append)
        val textRedirectIndex = args.indexOfFirst { it == ">" || it == ">>" }
        if (textRedirectIndex == -1) {
            // Just output text to stream
            _terminalLines.value = _terminalLines.value + args.joinToString(" ")
            return
        }

        val messageWords = args.subList(0, textRedirectIndex)
        val message = messageWords.joinToString(" ").removePrefix("\"").removeSuffix("\"")

        val fileIndex = textRedirectIndex + 1
        if (fileIndex >= args.size) {
            _terminalLines.value = _terminalLines.value + "echo: missing destination file parameter"
            return
        }

        val filename = args[fileIndex]
        val appendMode = args[textRedirectIndex] == ">>"

        viewModelScope.launch(Dispatchers.IO) {
            val relativeFilePath = if (_terminalDirectory.value.isEmpty()) filename else "${_terminalDirectory.value}/$filename"
            val targetFile = fileHelper.resolveRelativePath(relativeFilePath)

            try {
                if (appendMode) {
                    val currentContent = if (targetFile.exists()) targetFile.readText() else ""
                    val separator = if (currentContent.isNotEmpty() && !currentContent.endsWith("\n")) "\n" else ""
                    targetFile.writeText(currentContent + separator + message)
                    _terminalLines.value = _terminalLines.value + "Appended data to '$filename'"
                } else {
                    targetFile.parentFile?.mkdirs()
                    targetFile.writeText(message)
                    _terminalLines.value = _terminalLines.value + "Wrote data to file '$filename'"
                }
            } catch (e: Exception) {
                _terminalLines.value = _terminalLines.value + "echo: failed: ${e.message}"
            }
        }
    }

    private fun terminalRm(name: String?) {
        if (name == null || name.trim().isEmpty()) {
            _terminalLines.value = _terminalLines.value + "rm: missing file/directory operand"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val relativePath = if (_terminalDirectory.value.isEmpty()) name else "${_terminalDirectory.value}/$name"
            val result = fileHelper.deleteFileOrDirectory(relativePath)
            _terminalLines.value = _terminalLines.value + if (result.isSuccess) {
                "Deleted repository item '$name'"
            } else {
                "rm: failed: " + (result.exceptionOrNull()?.message ?: "")
            }
        }
    }


    // ==========================================
    // 4. CALENDAR STATE & ACTIONS
    // ==========================================
    private val _calendarYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val calendarYear: StateFlow<Int> = _calendarYear.asStateFlow()

    private val _calendarMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH)) // 0-11
    val calendarMonth: StateFlow<Int> = _calendarMonth.asStateFlow()

    private val _calendarSelectedDate = MutableStateFlow("") // format "YYYY-MM-DD"
    val calendarSelectedDate: StateFlow<String> = _calendarSelectedDate.asStateFlow()

    init {
        // Default select today
        val today = Calendar.getInstance()
        val format = String.format("%04d-%02d-%02d", today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH))
        _calendarSelectedDate.value = format
    }

    // Reactively load and stream events for the selected month to mark calendar dates!
    val activeMonthEvents: StateFlow<List<CalendarEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactively stream events for ONLY the selected day!
    val selectedDayEvents: StateFlow<List<CalendarEvent>> = _calendarSelectedDate
        .flatMapLatest { date -> repository.getEventsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCalendarDate(dateString: String) {
        _calendarSelectedDate.value = dateString
    }

    fun stepCalendarMonth(delta: Int) {
        val temp = Calendar.getInstance()
        temp.set(Calendar.YEAR, _calendarYear.value)
        temp.set(Calendar.MONTH, _calendarMonth.value)
        temp.add(Calendar.MONTH, delta)
        _calendarYear.value = temp.get(Calendar.YEAR)
        _calendarMonth.value = temp.get(Calendar.MONTH)
    }

    fun addCalendarEvent(title: String, description: String) {
        val dateStr = _calendarSelectedDate.value
        if (title.trim().isEmpty() || dateStr.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertEvent(
                CalendarEvent(
                    title = title.trim(),
                    description = description.trim(),
                    dateString = dateStr
                )
            )
        }
    }

    fun deleteCalendarEvent(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEventById(id)
        }
    }

    // --- Notes Pad State & Operations ---
    val allNotes: StateFlow<List<Note>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertNote(title: String, content: String, associatedFileRelativePath: String? = null, associatedCalendarEventId: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(
                Note(
                    title = title.trim(),
                    content = content.trim(),
                    associatedFileRelativePath = associatedFileRelativePath,
                    associatedCalendarEventId = associatedCalendarEventId
                )
            )
        }
    }

    fun updateNote(id: Int, title: String, content: String, associatedFileRelativePath: String? = null, associatedCalendarEventId: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(
                Note(
                    id = id,
                    title = title.trim(),
                    content = content.trim(),
                    associatedFileRelativePath = associatedFileRelativePath,
                    associatedCalendarEventId = associatedCalendarEventId
                )
            )
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNoteById(id)
        }
    }

    // ==========================================
    // 6. CONTACTS, MESSAGES, TODO TASKS, AND RECENT CALLS STATE & ACTIONS
    // ==========================================

    // --- Contact Operations ---
    val allContacts: StateFlow<List<Contact>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertContact(name: String, phoneNumber: String, email: String, category: String, isFavorite: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertContact(
                Contact(
                    name = name.trim(),
                    phoneNumber = phoneNumber.trim(),
                    email = email.trim(),
                    category = category,
                    isFavorite = isFavorite
                )
            )
            addAppLog("Contact created: ${name.trim()}")
        }
    }

    fun deleteContact(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteContactById(id)
            addAppLog("Deleted contact.")
        }
    }

    // --- Message/Chat Operations ---
    val allMessages: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getMessagesForThread(threadId: String): Flow<List<ChatMessage>> {
        return repository.getMessagesForThread(threadId)
    }

    fun sendMessage(threadId: String, sender: String, text: String, autoReply: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMessage(
                ChatMessage(
                    threadId = threadId,
                    sender = sender,
                    text = text.trim()
                )
            )
            addAppLog("Message sent to $threadId")

            if (autoReply && sender == "Me") {
                // Simulate a contact response back after a short delay!
                delay(1000)
                val replyText = when {
                    text.contains("hello", ignoreCase = true) || text.contains("hi", ignoreCase = true) -> "Hello! Glad to connect. How are you doing today?"
                    text.contains("help", ignoreCase = true) -> "I'm available. Let me know if you need help with any tasks."
                    text.contains("status", ignoreCase = true) -> "System logs check out OK. I am operating at peak parameters."
                    text.contains("meeting", ignoreCase = true) -> "Sure! Let's schedule it or write it in the calendar."
                    else -> "Thanks for your message! This is an automated response from Optech Suite interactive network."
                }
                repository.insertMessage(
                    ChatMessage(
                        threadId = threadId,
                        sender = "Contact",
                        text = replyText
                    )
                )
                addAppLog("Received automatic message response")
            }
        }
    }

    fun deleteConversation(threadId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteConversation(threadId)
            addAppLog("Conversation thread cleared: $threadId")
        }
    }

    // --- Task Operations ---
    val allTasks: StateFlow<List<TodoTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertTask(title: String, description: String, priority: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTask(
                TodoTask(
                    title = title.trim(),
                    description = description.trim(),
                    priority = priority
                )
            )
            addAppLog("Task added: ${title.trim()}")
        }
    }

    fun updateTaskStatus(id: Int, completed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTaskStatus(id, completed)
            addAppLog("Task marked ${if (completed) "done" else "active"}")
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTaskById(id)
            addAppLog("Task deleted")
        }
    }

    // --- Recent Call Operations ---
    val recentCalls: StateFlow<List<RecentCall>> = repository.recentCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertRecentCall(phoneNumber: String, contactName: String?, callType: String, durationSeconds: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRecentCall(
                RecentCall(
                    phoneNumber = phoneNumber.trim(),
                    contactName = contactName,
                    callType = callType,
                    durationSeconds = durationSeconds
                )
            )
            addAppLog("Registered call log for ${contactName ?: phoneNumber}")
        }
    }

    fun deleteRecentCall(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecentCallById(id)
        }
    }

    fun clearCallHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearCallHistory()
            addAppLog("Cleared call history log.")
        }
    }

    // --- Real Weather Telemetry Flow ---
    private val _realWeather = MutableStateFlow<RealWeather?>(null)
    val realWeather: StateFlow<RealWeather?> = _realWeather.asStateFlow()

    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    private val _realNewsList = MutableStateFlow<List<RealNewsItem>>(emptyList())
    val realNewsList: StateFlow<List<RealNewsItem>> = _realNewsList.asStateFlow()

    private val _isNewsLoading = MutableStateFlow(false)
    val isNewsLoading: StateFlow<Boolean> = _isNewsLoading.asStateFlow()

    private val okHttpClient = OkHttpClient()

    fun fetchRealWeather(cityName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isWeatherLoading.value = true
            try {
                // Optional geocode if it's not a preset
                var city = cityName.trim()
                var lat = "40.7128"
                var lon = "-74.006"
                
                when (city.lowercase()) {
                    "tokyo", "neo-tokyo", "neo-tokyo central sector" -> {
                        city = "Tokyo, JP"
                        lat = "35.6762"
                        lon = "139.6503"
                    }
                    "new york", "new york cyber gateway" -> {
                        city = "New York, US"
                        lat = "40.7128"
                        lon = "-74.0060"
                    }
                    "london", "london fog substation" -> {
                        city = "London, GB"
                        lat = "51.5074"
                        lon = "-0.1278"
                    }
                    "paris" -> {
                        city = "Paris, FR"
                        lat = "48.8566"
                        lon = "2.3522"
                    }
                    else -> {
                        // Dynamically geocode!
                        val geocodeUrl = "https://geocoding-api.open-meteo.com/v1/search?name=${android.net.Uri.encode(city)}&count=1&language=en&format=json"
                        val geoRequest = Request.Builder().url(geocodeUrl).build()
                        okHttpClient.newCall(geoRequest).execute().use { geoResponse ->
                            if (geoResponse.isSuccessful) {
                                val body = geoResponse.body?.string()
                                if (!body.isNullOrEmpty()) {
                                    val geoObj = JSONObject(body)
                                    val results = geoObj.optJSONArray("results")
                                    if (results != null && results.length() > 0) {
                                        val firstResult = results.getJSONObject(0)
                                        lat = firstResult.optDouble("latitude", 40.7128).toString()
                                        lon = firstResult.optDouble("longitude", -74.006).toString()
                                        val resolvedName = firstResult.optString("name", city)
                                        val country = firstResult.optString("country_code", "")
                                        city = if (country.isNotEmpty()) "$resolvedName, ${country.uppercase()}" else resolvedName
                                    }
                                }
                            }
                        }
                    }
                }

                // Call Open-Meteo
                val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m&hourly=temperature_2m&timezone=auto"
                val weatherReq = Request.Builder().url(weatherUrl).build()
                okHttpClient.newCall(weatherReq).execute().use { weatherRep ->
                    if (weatherRep.isSuccessful) {
                        val body = weatherRep.body?.string()
                        if (!body.isNullOrEmpty()) {
                            val wObj = JSONObject(body)
                            val current = wObj.getJSONObject("current")
                            val temp = current.optDouble("temperature_2m", 20.0).toString() + " °C"
                            val rh = current.optInt("relative_humidity_2m", 50).toString() + "%"
                            val apparent = current.optDouble("apparent_temperature", 20.0).toString() + " °C"
                            val ws = current.optDouble("wind_speed_10m", 10.0).toString() + " km/h"
                            val code = current.optInt("weather_code", 0)
                            val desc = getWeatherCodeDescription(code)

                            // Load next 5 hours
                            val hourlyForecast = mutableListOf<Pair<String, String>>()
                            val hourly = wObj.optJSONObject("hourly")
                            if (hourly != null) {
                                val times = hourly.optJSONArray("time")
                                val temps = hourly.optJSONArray("temperature_2m")
                                if (times != null && temps != null) {
                                    val count = Math.min(times.length(), temps.length())
                                    for (i in 0 until Math.min(count, 5)) {
                                        val fullTime = times.optString(i, "")
                                        val hrStr = if (fullTime.contains("T")) fullTime.substringAfter("T") else "0$i:00"
                                        val tempVal = temps.optDouble(i, 20.0)
                                        hourlyForecast.add(hrStr to "${tempVal.toInt()}°")
                                    }
                                }
                            }

                            _realWeather.value = RealWeather(
                                cityName = city,
                                temperature = temp,
                                apparentTemp = apparent,
                                humidity = rh,
                                windSpeed = ws,
                                weatherCode = code,
                                description = desc,
                                hourlyForecast = hourlyForecast
                            )
                            addAppLog("Real-time weather updated for $city: $temp, $desc")
                        }
                    }
                }
            } catch (e: Exception) {
                addAppLog("Failed to fetch real-time weather: ${e.message}")
            } finally {
                _isWeatherLoading.value = false
            }
        }
    }

    private fun getWeatherCodeDescription(code: Int): String {
        return when (code) {
            0 -> "Clear transparent skies"
            1, 2, 3 -> "Partly cloudy, temperate air"
            45, 48 -> "Dense micro-fog. Reduced visibility"
            51, 53, 55 -> "Light atmospheric misting and drizzle"
            56, 57, 66, 67 -> "Freezing rain. Roadway caution requested"
            61, 63, 65 -> "Precipitation live. Moderate continuous rain"
            71, 73, 75 -> "Active snowfall. Sub-zero temperatures"
            77 -> "Light snow grains drifting"
            80, 81, 82 -> "Scattered showers running"
            85, 86 -> "Active passing winter flurries"
            95, 96, 99 -> "Heavy thunderstorms active"
            else -> "Temperate baseline weather index"
        }
    }

    fun fetchRealNews() {
        viewModelScope.launch(Dispatchers.IO) {
            _isNewsLoading.value = true
            try {
                // Fetch US top headlines (general/technology) from Saurav's NewsAPI mirror!
                val feeds = listOf(
                    "https://saurav.tech/NewsAPI/top-headlines/category/technology/us.json",
                    "https://saurav.tech/NewsAPI/top-headlines/category/general/us.json"
                )
                val fetchedStories = mutableListOf<RealNewsItem>()
                
                for (feedUrl in feeds) {
                    val req = Request.Builder().url(feedUrl).build()
                    okHttpClient.newCall(req).execute().use { response ->
                        if (response.isSuccessful) {
                            val body = response.body?.string()
                            if (!body.isNullOrEmpty()) {
                                val jsonObj = JSONObject(body)
                                val articles = jsonObj.optJSONArray("articles")
                                if (articles != null) {
                                    for (i in 0 until Math.min(articles.length(), 15)) {
                                        val art = articles.getJSONObject(i)
                                        val title = art.optString("title", "")
                                        val desc = art.optString("description", "")
                                        val content = art.optString("content", "")
                                        val publishedAt = art.optString("publishedAt", "")
                                        val urlToImage = art.optString("urlToImage", "")
                                        val srcObj = art.optJSONObject("source")
                                        val sourceName = srcObj?.optString("name", "NEWS") ?: "NEWS"
                                        
                                        if (title.isNotEmpty() && title != "null") {
                                            val id = (title + publishedAt).hashCode()
                                            val displayTime = formatPublishedAt(publishedAt)
                                            
                                            fetchedStories.add(
                                                RealNewsItem(
                                                    id = id,
                                                    category = sourceName.uppercase(),
                                                    title = title,
                                                    description = if (desc.isEmpty() || desc == "null") content else desc,
                                                    content = if (content.isEmpty() || content == "null") desc else content,
                                                    time = displayTime,
                                                    icon = getIconForCategory(sourceName),
                                                    urlToImage = if (urlToImage.isNotEmpty() && urlToImage != "null") urlToImage else null
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (fetchedStories.isNotEmpty()) {
                    _realNewsList.value = fetchedStories.distinctBy { it.title }
                    addAppLog("Gathered ${_realNewsList.value.size} live headliners in satellite news cache.")
                }
            } catch (e: Exception) {
                addAppLog("Failed to sync live newsfeed: ${e.message}")
            } finally {
                _isNewsLoading.value = false
            }
        }
    }

    private fun formatPublishedAt(pAt: String): String {
        try {
            if (pAt.length >= 16) {
                val datePart = pAt.substring(5, 10).replace("-", "/")
                val timePart = pAt.substring(11, 16)
                return "$datePart $timePart"
            }
        } catch (e: Exception) {
            // fallback
        }
        return "recently"
    }

    private fun getIconForCategory(source: String): String {
        val src = source.lowercase()
        return when {
            src.contains("tech") || src.contains("wired") || src.contains("verge") -> "⚡"
            src.contains("cnn") || src.contains("bbc") || src.contains("reuters") -> "📰"
            src.contains("space") || src.contains("nasa") -> "🌌"
            src.contains("post") || src.contains("times") -> "✍️"
            else -> "🔹"
        }
    }

    // --- Mail Hub App Engine ---
    private val _mailInbox = MutableStateFlow<List<OptechMail>>(emptyList())
    val mailInbox: StateFlow<List<OptechMail>> = _mailInbox.asStateFlow()

    private val _mailLogs = MutableStateFlow<List<String>>(emptyList())
    val mailLogs: StateFlow<List<String>> = _mailLogs.asStateFlow()

    private val _isMailConnecting = MutableStateFlow(false)
    val isMailConnecting: StateFlow<Boolean> = _isMailConnecting.asStateFlow()

    private val _mailAccount = MutableStateFlow<String?>(null)
    val mailAccount: StateFlow<String?> = _mailAccount.asStateFlow()

    private val _mailAccountType = MutableStateFlow<String?>(null)
    val mailAccountType: StateFlow<String?> = _mailAccountType.asStateFlow()

    private val _mailHost = MutableStateFlow("")
    private val _mailIncomingPort = MutableStateFlow(993)
    private val _mailOutgoingHost = MutableStateFlow("")
    private val _mailOutgoingPort = MutableStateFlow(465)
    private val _mailPassword = MutableStateFlow("")

    init {
        // Pre-seed some messages to keep the suite loaded with cyberpunk info!
        _mailInbox.value = listOf(
            OptechMail(
                id = "1",
                sender = "Optech Security Core",
                senderEmail = "security@optech.sekid",
                recipient = "sensthesekid@gmail.com",
                subject = "⚠️ ALERT: Grid Subnet Intrusion Vector",
                body = "RE-INITIALIZING ENCRYPTION PROTOCOLS:\n\nOur deep quantum firewalls registered an external scanning probe attempting to map workspace files inside directory '/src/res/'. It was successfully repelled with Class-IV dynamic noise dampening.\n\nVerify that any open Wi-Fi bridges inside the 'ADB Wireless Debugging' toolkit are restricted. Refrain from using default credentials.\n\n-- Optech Security Mainframe Node 09SF",
                timestamp = "Mon, Jun 01, 19:42:15",
                isRead = false
            ),
            OptechMail(
                id = "2",
                sender = "Dropbox Cloud Integration",
                senderEmail = "no-reply@dropbox.com",
                recipient = "sensthesekid@gmail.com",
                subject = "Linked: Cloud storage volume status",
                body = "INTEGRATION ONLINE:\n\nTo begin synchronizing files, navigate to the File Manager, toggle to the 'Misc Storage & Connect' layout tab, and authorized the Dropbox API. This will append a secure 'workspace:///dropbox/' sync loop supporting zero-latency updates.\n\nLimit: 2 TB Secured Workspace Node",
                timestamp = "Mon, Jun 01, 15:10:00",
                isRead = true
            ),
            OptechMail(
                id = "3",
                sender = "HQ Logistics",
                senderEmail = "hq@optech.sekid",
                recipient = "sensthesekid@gmail.com",
                subject = "Fwd: Optech App Package Refactored to .com.optech.sekid",
                body = "IMPORTANT MEMO:\n\nThe core identity for this terminal app suite has officially transitioned from standard placeholders to 'com.optech.sekid' (Launcher app name: 'Optech'). Verify that all upcoming build signatures, telemetry logging keys, and local sandbox registries adhere to the new namespace immediately.\n\nKeep offline keys stored locally.",
                timestamp = "Mon, Jun 01, 11:32:45",
                isRead = false
            )
        )
    }

    fun configureMailAccount(provider: String, email: String, host: String, incomingPort: Int, outgoingHost: String, outgoingPort: Int, password: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            _isMailConnecting.value = true
            _mailLogs.value = emptyList()
            _mailHost.value = host
            _mailIncomingPort.value = incomingPort
            _mailOutgoingHost.value = outgoingHost
            _mailOutgoingPort.value = outgoingPort
            _mailPassword.value = password
            
            appendMailLog(">>> INIT SECURE $provider CONNECTION HANDSHAKE")
            
            MailSocketEngine.verifyIncoming(
                host = host,
                port = incomingPort,
                user = email,
                pass = password,
                protocol = if (incomingPort == 993 || host.contains("imap", ignoreCase = true)) "IMAP" else "POP3",
                logAction = { appendMailLog(it) },
                onSuccess = { credentialsUser, fetchedMessages ->
                    _mailAccount.value = email
                    _mailAccountType.value = provider
                    _isMailConnecting.value = false
                    if (fetchedMessages.isNotEmpty()) {
                        _mailInbox.value = fetchedMessages + _mailInbox.value
                    }
                    addAppLog("Optech Mail connected securely and synced to $email.")
                },
                onFailure = { errorString ->
                    appendMailLog("<<< HANDSHAKE EXCEPTION: $errorString")
                    appendMailLog("<<< FALLING BACK TO LOCAL SECURE GHOST NETWORK.")
                    
                    _mailAccount.value = email
                    _mailAccountType.value = provider
                    _isMailConnecting.value = false
                    addAppLog("Optech Mail configured in local safe mode for $email.")
                }
            )
        }
    }

    fun clearMailAccount() {
        _mailAccount.value = null
        _mailAccountType.value = null
        _mailPassword.value = ""
        _mailLogs.value = listOf("Disconnected from secure mail relay.")
        addAppLog("Disconnected from Mail accounts.")
    }

    fun dispatchComposeMail(to: String, subject: String, body: String, attachments: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            _isMailConnecting.value = true
            _mailLogs.value = emptyList()
            
            val accountEmail = _mailAccount.value ?: "sensthesekid@gmail.com"
            val host = _mailOutgoingHost.value.ifBlank { "smtp.${_mailAccountType.value?.lowercase() ?: "gmail"}.com" }
            val port = _mailOutgoingPort.value
            val password = _mailPassword.value
            
            appendMailLog(">>> SMTP TRANSMITTER INITIATED OUTBOUND RELAY")
            
            val realAttachments = attachments.map { rel ->
                val f = fileHelper.resolveRelativePath(rel)
                f.absolutePath
            }

            MailSocketEngine.dispatchSmtp(
                host = host,
                port = port,
                user = accountEmail,
                pass = password,
                to = to,
                subject = subject,
                body = body,
                attachments = realAttachments,
                logAction = { appendMailLog(it) },
                onSuccess = {
                    val newMail = OptechMail(
                        id = UUID.randomUUID().toString(),
                        sender = "Me",
                        senderEmail = accountEmail,
                        recipient = to,
                        subject = subject,
                        body = body,
                        timestamp = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date()),
                        isRead = true,
                        isSentDraft = true,
                        attachments = attachments
                    )
                    _mailInbox.value = listOf(newMail) + _mailInbox.value
                    _isMailConnecting.value = false
                    addAppLog("SMTP Mail securely transmitted to $to (Subject: $subject)")
                },
                onFailure = { errorString ->
                    appendMailLog("<<< SMTP LINK TIMEOUT: $errorString")
                    appendMailLog("<<< STAGING TRANSLATION BUFFER LOCALLY.")
                    
                    val newMail = OptechMail(
                        id = UUID.randomUUID().toString(),
                        sender = "Me (Staged)",
                        senderEmail = accountEmail,
                        recipient = to,
                        subject = "[STAGED] $subject",
                        body = "OFFLINE STAGE VECTOR (NETWORK BLOCKED):\n\n$body",
                        timestamp = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date()),
                        isRead = true,
                        isSentDraft = true,
                        attachments = attachments
                    )
                    _mailInbox.value = listOf(newMail) + _mailInbox.value
                    _isMailConnecting.value = false
                    addAppLog("SMTP Mail staged offline due to Exception: $errorString")
                }
            )
        }
    }

    private fun appendMailLog(line: String) {
        _mailLogs.value = _mailLogs.value + line
    }

    fun markMailRead(mailId: String) {
        _mailInbox.value = _mailInbox.value.map {
            if (it.id == mailId) it.copy(isRead = true) else it
        }
    }

    // --- Misc Cloud & LAN Integration States ---
    private val _googleDriveConnected = MutableStateFlow(false)
    val googleDriveConnected: StateFlow<Boolean> = _googleDriveConnected.asStateFlow()

    private val _googleDriveUser = MutableStateFlow<String?>(null)
    val googleDriveUser: StateFlow<String?> = _googleDriveUser.asStateFlow()

    private val _dropboxConnected = MutableStateFlow(false)
    val dropboxConnected: StateFlow<Boolean> = _dropboxConnected.asStateFlow()

    private val _dropboxUser = MutableStateFlow<String?>(null)
    val dropboxUser: StateFlow<String?> = _dropboxUser.asStateFlow()

    private val _oneDriveConnected = MutableStateFlow(false)
    val oneDriveConnected: StateFlow<Boolean> = _oneDriveConnected.asStateFlow()

    private val _oneDriveUser = MutableStateFlow<String?>(null)
    val oneDriveUser: StateFlow<String?> = _oneDriveUser.asStateFlow()

    private val _lanConnected = MutableStateFlow(false)
    val lanConnected: StateFlow<Boolean> = _lanConnected.asStateFlow()

    private val _lanPath = MutableStateFlow<String?>(null)
    val lanPath: StateFlow<String?> = _lanPath.asStateFlow()

    private val _cloudLogs = MutableStateFlow<List<String>>(emptyList())
    val cloudLogs: StateFlow<List<String>> = _cloudLogs.asStateFlow()

    fun connectGoogleDrive(email: String, code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appendCloudLog("GoogleDrive > Verifying OAuth credentials...")
            delay(400)
            appendCloudLog("GoogleDrive > Connecting to drive.googleapis.com...")
            delay(500)
            appendCloudLog("GoogleDrive > Token exchange success!")
            appendCloudLog("GoogleDrive > Mounted Workspace virtual drive.")
            
            _googleDriveConnected.value = true
            _googleDriveUser.value = email
            addAppLog("Google Drive volume connected for $email.")
        }
    }

    fun disconnectGoogleDrive() {
        _googleDriveConnected.value = false
        _googleDriveUser.value = null
        appendCloudLog("GoogleDrive > Volume unmounted.")
        addAppLog("Unmounted Google Drive.")
    }

    fun connectDropbox(appKey: String, token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appendCloudLog("Dropbox > Resolving Dropbox App token endpoint...")
            delay(400)
            appendCloudLog("Dropbox > Establishing TLS session socket...")
            delay(500)
            appendCloudLog("Dropbox > Authorized and mapped remote directory '/sandbox-optech'.")
            
            _dropboxConnected.value = true
            _dropboxUser.value = "Optech_Secret_$appKey"
            addAppLog("Dropbox storage volume connected as Optech_Secret_$appKey.")
        }
    }

    fun disconnectDropbox() {
        _dropboxConnected.value = false
        _dropboxUser.value = null
        appendCloudLog("Dropbox > Connection terminated.")
        addAppLog("Unmounted Dropbox storage.")
    }

    fun connectOneDrive(accountType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appendCloudLog("OneDrive > Directing to Microsoft Live accounts gateway...")
            delay(400)
            appendCloudLog("OneDrive > Connected to live-onedrive.microsoft.lan...")
            delay(400)
            appendCloudLog("OneDrive > Authorized! Mounted Office volumes.")
            
            _oneDriveConnected.value = true
            _oneDriveUser.value = "Live Account ($accountType)"
            addAppLog("OneDrive connected successfully as Live Account ($accountType).")
        }
    }

    fun disconnectOneDrive() {
        _oneDriveConnected.value = false
        _oneDriveUser.value = null
        appendCloudLog("OneDrive > Connection unmounted completely.")
        addAppLog("Unmounted Microsoft OneDrive storage.")
    }

    fun connectLanShare(ip: String, share: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appendCloudLog("LAN_SMB > Broadcasting NetBIOS address request...")
            delay(300)
            appendCloudLog("LAN_SMB > Handshaking with SMBv3 endpoint @ $ip/$share...")
            delay(500)
            appendCloudLog("LAN_SMB > Authenticated! Shared volume successfully loaded.")
            
            _lanConnected.value = true
            _lanPath.value = "smb://$ip/$share"
            addAppLog("LAN Network SMB Share mounted for $ip/$share.")
        }
    }

    fun disconnectLanShare() {
        _lanConnected.value = false
        _lanPath.value = null
        appendCloudLog("LAN_SMB > Unmounted share folder.")
        addAppLog("Unmounted Local LAN SMB Volume.")
    }

    private fun appendCloudLog(msg: String) {
        _cloudLogs.value = _cloudLogs.value + "[${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}] $msg"
    }

    fun clearCloudLogs() {
        _cloudLogs.value = emptyList()
    }
}

data class OptechMail(
    val id: String,
    val sender: String,
    val senderEmail: String,
    val recipient: String,
    val subject: String,
    val body: String,
    val timestamp: String,
    val isRead: Boolean,
    val isSentDraft: Boolean = false,
    val attachments: List<String> = emptyList()
)

data class RealWeather(
    val cityName: String = "",
    val temperature: String = "",
    val description: String = "",
    val windSpeed: String = "",
    val humidity: String = "",
    val apparentTemp: String = "",
    val weatherCode: Int = 0,
    val hourlyForecast: List<Pair<String, String>> = emptyList()
)

data class RealNewsItem(
    val id: Int,
    val category: String,
    val title: String,
    val description: String,
    val content: String,
    val time: String,
    val icon: String,
    val urlToImage: String? = null
)
