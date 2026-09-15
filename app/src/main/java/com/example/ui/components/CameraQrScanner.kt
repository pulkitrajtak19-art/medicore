package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.LightBluePrimary
import com.example.ui.theme.SoftEmeraldAccent
import com.google.common.util.concurrent.ListenableFuture
import com.google.zxing.*
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import java.io.InputStream
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "CameraQrScanner"

/**
 * High-performance, offline QR Code Analyzer utilizing CameraX ImageAnalysis and ZXing.
 * Correctly handles YUV 420 888 row padding, bitmap conversion, device rotation, and inverted QR patterns.
 */
class QrCodeImageAnalyzer(
    private val onQrScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val isScanned = AtomicBoolean(false)
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val reader = MultiFormatReader().apply {
        val hints = mapOf<DecodeHintType, Any>(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.CHARACTER_SET to "UTF-8"
        )
        setHints(hints)
    }

    fun reset() {
        isScanned.set(false)
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (isScanned.get()) {
            imageProxy.close()
            return
        }

        try {
            var detectedText: String? = null

            // Strategy 1: CameraX 1.5 toBitmap conversion (GPU/HAL accelerated, respects rotation)
            try {
                val bitmap = imageProxy.toBitmap()
                if (bitmap != null) {
                    val width = bitmap.width
                    val height = bitmap.height
                    val pixels = IntArray(width * height)
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                    val rgbSource = RGBLuminanceSource(width, height, pixels)

                    // 1a. HybridBinarizer
                    try {
                        val result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(rgbSource)))
                        detectedText = result.text
                    } catch (_: Exception) {
                        reader.reset()
                    }

                    // 1b. GlobalHistogramBinarizer fallback
                    if (detectedText.isNullOrBlank()) {
                        try {
                            val result = reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(rgbSource)))
                            detectedText = result.text
                        } catch (_: Exception) {
                            reader.reset()
                        }
                    }

                    // 1c. Inverted pass for dark/stylized QR
                    if (detectedText.isNullOrBlank()) {
                        try {
                            val result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(rgbSource.invert())))
                            detectedText = result.text
                        } catch (_: Exception) {
                            reader.reset()
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to YUV extraction if toBitmap is unavailable
            }

            // Strategy 2: Direct Y-Plane Luminance fallback
            if (detectedText.isNullOrBlank()) {
                val mediaImage = imageProxy.image
                if (mediaImage != null && mediaImage.planes.isNotEmpty()) {
                    val yPlane = mediaImage.planes[0]
                    val buffer = yPlane.buffer
                    buffer.rewind()
                    val rowStride = yPlane.rowStride
                    val width = imageProxy.width
                    val height = imageProxy.height

                    val yData = ByteArray(width * height)
                    if (rowStride == width) {
                        buffer.get(yData, 0, width * height)
                    } else {
                        for (row in 0 until height) {
                            buffer.position(row * rowStride)
                            val len = minOf(width, buffer.remaining())
                            buffer.get(yData, row * width, len)
                        }
                    }

                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    val (rotatedData, finalWidth, finalHeight) = rotateYData(yData, width, height, rotationDegrees)

                    val yuvSource = PlanarYUVLuminanceSource(
                        rotatedData,
                        finalWidth,
                        finalHeight,
                        0,
                        0,
                        finalWidth,
                        finalHeight,
                        false
                    )

                    try {
                        val result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(yuvSource)))
                        detectedText = result.text
                    } catch (_: Exception) {
                        reader.reset()
                        try {
                            val result = reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(yuvSource)))
                            detectedText = result.text
                        } catch (_: Exception) {
                            reader.reset()
                        }
                    }
                }
            }

            if (!detectedText.isNullOrBlank()) {
                if (isScanned.compareAndSet(false, true)) {
                    Log.d(TAG, "QR code detected successfully: $detectedText")
                    mainHandler.post {
                        onQrScanned(detectedText)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in QR analysis", e)
        } finally {
            imageProxy.close()
        }
    }

    private fun rotateYData(data: ByteArray, width: Int, height: Int, rotationDegrees: Int): Triple<ByteArray, Int, Int> {
        return when (rotationDegrees) {
            90 -> {
                val rotated = ByteArray(width * height)
                var k = 0
                for (x in 0 until width) {
                    for (y in height - 1 downTo 0) {
                        rotated[k++] = data[y * width + x]
                    }
                }
                Triple(rotated, height, width)
            }
            180 -> {
                val rotated = ByteArray(width * height)
                var k = 0
                for (i in width * height - 1 downTo 0) {
                    rotated[k++] = data[i]
                }
                Triple(rotated, width, height)
            }
            270 -> {
                val rotated = ByteArray(width * height)
                var k = 0
                for (x in width - 1 downTo 0) {
                    for (y in 0 until height) {
                        rotated[k++] = data[y * width + x]
                    }
                }
                Triple(rotated, height, width)
            }
            else -> Triple(data, width, height)
        }
    }
}

