package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserRole
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.HealthViewModel

sealed class PatientTab(val title: String, val iconFilled: ImageVector, val iconOutlined: ImageVector) {
    object Home : PatientTab("Medicore", Icons.Default.HealthAndSafety, Icons.Outlined.HealthAndSafety)
    object VoiceIntake : PatientTab("Voice Intake", Icons.Default.Mic, Icons.Outlined.Mic)
    object Timeline : PatientTab("Timeline", Icons.Default.Timeline, Icons.Outlined.Timeline)
    object Medicines : PatientTab("Medicines", Icons.Default.Medication, Icons.Outlined.Medication)
    object Consent : PatientTab("Privacy", Icons.Default.Shield, Icons.Outlined.Shield)

    companion object {
        val VoiceTouchIntake = VoiceIntake
    }
}

@Composable
fun MainAppContent(viewModel: HealthViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val vitals by viewModel.vitalsList.collectAsStateWithLifecycle()
    val prescriptions by viewModel.prescriptions.collectAsStateWithLifecycle()
    val consultations by viewModel.consultations.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val consents by viewModel.consents.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val stepMetrics by viewModel.stepMetrics.collectAsStateWithLifecycle()
    val caseIntakes by viewModel.caseIntakes.collectAsStateWithLifecycle()
    val doctorReviewReports by viewModel.doctorReviewReports.collectAsStateWithLifecycle()
    val activeCheckIn by viewModel.activeCheckIn.collectAsStateWithLifecycle()
    val checkInsHistory by viewModel.checkInsHistory.collectAsStateWithLifecycle()

    var selectedPatientTab by remember { mutableStateOf<PatientTab>(PatientTab.Home) }
    var showVitalsDialog by remember { mutableStateOf(false) }
    var showConsultationDialog by remember { mutableStateOf(false) }
    var activeDoctorPatientName by remember { mutableStateOf("Priya Sharma") }
    var showRoleSwitchDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    if (currentUser == null) {
        AuthScreen(
            onLoginSuccess = { id, role, isPhone -> viewModel.login(id, role, isPhone) },
            onSignUpSuccess = { name, id, role, age, gender, blood, isPhone ->
                viewModel.signUp(name, id, role, age, gender, blood, isPhone)
            },
            onUserProfileReady = { profile ->
                viewModel.setUserProfile(profile)
            }
        )
    } else {
        val user = currentUser!!

        Scaffold(
            topBar = {
                Surface(
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 840.dp)
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SoftEmeraldLight)
                                    .border(1.dp, SoftEmeraldBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = SoftEmeraldDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Health ID",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBlueHeader
                                )
                                Text(
                                    text = user.role.displayName,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Profile Details Button
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = LightBlueContainer.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                                modifier = Modifier
                                    .clickable { showProfileDialog = true }
                                    .testTag("top_bar_profile_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = "User Profile",
                                        tint = LightBluePrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Profile",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LightBlueHeader
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Quick Role Switcher Button
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = WarmWhiteSubtle,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                                modifier = Modifier.clickable { showRoleSwitchDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.SwapHoriz,
                                        contentDescription = "Switch Persona",
                                        tint = LightBluePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Role Switch",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LightBlueHeader
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Logout,
                                    contentDescription = "Logout",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
            bottomBar = {
                if (user.role == UserRole.PATIENT) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 4.dp,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        val tabs = listOf(
                            PatientTab.Home,
                            PatientTab.VoiceIntake,
                            PatientTab.Timeline,
                            PatientTab.Medicines,
                            PatientTab.Consent
                        )
                        tabs.forEach { tab ->
                            val isSelected = selectedPatientTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedPatientTab = tab },
                                modifier = Modifier.testTag(
                                    if (tab == PatientTab.VoiceIntake) "tab_voice_intake"
                                    else "tab_${tab.title.lowercase().replace(" ", "_")}"
                                ),
                                icon = {
                                    Icon(
                                        if (isSelected) tab.iconFilled else tab.iconOutlined,
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 9.5.sp,
                                        lineHeight = 11.sp,
                                        textAlign = TextAlign.Center,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SoftEmeraldDark,
                                    selectedTextColor = SoftEmeraldDark,
                                    indicatorColor = SoftEmeraldLight,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 840.dp)
                ) {
                    when (user.role) {
                        UserRole.PATIENT -> {
                            when (selectedPatientTab) {
                                PatientTab.Home -> PatientHomeScreen(
                                    user = user,
                                    vitals = vitals,
                                    reminders = reminders,
                                    stepMetrics = stepMetrics,
                                    activeCheckIn = activeCheckIn,
                                    onOpenCheckIn = { showVitalsDialog = true },
                                    onCheckInFacility = { fac ->
                                        viewModel.performFacilityCheckIn(
                                            fac.facilityId,
                                            fac.facilityName,
                                            fac.department,
                                            fac.counterNumber,
                                            fac.qrRawData
                                        )
                                    },
                                    onCheckoutFacility = { viewModel.checkoutFacility() },
                                    onToggleReminder = { viewModel.markReminderTaken(it) },
                                    onAddSteps = { viewModel.addSteps(500) },
                                    onAddWater = { viewModel.addWater() },
                                    onNavigateToTimeline = { selectedPatientTab = PatientTab.Timeline },
                                    onNavigateToMedications = { selectedPatientTab = PatientTab.Medicines },
                                    onNavigateToConsent = { selectedPatientTab = PatientTab.Consent },
                                    onNavigateToCaseIntake = { selectedPatientTab = PatientTab.VoiceIntake }
                                )
                                PatientTab.VoiceIntake -> VoiceIntakeScreen(
                                    user = user,
                                    vitals = vitals,
                                    caseIntakes = caseIntakes,
                                    onSaveIntake = { mode, lang, transcript, symptoms, duration, severity, allergies, meds, recTitle, recOcr ->
                                        viewModel.saveCaseIntake(
                                            mode, lang, transcript, symptoms, duration, severity, allergies, meds, recTitle, recOcr
                                        )
                                    },
                                    onNavigateToTimeline = { selectedPatientTab = PatientTab.Timeline }
                                )
                            PatientTab.Timeline -> PatientTimelineScreen(
                                consultations = consultations,
                                doctorReports = doctorReviewReports,
                                vitals = vitals,
                                prescriptions = prescriptions,
                                facilityCheckIns = checkInsHistory,
                                activeCheckIn = activeCheckIn,
                                user = user,
                                onUpdateDoctorNotes = { reportId, notes, diag ->
                                    viewModel.updateDoctorNotes(reportId, notes, diag)
                                },
                                onConfirmDoctorReview = { reportId, docName ->
                                    viewModel.confirmDoctorReview(reportId, docName)
                                },
                                onOpenPrescriptionDialog = { showConsultationDialog = true },
                                onNavigateToIntake = { selectedPatientTab = PatientTab.VoiceTouchIntake },
                                onOpenVitalsDialog = { showVitalsDialog = true }
                            )
                            PatientTab.Medicines -> PatientMedicationsScreen(
                                prescriptions = prescriptions,
                                reminders = reminders,
                                onMarkReminderTaken = { viewModel.markReminderTaken(it) }
                            )
                            PatientTab.Consent -> PatientConsentScreen(
                                consents = consents,
                                auditLogs = auditLogs,
                                onToggleConsent = { viewModel.toggleConsent(it) }
                            )
                        }
                    }
                    UserRole.DOCTOR -> {
                        DoctorPortalScreen(
                            doctor = user,
                            vitals = vitals,
                            consultations = consultations,
                            prescriptions = prescriptions,
                            doctorReports = doctorReviewReports,
                            onConfirmDoctorReview = { reportId, docName ->
                                viewModel.confirmDoctorReview(reportId, docName)
                            },
                            onOpenNewConsultation = { patientName ->
                                activeDoctorPatientName = patientName
                                showConsultationDialog = true
                            }
                        )
                    }
                    UserRole.MEDICINE_CENTRE -> {
                        MedicineCentreScreen(
                            user = user,
                            prescriptions = prescriptions,
                            onDispensePrescription = { rxId, name -> viewModel.dispensePrescription(rxId, name) }
                        )
                    }
                }
            }
        }
    }

        // Dialogs
        if (showVitalsDialog) {
            VitalsCheckInDialog(
                onDismiss = { showVitalsDialog = false },
                onSaveVitals = { sys, dia, pulse, temp, wt, sp ->
                    viewModel.recordVitals(sys, dia, pulse, temp, wt, sp)
                }
            )
        }

        if (showConsultationDialog) {
            DoctorConsultationDialog(
                patientName = activeDoctorPatientName,
                doctorName = user.name,
                onDismiss = { showConsultationDialog = false },
                onSaveConsultation = { complaint, diag, remarks, items ->
                    viewModel.createConsultationWithPrescription(
                        patientId = "patient_001",
                        doctorName = user.name,
                        doctorSpecialty = "Internal Medicine",
                        clinicName = user.city,
                        chiefComplaint = complaint,
                        diagnosis = diag,
                        remarks = remarks,
                        items = items
                    )
                }
            )
        }

        // Fast Role Switcher Dialog
        if (showRoleSwitchDialog) {
            AlertDialog(
                onDismissRequest = { showRoleSwitchDialog = false },
                title = { Text("Switch Portal Role", fontWeight = FontWeight.Bold, color = LightBlueHeader) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Test the closed-loop healthcare journey across different touchpoints:",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        UserRole.values().forEach { r ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (user.role == r) SoftEmeraldContainer else WarmWhiteSubtle
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (user.role == r) SoftEmeraldAccent else SurfaceBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.switchRole(r)
                                        showRoleSwitchDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        when (r) {
                                            UserRole.PATIENT -> Icons.Default.Person
                                            UserRole.DOCTOR -> Icons.Default.MedicalServices
                                            UserRole.MEDICINE_CENTRE -> Icons.Default.LocalPharmacy
                                        },
                                        contentDescription = null,
                                        tint = if (user.role == r) SoftEmeraldDark else TextDarkSlate
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = r.displayName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                                        Text(text = r.badgeTitle, fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRoleSwitchDialog = false }) {
                        Text("Close", color = LightBluePrimary)
                    }
                }
            )
        }

        if (showProfileDialog) {
            ProfileSectionDialog(
                user = user,
                onDismiss = { showProfileDialog = false },
                onSaveProfile = { name, email, phone, age, gender, bloodGroup, heightCm, weightKg, city, emergencyContact, allergies ->
                    viewModel.updateUserProfile(
                        name = name,
                        email = email,
                        phone = phone,
                        age = age,
                        gender = gender,
                        bloodGroup = bloodGroup,
                        heightCm = heightCm,
                        weightKg = weightKg,
                        city = city,
                        emergencyContact = emergencyContact,
                        allergies = allergies
                    )
                }
            )
        }
    }
}
