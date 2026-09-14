package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*

enum class TimelineFilter(val title: String, val chipLabel: String) {
    ALL_TIMELINE("All Records", "All Records"),
    DOCTOR_REVIEW_SUMMARY("Doctor Reviews", "Doctor Reviews"),
    CONSULTATION_HISTORY("Consultations", "Encounters"),
    PRESCRIPTIONS("Prescriptions", "Prescriptions"),
    VITALS("Vitals Logs", "Vitals"),
    FACILITY_CHECKINS("Hospital Passes", "Hospital Passes")
}

// Unified timeline event model for longitudinal stream
sealed class TimelineEvent(
    val id: String,
    val timestamp: String,
    val title: String,
    val subtitle: String,
    val category: TimelineFilter,
    val sortKey: Long
) {
    data class DoctorReviewEvent(val report: DoctorReviewReport) : TimelineEvent(
        id = report.id,
        timestamp = if (report.isDoctorConfirmed && report.confirmedAt != null) report.confirmedAt else report.generatedAt,
        title = "Doctor Review Summary Report",
        subtitle = if (report.isDoctorConfirmed) "Validated by ${report.confirmedByDoctorName ?: "Clinician"}" else "Awaiting Clinician Review",
        category = TimelineFilter.DOCTOR_REVIEW_SUMMARY,
        sortKey = 500L
    )

    data class ConsultationEvent(val consultation: DoctorConsultation) : TimelineEvent(
        id = consultation.id,
        timestamp = consultation.timestamp,
        title = consultation.diagnosis,
        subtitle = "${consultation.doctorName} • ${consultation.clinicName}",
        category = TimelineFilter.CONSULTATION_HISTORY,
        sortKey = 400L
    )

    data class PrescriptionEvent(val prescription: Prescription) : TimelineEvent(
        id = prescription.id,
        timestamp = prescription.date,
        title = "Digital Rx: ${prescription.id}",
        subtitle = "${prescription.doctorName} • ${prescription.items.size} Medications",
        category = TimelineFilter.PRESCRIPTIONS,
        sortKey = 300L
    )

    data class VitalsEvent(val vitals: HealthVitals) : TimelineEvent(
        id = vitals.id,
        timestamp = vitals.timestamp,
        title = "Check-in Vitals (BP ${vitals.bpSystolic}/${vitals.bpDiastolic})",
        subtitle = vitals.recordedBy,
        category = TimelineFilter.VITALS,
        sortKey = 200L
    )

    data class FacilityCheckInEvent(val checkIn: FacilityCheckIn) : TimelineEvent(
        id = checkIn.id,
        timestamp = checkIn.timestamp,
        title = "Hospital Check-in: ${checkIn.facilityName}",
        subtitle = "${checkIn.department} • Token ${checkIn.tokenNumber}",
        category = TimelineFilter.FACILITY_CHECKINS,
        sortKey = 100L
    )
}

