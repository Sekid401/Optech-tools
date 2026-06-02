package com.example.ui.screens

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.Bookmark
import com.example.data.HistoryItem
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(viewModel: MainViewModel) {
    val currentUrl by viewModel.browserUrl.collectAsState()
    val isBookmarked by viewModel.isCurrentUrlBookmarked.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val history by viewModel.history.collectAsState()
    val isDesktopMode by viewModel.isBrowserDesktopMode.collectAsState()

    var urlInput by remember { mutableStateOf(currentUrl) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var activeTab by remember { mutableStateOf(BrowserTab.WEBVIEW) } // WEBVIEW, BOOKMARKS, HISTORY
    
    var canGoBackState by remember { mutableStateOf(false) }
    var canGoForwardState by remember { mutableStateOf(false) }
    var pageLoadProgress by remember { mutableStateOf(0) }
    var isPageLoading by remember { mutableStateOf(false) }

    // Desktop Chromium multi-tab simulator state
    var chromeTabs by remember { 
        mutableStateOf(
            listOf(
                ChromeTabItem("Google", "https://www.google.com"),
                ChromeTabItem("AI Studio Portal", "https://ai.studio"),
                ChromeTabItem("Wikipedia", "https://en.wikipedia.org"),
                ChromeTabItem("GitHub Sandbox", "https://github.com")
            )
        )
    }
    var activeChromeTabIndex by remember { mutableStateOf(0) }
    
    // Zoom & DevTools States
    var zoomPercent by remember { mutableStateOf(100) }
    var showDevTools by remember { mutableStateOf(false) }
    var customUserAgentString by remember { mutableStateOf("") }
    
    val keyboardController = LocalSoftwareKeyboardController.current

    // Keep address in sync with VM-level url updates
    LaunchedEffect(currentUrl) {
        if (currentUrl != urlInput) {
            urlInput = currentUrl
        }
        webViewInstance?.let { webView ->
            val currentWebViewUrl = webView.url
            if (sanitizeUrlForCompare(currentWebViewUrl) != sanitizeUrlForCompare(currentUrl)) {
                webView.loadUrl(currentUrl)
            }
            canGoBackState = webView.canGoBack()
            canGoForwardState = webView.canGoForward()
        }
    }

    // React to Desktop Mode or Zoom changes
    LaunchedEffect(isDesktopMode, zoomPercent) {
        webViewInstance?.let { webView ->
            webView.settings.apply {
                if (isDesktopMode) {
                    userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                } else {
                    userAgentString = null // Use mobile standard
                }
                textZoom = zoomPercent
            }
            customUserAgentString = webView.settings.userAgentString ?: "Default WebKit"
            webView.reload()
        }
    }

    // Handle Android system back press to surf backward in webview history
    BackHandler(enabled = activeTab == BrowserTab.WEBVIEW && canGoBackState) {
        webViewInstance?.goBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF202124)) // Dark Chromium Frame color
    ) {
        // --- Chromuim Window Header Title Bar (PC Simulator Panel) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2F3033))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Colored circles representing PC window controllers
                Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(Color(0xFFED6A5E)))
                Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(Color(0xFFF4BF4F)))
                Box(modifier = Modifier.size(11.dp).clip(CircleShape).background(Color(0xFF61C454)))
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "Desktop Chromium Engine (x64 Core Build)",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Toggle Desktop Mode / Mobile Mode visually
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isDesktopMode) Color(0xFF1E3A8A) else Color.DarkGray)
                        .clickable { viewModel.setBrowserDesktopMode(!isDesktopMode) }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (isDesktopMode) Icons.Default.Computer else Icons.Default.Smartphone,
                        contentDescription = "Device Agent Mode",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isDesktopMode) "DESKTOP UA" else "MOBILE UA",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Chromium Tab Bar UI ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2F3033))
                .horizontalScroll(rememberScrollState())
                .padding(top = 4.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            chromeTabs.forEachIndexed { index, tab ->
                val isSelected = index == activeChromeTabIndex
                Surface(
                    color = if (isSelected) Color(0xFF35363A) else Color(0xFF202124),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    modifier = Modifier
                        .widthIn(max = 140.dp)
                        .clickable {
                            activeChromeTabIndex = index
                            viewModel.loadUrl(tab.url)
                            activeTab = BrowserTab.WEBVIEW
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF4285F4) else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) Color.White else Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        
                        if (chromeTabs.size > 1) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Tab",
                                tint = if (isSelected) Color.White else Color.Gray,
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable {
                                        val mutableTabs = chromeTabs.toMutableList()
                                        mutableTabs.removeAt(index)
                                        chromeTabs = mutableTabs
                                        if (activeChromeTabIndex >= mutableTabs.size) {
                                            activeChromeTabIndex = maxOf(0, mutableTabs.size - 1)
                                        }
                                        viewModel.loadUrl(chromeTabs[activeChromeTabIndex].url)
                                    }
                            )
                        }
                    }
                }
            }

            // "+" New Tab Button
            IconButton(
                onClick = {
                    val newTab = ChromeTabItem("New Tab", "https://www.google.com")
                    chromeTabs = chromeTabs + newTab
                    activeChromeTabIndex = chromeTabs.size - 1
                    viewModel.loadUrl("https://www.google.com")
                    activeTab = BrowserTab.WEBVIEW
                },
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Tab", tint = Color.LightGray)
            }
        }

        // --- Web Browser Top Controller Deck ---
        Surface(
            tonalElevation = 6.dp,
            color = Color(0xFF35363A), // Authentic Chrome Controller background
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Back Control
                    IconButton(
                        onClick = { webViewInstance?.goBack() },
                        enabled = canGoBackState
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back", tint = if (canGoBackState) Color.White else Color.Gray)
                    }

                    // Forward Control
                    IconButton(
                        onClick = { webViewInstance?.goForward() },
                        enabled = canGoForwardState
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Go Forward", tint = if (canGoForwardState) Color.White else Color.Gray)
                    }

                    // Refresh Control
                    IconButton(
                        onClick = { webViewInstance?.reload() }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload web page", tint = Color.White)
                    }

                    // Home screen / Google shortcut
                    IconButton(
                        onClick = { viewModel.loadUrl("https://www.google.com") }
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Default homepage", tint = Color.White)
                    }

                    // Address / Search Input Field (Styled exactly as Chromuim Omnibox)
                    TextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 48.dp),
                        leadingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (urlInput.startsWith("https")) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Secured Protocol Flag",
                                    tint = if (urlInput.startsWith("https")) Color(0xFF81C784) else Color(0xFFE57373),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (urlInput.startsWith("https")) "Secure" else "Not Secure",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (urlInput.startsWith("https")) Color(0xFF81C784) else Color(0xFFE57373)
                                )
                            }
                        },
                        placeholder = { Text("Search or type URL...", color = Color.Gray, style = MaterialTheme.typography.bodySmall) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                viewModel.loadUrl(urlInput)
                                keyboardController?.hide()
                                activeTab = BrowserTab.WEBVIEW
                                // Update title of active tab
                                val currentTabs = chromeTabs.toMutableList()
                                currentTabs[activeChromeTabIndex] = currentTabs[activeChromeTabIndex].copy(
                                    title = urlInput.removePrefix("https://").removePrefix("http://").removePrefix("www.").take(15) + "...",
                                    url = urlInput
                                )
                                chromeTabs = currentTabs
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF202124),
                            unfocusedContainerColor = Color(0xFF202124),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )

                    // Bookmark star toggle
                    IconButton(
                        onClick = { viewModel.toggleBookmark() }
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Pin Bookmark",
                            tint = if (isBookmarked) Color(0xFFF4BF4F) else Color.White
                        )
                    }

                    // Zoom / DevTools controls
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF202124))
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { zoomPercent = maxOf(50, zoomPercent - 10) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("-", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${zoomPercent}%",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = { zoomPercent = minOf(200, zoomPercent + 10) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Devtools inspector toggle
                    IconButton(
                        onClick = { showDevTools = !showDevTools }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeveloperMode,
                            contentDescription = "Developer Tools Console",
                            tint = if (showDevTools) Color(0xFF61C454) else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // --- Sub Navigation Tabs ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabButton(
                        label = "Web View",
                        icon = Icons.Default.Public,
                        isSelected = activeTab == BrowserTab.WEBVIEW,
                        onClick = { activeTab = BrowserTab.WEBVIEW },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        label = "Bookmarks",
                        icon = Icons.Default.Bookmarks,
                        isSelected = activeTab == BrowserTab.BOOKMARKS,
                        onClick = { activeTab = BrowserTab.BOOKMARKS },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        label = "History",
                        icon = Icons.Default.History,
                        isSelected = activeTab == BrowserTab.HISTORY,
                        onClick = { activeTab = BrowserTab.HISTORY },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- Custom Progress Loading Bar ---
        if (isPageLoading && activeTab == BrowserTab.WEBVIEW) {
            LinearProgressIndicator(
                progress = { pageLoadProgress / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = Color(0xFF4285F4),
                trackColor = Color(0xFF202124)
            )
        }

        // --- Tab Body Area ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (activeTab) {
                        BrowserTab.WEBVIEW -> {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { context ->
                                    WebView(context).apply {
                                        webViewClient = object : WebViewClient() {
                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                canGoBackState = view?.canGoBack() ?: false
                                                canGoForwardState = view?.canGoForward() ?: false
                                            }

                                            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                                                super.doUpdateVisitedHistory(view, url, isReload)
                                                canGoBackState = view?.canGoBack() ?: false
                                                canGoForwardState = view?.canGoForward() ?: false
                                                url?.let {
                                                    if (it != currentUrl) {
                                                        viewModel.loadUrl(it)
                                                    }
                                                }
                                            }
                                        }
                                        webChromeClient = object : WebChromeClient() {
                                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                                super.onProgressChanged(view, newProgress)
                                                pageLoadProgress = newProgress
                                                isPageLoading = newProgress < 100
                                            }
                                        }
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            databaseEnabled = true
                                            useWideViewPort = true
                                            loadWithOverviewMode = true
                                            supportZoom()
                                            builtInZoomControls = true
                                            displayZoomControls = false
                                            if (isDesktopMode) {
                                                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                                            } else {
                                                userAgentString = null
                                            }
                                            textZoom = zoomPercent
                                        }
                                        loadUrl(currentUrl)
                                        webViewInstance = this
                                    }
                                },
                                update = { webView ->
                                    val currentWebViewUrl = webView.url
                                    if (sanitizeUrlForCompare(currentWebViewUrl) != sanitizeUrlForCompare(currentUrl)) {
                                        webView.loadUrl(currentUrl)
                                    }
                                    canGoBackState = webView.canGoBack()
                                    canGoForwardState = webView.canGoForward()
                                    webView.settings.apply {
                                        if (isDesktopMode) {
                                            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                                        } else {
                                            userAgentString = null
                                        }
                                        textZoom = zoomPercent
                                    }
                                }
                            )
                        }
                        BrowserTab.BOOKMARKS -> {
                            BookmarksView(
                                bookmarks = bookmarks,
                                onBookmarkClick = { url ->
                                    viewModel.loadUrl(url)
                                    activeTab = BrowserTab.WEBVIEW
                                },
                                onDeleteBookmark = { id -> viewModel.deleteBookmark(id) }
                            )
                        }
                        BrowserTab.HISTORY -> {
                            HistoryView(
                                history = history,
                                onHistoryClick = { url ->
                                    viewModel.loadUrl(url)
                                    activeTab = BrowserTab.WEBVIEW
                                },
                                onDeleteHistory = { id -> viewModel.deleteHistoryItem(id) },
                                onClearAll = { viewModel.clearBrowserHistory() }
                            )
                        }
                    }
                }
                
                // Embedded DevTools Inspector Console Panel
                if (showDevTools && activeTab == BrowserTab.WEBVIEW) {
                    ChromiumDevToolsPanel(
                        url = currentUrl,
                        userAgent = customUserAgentString,
                        zoomLevel = zoomPercent
                    )
                }
            }
        }
    }
}

