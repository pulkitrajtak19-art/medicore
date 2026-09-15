package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.FacilityCheckIn
import com.example.data.model.FacilityQrScanInfo
import com.example.data.model.UserProfile
import com.example.ui.components.CameraQrScannerView
import com.example.ui.components.QrCodeView
import com.example.ui.components.decodeQrFromBitmap
import com.example.ui.theme.*

private val SampleFacilities = listOf(
    FacilityQrScanInfo(
        facilityId = "IN-DL-AIIMS-004",
        facilityName = "AIIMS New Delhi",
        department = "General Medicine OPD",
        counterNumber = "OPD Counter 4",
        rawQrPayload = "ABDM://SCAN-SHARE/IN-DL-AIIMS-004/GEN-MED/COUNTER-4?SEC=8A71F",
        checksum = "ABDM-SEC-8A71F"
    ),
    FacilityQrScanInfo(
        facilityId = "IN-RJ-APOLLO-002",
        facilityName = "Apollo Multispecialty Clinic",
        department = "Internal Medicine & Triage",
        counterNumber = "Express Kiosk 2",
        rawQrPayload = "ABDM://SCAN-SHARE/IN-RJ-APOLLO-002/INT-MED/KIOSK-2?SEC=99C4B",
        checksum = "ABDM-SEC-99C4B"
    ),
    FacilityQrScanInfo(
        facilityId = "IN-RJ-JPR-CARE-01",
        facilityName = "Jaipur Care Community Hospital",
        department = "Outpatient Department",
        counterNumber = "Registration Counter 1",
        rawQrPayload = "ABDM://SCAN-SHARE/IN-RJ-JPR-CARE-01/OPD/DESK-1?SEC=33E21",
        checksum = "ABDM-SEC-33E21"
    ),
    FacilityQrScanInfo(
        facilityId = "IN-HR-FORTIS-008",
        facilityName = "Fortis Memorial Healthcare",
        department = "Urgent Care & Assessment",
        counterNumber = "Triage Counter 8",
        rawQrPayload = "ABDM://SCAN-SHARE/IN-HR-FORTIS-008/URGENT/COUNTER-8?SEC=F510A",
        checksum = "ABDM-SEC-F510A"
    )
)

fun parseFacilityQr(scannedText: String): FacilityQrScanInfo {
    val trimmed = scannedText.trim()
    val matched = SampleFacilities.find {
        it.facilityId.contains(trimmed, ignoreCase = true) ||
        it.facilityName.contains(trimmed, ignoreCase = true) ||
        it.rawQrPayload.contains(trimmed, ignoreCase = true)
    }
    if (matched != null) return matched

    val facilityName = when {
        trimmed.contains("AIIMS", ignoreCase = true) -> "AIIMS Hospital"
        trimmed.contains("Apollo", ignoreCase = true) -> "Apollo Clinic"
        trimmed.contains("Fortis", ignoreCase = true) -> "Fortis Healthcare"
        trimmed.contains("Care", ignoreCase = true) -> "Jaipur Care Hospital"
        trimmed.contains("Max", ignoreCase = true) -> "Max Super Speciality"
        else -> "Medical Facility (${trimmed.take(16)})"
    }

    return FacilityQrScanInfo(
        facilityId = "FAC-" + Integer.toHexString(trimmed.hashCode()).uppercase().take(8),
        facilityName = facilityName,
        department = "General OPD & Triage",
        counterNumber = "Kiosk 1",
        rawQrPayload = trimmed,
        checksum = "ABDM-SEC-" + Integer.toHexString(trimmed.hashCode()).uppercase().take(5)
    )
}

enum class ScannerStep {
    SCANNING,
    CONFIRM_SHARE,
    SUCCESS_SLIP
}