@Composable
fun PatientTimelineScreen(
    consultations: List<DoctorConsultation>,
    doctorReports: List<DoctorReviewReport> = emptyList(),
    vitals: List<HealthVitals> = emptyList(),
    prescriptions: List<Prescription> = emptyList(),
    facilityCheckIns: List<FacilityCheckIn> = emptyList(),
    activeCheckIn: FacilityCheckIn? = null,
    user: UserProfile? = null,
    onUpdateDoctorNotes: (reportId: String, notes: String, diagnosis: String) -> Unit = { _, _, _ -> },
    onConfirmDoctorReview: (reportId: String, doctorName: String) -> Unit = { _, _ -> },
    onOpenPrescriptionDialog: () -> Unit = {},
    onNavigateToIntake: () -> Unit = {},
    onOpenVitalsDialog: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(TimelineFilter.ALL_TIMELINE) }
    var searchQuery by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }

    // Combine all facility check-ins including active one
    val allFacilityVisits = remember(facilityCheckIns, activeCheckIn) {
        val list = mutableListOf<FacilityCheckIn>()
        activeCheckIn?.let { list.add(it) }
        facilityCheckIns.forEach { fac ->
            if (list.none { it.id == fac.id }) {
                list.add(fac)
            }
        }
        list
    }

    // Build unified longitudinal list
    val allEvents = remember(doctorReports, consultations, prescriptions, vitals, allFacilityVisits) {
        val events = mutableListOf<TimelineEvent>()
        doctorReports.forEach { events.add(TimelineEvent.DoctorReviewEvent(it)) }
        consultations.forEach { events.add(TimelineEvent.ConsultationEvent(it)) }
        prescriptions.forEach { events.add(TimelineEvent.PrescriptionEvent(it)) }
        vitals.forEach { events.add(TimelineEvent.VitalsEvent(it)) }
        allFacilityVisits.forEach { events.add(TimelineEvent.FacilityCheckInEvent(it)) }
        // Keep in realistic chronological sequence
        events
    }

    // Filter events based on category and search query
    val filteredEvents = remember(allEvents, selectedFilter, searchQuery) {
        allEvents.filter { event ->
            val matchesFilter = when (selectedFilter) {
                TimelineFilter.ALL_TIMELINE -> true
                else -> event.category == selectedFilter
            }
            val query = searchQuery.trim().lowercase()
            val matchesSearch = if (query.isEmpty()) true else {
                when (event) {
                    is TimelineEvent.DoctorReviewEvent -> {
                        event.report.chiefComplaintSummary.lowercase().contains(query) ||
                                event.report.provisionalDiagnosis.lowercase().contains(query) ||
                                event.report.extractedSymptoms.any { it.lowercase().contains(query) } ||
                                event.report.editableDoctorNotes.lowercase().contains(query) ||
                                (event.report.confirmedByDoctorName?.lowercase()?.contains(query) == true)
                    }
                    is TimelineEvent.ConsultationEvent -> {
                        event.consultation.diagnosis.lowercase().contains(query) ||
                                event.consultation.doctorName.lowercase().contains(query) ||
                                event.consultation.clinicName.lowercase().contains(query) ||
                                event.consultation.chiefComplaint.lowercase().contains(query) ||
                                event.consultation.clinicalRemarks.lowercase().contains(query)
                    }
                    is TimelineEvent.PrescriptionEvent -> {
                        event.prescription.id.lowercase().contains(query) ||
                                event.prescription.doctorName.lowercase().contains(query) ||
                                event.prescription.items.any { it.medicineName.lowercase().contains(query) }
                    }
                    is TimelineEvent.VitalsEvent -> {
                        event.vitals.recordedBy.lowercase().contains(query) ||
                                "bp blood pressure pulse spo2".contains(query)
                    }
                    is TimelineEvent.FacilityCheckInEvent -> {
                        event.checkIn.facilityName.lowercase().contains(query) ||
                                event.checkIn.department.lowercase().contains(query) ||
                                event.checkIn.tokenNumber.lowercase().contains(query)
                    }
                }
            }
            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhiteBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Longitudinal Medical Timeline",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightBlueHeader
                )
                Text(
                    text = "Chronological lifetime EHR & ABDM encounter history",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SoftEmeraldLight,
                border = BorderStroke(1.dp, SoftEmeraldBorder),
                modifier = Modifier.clickable { showExportDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Export EHR",
                        tint = SoftEmeraldDark,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Export EHR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftEmeraldDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Patient Demographics Banner
        PatientProfileStrip(
            user = user,
            totalRecords = allEvents.size,
            doctorReportsCount = doctorReports.size,
            consultationsCount = consultations.size,
            prescriptionsCount = prescriptions.size,
            onSelectFilter = { selectedFilter = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search diagnosis, doctor, medicine, symptoms...", fontSize = 12.sp, color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = LightBluePrimary,
                unfocusedBorderColor = SurfaceBorder
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimelineFilter.values().forEach { filter ->
                val isSelected = selectedFilter == filter
                val count = when (filter) {
                    TimelineFilter.ALL_TIMELINE -> allEvents.size
                    TimelineFilter.DOCTOR_REVIEW_SUMMARY -> doctorReports.size
                    TimelineFilter.CONSULTATION_HISTORY -> consultations.size
                    TimelineFilter.PRESCRIPTIONS -> prescriptions.size
                    TimelineFilter.VITALS -> vitals.size
                    TimelineFilter.FACILITY_CHECKINS -> allFacilityVisits.size
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) SoftEmeraldAccent else Color.White,
                    border = BorderStroke(1.dp, if (isSelected) SoftEmeraldAccent else SurfaceBorder),
                    modifier = Modifier.clickable { selectedFilter = filter }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = filter.chipLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextDarkSlate
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White.copy(alpha = 0.25f) else SoftEmeraldLight)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "$count",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else SoftEmeraldDark
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Timeline Stream LazyColumn
        if (filteredEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Timeline,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No Matching Timeline Records" else "No Events in this Category",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightBlueHeader
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty())
                                "Try searching with a different term like 'fever', 'cough', or 'doctor'."
                            else
                                "Record symptoms via Voice / Touch Intake or log vitals to add events to the timeline.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (searchQuery.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { searchQuery = "" },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Clear Search", fontSize = 12.sp)
                                }
                            }
                            Button(
                                onClick = onNavigateToIntake,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent)
                            ) {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Case Intake", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                itemsIndexed(filteredEvents) { index, event ->
                    val isFirst = index == 0
                    val isLast = index == filteredEvents.lastIndex

                    TimelineNodeRow(
                        event = event,
                        isFirst = isFirst,
                        isLast = isLast,
                        vitals = vitals,
                        onUpdateDoctorNotes = onUpdateDoctorNotes,
                        onConfirmDoctorReview = onConfirmDoctorReview,
                        onOpenPrescriptionDialog = onOpenPrescriptionDialog
                    )
                }

                item {
                    // Timeline End Milestone
                    TimelineEndMilestone()
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // Bottom Persistent Voice / Touch Intake Dock
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("timeline_bottom_voice_touch_intake"),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToIntake() }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SoftEmeraldLight)
                            .border(1.dp, SoftEmeraldBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Voice / Touch Intake",
                            tint = SoftEmeraldDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Voice / Touch Intake",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightBlueHeader
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SoftEmeraldLight
                            ) {
                                Text(
                                    text = "AI + ABDM",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftEmeraldDark,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Record new symptoms via voice or touch",
                            fontSize = 10.5.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Button(
                    onClick = onNavigateToIntake,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("timeline_bottom_start_intake_button")
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start Intake", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Export EHR Dialog
    if (showExportDialog) {
        ExportEhrDialog(
            user = user,
            allEvents = allEvents,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
private fun PatientProfileStrip(
    user: UserProfile?,
    totalRecords: Int,
    doctorReportsCount: Int,
    consultationsCount: Int,
    prescriptionsCount: Int,
    onSelectFilter: (TimelineFilter) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(LightBlueContainer)
                            .border(1.5.dp, LightBluePrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.name?.split(" ")?.mapNotNull { it.firstOrNull()?.toString() }?.take(2)?.joinToString("") ?: "PS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = LightBlueHeader
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user?.name ?: "Priya Sharma",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkSlate
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SoftEmeraldLight,
                                border = BorderStroke(1.dp, SoftEmeraldBorder)
                            ) {
                                Text(
                                    text = "ABHA VERIFIED",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SoftEmeraldDark,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${user?.abhaId ?: "ABHA-91-8472-9102-4412"} • ${user?.age ?: 28}y, ${user?.gender ?: "Female"} • ${user?.bloodGroup ?: "O+"}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE),
                    border = BorderStroke(1.dp, Color(0xFFEF9A9A))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Penicillin Allergy",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickMetricPill(
                    label = "Total Records",
                    value = "$totalRecords",
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectFilter(TimelineFilter.ALL_TIMELINE) }
                )
                QuickMetricPill(
                    label = "Doctor Reviews",
                    value = "$doctorReportsCount",
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectFilter(TimelineFilter.DOCTOR_REVIEW_SUMMARY) }
                )
                QuickMetricPill(
                    label = "Encounters",
                    value = "$consultationsCount",
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectFilter(TimelineFilter.CONSULTATION_HISTORY) }
                )
                QuickMetricPill(
                    label = "Prescriptions",
                    value = "$prescriptionsCount",
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectFilter(TimelineFilter.PRESCRIPTIONS) }
                )
            }
        }
    }
}

