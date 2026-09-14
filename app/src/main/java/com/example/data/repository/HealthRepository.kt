package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class HealthRepository {

    // Pre-seeded Patient
    private val defaultPatient = UserProfile(
        id = "patient_001",
        name = "Priya Sharma",
        email = "priya.sharma@healthmail.in",
        phone = "+91 98290 12345",
        role = UserRole.PATIENT,
        abhaId = "ABHA-91-8472-9102-4412",
        age = 28,
        gender = "Female",
        bloodGroup = "O+",
        emergencyContact = "+91 98290 99887 (Rahul Sharma - Spouse)",
        city = "Jaipur, Rajasthan",
        qrToken = "PH-TOKEN-8472-9102"
    )

    // Pre-seeded Doctor
    private val defaultDoctor = UserProfile(
        id = "doc_001",
        name = "Dr. Rajesh Sharma",
        email = "dr.rajesh@jaipurcare.org",
        phone = "+91 94140 55667",
        role = UserRole.DOCTOR,
        abhaId = "HPR-DOC-RJ-40291",
        age = 44,
        gender = "Male",
        bloodGroup = "B+",
        emergencyContact = "+91 94140 11223",
        city = "Jaipur Care Clinic",
        qrToken = "DOC-RJ-40291"
    )

    // Pre-seeded Medicine Centre
    private val defaultMedicineCentre = UserProfile(
        id = "pharm_001",
        name = "Jaipur Central Dispensary & Pharmacy",
        email = "dispense@jaipurcentralpharma.in",
        phone = "+91 98290 44556",
        role = UserRole.MEDICINE_CENTRE,
        abhaId = "HFR-PHARM-RJ-0912",
        age = 35,
        gender = "Organization",
        bloodGroup = "N/A",
        emergencyContact = "+91 98290 44556",
        city = "MI Road, Jaipur",
        qrToken = "PHARM-RJ-0912"
    )

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // Patient Vitals history
    private val _vitalsList = MutableStateFlow<List<HealthVitals>>(
        listOf(
            HealthVitals(
                id = "vit_01",
                patientId = "patient_001",
                timestamp = "Today, 09:15 AM",
                bpSystolic = 118,
                bpDiastolic = 78,
                pulseRate = 72,
                temperatureF = 98.4,
                weightKg = 59.5,
                spo2Percent = 99,
                recordedBy = "Jaipur Care Check-in Desk"
            ),
            HealthVitals(
                id = "vit_02",
                patientId = "patient_001",
                timestamp = "12 Aug 2026, 11:30 AM",
                bpSystolic = 124,
                bpDiastolic = 82,
                pulseRate = 76,
                temperatureF = 99.1,
                weightKg = 60.0,
                spo2Percent = 98,
                recordedBy = "City Health Camp Vitals"
            )
        )
    )
    val vitalsList: StateFlow<List<HealthVitals>> = _vitalsList.asStateFlow()

    // Prescriptions
    private val _prescriptions = MutableStateFlow<List<Prescription>>(
        listOf(
            Prescription(
                id = "RX-2026-8841",
                consultationId = "cons_01",
                patientId = "patient_001",
                patientName = "Priya Sharma",
                doctorName = "Dr. Rajesh Sharma, MD",
                doctorSpecialty = "General Medicine",
                clinicName = "Jaipur Care Clinic",
                date = "19 Aug 2026",
                items = listOf(
                    PrescriptionItem(
                        id = "rx_item_1",
                        medicineName = "Amoxicillin / Clavulanate",
                        dosage = "625 mg",
                        frequency = "1 - 0 - 1",
                        timing = "After Meals",
                        durationDays = 5,
                        quantity = 10,
                        instructions = "Complete entire course for respiratory relief.",
                        isDispensed = true
                    ),
                    PrescriptionItem(
                        id = "rx_item_2",
                        medicineName = "Paracetamol",
                        dosage = "650 mg",
                        frequency = "1 - 0 - 1 (SOS)",
                        timing = "After Meals",
                        durationDays = 3,
                        quantity = 6,
                        instructions = "Take only if body temperature exceeds 99.5 F.",
                        isDispensed = true
                    ),
                    PrescriptionItem(
                        id = "rx_item_3",
                        medicineName = "Cetirizine Hydrochloride",
                        dosage = "10 mg",
                        frequency = "0 - 0 - 1",
                        timing = "Night after Dinner",
                        durationDays = 5,
                        quantity = 5,
                        instructions = "May cause mild drowsiness; take at bedtime.",
                        isDispensed = true
                    )
                ),
                qrPrescriptionToken = "RX-TOKEN-8841-VERIFY",
                isDispensed = true,
                dispensedDate = "19 Aug 2026, 04:30 PM",
                dispensedByCentre = "Jaipur Central Dispensary"
            )
        )
    )
    val prescriptions: StateFlow<List<Prescription>> = _prescriptions.asStateFlow()

    // Longitudinal Consultations
    private val _consultations = MutableStateFlow<List<DoctorConsultation>>(
        listOf(
            DoctorConsultation(
                id = "cons_01",
                patientId = "patient_001",
                doctorName = "Dr. Rajesh Sharma, MD",
                doctorSpecialty = "Internal Medicine & Pulmonology",
                clinicName = "Jaipur Care Clinic",
                timestamp = "19 Aug 2026, 10:45 AM",
                chiefComplaint = "Mild dry cough, fever sensations for 2 days, chest throat scratchiness.",
                diagnosis = "Acute Upper Respiratory Tract Infection (URTI) with mild pharyngitis.",
                clinicalRemarks = "Chest clear upon auscultation, no wheezing. Advised warm saline gargles, adequate hydration, and digital prescription issued.",
                vitalsSnapshot = "BP 118/78 mmHg | Pulse 72 bpm | Temp 98.4°F | SpO2 99%",
                followUpDays = 7
            ),
            DoctorConsultation(
                id = "cons_02",
                patientId = "patient_001",
                doctorName = "Dr. Ananya Sen, MS",
                doctorSpecialty = "Orthopaedics",
                clinicName = "SMS Specialty Hospital, Jaipur",
                timestamp = "04 Jul 2026, 02:20 PM",
                chiefComplaint = "Right ankle strain following morning walk slip.",
                diagnosis = "Grade 1 Lateral Ankle Ligament Sprain.",
                clinicalRemarks = "RICE protocol advised for 5 days. Crepe bandage support applied. Restrict high impact exercise.",
                vitalsSnapshot = "BP 120/80 mmHg | Pulse 70 bpm | Weight 60 kg",
                followUpDays = 14
            )
        )
    )
    val consultations: StateFlow<List<DoctorConsultation>> = _consultations.asStateFlow()

    // Active Medication Reminders
    private val _reminders = MutableStateFlow<List<MedicationReminder>>(
        listOf(
            MedicationReminder(
                id = "rem_1",
                prescriptionId = "RX-2026-8841",
                medicineName = "Amoxicillin / Clavulanate (625mg)",
                dosage = "1 Tablet",
                timeSlot = "08:30 AM (Morning)",
                mealInstruction = "After Breakfast",
                isTakenToday = true,
                streakDays = 4
            ),
            MedicationReminder(
                id = "rem_2",
                prescriptionId = "RX-2026-8841",
                medicineName = "Amoxicillin / Clavulanate (625mg)",
                dosage = "1 Tablet",
                timeSlot = "08:30 PM (Night)",
                mealInstruction = "After Dinner",
                isTakenToday = false,
                streakDays = 4
            ),
            MedicationReminder(
                id = "rem_3",
                prescriptionId = "RX-2026-8841",
                medicineName = "Cetirizine (10mg)",
                dosage = "1 Tablet",
                timeSlot = "09:30 PM (Bedtime)",
                mealInstruction = "Before Sleep",
                isTakenToday = false,
                streakDays = 4
            )
        )
    )
    val reminders: StateFlow<List<MedicationReminder>> = _reminders.asStateFlow()

    // Consents (ABDM-aligned)
    private val _consents = MutableStateFlow<List<ConsentRequest>>(
        listOf(
            ConsentRequest(
                id = "con_01",
                patientId = "patient_001",
                requesterName = "Dr. Rajesh Sharma",
                requesterRole = "HPR Verified Clinician",
                facilityName = "Jaipur Care Clinic",
                categories = listOf("Medical Timeline", "Consultation Notes", "Prescriptions", "Vitals"),
                durationHours = 24,
                status = ConsentStatus.ACTIVE,
                grantedAt = "Today, 09:30 AM"
            ),
            ConsentRequest(
                id = "con_02",
                patientId = "patient_001",
                requesterName = "Jaipur Central Dispensary",
                requesterRole = "Medicine Centre Staff",
                facilityName = "MI Road Pharmacy Unit",
                categories = listOf("Prescriptions", "Dispensing Log"),
                durationHours = 12,
                status = ConsentStatus.ACTIVE,
                grantedAt = "Today, 10:50 AM"
            )
        )
    )
    val consents: StateFlow<List<ConsentRequest>> = _consents.asStateFlow()

    // Audit Logs
    private val _auditLogs = MutableStateFlow<List<AuditLog>>(
        listOf(
            AuditLog(
                id = "aud_1",
                timestamp = "Today, 09:15 AM",
                actorName = "Early Check-in Desk",
                actorRole = "Staff",
                action = "Vitals Captured",
                details = "BP (118/78), Pulse (72), Temp (98.4F) linked to ABHA profile"
            ),
            AuditLog(
                id = "aud_2",
                timestamp = "Today, 09:32 AM",
                actorName = "Dr. Rajesh Sharma",
                actorRole = "Doctor",
                action = "Consent Verified",
                details = "Accessed longitudinal history & past consultations with patient QR token"
            ),
            AuditLog(
                id = "aud_3",
                timestamp = "Today, 10:48 AM",
                actorName = "Dr. Rajesh Sharma",
                actorRole = "Doctor",
                action = "Digital Prescription Issued",
                details = "Generated Rx-2026-8841 with 3 medications"
            ),
            AuditLog(
                id = "aud_4",
                timestamp = "Today, 04:30 PM",
                actorName = "Jaipur Central Dispensary",
                actorRole = "Medicine Centre",
                action = "Medicines Dispensed",
                details = "Scanned QR, verified and dispensed 3 items (Batch #JPR-481)"
            )
        )
    )
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    // Step & Health tracking metrics
    private val _stepMetrics = MutableStateFlow(HealthStepMetrics())
    val stepMetrics: StateFlow<HealthStepMetrics> = _stepMetrics.asStateFlow()

    // Case-Taking: Voice/Touch Intake History (SIH 2026 Problem 26047)
    private val _caseIntakes = MutableStateFlow<List<CaseIntake>>(
        listOf(
            CaseIntake(
                id = "intake_01",
                patientId = "patient_001",
                timestamp = "Today, 09:20 AM",
                intakeMode = "Voice (Bhashini AI Hindi)",
                language = "हिन्दी (Hindi) + English",
                voiceTranscript = "Mujhe pichle 2 din se tez bukhar hai, gale me khrash aur sukhi khansi hai. Penicillin se allergy hai aur subah se paracetamol li hai.",
                chiefSymptoms = listOf("High Fever (100.2°F)", "Dry Cough", "Sore Throat & Scratchiness"),
                symptomDuration = "2-3 Days",
                severityLevel = "Moderate",
                allergies = listOf("Penicillin (Severe Rash Risk)", "Dust / Pollen"),
                currentMedicines = listOf("Paracetamol 650mg (SOS)"),
                attachedRecordTitle = "CBC_Blood_Report_Aug2026.pdf",
                attachedRecordOcrText = "WBC: 11,200/mcL (Elevated mild infection) | Platelets: 2.4 Lakh | Hb: 12.8 g/dL. Chest X-Ray: Clear lung fields, no infiltrates.",
                isSynthesized = true
            )
        )
    )
    val caseIntakes: StateFlow<List<CaseIntake>> = _caseIntakes.asStateFlow()

    // Doctor Review Summary Reports (Prepared before consultation)
    private val _doctorReviewReports = MutableStateFlow<List<DoctorReviewReport>>(
        listOf(
            DoctorReviewReport(
                id = "rev_01",
                intakeId = "intake_01",
                patientId = "patient_001",
                patientName = "Priya Sharma",
                abhaId = "ABHA-91-8472-9102-4412",
                generatedAt = "Today, 09:25 AM",
                chiefComplaintSummary = "Patient presents with acute upper respiratory symptoms for 48 hours. Voice intake captured via Bhashini ASR.",
                extractedSymptoms = listOf("Fever (99.8°F at check-in)", "Throat Scratchiness", "Dry Cough"),
                severityLevel = "Moderate",
                duration = "2-3 Days",
                allergies = listOf("Penicillin (Severe Rash Risk)", "Dust / Pollen"),
                currentMedicines = listOf("Paracetamol 650mg (1 dose taken today)"),
                vitalsSnapshot = "BP 118/78 mmHg | Pulse 72 bpm | Temp 98.4°F | SpO2 99%",
                attachedReportTitle = "CBC_Blood_Report_Aug2026.pdf",
                attachedOcrSummary = "Mild leukocytosis (WBC 11,200). Normal platelets. Correlates with early acute viral pharyngitis.",
                clinicalImpression = "Acute Upper Respiratory Tract Infection (URTI) with mild pharyngeal congestion. Penicillin group contraindicated.",
                editableDoctorNotes = "Advised warm saline gargles, adequate hydration. Avoid penicillin/amoxicillin derivatives due to patient's confirmed allergy flag. Monitor for 3 days.",
                provisionalDiagnosis = "Acute Viral Pharyngitis / URTI",
                isDoctorConfirmed = true,
                confirmedByDoctorName = "Dr. Rajesh Sharma, MD",
                confirmedAt = "Today, 09:35 AM"
            )
        )
    )
    val doctorReviewReports: StateFlow<List<DoctorReviewReport>> = _doctorReviewReports.asStateFlow()

    // Medical Facility Fast Check-ins (ABDM Scan & Share)
    private val _activeCheckIn = MutableStateFlow<FacilityCheckIn?>(null)
    val activeCheckIn: StateFlow<FacilityCheckIn?> = _activeCheckIn.asStateFlow()

    private val _checkInsHistory = MutableStateFlow<List<FacilityCheckIn>>(emptyList())
    val checkInsHistory: StateFlow<List<FacilityCheckIn>> = _checkInsHistory.asStateFlow()


    fun login(emailOrPhone: String, role: UserRole, isPhone: Boolean = false): UserProfile {
        val user = when (role) {
            UserRole.PATIENT -> defaultPatient.copy(
                email = if (isPhone) "priya.sharma@healthmail.in" else emailOrPhone,
                phone = if (isPhone) emailOrPhone else "+91 98290 12345"
            )
            UserRole.DOCTOR -> defaultDoctor.copy(
                email = if (isPhone) "dr.rajesh@jaipurcare.org" else emailOrPhone,
                phone = if (isPhone) emailOrPhone else "+91 94140 55667"
            )
            UserRole.MEDICINE_CENTRE -> defaultMedicineCentre.copy(
                email = if (isPhone) "dispense@jaipurcentralpharma.in" else emailOrPhone,
                phone = if (isPhone) emailOrPhone else "+91 98290 44556"
            )
        }
        _currentUser.value = user
        return user
    }

    fun signUp(name: String, emailOrPhone: String, role: UserRole, age: Int, gender: String, bloodGroup: String, isPhone: Boolean): UserProfile {
        val randomDigits = (1000..9999).random()
        val abhaId = if (role == UserRole.PATIENT) "ABHA-91-$randomDigits-4412-9901" else "HPR-RJ-$randomDigits"
        val qrToken = "TOKEN-$randomDigits"
        val newUser = UserProfile(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "User" },
            email = if (!isPhone) emailOrPhone else "user$randomDigits@healthmail.in",
            phone = if (isPhone) emailOrPhone else "+91 98290 $randomDigits",
            role = role,
            abhaId = abhaId,
            age = if (age > 0) age else 30,
            gender = gender.ifBlank { "Not Specified" },
            bloodGroup = bloodGroup.ifBlank { "B+" },
            emergencyContact = "+91 98290 00000",
            city = "Jaipur, Rajasthan",
            qrToken = qrToken
        )
        _currentUser.value = newUser
        return newUser
    }

    fun switchRole(role: UserRole) {
        when (role) {
            UserRole.PATIENT -> _currentUser.value = defaultPatient
            UserRole.DOCTOR -> _currentUser.value = defaultDoctor
            UserRole.MEDICINE_CENTRE -> _currentUser.value = defaultMedicineCentre
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun recordVitals(
        systolic: Int,
        diastolic: Int,
        pulse: Int,
        temp: Double,
        weight: Double,
        spo2: Int
    ) {
        val now = "Today, " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val newVital = HealthVitals(
            id = UUID.randomUUID().toString(),
            patientId = "patient_001",
            timestamp = now,
            bpSystolic = systolic,
            bpDiastolic = diastolic,
            pulseRate = pulse,
            temperatureF = temp,
            weightKg = weight,
            spo2Percent = spo2,
            recordedBy = "Early Check-in Self / Desk"
        )
        _vitalsList.value = listOf(newVital) + _vitalsList.value

        addAuditLog(
            actorName = _currentUser.value?.name ?: "Patient",
            actorRole = _currentUser.value?.role?.displayName ?: "Patient",
            action = "Vitals Captured",
            details = "BP ($systolic/$diastolic), Pulse ($pulse bpm), Temp ($temp°F), SpO2 ($spo2%)"
        )
    }

    fun createConsultationWithPrescription(
        patientId: String,
        doctorName: String,
        doctorSpecialty: String,
        clinicName: String,
        chiefComplaint: String,
        diagnosis: String,
        remarks: String,
        items: List<PrescriptionItem>
    ) {
        val now = "Today, " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val consId = "cons_${System.currentTimeMillis()}"
        val rxId = "RX-2026-${(1000..9999).random()}"

        val prescription = if (items.isNotEmpty()) {
            Prescription(
                id = rxId,
                consultationId = consId,
                patientId = patientId,
                patientName = defaultPatient.name,
                doctorName = doctorName,
                doctorSpecialty = doctorSpecialty,
                clinicName = clinicName,
                date = dateStr,
                items = items,
                qrPrescriptionToken = "$rxId-VERIFY-TOKEN",
                isDispensed = false
            )
        } else null

        if (prescription != null) {
            _prescriptions.value = listOf(prescription) + _prescriptions.value
        }

        val vitalsSnap = _vitalsList.value.firstOrNull()?.let {
            "BP ${it.bpSystolic}/${it.bpDiastolic} mmHg | Pulse ${it.pulseRate} bpm | Temp ${it.temperatureF}°F | SpO2 ${it.spo2Percent}%"
        } ?: "Standard Check-in Vitals recorded"

        val consultation = DoctorConsultation(
            id = consId,
            patientId = patientId,
            doctorName = doctorName,
            doctorSpecialty = doctorSpecialty,
            clinicName = clinicName,
            timestamp = now,
            chiefComplaint = chiefComplaint,
            diagnosis = diagnosis,
            clinicalRemarks = remarks,
            vitalsSnapshot = vitalsSnap,
            followUpDays = 7,
            prescription = prescription
        )

        _consultations.value = listOf(consultation) + _consultations.value

        addAuditLog(
            actorName = doctorName,
            actorRole = "Doctor",
            action = "Consultation & Prescription Issued",
            details = "Diagnosed '$diagnosis' and issued digital prescription ($rxId)"
        )
    }

    fun dispensePrescription(prescriptionId: String, pharmacyName: String): Boolean {
        val currentList = _prescriptions.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == prescriptionId }
        if (index != -1) {
            val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            val target = currentList[index]
            val updatedItems = target.items.map { it.copy(isDispensed = true) }
            val updatedRx = target.copy(
                isDispensed = true,
                dispensedDate = now,
                dispensedByCentre = pharmacyName,
                items = updatedItems
            )
            currentList[index] = updatedRx
            _prescriptions.value = currentList

            // Also configure daily patient medication reminders
            val newReminders = target.items.flatMap { item ->
                listOf(
                    MedicationReminder(
                        id = "rem_${UUID.randomUUID()}",
                        prescriptionId = target.id,
                        medicineName = "${item.medicineName} (${item.dosage})",
                        dosage = "1 Unit",
                        timeSlot = "08:30 AM (Morning)",
                        mealInstruction = item.timing,
                        isTakenToday = false,
                        streakDays = 1
                    ),
                    MedicationReminder(
                        id = "rem_${UUID.randomUUID()}",
                        prescriptionId = target.id,
                        medicineName = "${item.medicineName} (${item.dosage})",
                        dosage = "1 Unit",
                        timeSlot = "08:30 PM (Night)",
                        mealInstruction = item.timing,
                        isTakenToday = false,
                        streakDays = 1
                    )
                )
            }
            _reminders.value = newReminders + _reminders.value

            addAuditLog(
                actorName = pharmacyName,
                actorRole = "Medicine Centre",
                action = "Medicines Dispensed",
                details = "Verified and dispensed all medicines for $prescriptionId"
            )
            return true
        }
        return false
    }

    fun toggleReminderTaken(reminderId: String) {
        _reminders.value = _reminders.value.map {
            if (it.id == reminderId) {
                val newStatus = !it.isTakenToday
                val newStreak = if (newStatus) it.streakDays + 1 else maxOf(1, it.streakDays - 1)
                it.copy(isTakenToday = newStatus, streakDays = newStreak)
            } else it
        }
    }

    fun markReminderTaken(reminderId: String) {
        _reminders.value = _reminders.value.map {
            if (it.id == reminderId && !it.isTakenToday) {
                it.copy(isTakenToday = true, streakDays = it.streakDays + 1)
            } else it
        }
        addAuditLog(
            actorName = _currentUser.value?.name ?: "Patient",
            actorRole = "Patient",
            action = "Medicine Dose Taken",
            details = "Dose marked taken and locked into adherence record"
        )
    }

    fun saveCaseIntake(
        mode: String,
        language: String,
        transcript: String,
        symptoms: List<String>,
        duration: String,
        severity: String,
        allergies: List<String>,
        medicines: List<String>,
        attachedRecordTitle: String? = null,
        attachedRecordOcrText: String? = null
    ): DoctorReviewReport {
        val now = "Today, " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val intakeId = "intake_${System.currentTimeMillis()}"
        val reviewId = "rev_${System.currentTimeMillis()}"
        val patient = _currentUser.value ?: defaultPatient

        val intake = CaseIntake(
            id = intakeId,
            patientId = patient.id,
            timestamp = now,
            intakeMode = mode,
            language = language,
            voiceTranscript = transcript,
            chiefSymptoms = symptoms,
            symptomDuration = duration,
            severityLevel = severity,
            allergies = allergies,
            currentMedicines = medicines,
            attachedRecordTitle = attachedRecordTitle,
            attachedRecordOcrText = attachedRecordOcrText,
            isSynthesized = true
        )
        _caseIntakes.value = listOf(intake) + _caseIntakes.value

        val vitalsSnap = _vitalsList.value.firstOrNull()?.let {
            "BP ${it.bpSystolic}/${it.bpDiastolic} mmHg | Pulse ${it.pulseRate} bpm | Temp ${it.temperatureF}°F | SpO2 ${it.spo2Percent}%"
        } ?: "BP 120/80 mmHg | Pulse 72 bpm | Temp 98.6°F | SpO2 99%"

        val complaintSummary = if (transcript.isNotBlank()) {
            "Patient recorded history ($language): \"$transcript\""
        } else {
            "Structured touch intake: ${symptoms.joinToString(", ")} ongoing for $duration with $severity severity."
        }

        val impression = "Synthesized Pre-Consultation History: ${symptoms.joinToString(", ")} ($severity) lasting $duration. " +
                (if (allergies.isNotEmpty()) "CRITICAL ALLERGY ALERT: ${allergies.joinToString(", ")}. " else "") +
                (if (medicines.isNotEmpty()) "Ongoing medications: ${medicines.joinToString(", ")}." else "")

        val report = DoctorReviewReport(
            id = reviewId,
            intakeId = intakeId,
            patientId = patient.id,
            patientName = patient.name,
            abhaId = patient.abhaId,
            generatedAt = now,
            chiefComplaintSummary = complaintSummary,
            extractedSymptoms = symptoms,
            severityLevel = severity,
            duration = duration,
            allergies = allergies,
            currentMedicines = medicines,
            vitalsSnapshot = vitalsSnap,
            attachedReportTitle = attachedRecordTitle,
            attachedOcrSummary = attachedRecordOcrText,
            clinicalImpression = impression,
            editableDoctorNotes = "Pre-consultation history synthesized and verified with check-in vitals. Ready for clinical review and doctor confirmation.",
            provisionalDiagnosis = if (symptoms.any { it.contains("Fever", ignoreCase = true) || it.contains("Cough", ignoreCase = true) }) "Acute Viral Infection / URTI" else "Clinical Examination & Confirmation Advised",
            isDoctorConfirmed = false
        )

        _doctorReviewReports.value = listOf(report) + _doctorReviewReports.value

        addAuditLog(
            actorName = patient.name,
            actorRole = "Patient",
            action = "Voice/Touch Intake Captured",
            details = "Synthesized clinical story with ${symptoms.size} symptoms and ${allergies.size} allergies"
        )

        return report
    }

    fun updateDoctorNotes(reportId: String, doctorNotes: String, diagnosis: String) {
        _doctorReviewReports.value = _doctorReviewReports.value.map {
            if (it.id == reportId) {
                it.copy(editableDoctorNotes = doctorNotes, provisionalDiagnosis = diagnosis)
            } else it
        }
    }

    fun confirmDoctorReview(reportId: String, doctorName: String) {
        val now = "Today, " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        _doctorReviewReports.value = _doctorReviewReports.value.map {
            if (it.id == reportId) {
                it.copy(
                    isDoctorConfirmed = true,
                    confirmedByDoctorName = doctorName,
                    confirmedAt = now
                )
            } else it
        }
        addAuditLog(
            actorName = doctorName,
            actorRole = "Doctor",
            action = "Doctor Confirmed Case Summary",
            details = "Clinician reviewed, validated and signed pre-consultation summary #$reportId"
        )
    }


    fun toggleConsentStatus(consentId: String) {
        _consents.value = _consents.value.map {
            if (it.id == consentId) {
                val newStatus = if (it.status == ConsentStatus.ACTIVE) ConsentStatus.REVOKED else ConsentStatus.ACTIVE
                addAuditLog(
                    actorName = _currentUser.value?.name ?: "Patient",
                    actorRole = "Patient",
                    action = if (newStatus == ConsentStatus.ACTIVE) "Consent Granted" else "Consent Revoked",
                    details = "Updated consent for ${it.requesterName} (${it.facilityName})"
                )
                it.copy(status = newStatus)
            } else it
        }
    }

    fun addStepProgress(stepsToAdd: Int = 500) {
        val current = _stepMetrics.value
        val updated = current.copy(
            stepsToday = minOf(15000, current.stepsToday + stepsToAdd),
            caloriesBurned = current.caloriesBurned + (stepsToAdd * 0.04).toInt(),
            distanceKm = ((current.stepsToday + stepsToAdd) * 0.00075 * 10).toInt() / 10.0
        )
        _stepMetrics.value = updated
    }

    fun addWaterGlass() {
        val current = _stepMetrics.value
        _stepMetrics.value = current.copy(waterGlasses = minOf(12, current.waterGlasses + 1))
    }

    fun performFacilityCheckIn(
        facilityId: String,
        facilityName: String,
        department: String,
        counterNumber: String,
        rawQr: String
    ): FacilityCheckIn {
        val now = "Today, " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val tokenNum = "OPD-" + ('A'..'D').random() + "-" + String.format(Locale.getDefault(), "%03d", (10..150).random())
        val checkIn = FacilityCheckIn(
            id = "chk_${UUID.randomUUID().toString().take(8)}",
            facilityId = facilityId,
            facilityName = facilityName,
            department = department,
            counterNumber = counterNumber,
            tokenNumber = tokenNum,
            timestamp = now,
            estimatedWaitMinutes = (8..20).random(),
            status = "CONFIRMED",
            abhaShared = _currentUser.value?.abhaId ?: "ABHA-91-8472-9102-4412",
            qrRawData = rawQr,
            securityChecksum = "SHA256:ABDM-" + UUID.randomUUID().toString().take(10).uppercase()
        )
        _activeCheckIn.value = checkIn
        _checkInsHistory.value = listOf(checkIn) + _checkInsHistory.value

        addAuditLog(
            actorName = facilityName,
            actorRole = "ABDM Facility Desk",
            action = "Fast QR Check-in",
            details = "Linked ABHA ${_currentUser.value?.abhaId ?: "ABHA-91-8472-9102-4412"} at $counterNumber. OPD Token #$tokenNum assigned."
        )

        // Automatically provision temporary active consent for the hospital
        val existingConsent = _consents.value.find { it.facilityName.equals(facilityName, ignoreCase = true) }
        if (existingConsent == null) {
            val newConsent = ConsentRequest(
                id = "con_fac_${UUID.randomUUID().toString().take(6)}",
                patientId = _currentUser.value?.id ?: "patient_001",
                requesterName = "$facilityName Triage Desk",
                requesterRole = "Hospital Desk Staff",
                facilityName = facilityName,
                categories = listOf("Medical Timeline", "Vitals", "Prescriptions"),
                durationHours = 12,
                status = ConsentStatus.ACTIVE,
                grantedAt = now
            )
            _consents.value = listOf(newConsent) + _consents.value
        }

        return checkIn
    }

    fun checkoutFacility() {
        val current = _activeCheckIn.value
        if (current != null) {
            addAuditLog(
                actorName = current.facilityName,
                actorRole = "Patient Exit",
                action = "Facility Check-out",
                details = "Completed visit for token ${current.tokenNumber} at ${current.facilityName}"
            )
            _activeCheckIn.value = null
        }
    }

    private fun addAuditLog(actorName: String, actorRole: String, action: String, details: String) {
        val now = "Today, " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val log = AuditLog(
            id = "aud_${System.currentTimeMillis()}",
            timestamp = now,
            actorName = actorName,
            actorRole = actorRole,
            action = action,
            details = details
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }
}
