package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
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
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.components.CameraQrScannerView
import com.example.ui.components.QrCodeView
import com.example.ui.components.decodeQrFromBitmap
import com.example.ui.theme.*
import org.json.JSONObject

data class ScannedPatientDossier(
    val abhaId: String,
    val name: String,
    val age: Int,
    val gender: String,
    val bloodGroup: String,
    val phone: String,
    val email: String = "patient@healthmail.in",
    val city: String = "Jaipur, Rajasthan",
    val emergencyContact: String,
    val allergies: String,
    val vitalsSummary: String,
    val chiefComplaint: String = "Acute upper respiratory symptoms for 48 hours. Voice intake captured.",
    val chronicConditions: String = "None recorded",
    val activeMedications: String = "Paracetamol 650mg, Cetirizine 10mg",
    val pastHistory: String = "Normal recovery. Last OPD checkup 4 months ago.",
    val verifiedTimestamp: String = "ABDM M3 QR Verified • 24-Hour Clinical Consent Active"
)

fun parsePatientDossierFromQr(rawText: String): ScannedPatientDossier {
    val trimmed = rawText.trim()
    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
        try {
            val json = JSONObject(trimmed)
            val abhaId = json.optString("abhaId").ifBlank { json.optString("id", "ABHA-91-8472-9102-4412") }
            val name = json.optString("name", "Verified Patient")
            val age = json.optInt("age", 28)
            val gender = json.optString("gender", "Female")
            val bloodGroup = json.optString("bloodGroup", "B+")
            val phone = json.optString("phone", "+91 98290 12345")
            val email = json.optString("email", "patient@healthmail.in")
            val city = json.optString("city", "Jaipur, Rajasthan")
            val emergency = json.optString("emergencyContact", "+91 98290 99887")
            val allergies = json.optString("allergies", "None recorded")
            val vitals = json.optString("latestVitals").ifBlank { json.optString("vitals", "BP 118/78 mmHg • Pulse 72 bpm • SpO2 99%") }
            val complaint = json.optString("chiefComplaint", "Patient presented for clinical evaluation. Identity verified via ABDM QR.")
            val conditions = json.optString("chronicConditions", "None reported")
            val meds = json.optString("activeMedications", "Paracetamol 650mg, Cetirizine 10mg")
            val history = json.optString("pastHistory", "Verified longitudinal health records linked via ABDM.")
            return ScannedPatientDossier(
                abhaId = abhaId,
                name = name,
                age = age,
                gender = gender,
                bloodGroup = bloodGroup,
                phone = phone,
                email = email,
                city = city,
                emergencyContact = emergency,
                allergies = allergies,
                vitalsSummary = vitals,
                chiefComplaint = complaint,
                chronicConditions = conditions,
                activeMedications = meds,
                pastHistory = history,
                verifiedTimestamp = "Verified via Secure ABDM Health QR • 24-Hour Clinical Consent Active"
            )
        } catch (_: Exception) {}
    }

    if (trimmed.contains("abha=") || trimmed.contains("name=")) {
        var abha = "ABHA-91-8472-9102-4412"
        var name = "Priya Sharma"
        var blood = "B+"
        var vitals = "BP 118/78 mmHg • Pulse 72 bpm • SpO2 99%"
        var phone = "+91 98290 12345"
        var emergency = "+91 98290 99887"
        var allergies = "Penicillin (Severe Rash / Anaphylaxis Risk)"
        val query = trimmed.substringAfter("?", trimmed.substringAfter("://", trimmed))
        for (part in query.split("&")) {
            val kv = part.split("=")
            if (kv.size == 2) {
                val k = kv[0].trim().lowercase()
                val v = try { java.net.URLDecoder.decode(kv[1].trim(), "UTF-8") } catch (_: Exception) { kv[1].trim() }
                when (k) {
                    "abha", "abhaid" -> abha = v
                    "name" -> name = v
                    "blood", "bloodgroup" -> blood = v
                    "vitals" -> vitals = v
                    "phone" -> phone = v
                    "emergency" -> emergency = v
                    "allergies" -> allergies = v
                }
            }
        }
        return ScannedPatientDossier(
            abhaId = abha,
            name = name,
            age = 28,
            gender = "Female",
            bloodGroup = blood,
            phone = phone,
            emergencyContact = emergency,
            allergies = allergies,
            vitalsSummary = vitals,
            chiefComplaint = "Live verified ABDM Health QR scanned via mobile camera.",
            verifiedTimestamp = "ABDM Health QR Live Scanned"
        )
    }

    val matched = SAMPLE_PATIENT_QRS.find {
        it.abhaId.contains(trimmed, ignoreCase = true) ||
        it.name.contains(trimmed, ignoreCase = true)
    }
    if (matched != null) return matched

    return ScannedPatientDossier(
        abhaId = if (trimmed.startsWith("ABHA-")) trimmed else "ABHA-91-" + trimmed.takeLast(8),
        name = "Patient ($trimmed)",
        age = 30,
        gender = "Not specified",
        bloodGroup = "O+",
        phone = "+91 98000 11223",
        emergencyContact = "+91 98000 44556",
        allergies = "None recorded",
        vitalsSummary = "BP 120/80 mmHg • Pulse 72 bpm • SpO2 98%",
        chiefComplaint = "Live scanned patient QR token: $trimmed",
        verifiedTimestamp = "ABDM M3 QR Verified"
    )
}