@Composable
private fun QuickMetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = WarmWhiteSubtle,
        border = BorderStroke(1.dp, SurfaceBorder),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = LightBlueHeader)
            Text(text = label, fontSize = 8.sp, color = TextMuted, maxLines = 1)
        }
    }
}

@Composable
private fun TimelineNodeRow(
    event: TimelineEvent,
    isFirst: Boolean,
    isLast: Boolean,
    vitals: List<HealthVitals>,
    onUpdateDoctorNotes: (reportId: String, notes: String, diagnosis: String) -> Unit,
    onConfirmDoctorReview: (reportId: String, doctorName: String) -> Unit,
    onOpenPrescriptionDialog: () -> Unit
) {
    val nodeConfig = when (event) {
        is TimelineEvent.DoctorReviewEvent -> NodeConfig(
            icon = Icons.Default.Assignment,
            iconTint = SoftEmeraldDark,
            containerColor = SoftEmeraldLight,
            borderColor = SoftEmeraldAccent,
            badgeLabel = "DOCTOR REVIEW SUMMARY",
            badgeColor = SoftEmeraldLight,
            badgeText = SoftEmeraldDark,
            badgeBorder = SoftEmeraldBorder
        )
        is TimelineEvent.ConsultationEvent -> NodeConfig(
            icon = Icons.Default.MedicalServices,
            iconTint = LightBlueHeader,
            containerColor = LightBlueContainer,
            borderColor = LightBluePrimary,
            badgeLabel = "CLINICAL ENCOUNTER",
            badgeColor = LightBlueContainer,
            badgeText = LightBlueHeader,
            badgeBorder = LightBlueSoft
        )
        is TimelineEvent.PrescriptionEvent -> NodeConfig(
            icon = Icons.Default.Medication,
            iconTint = Color(0xFF7C3AED),
            containerColor = Color(0xFFF5F3FF),
            borderColor = Color(0xFF8B5CF6),
            badgeLabel = "DIGITAL PRESCRIPTION",
            badgeColor = Color(0xFFF5F3FF),
            badgeText = Color(0xFF7C3AED),
            badgeBorder = Color(0xFFDDD6FE)
        )
        is TimelineEvent.VitalsEvent -> NodeConfig(
            icon = Icons.Default.Favorite,
            iconTint = Color(0xFFE11D48),
            containerColor = Color(0xFFFFEFF2),
            borderColor = Color(0xFFF43F5E),
            badgeLabel = "VITALS CHECKPOINT",
            badgeColor = Color(0xFFFFEFF2),
            badgeText = Color(0xFFE11D48),
            badgeBorder = Color(0xFFFECDD3)
        )
        is TimelineEvent.FacilityCheckInEvent -> NodeConfig(
            icon = Icons.Default.LocalHospital,
            iconTint = Color(0xFF0D9488),
            containerColor = Color(0xFFF0FDFA),
            borderColor = Color(0xFF14B8A6),
            badgeLabel = "HOSPITAL CHECK-IN",
            badgeColor = Color(0xFFF0FDFA),
            badgeText = Color(0xFF0D9488),
            badgeBorder = Color(0xFF99F6E4)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Continuous vertical timeline spine
                val lineX = 17.dp.toPx()
                val startY = if (isFirst) 18.dp.toPx() else 0f
                val endY = if (isLast) 18.dp.toPx() else size.height
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(lineX, startY),
                    end = Offset(lineX, endY),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
    ) {
        // Left Column: Timeline Node Circle
        Box(
            modifier = Modifier
                .width(34.dp)
                .padding(top = 2.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                shape = CircleShape,
                color = nodeConfig.containerColor,
                border = BorderStroke(2.dp, nodeConfig.borderColor),
                shadowElevation = 2.dp,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = nodeConfig.icon,
                        contentDescription = null,
                        tint = nodeConfig.iconTint,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right Column: Event Header & Rich Card
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 18.dp)
        ) {
            // Milestone Header Pill (Date & Category)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = LightBluePrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = event.timestamp,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkSlate
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = nodeConfig.badgeColor,
                    border = BorderStroke(1.dp, nodeConfig.badgeBorder)
                ) {
                    Text(
                        text = nodeConfig.badgeLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = nodeConfig.badgeText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Rich Card Content based on event type
            when (event) {
                is TimelineEvent.DoctorReviewEvent -> {
                    DoctorReviewSummaryCard(
                        report = event.report,
                        vitals = vitals,
                        onUpdateDoctorNotes = onUpdateDoctorNotes,
                        onConfirmDoctorReview = onConfirmDoctorReview,
                        onOpenPrescriptionDialog = onOpenPrescriptionDialog
                    )
                }
                is TimelineEvent.ConsultationEvent -> {
                    ConsultationCard(cons = event.consultation)
                }
                is TimelineEvent.PrescriptionEvent -> {
                    PrescriptionTimelineCard(prescription = event.prescription)
                }
                is TimelineEvent.VitalsEvent -> {
                    VitalsTimelineCard(vitals = event.vitals)
                }
                is TimelineEvent.FacilityCheckInEvent -> {
                    FacilityCheckInTimelineCard(checkIn = event.checkIn)
                }
            }
        }
    }
}

private data class NodeConfig(
    val icon: ImageVector,
    val iconTint: Color,
    val containerColor: Color,
    val borderColor: Color,
    val badgeLabel: String,
    val badgeColor: Color,
    val badgeText: Color,
    val badgeBorder: Color
)

@Composable
fun DoctorReviewSummaryCard(
    report: DoctorReviewReport,
    vitals: List<HealthVitals>,
    onUpdateDoctorNotes: (reportId: String, notes: String, diagnosis: String) -> Unit,
    onConfirmDoctorReview: (reportId: String, doctorName: String) -> Unit,
    onOpenPrescriptionDialog: () -> Unit
) {
    var editableNotes by remember(report.id) { mutableStateOf(report.editableDoctorNotes) }
    var editableDiagnosis by remember(report.id) { mutableStateOf(report.provisionalDiagnosis) }
    var isEditing by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, if (report.isDoctorConfirmed) SoftEmeraldBorder else WarningAmber.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Doctor Review Summary Report",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightBlueHeader
                    )
                    Text(
                        text = "Pre-consultation AI synthesis • Patient intake transcript",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (report.isDoctorConfirmed) SoftEmeraldLight else WarningContainer,
                        border = BorderStroke(
                            1.dp,
                            if (report.isDoctorConfirmed) SoftEmeraldBorder else WarningAmber
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (report.isDoctorConfirmed) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SoftEmeraldDark, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                            Text(
                                text = if (report.isDoctorConfirmed) "CONFIRMED" else "AWAITING CLINICIAN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (report.isDoctorConfirmed) SoftEmeraldDark else WarningAmber
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = if (report.isDoctorConfirmed) SoftEmeraldDark else WarningAmber,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (report.isDoctorConfirmed) (report.confirmedAt ?: report.generatedAt) else "Intake: ${report.generatedAt}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkSlate
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Patient Snapshot & Check-in Vitals
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = WarmWhiteSubtle,
                border = BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PATIENT SNAPSHOT: Priya Sharma (28y, Female, O+)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightBlueHeader
                        )
                        val v = vitals.firstOrNull()
                        val vitalsStr = if (v != null) "BP: ${v.bpSystolic}/${v.bpDiastolic} mmHg • Pulse: ${v.pulseRate} bpm • SpO2: ${v.spo2Percent}% • Temp: ${v.temperatureF}°F"
                        else "BP: 118/78 mmHg • Pulse: 72 bpm • SpO2: 99% • Temp: 98.4°F"
                        Text(
                            text = "Check-in Vitals: $vitalsStr",
                            fontSize = 10.sp,
                            color = TextDarkSlate
                        )
                    }
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SoftEmeraldDark, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI Synthesized Complaint Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SoftEmeraldLight,
                border = BorderStroke(1.dp, SoftEmeraldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI-SYNTHESIZED CHIEF COMPLAINT STORY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftEmeraldDark
                        )
                        Text(
                            text = "${report.duration} • ${report.severityLevel} Severity",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SoftEmeraldDark
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.chiefComplaintSummary,
                        fontSize = 11.sp,
                        color = TextDarkSlate,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Extracted Symptoms Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        report.extractedSymptoms.forEach { s ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, SoftEmeraldBorder)
                            ) {
                                Text(
                                    text = s,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SoftEmeraldDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Allergy Safety Alert
            if (report.allergies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE),
                    border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
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
                        Column {
                            Text(
                                text = "CONTRAINDICATION SAFETY WARNING",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Text(
                                text = "Severe allergy to: ${report.allergies.joinToString(", ")}. Avoid prescribing beta-lactam antibiotics.",
                                fontSize = 10.sp,
                                color = Color(0xFFB71C1C)
                            )
                        }
                    }
                }
            }

            // Attached Report / OCR text
            if (report.attachedReportTitle != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WarmWhiteSubtle,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = LightBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "ATTACHED REPORT: ${report.attachedReportTitle}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightBlueHeader
                            )
                            if (report.attachedOcrSummary != null) {
                                Text(
                                    text = report.attachedOcrSummary,
                                    fontSize = 10.sp,
                                    color = TextDarkSlate
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Clinician's Notes & Diagnosis
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, if (isEditing) SoftEmeraldAccent else SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CLINICIAN'S NOTES & DIAGNOSIS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightBlueHeader
                        )
                        TextButton(
                            onClick = {
                                if (isEditing) {
                                    onUpdateDoctorNotes(report.id, editableNotes, editableDiagnosis)
                                }
                                isEditing = !isEditing
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (isEditing) "Save Notes" else "Edit Notes",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftEmeraldDark
                            )
                        }
                    }

                    if (isEditing) {
                        OutlinedTextField(
                            value = editableDiagnosis,
                            onValueChange = { editableDiagnosis = it },
                            label = { Text("Provisional Diagnosis", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editableNotes,
                            onValueChange = { editableNotes = it },
                            label = { Text("Clinical Remarks & Plan", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(8.dp)
                        )
                    } else {
                        Text(
                            text = "Diagnosis: $editableDiagnosis",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftEmeraldDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = editableNotes,
                            fontSize = 11.sp,
                            color = TextDarkSlate
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Doctor Confirmation Actions (Slide 4: "AI mistakes -> Doctor confirms before action")
            if (!report.isDoctorConfirmed) {
                Button(
                    onClick = { onConfirmDoctorReview(report.id, "Dr. Rajesh Sharma, MD") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("doctor_confirm_report_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Doctor Confirms Summary Before Action", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SoftEmeraldLight,
                    border = BorderStroke(1.dp, SoftEmeraldBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SoftEmeraldDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Clinician Signed & Validated",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftEmeraldDark
                                    )
                                    Text(
                                        text = report.confirmedByDoctorName ?: "Dr. Rajesh Sharma, MD",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDarkSlate
                                    )
                                }
                            }

                            Button(
                                onClick = onOpenPrescriptionDialog,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Create Rx", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Dr. Rajesh Sharma validation timing banner
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.9f),
                            border = BorderStroke(0.5.dp, SoftEmeraldBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = SoftEmeraldDark,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Validated at: ${report.confirmedAt ?: "Today, 09:35 AM"}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextDarkSlate
                                    )
                                }
                                Text(
                                    text = "Turnaround: 10m post-intake",
                                    fontSize = 9.sp,
                                    color = TextMuted
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
fun ConsultationCard(cons: DoctorConsultation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Doctor Info & Clinic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(LightBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = LightBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = cons.doctorName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkSlate
                        )
                        Text(
                            text = "${cons.doctorSpecialty} • ${cons.clinicName}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SoftEmeraldLight,
                        border = BorderStroke(1.dp, SoftEmeraldBorder)
                    ) {
                        Text(
                            text = "COMPLETED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftEmeraldDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = LightBlueContainer,
                        border = BorderStroke(1.dp, LightBlueSoft)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = LightBlueHeader,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (cons.timestamp.contains(",")) cons.timestamp.substringAfter(", ").trim() else cons.timestamp,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightBlueHeader
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clinical Encounter Timing Banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF0F9FF),
                border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = LightBlueHeader,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CONSULTATION APPOINTMENT TIME",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LightBlueHeader,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = cons.timestamp,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkSlate
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                    ) {
                        Text(
                            text = if (cons.timestamp.contains("AM")) "Morning Session" else "Afternoon Session",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LightBlueHeader,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Diagnosis Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SoftEmeraldLight,
                border = BorderStroke(1.dp, SoftEmeraldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "DIAGNOSIS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftEmeraldDark
                    )
                    Text(
                        text = cons.diagnosis,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDarkSlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Chief Complaint: ${cons.chiefComplaint}",
                fontSize = 11.sp,
                color = TextDarkSlate
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Clinical Remarks: ${cons.clinicalRemarks}",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Vitals recorded: ${cons.vitalsSnapshot}",
                fontSize = 10.sp,
                color = LightBlueHeader
            )

            if (cons.prescription != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightBlueContainer)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        tint = LightBluePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Digital Rx: ${cons.prescription.id} (${cons.prescription.items.size} medicines prescribed)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightBlueHeader
                    )
                }
            }
        }
    }
}

@Composable
fun PrescriptionTimelineCard(prescription: Prescription) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFDDD6FE))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F3FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Medication,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = prescription.id,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkSlate
                        )
                        Text(
                            text = "Prescribed by ${prescription.doctorName} • ${prescription.clinicName}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (prescription.isDispensed) SoftEmeraldLight else Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, if (prescription.isDispensed) SoftEmeraldBorder else LightBlueSoft)
                    ) {
                        Text(
                            text = if (prescription.isDispensed) "DISPENSED" else "ACTIVE COURSE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (prescription.isDispensed) SoftEmeraldDark else LightBlueHeader,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${prescription.date} • 11:00 AM",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDarkSlate
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Medications List
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                prescription.items.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WarmWhiteSubtle,
                        border = BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.medicineName} (${item.dosage})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkSlate
                                )
                                Text(
                                    text = "${item.frequency} • ${item.timing} • ${item.durationDays} Days",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                                if (item.instructions.isNotBlank()) {
                                    Text(
                                        text = item.instructions,
                                        fontSize = 9.sp,
                                        color = TextDarkSlate
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, SurfaceBorder)
                            ) {
                                Text(
                                    text = "Qty: ${item.quantity}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDarkSlate,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (prescription.dispensedByCentre != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Dispensed at: ${prescription.dispensedByCentre} on ${prescription.dispensedDate ?: prescription.date}",
                    fontSize = 9.sp,
                    color = SoftEmeraldDark
                )
            }
        }
    }
}

@Composable
fun VitalsTimelineCard(vitals: HealthVitals) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFFECDD3))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEFF2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Check-in Vitals Capture",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkSlate
                        )
                        Text(
                            text = "Recorded by ${vitals.recordedBy}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SoftEmeraldLight,
                    border = BorderStroke(1.dp, SoftEmeraldBorder)
                ) {
                    Text(
                        text = "NORMAL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftEmeraldDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Vitals Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VitalMetricBox(label = "Blood Pressure", value = "${vitals.bpSystolic}/${vitals.bpDiastolic}", unit = "mmHg", modifier = Modifier.weight(1f))
                VitalMetricBox(label = "Pulse Rate", value = "${vitals.pulseRate}", unit = "bpm", modifier = Modifier.weight(1f))
                VitalMetricBox(label = "SpO2 Level", value = "${vitals.spo2Percent}", unit = "%", modifier = Modifier.weight(1f))
                VitalMetricBox(label = "Temp", value = "${vitals.temperatureF}", unit = "°F", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun VitalMetricBox(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = WarmWhiteSubtle,
        border = BorderStroke(1.dp, SurfaceBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 8.sp, color = TextMuted, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
            Text(text = unit, fontSize = 8.sp, color = TextMuted)
        }
    }
}

@Composable
fun FacilityCheckInTimelineCard(checkIn: FacilityCheckIn) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFF99F6E4))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0FDFA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = Color(0xFF0D9488),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = checkIn.facilityName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkSlate
                        )
                        Text(
                            text = "${checkIn.department} • ${checkIn.counterNumber}",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF0FDFA),
                    border = BorderStroke(1.dp, Color(0xFF14B8A6))
                ) {
                    Text(
                        text = "TOKEN: ${checkIn.tokenNumber}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0D9488),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ABDM Scan & Share Check-in • Wait time: ~${checkIn.estimatedWaitMinutes} min • Status: ${checkIn.status}",
                fontSize = 10.sp,
                color = TextDarkSlate
            )
        }
    }
}

@Composable
private fun TimelineEndMilestone() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(34.dp)
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(LightBlueSoft)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Beginning of Longitudinal Health Identity Record (ABHA 2026)",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted
        )
    }
}

@Composable
private fun ExportEhrDialog(
    user: UserProfile?,
    allEvents: List<TimelineEvent>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(SoftEmeraldLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = SoftEmeraldDark, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export Longitudinal EHR",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightBlueHeader
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WarmWhiteSubtle,
                    border = BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ABDM FHIR R4 HEALTH RECORD BUNDLE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftEmeraldDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Patient: ${user?.name ?: "Priya Sharma"} (${user?.abhaId ?: "ABHA-91-8472-9102-4412"})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDarkSlate
                        )
                        Text(
                            text = "Encounters: ${allEvents.size} historical records • Cryptographically signed",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = """
{
  "resourceType": "Bundle",
  "type": "document",
  "identifier": "ABDM-EHR-${user?.abhaId ?: "91024412"}",
  "totalRecords": ${allEvents.size},
  "patient": "${user?.name ?: "Priya Sharma"}",
  "status": "VERIFIED_ACTIVE"
}
                        """.trimIndent(),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download FHIR Health Summary", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