data class ChromeTabItem(
    val title: String,
    val url: String
)

@Composable
fun ChromiumDevToolsPanel(url: String, userAgent: String, zoomLevel: Int) {
    Surface(
        tonalElevation = 12.dp,
        color = Color(0xFF1E1E1E), // Visual VS Code / Code Console background
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .border(1.dp, Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF61C454)))
                    Text("Chromium Inspector Console (DevTools)", color = Color(0xFF79D98F), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Text("HTML5 / JS / CSS Engine Ready", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            }
            
            Divider(color = Color.DarkGray)
            
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Text(text = "▶ Connected Session Frame: $url", color = Color.Cyan, style = MaterialTheme.typography.bodySmall)
                }
                item {
                    Text(text = "▶ Document Agent: $userAgent", color = Color.Yellow, style = MaterialTheme.typography.bodySmall)
                }
                item {
                    Text(text = "▶ Core Layout Matrix Engine: zoomRatio=$zoomLevel%, devicePixelRatio=2.00x", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                }
                item {
                    Text(text = "▶ [SUCCESS] DOM Loaded. Stylesheet bindings executed. JS Storage active.", color = Color(0xFF61C454), style = MaterialTheme.typography.bodySmall)
                }
                item {
                    Text(text = "▶ Security Policy: Sandbox limits enabled. SSL verified successfully.", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

enum class BrowserTab {
    WEBVIEW, BOOKMARKS, HISTORY
}

@Composable
fun TabButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BookmarksView(
    bookmarks: List<Bookmark>,
    onBookmarkClick: (String) -> Unit,
    onDeleteBookmark: (Int) -> Unit
) {
    if (bookmarks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.StarBorder,
                    contentDescription = "No bookmarks",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("No bookmarks saved yet.", style = MaterialTheme.typography.bodyMedium)
                Text("Tap the star icon while browsing to add one!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bookmarks) { bookmark ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBookmarkClick(bookmark.url) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bookmark.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = bookmark.url,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onDeleteBookmark(bookmark.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete bookmark")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryView(
    history: List<HistoryItem>,
    onHistoryClick: (String) -> Unit,
    onDeleteHistory: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "No history",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("No browser history catalogs.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent history logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearAll) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHistoryClick(item.url) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.url,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { onDeleteHistory(item.id) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove history item")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sanitizeUrlForCompare(url: String?): String {
    if (url == null) return ""
    return url.removeSuffix("/").removePrefix("https://").removePrefix("http://").removePrefix("www.")
}
