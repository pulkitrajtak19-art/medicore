package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun DoctorPortalScreen(
    doctor: UserProfile,
    vitals: List<HealthVitals>,
    consultations: List<DoctorConsultation>,
    prescriptions: List<Prescription>,
    doctorReports: List<DoctorReviewReport> = emptyList(),
    onConfirmDoctorReview: (String, String) -> Unit = { _, _ -> },
    onOpenNewConsultation: (String) -> Unit = {}
) {
    var searchToken by remember { mutableStateOf("ABHA-91-8472-9102-4412") }
    var isPatientVerified by remember { mutableStateOf(true) }
    var showDoctorQrScanner by remember { mutableStateOf(false) }
    var activeScannedPatient by remember { mutableStateOf<ScannedPatientDossier?>(null) }
    val latestReport = doctorReports.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhiteBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
        // Doctor Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SoftEmeraldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MedicalInformation,
                        contentDescription = null,
                        tint = SoftEmeraldDark,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = doctor.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LightBlueHeader)
                    Text(text = "Internal Medicine • ${doctor.abhaId}", fontSize = 12.sp, color = TextDarkSlate)
                    Text(text = "Jaipur Care Clinic (OPD Room 4)", fontSize = 11.sp, color = TextMuted)
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SoftEmeraldLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
                ) {
                    Text(
                        text = "ACTIVE CLINICIAN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftEmeraldDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scan & Search Patient QR / Token
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Scan / Verify Patient Health QR Token",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightBlueHeader
                )
                Text(
                    text = "Doctor scans patient's ABHA token to query authorized longitudinal timeline",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchToken,
                        onValueChange = { searchToken = it },
                        label = { Text("Patient Health Token / ABHA ID") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = LightBluePrimary) },
                        trailingIcon = {
                            if (searchToken.isNotEmpty()) {
                                IconButton(onClick = { searchToken = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("doctor_patient_token_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightBluePrimary,
                            unfocusedBorderColor = SurfaceBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = { isPatientVerified = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                        modifier = Modifier.height(52.dp).testTag("doctor_verify_patient_btn")
                    ) {
                        Text("Verify & Pull", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showDoctorQrScanner = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("doctor_open_qr_scanner_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftEmeraldAccent,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Scan Patient Health QR Code",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (searchToken.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "Quick fill:", fontSize = 10.sp, color = TextMuted)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WarmWhiteSubtle,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.clickable {
                                searchToken = "ABHA-91-8472-9102-4412"
                                isPatientVerified = true
                            }
                        ) {
                            Text(
                                text = "Priya's Token (ABHA-91-8472-9102-4412)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LightBlueHeader,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (isPatientVerified) {
                    Spacer(modifier = Modifier.height(14.dp))
                    val pName = activeScannedPatient?.name ?: "Priya Sharma"
                    val pAge = activeScannedPatient?.age ?: 28
                    val pGender = activeScannedPatient?.gender ?: "Female"
                    val pBlood = activeScannedPatient?.bloodGroup ?: "B+"
                    val pAbha = activeScannedPatient?.abhaId ?: "ABHA-91-8472-9102-4412"
                    val pPhone = activeScannedPatient?.phone ?: "+91 98290 12345"
                    val pEmail = activeScannedPatient?.email ?: "priya.sharma@healthmail.in"
                    val pCity = activeScannedPatient?.city ?: "Jaipur, Rajasthan"
                    val pEmergency = activeScannedPatient?.emergencyContact ?: "+91 98290 99887 (Rahul Sharma - Spouse)"
                    val pAllergies = activeScannedPatient?.allergies ?: "Penicillin (Severe Rash / Anaphylaxis Risk)"
                    val pComplaint = activeScannedPatient?.chiefComplaint ?: "High fever (100.2°F), dry cough, and sore throat for 3 days. Voice symptom intake recorded."
                    val pConditions = activeScannedPatient?.chronicConditions ?: "Seasonal Allergic Rhinitis"
                    val pMeds = activeScannedPatient?.activeMedications ?: "Azithromycin 500mg (Completed), Paracetamol 650mg SOS"
                    val pHistory = activeScannedPatient?.pastHistory ?: "Tonsillectomy in 2018. Full recovery."
                    val pVitals = activeScannedPatient?.vitalsSummary ?: "BP 118/78 mmHg • Pulse 72 bpm • Temp 98.4°F • SpO2 99%"
                    val pVerified = activeScannedPatient?.verifiedTimestamp ?: "ABDM M3 QR Verified • 24h Clinical Consent Granted"

                    // Master Scanned Patient Clinical Dossier
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftEmeraldLight),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, SoftEmeraldAccent)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header Row: Avatar + Name + Verified Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(SoftEmeraldAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = pName.take(1),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = pName,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LightBlueHeader
                                        )
                                        Text(
                                            text = "$pAge Yrs • $pGender • Blood: $pBlood",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextDarkSlate
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SoftEmeraldAccent
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "QR LINKED",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = SoftEmeraldBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Identifiers & Contact Details
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("ABHA ID", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Text(pAbha, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LightBluePrimary)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("CONTACT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Text(pPhone, fontSize = 12.sp, color = TextDarkSlate)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("EMAIL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Text(pEmail, fontSize = 11.sp, color = TextDarkSlate)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("LOCATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Text(pCity, fontSize = 11.sp, color = TextDarkSlate)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("EMERGENCY CONTACT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text(pEmergency, fontSize = 11.sp, color = TextDarkSlate)

                            // Allergy / Contraindication Alert
                            if (pAllergies.isNotBlank() && !pAllergies.equals("None recorded", ignoreCase = true) && !pAllergies.equals("None known", ignoreCase = true)) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFFF1F2),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "KNOWN DRUG ALLERGIES / CONTRAINDICATIONS",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFBE123C)
                                            )
                                            Text(
                                                text = pAllergies,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF9F1239)
                                            )
                                        }
                                    }
                                }
                            }

                            // Vitals Snapshot
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Favorite, contentDescription = null, tint = SoftEmeraldDark, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("VITALS SNAPSHOT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SoftEmeraldDark)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(pVitals, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDarkSlate)
                                }
                            }

                            // Chief Complaint & Voice Intake
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = LightBluePrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("CHIEF COMPLAINT / INTAKE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LightBluePrimary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(pComplaint, fontSize = 12.sp, color = TextDarkSlate, lineHeight = 16.sp)
                                }
                            }

                            // Chronic Conditions & Active Medications
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Medication, contentDescription = null, tint = LightBluePrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("CHRONIC CONDITIONS & ACTIVE MEDICATIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LightBluePrimary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Conditions: $pConditions", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextDarkSlate)
                                    Text("Active Rx: $pMeds", fontSize = 11.sp, color = TextDarkSlate)
                                    Text("Past History: $pHistory", fontSize = 10.sp, color = TextMuted)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Fast Action Buttons for Doctor
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onOpenNewConsultation(pName) },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent)
                                ) {
                                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Prescribe for $pName", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showDoctorQrScanner = true },
                                    modifier = Modifier.height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftEmeraldDark)
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rescan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDoctorQrScanner) {
            DoctorPatientQrScannerDialog(
                onDismiss = { showDoctorQrScanner = false },
                onPatientLoaded = { dossier ->
                    activeScannedPatient = dossier
                    searchToken = dossier.abhaId
                    isPatientVerified = true
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Patient Vitals Summary (from Check-in)
        if (vitals.isNotEmpty()) {
            val v = vitals.first()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WarmWhiteSubtle),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PRE-CONSULTATION CHECK-IN VITALS (${v.timestamp})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightBlueHeader
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "BP: ${v.bpSystolic}/${v.bpDiastolic} mmHg", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                        Text(text = "Pulse: ${v.pulseRate} bpm", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                        Text(text = "SpO2: ${v.spo2Percent}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                        Text(text = "Temp: ${v.temperatureF}°F", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // SIH 2026: Pre-Consultation Case-Taking & Review Summary (Prepared by HealthID before visit)
        if (latestReport != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Assignment,
                                contentDescription = null,
                                tint = SoftEmeraldDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pre-Consultation Case Summary",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightBlueHeader
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (latestReport.isDoctorConfirmed) SoftEmeraldLight else WarningContainer
                        ) {
                            Text(
                                text = if (latestReport.isDoctorConfirmed) "CONFIRMED ✓" else "REVIEW NEEDED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (latestReport.isDoctorConfirmed) SoftEmeraldDark else WarningAmber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = latestReport.chiefComplaintSummary,
                        fontSize = 11.sp,
                        color = TextDarkSlate
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        latestReport.extractedSymptoms.forEach { s ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SoftEmeraldLight
                            ) {
                                Text(
                                    text = s,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SoftEmeraldDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (latestReport.allergies.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFEBEE),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Dangerous,
                                    contentDescription = null,
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Allergy Contraindication Alert: ${latestReport.allergies.joinToString(", ")}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!latestReport.isDoctorConfirmed) {
                        OutlinedButton(
                            onClick = { onConfirmDoctorReview(latestReport.id, doctor.name) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Doctor Confirms Summary Before Action", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "✓ Clinician validated by ${latestReport.confirmedByDoctorName ?: doctor.name}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftEmeraldDark
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action: Start New Consultation & Issue Digital Rx
        Button(
            onClick = { onOpenNewConsultation(activeScannedPatient?.name ?: "Priya Sharma") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("start_consultation_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent)
        ) {
            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Record Consultation & Create Digital Rx", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Longitudinal History View for Doctor
        Text(
            text = "Patient Authorized Longitudinal Timeline",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = LightBlueHeader
        )
        Spacer(modifier = Modifier.height(8.dp))

        consultations.forEach { cons ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = cons.diagnosis, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SoftEmeraldDark)
                        Text(text = cons.timestamp, fontSize = 10.sp, color = TextMuted)
                    }
                    Text(text = "By ${cons.doctorName} (${cons.clinicName})", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Remarks: ${cons.clinicalRemarks}", fontSize = 11.sp, color = TextDarkSlate)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
