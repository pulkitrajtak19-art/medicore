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
    onOpenNewConsultation: () -> Unit
) {
    var searchToken by remember { mutableStateOf("PH-TOKEN-8472-9102") }
    var isPatientVerified by remember { mutableStateOf(true) }
    val latestReport = doctorReports.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhiteBackground)
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
                                searchToken = "ABHA-TOKEN-PRIYA-9842"
                                isPatientVerified = true
                            }
                        ) {
                            Text(
                                text = "Priya's Token (ABHA-TOKEN-PRIYA-9842)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LightBlueHeader,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (isPatientVerified) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftEmeraldLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Patient: Priya Sharma (28y, Female, O+)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftEmeraldDark
                                )
                                Text(
                                    text = "ABHA: ABHA-91-8472-9102-4412 • Consent Granted (24h)",
                                    fontSize = 11.sp,
                                    color = TextDarkSlate
                                )
                            }
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SoftEmeraldDark)
                        }
                    }
                }
            }
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
            onClick = onOpenNewConsultation,
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