/**
 * Decodes a QR code from a Bitmap (e.g., loaded from the Photo Picker / Gallery).
 */
fun decodeQrFromBitmap(bitmap: Bitmap): String? {
    return try {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val source = RGBLuminanceSource(width, height, pixels)
        val reader = MultiFormatReader().apply {
            setHints(mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.CHARACTER_SET to "UTF-8"
            ))
        }

        // Try 1: HybridBinarizer
        try {
            return reader.decodeWithState(BinaryBitmap(HybridBinarizer(source))).text
        } catch (_: Exception) {
            reader.reset()
        }

        // Try 2: GlobalHistogramBinarizer
        try {
            return reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(source))).text
        } catch (_: Exception) {
            reader.reset()
        }

        // Try 3: Inverted HybridBinarizer
        try {
            return reader.decodeWithState(BinaryBitmap(HybridBinarizer(source.invert()))).text
        } catch (_: Exception) {
            reader.reset()
        }

        // Try 4: Inverted GlobalHistogramBinarizer
        try {
            return reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(source.invert()))).text
        } catch (_: Exception) {
            reader.reset()
        }

        null
    } catch (e: Exception) {
        Log.w(TAG, "Failed to decode QR from bitmap", e)
        null
    }
}

/**
 * Composable CameraX Live QR Scanner View.
 * Displays live camera feed and actively scans for QR codes.
 */