@Composable
fun FacilityQrScannerDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onCheckInSuccess: (FacilityCheckIn) -> Unit
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
    }

    var currentStep by remember { mutableStateOf(ScannerStep.SCANNING) }
    var selectedFacility by remember { mutableStateOf(SampleFacilities[0]) }
    var confirmedCheckIn by remember { mutableStateOf<FacilityCheckIn?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var manualQrInput by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }

    // Auto-prompt camera permission on dialog open
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val decoded = decodeQrFromBitmap(bitmap)
                    if (!decoded.isNullOrBlank()) {
                        Toast.makeText(context, "Facility QR recognized from photo!", Toast.LENGTH_SHORT).show()
                        selectedFacility = parseFacilityQr(decoded)
                        currentStep = ScannerStep.CONFIRM_SHARE
                    } else {
                        Toast.makeText(context, "No valid QR code found in selected photo", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open image", Toast.LENGTH_SHORT).show()
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B1120))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            when (currentStep) {
                ScannerStep.SCANNING -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Live Camera Feed behind viewfinder overlay
                        if (hasCameraPermission) {
                            CameraQrScannerView(
                                modifier = Modifier.fillMaxSize(),
                                isFlashOn = isFlashOn,
                                useFrontCamera = isFrontCamera,
                                onQrScanned = { rawQr ->
                                    selectedFacility = parseFacilityQr(rawQr)
                                    currentStep = ScannerStep.CONFIRM_SHARE
                                }
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top Header Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
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
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ABDM Fast Check-in",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "Scan Hospital / Kiosk QR Code",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Gallery Picker Button
                                    IconButton(
                                        onClick = {
                                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                    ) {
                                        Icon(
                                            Icons.Default.PhotoLibrary,
                                            contentDescription = "Upload QR Photo",
                                            tint = Color.White
                                        )
                                    }

                                    IconButton(
                                        onClick = { isFlashOn = !isFlashOn },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isFlashOn) SoftEmeraldAccent else Color.Black.copy(alpha = 0.5f)
                                            )
                                    ) {
                                        Icon(
                                            if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                            contentDescription = "Flashlight",
                                            tint = if (isFlashOn) Color.Black else Color.White
                                        )
                                    }

                                    IconButton(
                                        onClick = { isFrontCamera = !isFrontCamera },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                    ) {
                                        Icon(
                                            Icons.Default.FlipCameraAndroid,
                                            contentDescription = "Flip Camera",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Camera Permission Banner if not granted
                            if (!hasCameraPermission) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1E293B),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldAccent.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SoftEmeraldAccent)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Camera access required for live optical scanner",
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                        }
                                        Button(
                                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Enable", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Central Scanning Viewfinder Frame
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Viewfinder Reticle Box
                                Box(
                                    modifier = Modifier
                                        .size(260.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .border(2.dp, if (hasCameraPermission) SoftEmeraldAccent else Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                        .testTag("qr_scanner_viewfinder")
                                ) {
                                    // Corner brackets & Reticle Canvas
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val strokeWidth = 5.dp.toPx()
                                        val cornerLength = 32.dp.toPx()
                                        val cornerColor = SoftEmeraldAccent

                                        // Top-Left Corner
                                        drawLine(cornerColor, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth)
                                        drawLine(cornerColor, Offset(0f, 0f), Offset(0f, cornerLength), strokeWidth)

                                        // Top-Right Corner
                                        drawLine(cornerColor, Offset(size.width, 0f), Offset(size.width - cornerLength, 0f), strokeWidth)
                                        drawLine(cornerColor, Offset(size.width, 0f), Offset(size.width, cornerLength), strokeWidth)

                                        // Bottom-Left Corner
                                        drawLine(cornerColor, Offset(0f, size.height), Offset(cornerLength, size.height), strokeWidth)
                                        drawLine(cornerColor, Offset(0f, size.height), Offset(0f, size.height - cornerLength), strokeWidth)

                                        // Bottom-Right Corner
                                        drawLine(cornerColor, Offset(size.width, size.height), Offset(size.width - cornerLength, size.height), strokeWidth)
                                        drawLine(cornerColor, Offset(size.width, size.height), Offset(size.width, size.height - cornerLength), strokeWidth)

                                        // Animated Laser Line
                                        val laserY = size.height * laserYRatio
                                        drawLine(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    SoftEmeraldAccent.copy(alpha = 0.8f),
                                                    SoftEmeraldAccent,
                                                    SoftEmeraldAccent.copy(alpha = 0.8f),
                                                    Color.Transparent
                                                )
                                            ),
                                            start = Offset(12.dp.toPx(), laserY),
                                            end = Offset(size.width - 12.dp.toPx(), laserY),
                                            strokeWidth = 3.dp.toPx()
                                        )
                                    }

                                    if (!hasCameraPermission) {
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Outlined.QrCode,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.25f),
                                                modifier = Modifier.size(64.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Enable camera to scan hospital QR",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.6f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            // Bottom Section: Instant Facility Selector & Manual Trigger
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Live Camera status indicator
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.Black.copy(alpha = 0.6f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(if (hasCameraPermission) SoftEmeraldAccent else Color.Red)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (hasCameraPermission) "Live Camera Active • Point at Hospital QR" else "Camera Offline",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "OR SELECT TEST HOSPITAL PRESET:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                // Quick facility test chips
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(SampleFacilities) { fac ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (selectedFacility.facilityId == fac.facilityId)
                                                SoftEmeraldAccent.copy(alpha = 0.25f)
                                            else Color(0xFF1E293B).copy(alpha = 0.85f),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (selectedFacility.facilityId == fac.facilityId) SoftEmeraldAccent else Color.White.copy(alpha = 0.15f)
                                            ),
                                            modifier = Modifier
                                                .clickable {
                                                    selectedFacility = fac
                                                    currentStep = ScannerStep.CONFIRM_SHARE
                                                }
                                                .testTag("preset_facility_${fac.facilityId}")
                                        ) {
                                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.LocalHospital,
                                                        contentDescription = null,
                                                        tint = SoftEmeraldAccent,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = fac.facilityName,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                                Text(
                                                    text = "${fac.counterNumber} • ${fac.department}",
                                                    fontSize = 9.sp,
                                                    color = Color.White.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    }
                                }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Main Action: Trigger Scan & Verify Selected
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showManualInput = !showManualInput },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Manual Token", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        currentStep = ScannerStep.CONFIRM_SHARE
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                                    modifier = Modifier
                                        .weight(1.4f)
                                        .testTag("trigger_scan_button")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Confirm & Proceed", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            if (showManualInput) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = manualQrInput,
                                        onValueChange = { manualQrInput = it },
                                        placeholder = { Text("Paste facility QR token/URL...", fontSize = 12.sp, color = Color.Gray) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = SoftEmeraldAccent,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (manualQrInput.isNotBlank()) {
                                                selectedFacility = FacilityQrScanInfo(
                                                    facilityId = "MANUAL-${manualQrInput.take(6).uppercase()}",
                                                    facilityName = "Medical Facility Desk",
                                                    department = "General OPD",
                                                    counterNumber = "Counter 1",
                                                    rawQrPayload = manualQrInput,
                                                    checksum = "MANUAL-CHK"
                                                )
                                                currentStep = ScannerStep.CONFIRM_SHARE
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                                    ) {
                                        Text("Go")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ScannerStep.CONFIRM_SHARE -> {
                // ABDM Scan & Share Confirmation Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { currentStep = ScannerStep.SCANNING },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Text(
                                text = "Scan & Share Check-In",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Verified Facility Card
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(SoftEmeraldAccent.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = SoftEmeraldAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "QR CODE VERIFIED",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SoftEmeraldAccent,
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = selectedFacility.facilityName,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Department", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                                        Text(selectedFacility.department, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Counter / Kiosk", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                                        Text(selectedFacility.counterNumber, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SoftEmeraldAccent)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Facility ID: ${selectedFacility.facilityId} • ${selectedFacility.checksum}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Health ID Sharing Preview
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131E32)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = LightBluePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Secure Data Handshake (ABHA Profile)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "By checking in, the following verified demographic information will be securely transmitted to the hospital registration desk:",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.Black.copy(alpha = 0.3f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        ProfileDataRow("Patient Name", user.name)
                                        ProfileDataRow("ABHA Health ID", user.abhaId)
                                        ProfileDataRow("Age & Gender", "${user.age} Years • ${user.gender}")
                                        ProfileDataRow("Blood Group", user.bloodGroup)
                                        ProfileDataRow("Emergency Contact", user.emergencyContact)
                                        ProfileDataRow("Access Window", "Temporary 12-Hour Visit Window")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Confirm Check-in Button
                        Button(
                            onClick = {
                                val newCheckIn = FacilityCheckIn(
                                    id = "chk_${System.currentTimeMillis()}",
                                    facilityId = selectedFacility.facilityId,
                                    facilityName = selectedFacility.facilityName,
                                    department = selectedFacility.department,
                                    counterNumber = selectedFacility.counterNumber,
                                    tokenNumber = "OPD-" + ('A'..'D').random() + "-" + String.format("%03d", (12..88).random()),
                                    timestamp = "Today, Just now",
                                    estimatedWaitMinutes = (8..18).random(),
                                    status = "CONFIRMED",
                                    abhaShared = user.abhaId,
                                    qrRawData = selectedFacility.rawQrPayload,
                                    securityChecksum = selectedFacility.checksum
                                )
                                confirmedCheckIn = newCheckIn
                                onCheckInSuccess(newCheckIn)
                                currentStep = ScannerStep.SUCCESS_SLIP
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("confirm_fast_checkin_button")
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Confirm Fast Check-In & Get Token",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(onClick = { currentStep = ScannerStep.SCANNING }) {
                            Text("Rescan / Change Facility", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }

                ScannerStep.SUCCESS_SLIP -> {
                    // Generated Digital OPD Queue Slip
                    val slip = confirmedCheckIn ?: return@Box

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SoftEmeraldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Check-In Confirmed!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "You are registered in the electronic hospital queue",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Digital Token Pass Slip
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("opd_queue_slip_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SoftEmeraldLight,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
                                ) {
                                    Text(
                                        text = "ABDM FAST-TRACK OPD PASS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftEmeraldDark,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = slip.facilityName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBlueHeader,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${slip.department} • ${slip.counterNumber}",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Massive Token Number Display
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = SoftEmeraldLight,
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SoftEmeraldAccent),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "YOUR QUEUE TOKEN",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SoftEmeraldDark,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = slip.tokenNumber,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SoftEmeraldDark,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Est. Wait: ~${slip.estimatedWaitMinutes} mins",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextDarkSlate
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // QR Code Verification view
                                QrCodeView(dataToken = slip.securityChecksum, sizeDp = 110)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Pass Checksum: ${slip.securityChecksum}",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = SurfaceBorder)
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Patient", fontSize = 10.sp, color = TextMuted)
                                        Text(user.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("ABHA ID", fontSize = 10.sp, color = TextMuted)
                                        Text(slip.abhaShared, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = LightBluePrimary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("scanner_done_button")
                        ) {
                            Text("Done & View on Health ID", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileDataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}