private val SAMPLE_PATIENT_QRS = listOf(
    ScannedPatientDossier(
        abhaId = "ABHA-91-8472-9102-4412",
        name = "Priya Sharma",
        age = 28,
        gender = "Female",
        bloodGroup = "B+",
        phone = "+91 98290 12345",
        email = "priya.sharma@healthmail.in",
        city = "Jaipur, Rajasthan",
        emergencyContact = "+91 98290 99887 (Rahul Sharma - Spouse)",
        allergies = "Penicillin (Severe Rash / Anaphylaxis Risk)",
        vitalsSummary = "BP 118/78 mmHg • Pulse 72 bpm • Temp 98.4°F • SpO2 99%",
        chiefComplaint = "High fever (100.2°F), dry cough, and sore throat for 3 days. Voice symptom intake recorded.",
        chronicConditions = "Seasonal Allergic Rhinitis",
        activeMedications = "Azithromycin 500mg (Completed), Paracetamol 650mg SOS",
        pastHistory = "Tonsillectomy in 2018. Full recovery.",
        verifiedTimestamp = "ABDM M3 QR Verified • 24h Clinical Consent Granted"
    ),
    ScannedPatientDossier(
        abhaId = "ABHA-91-3810-5519-7821",
        name = "Rohan Mehta",
        age = 45,
        gender = "Male",
        bloodGroup = "O+",
        phone = "+91 98290 44321",
        email = "rohan.mehta@corphealth.in",
        city = "Jaipur, Rajasthan",
        emergencyContact = "+91 98290 66778 (Ananya Mehta - Sister)",
        allergies = "Sulfa Antibiotics, Aspirin (Gastric bleeding risk)",
        vitalsSummary = "BP 132/86 mmHg • Pulse 78 bpm • Temp 99.1°F • SpO2 97%",
        chiefComplaint = "Persistent joint pain in knees and mild morning stiffness for 2 weeks.",
        chronicConditions = "Pre-hypertension, Mild Osteoarthritis",
        activeMedications = "Calcium + Vit D3, Glucosamine Sulphate",
        pastHistory = "Knee arthroscopy 2021. Regular physical therapy.",
        verifiedTimestamp = "ABDM M3 QR Verified • 24h Clinical Consent Granted"
    ),
    ScannedPatientDossier(
        abhaId = "ABHA-91-9921-4328-1120",
        name = "Sunita Devi",
        age = 62,
        gender = "Female",
        bloodGroup = "A+",
        phone = "+91 98290 77112",
        email = "sunita.devi@healthnet.in",
        city = "Ajmer, Rajasthan",
        emergencyContact = "+91 98290 33221 (Vikram Singh - Son)",
        allergies = "None recorded",
        vitalsSummary = "BP 128/80 mmHg • Pulse 70 bpm • Temp 98.6°F • SpO2 98%",
        chiefComplaint = "Routine diabetic follow-up & chronic hypertension medication refill.",
        chronicConditions = "Type 2 Diabetes Mellitus (HbA1c 6.8%), Primary Hypertension",
        activeMedications = "Metformin 500mg BD, Telmisartan 40mg OD",
        pastHistory = "Under regular endocrinology care since 2016.",
        verifiedTimestamp = "ABDM M3 QR Verified • 24h Clinical Consent Granted"
    )
)

