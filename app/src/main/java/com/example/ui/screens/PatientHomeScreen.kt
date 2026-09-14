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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.HealthIdCard
import com.example.ui.components.QrCodeView
import com.example.ui.theme.*

@Composable
fun PatientHomeScreen(
    user: UserProfile,
    vitals: List<HealthVitals>,
    reminders: List<MedicationReminder>,
    stepMetrics: HealthStepMetrics,
    activeCheckIn: FacilityCheckIn? = null,
    onOpenCheckIn: () -> Unit,
    onCheckInFacility: (FacilityCheckIn) -> Unit = {},
    onCheckoutFacility: () -> Unit = {},
    onToggleReminder: (String) -> Unit,
    onAddSteps: () -> Unit,
    onAddWater: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToMedications: () -> Unit,
    onNavigateToConsent: () -> Unit,
    onNavigateToCaseIntake: () -> Unit = {}
) {
    var showQrDialog by remember { mutableStateOf(false) }
    var showFacilityScanner by remember { mutableStateOf(false) }
    var showDigitalPassDialog by remember { mutableStateOf(false) }
    val latestVital = vitals.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhiteBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Welcome strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Text(
                    text = user.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightBlueHeader
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = SoftEmeraldAccent,
                    modifier = Modifier
                        .clickable { showFacilityScanner = true }
                        .testTag("scan_facility_qr_top_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Facility QR",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Scan QR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = CircleShape,
                    color = SoftEmeraldLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder),
                    modifier = Modifier.clickable { showQrDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.QrCode,
                            contentDescription = "My QR",
                            tint = SoftEmeraldDark,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "My QR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftEmeraldDark
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Digital Health ID Card
        HealthIdCard(user = user, onShowFullQr = { showQrDialog = true })

        Spacer(modifier = Modifier.height(14.dp))

        // Medical Facility QR Check-In Section (ABDM Scan & Share)
        if (activeCheckIn != null) {
            // Active OPD Pass Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(18.dp))
                    .testTag("active_checkin_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SoftEmeraldAccent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(SoftEmeraldLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SoftEmeraldDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CHECKED IN AT HOSPITAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftEmeraldDark,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftEmeraldLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
                        ) {
                            Text(
                                text = "IN QUEUE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftEmeraldDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeCheckIn.facilityName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightBlueHeader
                            )
                            Text(
                                text = "${activeCheckIn.department} • ${activeCheckIn.counterNumber}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SoftEmeraldLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldAccent)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "TOKEN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftEmeraldDark
                                )
                                Text(
                                    text = activeCheckIn.tokenNumber,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SoftEmeraldDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextDarkSlate,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Estimated Wait: ~${activeCheckIn.estimatedWaitMinutes} mins • Checked in ${activeCheckIn.timestamp}",
                            fontSize = 11.sp,
                            color = TextDarkSlate
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDigitalPassDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("view_digital_pass_button")
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Digital Pass", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                onCheckoutFacility()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("Check out", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // Fast Hospital Check-In Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(18.dp))
                    .testTag("fast_hospital_checkin_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SoftEmeraldLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = SoftEmeraldDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Fast Hospital Check-In",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBlueHeader
                                )
                                Text(
                                    text = "ABDM Scan & Share Protocol",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftEmeraldLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
                        ) {
                            Text(
                                text = "FAST-TRACK",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftEmeraldDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Scan hospital OPD counter or kiosk QR code to share your Health ID instantly and skip registration queues.",
                        fontSize = 12.sp,
                        color = TextDarkSlate,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showFacilityScanner = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("scan_hospital_qr_button")
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scan Hospital QR Code",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionCard(
                title = "Scan to Check-In",
                subtitle = "Facility QR Scanner",
                icon = Icons.Default.QrCodeScanner,
                tint = SoftEmeraldDark,
                containerColor = SoftEmeraldLight,
                borderColor = SoftEmeraldBorder,
                modifier = Modifier.weight(1f).testTag("quick_action_scan_qr"),
                onClick = { showFacilityScanner = true }
            )
            QuickActionCard(
                title = "Early Vitals",
                subtitle = "Log Before Visit",
                icon = Icons.Default.Favorite,
                tint = SoftEmeraldDark,
                containerColor = WarmWhiteSubtle,
                borderColor = SurfaceBorder,
                modifier = Modifier.weight(1f).testTag("early_checkin_card"),
                onClick = onOpenCheckIn
            )
            QuickActionCard(
                title = "Doctor Consent",
                subtitle = "Manage Access",
                icon = Icons.Default.Shield,
                tint = LightBlueHeader,
                containerColor = LightBlueContainer,
                borderColor = LightBlueSoft,
                modifier = Modifier.weight(1f).testTag("doctor_consent_card"),
                onClick = onNavigateToConsent
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SIH 2026 Case-Taking Intake Banner (Voice / Touch Intake & Doctor Review)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SoftEmeraldLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToCaseIntake() }
                .testTag("home_case_intake_banner")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = SoftEmeraldAccent,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Voice / Touch Case Intake",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftEmeraldDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SoftEmeraldContainer
                        ) {
                            Text(
                                text = "NEW",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftEmeraldDark,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Prepare symptoms & doctor review summary before visit",
                        fontSize = 11.sp,
                        color = TextDarkSlate
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = SoftEmeraldDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Vitals Overview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.MonitorHeart,
                            contentDescription = null,
                            tint = SoftEmeraldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Current Health Vitals",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightBlueHeader
                        )
                    }
                    Text(
                        text = latestVital?.timestamp ?: "No record",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (latestVital != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        VitalMetricPill(
                            label = "Blood Pressure",
                            value = "${latestVital.bpSystolic}/${latestVital.bpDiastolic}",
                            unit = "mmHg",
                            isNormal = latestVital.bpSystolic in 100..130
                        )
                        VitalMetricPill(
                            label = "Pulse Rate",
                            value = "${latestVital.pulseRate}",
                            unit = "bpm",
                            isNormal = latestVital.pulseRate in 60..100
                        )
                        VitalMetricPill(
                            label = "SpO2 Level",
                            value = "${latestVital.spo2Percent}%",
                            unit = "O2 Sat",
                            isNormal = latestVital.spo2Percent >= 95
                        )
                        VitalMetricPill(
                            label = "Temperature",
                            value = "${latestVital.temperatureF}°",
                            unit = "Fahrenheit",
                            isNormal = latestVital.temperatureF in 97.0..99.5
                        )
                    }
                } else {
                    Text(
                        text = "No vitals logged yet. Tap Early Check-in to capture baseline vitals.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Step & Health Status Tracker (from Sketch: "Also tracks steps and Health status with vitals")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = LightBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily Health & Step Tracker",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightBlueHeader
                        )
                    }
                    TextButton(onClick = onAddSteps) {
                        Text("+500 Steps", fontSize = 11.sp, color = SoftEmeraldDark, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val progress = (stepMetrics.stepsToday.toFloat() / stepMetrics.stepGoal.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SoftEmeraldAccent,
                    trackColor = WarmWhiteSubtle
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "${stepMetrics.stepsToday} / ${stepMetrics.stepGoal}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                        Text(text = "Daily Steps", fontSize = 11.sp, color = TextMuted)
                    }
                    Column {
                        Text(text = "${stepMetrics.caloriesBurned} kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                        Text(text = "Active Burn", fontSize = 11.sp, color = TextMuted)
                    }
                    Column {
                        Text(text = "${stepMetrics.distanceKm} km", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                        Text(text = "Distance", fontSize = 11.sp, color = TextMuted)
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.clickable { onAddWater() }
                    ) {
                        Text(text = "${stepMetrics.waterGlasses} 💧", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightBlueHeader)
                        Text(text = "Tap +Water", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Active Medication Reminders Strip
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Medication,
                            contentDescription = null,
                            tint = SoftEmeraldDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Today's Medication Schedule",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightBlueHeader
                        )
                    }
                    TextButton(onClick = onNavigateToMedications) {
                        Text("View All", fontSize = 12.sp, color = LightBluePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (reminders.isEmpty()) {
                    Text("No active medicines scheduled.", fontSize = 12.sp, color = TextMuted)
                } else {
                    reminders.take(3).forEach { rem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (rem.isTakenToday) SoftEmeraldLight else WarmWhiteSubtle)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rem.medicineName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rem.isTakenToday) SoftEmeraldDark else TextDarkSlate
                                )
                                Text(
                                    text = "${rem.timeSlot} • ${rem.mealInstruction}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            if (rem.isTakenToday) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SoftEmeraldContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Taken",
                                            tint = SoftEmeraldDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Taken",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SoftEmeraldDark
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { onToggleReminder(rem.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Take", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Full QR Dialog
    if (showQrDialog) {
        Dialog(onDismissRequest = { showQrDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Secure Patient Identity Token",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightBlueHeader
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Show this QR to Doctor or Medicine Centre to grant consent-controlled access.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    QrCodeView(dataToken = user.qrToken, sizeDp = 200)

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = user.abhaId,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightBluePrimary
                    )
                    Text(
                        text = "Token: ${user.qrToken}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showQrDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }

    // Live Hospital Facility QR Scanner Dialog
    if (showFacilityScanner) {
        FacilityQrScannerDialog(
            user = user,
            onDismiss = { showFacilityScanner = false },
            onCheckInSuccess = { newCheckIn ->
                onCheckInFacility(newCheckIn)
            }
        )
    }

    // Active Digital OPD Pass Dialog
    if (showDigitalPassDialog && activeCheckIn != null) {
        DigitalOpdPassDialog(
            user = user,
            checkIn = activeCheckIn,
            onDismiss = { showDigitalPassDialog = false },
            onCheckout = {
                onCheckoutFacility()
            }
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    containerColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
            Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
fun VitalMetricPill(label: String, value: String, unit: String, isNormal: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isNormal) SoftEmeraldDark else WarningAmber
        )
        Text(text = unit, fontSize = 10.sp, color = TextMuted)
        Text(text = label, fontSize = 9.sp, color = TextLightSlate)
    }
}