@Composable
fun CameraQrScannerView(
    modifier: Modifier = Modifier,
    isFlashOn: Boolean = false,
    useFrontCamera: Boolean = false,
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val analyzer = remember {
        QrCodeImageAnalyzer { result ->
            onQrScanned(result)
        }
    }

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraInfo by remember { mutableStateOf<CameraInfo?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(Unit) {
        analyzer.reset()
    }

    // Synchronize flash / torch toggle
    LaunchedEffect(isFlashOn, cameraControl, cameraInfo) {
        if (cameraInfo?.hasFlashUnit() == true) {
            try {
                cameraControl?.enableTorch(isFlashOn)
            } catch (e: Exception) {
                Log.w(TAG, "Could not toggle torch", e)
            }
        }
    }

    // Bind camera reactively whenever provider, preview, or camera selector (front/back) changes
    LaunchedEffect(cameraProvider, previewViewRef, useFrontCamera, lifecycleOwner) {
        val provider = cameraProvider ?: return@LaunchedEffect
        val previewView = previewViewRef ?: return@LaunchedEffect

        try {
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(android.util.Size(1280, 720))
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, analyzer)
                }

            val cameraSelector = if (useFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            provider.unbindAll()
            val camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )

            cameraControl = camera.cameraControl
            cameraInfo = camera.cameraInfo
        } catch (e: Exception) {
            Log.e(TAG, "Camera bind failed", e)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraProvider?.unbindAll()
                cameraExecutor.shutdown()
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        modifier = modifier.testTag("camera_preview_view"),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            previewViewRef = previewView

            val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                try {
                    cameraProvider = cameraProviderFuture.get()
                } catch (e: Exception) {
                    Log.e(TAG, "Camera initialization failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        update = {
            // AndroidView update hook
        }
    )
}

/**
 * Reusable full-featured Camera Scanner Overlay with permissions, gallery picker, and viewfinder reticle.
 */
@Composable
fun QrScannerViewfinder(
    modifier: Modifier = Modifier,
    title: String = "Scan QR Code",
    subtitle: String = "Point camera at QR code",
    onQrScanned: (String) -> Unit,
    onDismiss: () -> Unit,
    extraBottomContent: @Composable (() -> Unit)? = null
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(context, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show()
        }
    }

    // Automatically prompt for camera permission if not granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var isFlashOn by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(false) }

    // Gallery Photo Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val decoded = decodeQrFromBitmap(bitmap)
                    if (!decoded.isNullOrBlank()) {
                        Toast.makeText(context, "QR Code recognized from photo!", Toast.LENGTH_SHORT).show()
                        onQrScanned(decoded)
                    } else {
                        Toast.makeText(context, "No valid QR code found in selected image", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed reading gallery image", e)
                Toast.makeText(context, "Could not process image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Laser vertical translation animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1120))
    ) {
        // 1. Live Camera Feed (when permission is granted)
        if (hasCameraPermission) {
            CameraQrScannerView(
                modifier = Modifier.fillMaxSize(),
                isFlashOn = isFlashOn,
                useFrontCamera = useFrontCamera,
                onQrScanned = onQrScanned
            )
        } else {
            // Permission request screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SoftEmeraldAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = SoftEmeraldAccent,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera Access Required",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Medicore needs camera access to scan patient ABHA health QR codes and hospital reception check-in kiosks.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("grant_camera_permission_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enable Camera Access", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan from Gallery Image", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2. Camera Controls & Reticle Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .testTag("qr_scanner_close_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = SoftEmeraldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                // Gallery scan button
                IconButton(
                    onClick = {
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .testTag("qr_scanner_gallery_button")
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Upload QR Image", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Reticle / Viewfinder Frame
            Box(
                modifier = Modifier
                    .size(270.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
                    .testTag("qr_scanner_viewfinder"),
                contentAlignment = Alignment.Center
            ) {
                // Corner Reticle Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 5.dp.toPx()
                    val cornerLength = 34.dp.toPx()
                    val cornerColor = androidx.compose.ui.graphics.Color(0xFF10B981)

                    // Top-Left
                    drawLine(cornerColor, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth)
                    drawLine(cornerColor, Offset(0f, 0f), Offset(0f, cornerLength), strokeWidth)
                    // Top-Right
                    drawLine(cornerColor, Offset(size.width, 0f), Offset(size.width - cornerLength, 0f), strokeWidth)
                    drawLine(cornerColor, Offset(size.width, 0f), Offset(size.width, cornerLength), strokeWidth)
                    // Bottom-Left
                    drawLine(cornerColor, Offset(0f, size.height), Offset(cornerLength, size.height), strokeWidth)
                    drawLine(cornerColor, Offset(0f, size.height), Offset(0f, size.height - cornerLength), strokeWidth)
                    // Bottom-Right
                    drawLine(cornerColor, Offset(size.width, size.height), Offset(size.width - cornerLength, size.height), strokeWidth)
                    drawLine(cornerColor, Offset(size.width, size.height), Offset(size.width, size.height - cornerLength), strokeWidth)

                    // Laser Line
                    val laserY = size.height * laserYRatio
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0xFF10B981), Color(0xFF34D399), Color(0xFF10B981), Color.Transparent)
                        ),
                        start = Offset(10.dp.toPx(), laserY),
                        end = Offset(size.width - 10.dp.toPx(), laserY),
                        strokeWidth = 3.dp.toPx()
                    )
                }

                // Small center targeting crosshair
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Camera status pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.55f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (hasCameraPermission) SoftEmeraldAccent else Color.Red)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasCameraPermission) "Live Camera Scanning..." else "Camera Offline - Grant Permission",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Torch & Camera Flip Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Torch toggle
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isFlashOn) SoftEmeraldAccent else Color.Black.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isFlashOn) SoftEmeraldAccent else Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .clickable { isFlashOn = !isFlashOn }
                        .testTag("qr_scanner_torch_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torch",
                            tint = if (isFlashOn) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFlashOn) "Torch ON" else "Torch",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFlashOn) Color.Black else Color.White
                        )
                    }
                }

                // Camera Switch (Back / Front)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .clickable { useFrontCamera = !useFrontCamera }
                        .testTag("qr_scanner_camera_flip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FlipCameraAndroid,
                            contentDescription = "Flip Camera",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (useFrontCamera) "Front" else "Rear",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Extra bottom controls (e.g. manual entry or facility shortcuts)
            extraBottomContent?.invoke()
        }
    }
}