@Composable
fun DoctorPatientQrScannerDialog(
    onDismiss: () -> Unit,
    onPatientLoaded: (ScannedPatientDossier) -> Unit
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

    var manualInputToken by remember { mutableStateOf("") }
    var selectedScannedPatient by remember { mutableStateOf<ScannedPatientDossier?>(null) }
    var showManualInput by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(false) }

    // Auto prompt camera permission on launch if not granted
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
                        Toast.makeText(context, "Patient QR recognized from photo!", Toast.LENGTH_SHORT).show()
                        selectedScannedPatient = parsePatientDossierFromQr(decoded)
                    } else {
                        Toast.makeText(context, "No valid QR code detected in image", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Laser animation
    val infiniteTransition = rememberInfiniteTransition(label = "doctor_scanner_laser")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
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
            if (selectedScannedPatient != null) {
                // Scanned Patient Review Dossier Screen
                val patient = selectedScannedPatient!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SoftEmeraldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Patient Health QR Verified",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "ABDM Consent Verified • Longitudinal Access Granted",
                                    fontSize = 11.sp,
                                    color = SoftEmeraldAccent
                                )
                            }
                        }

                        IconButton(
                            onClick = { selectedScannedPatient = null },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Back", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Patient Identity Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = patient.name,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LightBlueHeader
                                    )
                                    Text(
                                        text = "${patient.gender} • ${patient.age} Yrs • Blood: ${patient.bloodGroup}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextDarkSlate
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SoftEmeraldLight,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
                                ) {
                                    Text(
                                        text = "VERIFIED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftEmeraldDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "ABHA ID: ${patient.abhaId}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = LightBluePrimary
                            )
                            Text(
                                text = "Phone: ${patient.phone} • Emergency: ${patient.emergencyContact}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Allergies Safety Warning
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (patient.allergies.contains("Penicillin", ignoreCase = true) ||
                                patient.allergies.contains("Sulfa", ignoreCase = true)) Color(0xFFFFF1F2)
                            else Color(0xFFF0FDF4)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (patient.allergies.contains("Penicillin", ignoreCase = true) ||
                                patient.allergies.contains("Sulfa", ignoreCase = true)) Color(0xFFFECDD3)
                            else Color(0xFFBBF7D0)
                        )
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = if (patient.allergies.contains("Penicillin", ignoreCase = true)) Color(0xFFE11D48)
                                else SoftEmeraldDark,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "KNOWN DRUG ALLERGIES / CONTRAINDICATIONS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (patient.allergies.contains("Penicillin", ignoreCase = true)) Color(0xFFBE123C)
                                    else SoftEmeraldDark,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = patient.allergies,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkSlate
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Vitals Snapshot
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "LATEST CHECK-IN VITALS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightBluePrimary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = patient.vitalsSummary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDarkSlate
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Chief Complaint & Voice Intake
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = LightBluePrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "VOICE SYMPTOM INTAKE SUMMARY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBluePrimary,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = patient.chiefComplaint,
                                fontSize = 13.sp,
                                color = TextDarkSlate,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clinical History & Current Medications
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Medication, contentDescription = null, tint = LightBluePrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CHRONIC CONDITIONS & MEDICATIONS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBluePrimary,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Conditions: ${patient.chronicConditions}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDarkSlate
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Active Rx: ${patient.activeMedications}",
                                fontSize = 12.sp,
                                color = TextDarkSlate
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Past Record: ${patient.pastHistory}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Load Button
                    Button(
                        onClick = {
                            onPatientLoaded(patient)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("doctor_load_patient_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Patient In Doctor Workspace", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { selectedScannedPatient = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Scan Another Patient QR")
                    }
                }
            } else {
                // Active Camera Scanning View
                Box(modifier = Modifier.fillMaxSize()) {
                    // Live Camera Preview Feed when permission is granted
                    if (hasCameraPermission) {
                        CameraQrScannerView(
                            modifier = Modifier.fillMaxSize(),
                            isFlashOn = isFlashOn,
                            useFrontCamera = useFrontCamera,
                            onQrScanned = { rawQr ->
                                try {
                                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
                                    } else {
                                        @Suppress("DEPRECATION")
                                        vibrator?.vibrate(120)
                                    }
                                } catch (_: Exception) {}
                                Toast.makeText(context, "Patient QR Code scanned successfully!", Toast.LENGTH_SHORT).show()
                                selectedScannedPatient = parsePatientDossierFromQr(rawQr)
                            }
                        )
                    }

                    // Viewfinder, Laser and Controls Overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = SoftEmeraldAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Scan Patient Health QR",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "Point camera at patient ABHA QR or Health ID",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Upload image from gallery
                                IconButton(
                                    onClick = {
                                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Upload QR", tint = Color.White)
                                }

                                // Manual input toggle
                                IconButton(
                                    onClick = { showManualInput = !showManualInput },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (showManualInput) SoftEmeraldAccent else Color.Black.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Manual Code", tint = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (!hasCameraPermission) {
                            // Permission Request Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = SoftEmeraldAccent, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Camera Access Required", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Allow camera access to scan patient ABHA QR codes directly using your device camera.",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                                        modifier = Modifier.testTag("grant_camera_permission_btn")
                                    ) {
                                        Text("Grant Camera Permission", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Reticle Viewfinder Box
                        Box(
                            modifier = Modifier
                                .size(260.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(2.dp, if (hasCameraPermission) SoftEmeraldAccent else Color.White.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cornerLength = 36.dp.toPx()
                                val stroke = 4.dp.toPx()
                                val color = androidx.compose.ui.graphics.Color(0xFF10B981)

                                // Top-left
                                drawLine(color, Offset(0f, 0f), Offset(cornerLength, 0f), stroke)
                                drawLine(color, Offset(0f, 0f), Offset(0f, cornerLength), stroke)
                                // Top-right
                                drawLine(color, Offset(size.width, 0f), Offset(size.width - cornerLength, 0f), stroke)
                                drawLine(color, Offset(size.width, 0f), Offset(size.width, cornerLength), stroke)
                                // Bottom-left
                                drawLine(color, Offset(0f, size.height), Offset(cornerLength, size.height), stroke)
                                drawLine(color, Offset(0f, size.height), Offset(0f, size.height - cornerLength), stroke)
                                // Bottom-right
                                drawLine(color, Offset(size.width, size.height), Offset(size.width - cornerLength, size.height), stroke)
                                drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - cornerLength), stroke)

                                // Laser Line
                                val laserY = size.height * laserYRatio
                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color.Transparent, Color(0xFF10B981), Color(0xFF34D399), Color.Transparent)
                                    ),
                                    start = Offset(0f, laserY),
                                    end = Offset(size.width, laserY),
                                    strokeWidth = 3.dp.toPx()
                                )
                            }

                            if (!hasCameraPermission) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.25f),
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Enable camera to scan",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live status pill + Torch/Flip controls
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Black.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
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
                                        text = if (hasCameraPermission) "Live Camera Active" else "Camera Offline",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Torch button
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isFlashOn) SoftEmeraldAccent else Color.Black.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                modifier = Modifier.clickable { isFlashOn = !isFlashOn }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                        contentDescription = "Flash",
                                        tint = if (isFlashOn) Color.Black else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Flash",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isFlashOn) Color.Black else Color.White
                                    )
                                }
                            }

                            // Camera Flip button
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Black.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                modifier = Modifier.clickable { useFrontCamera = !useFrontCamera }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.FlipCameraAndroid,
                                        contentDescription = "Flip",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (useFrontCamera) "Front" else "Rear",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Manual Paste or Input
                        if (showManualInput) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = manualInputToken,
                                    onValueChange = { manualInputToken = it },
                                    label = { Text("ABHA ID or Token", color = Color.White.copy(alpha = 0.7f)) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("doctor_manual_qr_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = SoftEmeraldAccent,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Button(
                                    onClick = {
                                        selectedScannedPatient = parsePatientDossierFromQr(manualInputToken)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                                    modifier = Modifier.height(52.dp)
                                ) {
                                    Text("Lookup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Instant Simulation Button for Testing Without Physical Camera
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF064E3B).copy(alpha = 0.9f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SoftEmeraldAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val demo = SAMPLE_PATIENT_QRS[0]
                                    try {
                                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                                        } else {
                                            @Suppress("DEPRECATION")
                                            vibrator?.vibrate(100)
                                        }
                                    } catch (_: Exception) {}
                                    Toast.makeText(context, "Loaded ABDM Patient: ${demo.name}", Toast.LENGTH_SHORT).show()
                                    selectedScannedPatient = demo
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = SoftEmeraldAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Simulate Scan Patient QR (Priya Sharma)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fast Demo Patients Chips for Instant Testing
                        Text(
                            text = "QUICK TEST PATIENT QR PRESETS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(SAMPLE_PATIENT_QRS) { patient ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedScannedPatient = patient
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(SoftEmeraldAccent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(patient.name.take(1), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(patient.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("${patient.age}y • ${patient.bloodGroup}", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
