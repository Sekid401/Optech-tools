package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.ui.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraSubScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("camera_scaffold"),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("camera_back_btn")
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
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "OPTECH DIGI-CAMERA HUD",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFF020617)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (cameraPermissionState.status.isGranted) {
                CameraEngineView(context, viewModel)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoCameraFront,
                        contentDescription = "No Permission",
                        tint = Color(0xFF6366F1),
                        modifier = Modifier
                            .size(72.dp)
                            .padding(bottom = 16.dp)
                    )
                    Text(
                        text = "CAMERA ACCESS REQUIRED",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To catalog system components, take photos, and test diagnostic vision modules, please grant Camera access.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("grant_camera_permission_btn")
                    ) {
                        Text(
                            text = "ACTIVATE EYE DEV-CAMERA",
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
fun CameraEngineView(context: Context, viewModel: MainViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }

    val preview = remember { Preview.Builder().build() }
    val imageCapture = remember { ImageCapture.Builder().setFlashMode(flashMode).build() }
    val previewView = remember { PreviewView(context) }

    var savedPhotos by remember { mutableStateOf(emptyList<File>()) }
    var activePreviewImageUri by remember { mutableStateOf<Uri?>(null) }

    fun refreshGallery() {
        val dir = context.filesDir
        val files = dir.listFiles { file -> file.name.startsWith("CAM_CAP_") && file.name.endsWith(".jpg") } ?: emptyArray()
        savedPhotos = files.sortedByDescending { it.lastModified() }
    }

    LaunchedEffect(Unit) {
        refreshGallery()
    }

    // Effect to bind preview on lensFacing change
    LaunchedEffect(lensFacing, flashMode) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        imageCapture.flashMode = flashMode

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)
        } catch (exc: Exception) {
            Toast.makeText(context, "Failed to load camera preview: ${exc.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun takePhoto(executor: Executor) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File(context.filesDir, "CAM_CAP_${timestamp}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val msg = "Captured image cataloged: ${photoFile.name}"
                    viewModel.addAppLog(msg)
                    // Post callback to Main Dispatcher for toast etc.
                    ContextCompat.getMainExecutor(context).execute {
                        Toast.makeText(context, "Photo logged successfully!", Toast.LENGTH_SHORT).show()
                        refreshGallery()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    val errMsg = "Camera recording fault: ${exception.message}"
                    viewModel.addAppLog(errMsg)
                    ContextCompat.getMainExecutor(context).execute {
                        Toast.makeText(context, "Capture failure: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen Active Preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Diagnostic HUD overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                    Text(
                        text = "LIVE R-STREAMING HUD",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Flash & Lens Control
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Flash Mode Selector
                    IconButton(
                        onClick = {
                            flashMode = when (flashMode) {
                                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                                else -> ImageCapture.FLASH_MODE_OFF
                            }
                        }
                    ) {
                        val icon = when (flashMode) {
                            ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                            ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                            else -> Icons.Default.FlashOff
                        }
                        Icon(icon, contentDescription = "Flash Options", tint = Color.White)
                    }

                    // Toggle Front/Back Lens
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        }
                    ) {
                        Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Swap Camera", tint = Color.White)
                    }
                }
            }

            // Bottom Gallery & Actions Layer
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Horizontally Scrolling Captured Gallery Strip
                if (savedPhotos.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(savedPhotos) { file ->
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        activePreviewImageUri = Uri.fromFile(file)
                                    }
                            ) {
                                AsyncImage(
                                    model = file,
                                    contentDescription = file.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                // Shutter Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { takePhoto(ContextCompat.getMainExecutor(context)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = CircleShape,
                        border = BorderStroke(4.dp, Color(0xFF6366F1)),
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("camera_shutter_btn")
                    ) {
                        // Empty inner to act like a real camera trigger
                    }
                }
            }
        }

        // Full Screen Zoomed Image Detail Preview Dialog Overlay
        AnimatedVisibility(
            visible = activePreviewImageUri != null,
            enter = fadeIn() + expandIn(),
            exit = fadeOut() + shrinkOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { activePreviewImageUri = null },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(0.8f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = activePreviewImageUri,
                            contentDescription = "Zoomed Preview Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Button(
                            onClick = { activePreviewImageUri = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                        ) {
                            Text("CLOSE DETAILS", color = Color.White, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = {
                                activePreviewImageUri?.path?.let { path ->
                                    val f = File(path)
                                    f.delete()
                                    activePreviewImageUri = null
                                    viewModel.addAppLog("Image deleted from catalog.")
                                    refreshGallery()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("DELETE PHOTO", color = Color.White, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
