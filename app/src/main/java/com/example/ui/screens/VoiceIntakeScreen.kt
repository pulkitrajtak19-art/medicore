package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.*
import com.example.data.voice.ExtractedSymptomEntity
import com.example.data.voice.IndianVoiceSpeechManager
import com.example.data.voice.IndicLanguage
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceIntakeScreen(
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val voiceState by IndianVoiceSpeechManager.state.collectAsState()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            IndianVoiceSpeechManager.startListening(context)
        }
    }

    var isDocumentAttached by remember { mutableStateOf(false) }
    var attachedDocTitle by remember { mutableStateOf("") }
    var manualEditMode by remember { mutableStateOf(false) }
    var manualText by remember { mutableStateOf("") }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }
    var showAllLanguagesModal by remember { mutableStateOf(false) }
    var languageSearchQuery by remember { mutableStateOf("") }

    // Outer container: adapts beautifully to small phones, standard phones, and tablets/foldables
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhiteBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp) // Responsive tablet/foldable restraint
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Voice Health Intake",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightBlueHeader,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SoftEmeraldLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
                        ) {
                            Text(
                                text = "INDIC AI",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftEmeraldDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Real-time speech triage in 22+ official Indian languages",
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = WarmWhiteSubtle,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier
                        .clickable { onNavigateToTimeline() }
                        .testTag("voice_intake_timeline_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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

            // Main Scrollable Body
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("voice_intake_scrollable_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Language Selection Bar
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SELECT SPOKEN INDIAN LANGUAGE:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 0.8.sp
                            )

                            Text(
                                text = "All 22 Languages (${IndianVoiceSpeechManager.SUPPORTED_INDIC_LANGUAGES.size})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightBluePrimary,
                                modifier = Modifier
                                    .clickable { showAllLanguagesModal = true }
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Horizontal Row of top Indian Languages
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(IndianVoiceSpeechManager.SUPPORTED_INDIC_LANGUAGES) { lang ->
                                val isLangSelected = voiceState.currentLanguage.code == lang.code &&
                                        voiceState.currentLanguage.nativeName == lang.nativeName

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isLangSelected) SoftEmeraldAccent else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isLangSelected) SoftEmeraldAccent else SurfaceBorder
                                    ),
                                    modifier = Modifier
                                        .clickable {
                                            IndianVoiceSpeechManager.selectLanguage(lang)
                                        }
                                        .testTag("lang_chip_${lang.englishName.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = lang.nativeName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLangSelected) Color.White else TextDarkSlate
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "(${lang.englishName})",
                                            fontSize = 10.sp,
                                            color = if (isLangSelected) Color.White.copy(alpha = 0.85f) else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Real-Time Microphone & Audio Waveform Hero Card
                item {
                    RealTimeVoiceHeroCard(
                        voiceState = voiceState,
                        hasAudioPermission = hasAudioPermission,
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        onStartListening = {
                            if (hasAudioPermission) {
                                IndianVoiceSpeechManager.startListening(context)
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onStopListening = {
                            IndianVoiceSpeechManager.stopListening()
                        },
                        onInjectSample = {
                            IndianVoiceSpeechManager.injectSampleVoiceIntake()
                        }
                    )
                }

                // 3. Live Speech Transcript Stream Box
                item {
                    val activeTranscript = voiceState.partialTranscript.ifBlank {
                        voiceState.finalTranscript
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
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
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (voiceState.isListening) Color(0xFFEF4444) else SoftEmeraldAccent
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (voiceState.isListening) "REAL-TIME SPOKEN STREAM (${voiceState.currentLanguage.englishName})"
                                        else "SPOKEN TRANSCRIPT (${voiceState.currentLanguage.englishName})",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        color = if (voiceState.isListening) Color(0xFFDC2626) else LightBluePrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (manualEditMode) "Done" else "Edit Text",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LightBluePrimary,
                                        modifier = Modifier
                                            .clickable {
                                                if (!manualEditMode) {
                                                    manualText = activeTranscript
                                                } else {
                                                    IndianVoiceSpeechManager.updateManualTranscript(manualText)
                                                }
                                                manualEditMode = !manualEditMode
                                            }
                                            .padding(4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (manualEditMode) {
                                OutlinedTextField(
                                    value = manualText,
                                    onValueChange = { manualText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("voice_manual_text_input"),
                                    minLines = 3,
                                    maxLines = 5,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SoftEmeraldAccent,
                                        unfocusedBorderColor = SurfaceBorder
                                    )
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (voiceState.isListening) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (voiceState.isListening) Color(0xFF86EFAC) else SurfaceBorder
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        if (activeTranscript.isNotBlank()) {
                                            Text(
                                                text = "\"$activeTranscript\"",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                lineHeight = 20.sp,
                                                color = TextDarkSlate
                                            )
                                        } else {
                                            Text(
                                                text = "Microphone is idle. Tap 'Start Voice Intake' and speak your health issue in ${voiceState.currentLanguage.nativeName} (${voiceState.currentLanguage.englishName}).",
                                                fontSize = 12.sp,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                color = TextMuted
                                            )
                                        }

                                        if (voiceState.isListening) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(12.dp),
                                                    strokeWidth = 1.5.dp,
                                                    color = SoftEmeraldDark
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Transcribing Indic voice stream live...",
                                                    fontSize = 10.sp,
                                                    color = SoftEmeraldDark,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Real-time Extracted Clinical Entities (AI Indic NLP)
                item {
                    val entities = voiceState.extractedEntities ?: IndianVoiceSpeechManager.extractClinicalEntities(
                        voiceState.finalTranscript.ifBlank { voiceState.currentLanguage.sampleIntakePhrase },
                        voiceState.currentLanguage
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
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
                                    text = "AI CLINICAL SYMPTOM EXTRACTION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = LightBlueHeader
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SoftEmeraldLight,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder)
                                ) {
                                    Text(
                                        text = "${entities.symptoms.size} Extracted",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftEmeraldDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Extracted Symptoms Chips
                            Text(
                                text = "Identified Symptoms:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDarkSlate
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                entities.symptoms.forEach { sym ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SoftEmeraldContainer,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldAccent)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = SoftEmeraldDark,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = sym,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SoftEmeraldDark
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Responsive Duration, Severity, Allergies grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(text = "DURATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text(
                                            text = entities.duration,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDarkSlate,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = when (entities.severity) {
                                        "Severe" -> Color(0xFFFEF2F2)
                                        "Moderate" -> Color(0xFFFFFBEB)
                                        else -> Color(0xFFF0FDF4)
                                    },
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        when (entities.severity) {
                                            "Severe" -> Color(0xFFFCA5A5)
                                            "Moderate" -> Color(0xFFFDE68A)
                                            else -> Color(0xFF86EFAC)
                                        }
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(text = "SEVERITY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text(
                                            text = entities.severity,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (entities.severity) {
                                                "Severe" -> Color(0xFFDC2626)
                                                "Moderate" -> WarningAmber
                                                else -> SoftEmeraldDark
                                            },
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(text = "ALLERGIES MENTIONED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text(
                                            text = entities.allergies.joinToString(", "),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (entities.allergies.any { it != "No Known Allergies" }) Color(0xFFDC2626) else TextDarkSlate,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(text = "MEDS REPORTED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        Text(
                                            text = entities.medications.joinToString(", "),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextDarkSlate,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Document & Lab OCR Attachment Card (Multimodal)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
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
                                        Icons.Outlined.DocumentScanner,
                                        contentDescription = null,
                                        tint = LightBluePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Prior Medical Record / Lab OCR",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LightBlueHeader
                                    )
                                }

                                Switch(
                                    checked = isDocumentAttached,
                                    onCheckedChange = { isDocumentAttached = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = SoftEmeraldAccent
                                    ),
                                    modifier = Modifier.scale(0.8f)
                                )
                            }

                            if (isDocumentAttached) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.PictureAsPdf,
                                                contentDescription = null,
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = attachedDocTitle,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextDarkSlate,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "OCR text parsed: Hb 13.2 g/dL, WBC 8,400 /uL (Normal)",
                                                    fontSize = 10.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Attached",
                                            tint = SoftEmeraldDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Final Save to Timeline Button
                item {
                    val activeTranscript = voiceState.partialTranscript.ifBlank {
                        voiceState.finalTranscript.ifBlank { voiceState.currentLanguage.sampleIntakePhrase }
                    }
                    val entities = voiceState.extractedEntities ?: IndianVoiceSpeechManager.extractClinicalEntities(
                        activeTranscript,
                        voiceState.currentLanguage
                    )

                    Button(
                        onClick = {
                            onSaveIntake(
                                "Voice (BhashiniIndicASR)",
                                voiceState.currentLanguage.englishName,
                                activeTranscript,
                                entities.symptoms,
                                entities.duration,
                                entities.severity,
                                entities.allergies,
                                entities.medications,
                                if (isDocumentAttached) attachedDocTitle else null,
                                if (isDocumentAttached) "Hb 13.2 g/dL, Normal Platelet Count" else null
                            )
                            saveSuccessMessage = "Voice intake saved to Longitudinal Timeline!"
                            coroutineScope.launch {
                                delay(1800)
                                onNavigateToTimeline()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_voice_intake_to_timeline_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftEmeraldAccent)
                    ) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Voice Intake to Health Timeline",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (saveSuccessMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SoftEmeraldLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SoftEmeraldBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SoftEmeraldDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = saveSuccessMessage!!,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftEmeraldDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // All 22 Indian Languages Full Dialog
        if (showAllLanguagesModal) {
            AllIndianLanguagesDialog(
                languages = IndianVoiceSpeechManager.SUPPORTED_INDIC_LANGUAGES,
                currentSelected = voiceState.currentLanguage,
                onDismiss = { showAllLanguagesModal = false },
                onSelectLanguage = { lang ->
                    IndianVoiceSpeechManager.selectLanguage(lang)
                    showAllLanguagesModal = false
                }
            )
        }
    }
}

/**
 * Real-Time Animated Voice Hero Card with multi-bar frequency audio waveform visualizer.
 */
@Composable
fun RealTimeVoiceHeroCard(
    voiceState: com.example.data.voice.VoiceIntakeState,
    hasAudioPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onInjectSample: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (voiceState.isListening) 1.14f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (voiceState.isListening) Color(0xFF0F172A) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (voiceState.isListening) SoftEmeraldAccent else SurfaceBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status and language indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (voiceState.isListening) Color(0xFF1E293B) else WarmWhiteSubtle,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (voiceState.isListening) Color(0xFF334155) else SurfaceBorder
                    )
                ) {
                    Text(
                        text = "Active: ${voiceState.currentLanguage.nativeName} (${voiceState.currentLanguage.englishName})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (voiceState.isListening) SoftEmeraldAccent else LightBlueHeader,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (voiceState.isListening) Color(0xFFEF4444).copy(alpha = 0.2f) else SoftEmeraldLight,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (voiceState.isListening) Color(0xFFEF4444) else SoftEmeraldBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (voiceState.isListening) Color(0xFFEF4444) else SoftEmeraldDark)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (voiceState.isListening) "REC • LIVE" else "READY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (voiceState.isListening) Color(0xFFEF4444) else SoftEmeraldDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Central Animated Microphone Button
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        if (voiceState.isListening) {
                            Brush.radialGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                        } else {
                            Brush.radialGradient(listOf(SoftEmeraldAccent, SoftEmeraldDark))
                        }
                    )
                    .clickable {
                        if (voiceState.isListening) {
                            onStopListening()
                        } else {
                            onStartListening()
                        }
                    }
                    .testTag("main_voice_mic_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (voiceState.isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (voiceState.isListening) "Stop Listening" else "Start Listening",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-bar Real-time Equalizer Sound Wave
            RealTimeEqualizerWave(
                isListening = voiceState.isListening,
                audioLevel = voiceState.audioLevel
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = voiceState.statusMessage,
                fontSize = 11.5.sp,
                textAlign = TextAlign.Center,
                color = if (voiceState.isListening) Color(0xFFCBD5E1) else TextMuted,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row (Mic control + 1-Tap Sample Voice Phrase)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onInjectSample()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("try_voice_sample_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (voiceState.isListening) Color.White else LightBluePrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (voiceState.isListening) Color(0xFF475569) else SurfaceBorder
                    )
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Try ${voiceState.currentLanguage.englishName} Sample",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = {
                        if (voiceState.isListening) {
                            onStopListening()
                        } else {
                            onStartListening()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("toggle_voice_recognition_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (voiceState.isListening) Color(0xFFEF4444) else SoftEmeraldAccent
                    )
                ) {
                    Icon(
                        if (voiceState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (voiceState.isListening) "Stop Mic" else "Start Voice Intake",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Animated real-time frequency equalizer responding dynamically to audio amplitude.
 */
@Composable
fun RealTimeEqualizerWave(isListening: Boolean, audioLevel: Float) {
    val barCount = 18
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val baseHeight = if (isListening) {
                val waveFactor = kotlin.math.sin(phase + (i * 0.4f)).toFloat()
                val dynamicHeight = (8f + (audioLevel * 18f) + (waveFactor * 5f)).coerceIn(4f, 26f)
                dynamicHeight
            } else {
                4f
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(baseHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isListening) {
                            if (i % 2 == 0) SoftEmeraldAccent else Color(0xFF38BDF8)
                        } else {
                            Color(0xFFCBD5E1)
                        }
                    )
            )
        }
    }
}

/**
 * Full Screen Dialog listing all 22 Official Scheduled Indian Languages + English & Hinglish
 */
@Composable
fun AllIndianLanguagesDialog(
    languages: List<IndicLanguage>,
    currentSelected: IndicLanguage,
    onDismiss: () -> Unit,
    onSelectLanguage: (IndicLanguage) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) languages
        else languages.filter {
            it.englishName.contains(searchQuery, ignoreCase = true) ||
                    it.nativeName.contains(searchQuery, ignoreCase = true) ||
                    it.code.contains(searchQuery, ignoreCase = true)
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Select Indian Language",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightBlueHeader
                        )
                        Text(
                            text = "22 Official Scheduled Languages Supported",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search language (e.g. Tamil, বাংলা, Hindi)...", fontSize = 12.sp) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftEmeraldAccent,
                        unfocusedBorderColor = SurfaceBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered) { lang ->
                        val isSelected = currentSelected.code == lang.code && currentSelected.nativeName == lang.nativeName
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) SoftEmeraldContainer else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) SoftEmeraldAccent else SurfaceBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectLanguage(lang) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = lang.nativeName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) SoftEmeraldDark else TextDarkSlate
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${lang.englishName}",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Text(
                                        text = "${lang.greeting} • Locale: ${lang.code}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = SoftEmeraldDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
