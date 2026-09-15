package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.data.model.CaseIntake
import com.example.data.model.HealthVitals
import com.example.data.model.UserProfile

/**
 * Backward-compatible delegator to VoiceIntakeScreen.
 * Touch screen has been completely removed in favor of real-time multi-lingual Indic voice triage.
 */
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
    VoiceIntakeScreen(
        user = user,
        vitals = vitals,
        caseIntakes = caseIntakes,
        onSaveIntake = onSaveIntake,
        onNavigateToTimeline = onNavigateToTimeline
    )
}
