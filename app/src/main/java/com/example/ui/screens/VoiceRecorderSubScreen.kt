package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.delay
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VoiceRecorderSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val customBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("recorder_scaffold"),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("recorder_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back to suite apps",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "VOICE TERMINAL RECORDER",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0B132B)
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(customBg)
                .padding(innerPadding)
        ) {
            if (recordAudioPermissionState.status.isGranted) {
                VoiceRecorderEngine(context, viewModel)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Permission Required",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier
                            .size(72.dp)
                            .padding(bottom = 16.dp)
                    )
                    Text(
                        text = "MICROPHONE ACCESS REQUIRED",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To capture high fidelity voice briefings and digital memos, this application requires recording privileges. Please grant access below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Button(
                        onClick = { recordAudioPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("grant_mic_permission_btn")
                    ) {
                        Text(
                            text = "AUTHORIZE MIC MICROPHONE",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceRecorderEngine(context: Context, viewModel: MainViewModel) {
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    var isRecording by remember { mutableStateOf(false) }
    var recordingTimeSec by remember { mutableIntStateOf(0) }
    var currentOutputFilePath by remember { mutableStateOf<String?>(null) }

    var isPlaying by remember { mutableStateOf(false) }
    var currentlyPlayingFilePath by remember { mutableStateOf<String?>(null) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }

    // List of previously recorded files
    var recordList by remember { mutableStateOf(emptyList<File>()) }

    fun refreshRecordings() {
        val dir = context.filesDir
        val files = dir.listFiles { file -> file.name.endsWith(".m4a") } ?: emptyArray()
        recordList = files.sortedByDescending { it.lastModified() }
    }

    LaunchedEffect(Unit) {
        refreshRecordings()
    }

    // Timer logic for Recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTimeSec = 0
            while (isRecording) {
                delay(1000L)
                recordingTimeSec++
            }
        }
    }

    // Playback monitor loop
    LaunchedEffect(isPlaying, currentlyPlayingFilePath) {
        if (isPlaying && currentlyPlayingFilePath != null) {
            while (isPlaying && mediaPlayer != null) {
                try {
                    val duration = mediaPlayer?.duration ?: 0
                    val current = mediaPlayer?.currentPosition ?: 0
                    if (duration > 0) {
                        playbackProgress = current.toFloat() / duration.toFloat()
                    }
                } catch (e: Exception) {
                    // Ignore transient exceptions during state release
                }
                delay(150L)
            }
        } else {
            playbackProgress = 0f
        }
    }

    fun startRecording() {
        if (isPlaying) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
            currentlyPlayingFilePath = null
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputName = "VOICE_RECO_${timestamp}.m4a"
        val outputFile = File(context.filesDir, outputName)
        currentOutputFilePath = outputFile.absolutePath

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            try {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                prepare()
                start()
                isRecording = true
                viewModel.addAppLog("Voice recorder active. Saving: $outputName")
            } catch (io: IOException) {
                Toast.makeText(context, "Failed to initialize recorder: ${io.message}", Toast.LENGTH_SHORT).show()
                viewModel.addAppLog("Recorder initialization failed: ${io.message}")
            } catch (e: Exception) {
                Toast.makeText(context, "Error starting voice recording: ${e.message}", Toast.LENGTH_SHORT).show()
                viewModel.addAppLog("Recording startup error: ${e.message}")
            }
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            viewModel.addAppLog("Recorded voice track successfully cached.")
            refreshRecordings()
        } catch (e: Exception) {
            viewModel.addAppLog("Error stopping recorder safely: ${e.message}")
            mediaRecorder = null
            isRecording = false
        }
    }

    fun startPlaying(filePath: String) {
        if (filePath == currentlyPlayingFilePath && !isPlaying) {
            mediaPlayer?.start()
            isPlaying = true
            viewModel.addAppLog("Resume voice memo briefing playback.")
            return
        }

        if (isPlaying) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
        }

        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                start()
                isPlaying = true
                currentlyPlayingFilePath = filePath
                viewModel.addAppLog("Playing selected recorded memo.")
                setOnCompletionListener {
                    isPlaying = false
                    currentlyPlayingFilePath = null
                    it.release()
                    mediaPlayer = null
                    viewModel.addAppLog("Voice briefing completed.")
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Playback error: ${e.message}", Toast.LENGTH_SHORT).show()
                viewModel.addAppLog("Audio playback failed: ${e.message}")
                isPlaying = false
                currentlyPlayingFilePath = null
            }
        }
    }

    fun pausePlaying() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                isPlaying = false
                viewModel.addAppLog("Audio playback paused.")
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun stopPlaying() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
            currentlyPlayingFilePath = null
            viewModel.addAppLog("Voice playback stopped.")
        } catch (e: Exception) {
            // Ignore
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaRecorder?.release()
            mediaPlayer?.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper Visualizer Dashboard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isRecording) {
                    Text(
                        text = "RECORDING LIVE FEED",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = formatTime(recordingTimeSec),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 44.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    // Real-time Visualizer bars animation
                    FakeVoiceVisualizer(active = true, color = Color(0xFFEF4444))
                } else if (isPlaying) {
                    Text(
                        text = "DECRYPTING AUDIO STREAM",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val fileName = currentlyPlayingFilePath?.substringAfterLast("/") ?: ""
                    Text(
                        text = fileName,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = playbackProgress,
                        color = Color(0xFF10B981),
                        trackColor = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FakeVoiceVisualizer(active = true, color = Color(0xFF10B981))
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "RECORDER HUB IDLE",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Boot live mic session to index high-speed vocal logs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center primary control widget row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stop button
            if (isRecording || isPlaying) {
                IconButton(
                    onClick = {
                        if (isRecording) stopRecording()
                        if (isPlaying) stopPlaying()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF334155))
                        .testTag("recorder_stop_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Record Toggle Button
            val animScale by rememberInfiniteTransition(label = "").animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = ""
            )

            val recMod = if (isRecording) Modifier.scale(animScale) else Modifier

            Button(
                onClick = {
                    if (isRecording) {
                        stopRecording()
                    } else {
                        startRecording()
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.15f)
                ),
                border = BorderStroke(2.dp, Color(0xFFEF4444)),
                modifier = recMod
                    .size(76.dp)
                    .testTag("recorder_record_toggle")
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Pause else Icons.Default.FiberManualRecord,
                    contentDescription = "Record Trigger",
                    tint = if (isRecording) Color.White else Color(0xFFEF4444),
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lower File logs dashboard container
        Text(
            text = "PERSISTED VOICE MEMOS INDEX",
            style = MaterialTheme.typography.labelSmall,
            color = Color.LightGray,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1728).copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (recordList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "NO LOCAL RECORDS FOUND",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recordList) { file ->
                        val isThisPlaying = currentlyPlayingFilePath == file.absolutePath
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isThisPlaying) Color(0xFF10B981).copy(alpha = 0.08f) else Color(0xFF1E293B).copy(alpha = 0.4f))
                                .border(1.dp, if (isThisPlaying) Color(0xFF10B981).copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isThisPlaying && isPlaying) {
                                        pausePlaying()
                                    } else {
                                        startPlaying(file.absolutePath)
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isThisPlaying) Color(0xFF10B981) else Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isThisPlaying && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Playback Control",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatFileSize(file.length()),
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = formatDate(file.lastModified()),
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (isThisPlaying) {
                                        stopPlaying()
                                    }
                                    file.delete()
                                    viewModel.addAppLog("Voice briefing deleted: ${file.name}")
                                    refreshRecordings()
                                },
                                modifier = Modifier.testTag("delete_record_btn_${file.name}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete this file",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FakeVoiceVisualizer(active: Boolean, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barsCount = 15
        val infiniteTransition = rememberInfiniteTransition(label = "Visualizer")
        
        repeat(barsCount) { index ->
            // Use different multipliers and cycles for organic chaotic movement
            val cycle = 400 + (index * 60)
            val barHeightScale by if (active) {
                infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.95f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(cycle, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "VisualizerBar"
                )
            } else {
                remember { mutableStateOf(0.15f) }
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(barHeightScale)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", m, s)
}

private fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val k = size / 1024f
    if (k < 1024) {
        return String.format(Locale.getDefault(), "%.1f KB", k)
    }
    val m = k / 1024f
    return String.format(Locale.getDefault(), "%.1f MB", m)
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
