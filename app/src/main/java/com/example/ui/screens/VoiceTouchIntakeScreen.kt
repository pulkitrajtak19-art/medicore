package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceTouchIntakeScreen(
    user: UserProfile,
    vitals: List<HealthVitals>,
    caseIntakes: List<CaseIntake>,
    onSaveIntake: (
        mode: String,
        language: String,
        transcript: String,
        symptoms: List<String>,
        duration: String,
        severity: String,
        allergies: List<String>,
        medicines: List<String>,
        recordTitle: String?,
        recordOcr: String?
    ) -> Unit,
    onNavigateToTimeline: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Mode: Voice (Bhashini AI) or Touch Screen Fallback
    var intakeMode by remember { mutableStateOf("Voice (Bhashini AI)") }
    var selectedLanguage by remember { mutableStateOf("हिन्दी (Hindi)") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingTimer by remember { mutableIntStateOf(0) }
    var voiceTranscript by remember {
        mutableStateOf("Mujhe pichle 2 din se tez bukhar hai, gale me khrash aur sukhi khansi hai. Penicillin se allergy hai aur subah se paracetamol li hai.")
    }

    val availableLanguages = listOf(
        "हिन्दी (Hindi)", "English", "Hinglish", "বাংলা (Bengali)", "தமிழ் (Tamil)", "मराठी (Marathi)"
    )

    val commonSymptoms = listOf(
        "Fever", "Dry Cough", "Sore Throat", "Headache", "Body Ache",
        "Chest Tightness", "Fatigue", "Runny Nose", "Shortness of breath", "Stomach Pain"
    )
    val selectedSymptoms = remember {
        mutableStateListOf("Fever", "Dry Cough", "Sore Throat")
    }

    var selectedDuration by remember { mutableStateOf("2-3 Days") }
    val durationOptions = listOf("< 24 Hours", "2-3 Days", "1 Week", "> 2 Weeks")

    var selectedSeverity by remember { mutableStateOf("Moderate") }
    val severityOptions = listOf("Mild", "Moderate", "Severe")

    val commonAllergies = listOf(
        "Penicillin", "Sulfa Drugs", "Aspirin / NSAIDs", "Dust / Pollen", "No Known Allergies"
    )
    val selectedAllergies = remember {
        mutableStateListOf("Penicillin")
    }

    val commonMedicines = listOf(
        "Paracetamol 650mg", "Cetirizine 10mg", "Metformin 500mg", "Amlodipine 5mg", "None"
    )
    val selectedMedicines = remember {
        mutableStateListOf("Paracetamol 650mg")
    }

    var isDocumentAttached by remember { mutableStateOf(true) }
    var isSynthesizing by remember { mutableStateOf(false) }

    // Recording simulation timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTimer = 0
            while (isRecording) {
                delay(1000)
                recordingTimer++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhiteBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Voice & Touch Intake",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightBlueHeader
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SoftEmeraldLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
                    ) {
                        Text(
                            text = "AI4BHARAT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftEmeraldDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Multimodal intake with touch fallback & Indian language support",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = WarmWhiteSubtle,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier.clickable { onNavigateToTimeline() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Timeline →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightBlueHeader
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Mode Selector Pill
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val modes = listOf("Voice (Bhashini AI)", "Touch Screen")
                    modes.forEach { m ->
                        val isSelected = intakeMode == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SoftEmeraldContainer else Color.Transparent)
                                .clickable { intakeMode = m }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (m.startsWith("Voice")) Icons.Default.Mic else Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = if (isSelected) SoftEmeraldDark else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = m,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) SoftEmeraldDark else TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // Language Bar
            item {
                Column {
                    Text(
                        text = "SELECT INTAKE LANGUAGE (Bhashini Indic ASR):",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableLanguages.size) { idx ->
                            val lang = availableLanguages[idx]
                            val isLangSelected = selectedLanguage == lang
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isLangSelected) SoftEmeraldAccent else Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isLangSelected) SoftEmeraldAccent else SurfaceBorder
                                ),
                                modifier = Modifier.clickable { selectedLanguage = lang }
                            ) {
                                Text(
                                    text = lang,
                                    fontSize = 11.sp,
                                    fontWeight = if (isLangSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isLangSelected) Color.White else TextDarkSlate,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Voice Capture Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = SoftEmeraldDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Voice Symptom Input ($selectedLanguage)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBlueHeader
                                )
                            }
                            if (isRecording) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFFEBEE)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.Red)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "REC ${recordingTimer}s",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Mic Button & Sound Wave
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    isRecording = !isRecording
                                    if (!isRecording) {
                                        voiceTranscript = "Mujhe 2 din se tez bukhar hai, gale me khrash aur sukhi khansi hai. Penicillin se allergy hai aur subah paracetamol li hai."
                                    }
                                },
                                containerColor = if (isRecording) Color(0xFFD32F2F) else SoftEmeraldAccent,
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(56.dp)
                                    .testTag("voice_record_button")
                            ) {
                                Icon(
                                    if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = if (isRecording) "Stop" else "Speak",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isRecording) "Listening in $selectedLanguage... Speak naturally about symptoms & medications" else "Tap microphone to speak symptoms in your native dialect",
                            fontSize = 11.sp,
                            color = if (isRecording) SoftEmeraldDark else TextMuted,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = voiceTranscript,
                            onValueChange = { voiceTranscript = it },
                            label = { Text("ASR Voice Transcript / Symptoms", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("voice_transcript_input"),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SoftEmeraldAccent,
                                unfocusedBorderColor = SurfaceBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Try sample:", fontSize = 10.sp, color = TextMuted)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = WarmWhiteSubtle,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                                modifier = Modifier.clickable {
                                    voiceTranscript = "Mujhe 2 din se tez bukhar hai, gale me khrash aur sukhi khansi hai. Penicillin se allergy hai aur subah paracetamol li hai."
                                    selectedLanguage = "हिन्दी (Hindi)"
                                }
                            ) {
                                Text(
                                    text = "Hindi: URTI + Allergy",
                                    fontSize = 10.sp,
                                    color = LightBlueHeader,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = WarmWhiteSubtle,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                                modifier = Modifier.clickable {
                                    voiceTranscript = "Severe sore throat, throat scratchiness, and high fever since yesterday. Allergic to sulfa drugs."
                                    selectedLanguage = "English"
                                }
                            ) {
                                Text(
                                    text = "English: Throat + Fever",
                                    fontSize = 10.sp,
                                    color = LightBlueHeader,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Touch Fallback: Symptom Picker
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Touch Fallback: Chief Symptoms",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightBlueHeader
                            )
                            Text(
                                text = "${selectedSymptoms.size} selected",
                                fontSize = 11.sp,
                                color = SoftEmeraldDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Tap to add or refine symptoms (controls accent / background noise)",
                            fontSize = 10.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            commonSymptoms.forEach { symptom ->
                                val isSelected = selectedSymptoms.contains(symptom)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) selectedSymptoms.remove(symptom)
                                        else selectedSymptoms.add(symptom)
                                    },
                                    label = {
                                        Text(
                                            text = symptom,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SoftEmeraldContainer,
                                        selectedLabelColor = SoftEmeraldDark
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Duration & Severity
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Onset Duration", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    durationOptions.forEach { d ->
                                        val isDSelected = selectedDuration == d
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isDSelected) LightBlueContainer else WarmWhiteSubtle,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isDSelected) LightBlueSoft else SurfaceBorder
                                            ),
                                            modifier = Modifier.clickable { selectedDuration = d }
                                        ) {
                                            Text(
                                                text = d,
                                                fontSize = 10.sp,
                                                fontWeight = if (isDSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isDSelected) LightBlueHeader else TextDarkSlate,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Severity", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    severityOptions.forEach { s ->
                                        val isSSelected = selectedSeverity == s
                                        val col = when (s) {
                                            "Mild" -> SoftEmeraldAccent
                                            "Moderate" -> WarningAmber
                                            else -> Color(0xFFD32F2F)
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSSelected) col.copy(alpha = 0.15f) else WarmWhiteSubtle,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSSelected) col else SurfaceBorder
                                            ),
                                            modifier = Modifier.clickable { selectedSeverity = s }
                                        ) {
                                            Text(
                                                text = s,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSSelected) col else TextDarkSlate,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Allergies & Ongoing Medicines
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Drug Allergies & Ongoing Medicines",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightBlueHeader
                            )
                        }
                        Text(
                            text = "Prevents dangerous contraindications before the doctor prescribes",
                            fontSize = 10.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = "Known Drug Allergies:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            commonAllergies.forEach { allergy ->
                                val isSel = selectedAllergies.contains(allergy)
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        if (isSel) selectedAllergies.remove(allergy)
                                        else selectedAllergies.add(allergy)
                                    },
                                    label = { Text(allergy, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFFEBEE),
                                        selectedLabelColor = Color(0xFFC62828)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(text = "Current Ongoing Medicines:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDarkSlate)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            commonMedicines.forEach { med ->
                                val isSel = selectedMedicines.contains(med)
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        if (isSel) selectedMedicines.remove(med)
                                        else selectedMedicines.add(med)
                                    },
                                    label = { Text(med, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LightBlueContainer,
                                        selectedLabelColor = LightBlueHeader
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Attached Diagnostic Document OCR
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.DocumentScanner,
                                    contentDescription = null,
                                    tint = LightBluePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Lab Reports & Diagnostic Scans",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBlueHeader
                                )
                            }
                            Switch(
                                checked = isDocumentAttached,
                                onCheckedChange = { isDocumentAttached = it },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        Text(
                            text = "Extracts OCR numbers while keeping original document intact",
                            fontSize = 10.sp,
                            color = TextMuted
                        )

                        if (isDocumentAttached) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = WarmWhiteSubtle,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📎 CBC_Blood_Report_Aug2026.pdf",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LightBlueHeader
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = SoftEmeraldContainer
                                        ) {
                                            Text(
                                                text = "OCR EXTRACTED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SoftEmeraldDark,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Extracted OCR: WBC: 11,200/mcL (Mild Infection) • Platelets: 2.4 Lakh • Chest X-Ray: Clear fields",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Primary Action Button: Synthesize & Send to Doctor Review in Timeline
            item {
                Button(
                    onClick = {
                        isSynthesizing = true
                        coroutineScope.launch {
                            delay(500)
                            onSaveIntake(
                                intakeMode,
                                selectedLanguage,
                                voiceTranscript,
                                selectedSymptoms.toList(),
                                selectedDuration,
                                selectedSeverity,
                                selectedAllergies.toList(),
                                selectedMedicines.toList(),
                                if (isDocumentAttached) "CBC_Blood_Report_Aug2026.pdf" else null,
                                if (isDocumentAttached) "WBC: 11,200/mcL | Platelets: 2.4 Lakh | Chest X-Ray Clear" else null
                            )
                            isSynthesizing = false
                            // Navigate directly to Timeline where the Doctor Review Summary Report is presented
                            onNavigateToTimeline()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("synthesize_summary_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent)
                ) {
                    if (isSynthesizing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Synthesizing Clinical Story...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Synthesize & Send to Doctor Review Timeline", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
