package com.example.data.model

enum class UserRole(val displayName: String, val badgeTitle: String) {
    PATIENT("Patient", "ABHA Health ID"),
    DOCTOR("Doctor / Clinician", "HPR Verified Doctor"),
    MEDICINE_CENTRE("Medicine Centre", "Registered Pharmacy")
}

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val abhaId: String,
    val age: Int,
    val gender: String,
    val bloodGroup: String,
    val emergencyContact: String,
    val city: String = "Jaipur, Rajasthan",
    val qrToken: String
)

data class HealthVitals(
    val id: String,
    val patientId: String,
    val timestamp: String,
    val bpSystolic: Int,
    val bpDiastolic: Int,
    val pulseRate: Int,
    val temperatureF: Double,
    val weightKg: Double,
    val spo2Percent: Int,
    val recordedBy: String = "Early Check-in Desk"
)

data class PrescriptionItem(
    val id: String,
    val medicineName: String,
    val dosage: String,
    val frequency: String, // e.g. "1 - 0 - 1"
    val timing: String,    // "After Meals", "Before Meals"
    val durationDays: Int,
    val quantity: Int,
    val instructions: String = "",
    val isDispensed: Boolean = false
)

data class Prescription(
    val id: String,
    val consultationId: String,
    val patientId: String,
    val patientName: String,
    val doctorName: String,
    val doctorSpecialty: String,
    val clinicName: String,
    val date: String,
    val items: List<PrescriptionItem>,
    val qrPrescriptionToken: String,
    val isDispensed: Boolean,
    val dispensedDate: String? = null,
    val dispensedByCentre: String? = null
)

data class DoctorConsultation(
    val id: String,
    val patientId: String,
    val doctorName: String,
    val doctorSpecialty: String,
    val clinicName: String,
    val timestamp: String,
    val chiefComplaint: String,
    val diagnosis: String,
    val clinicalRemarks: String,
    val vitalsSnapshot: String,
    val followUpDays: Int = 7,
    val prescription: Prescription? = null
)

data class MedicationReminder(
    val id: String,
    val prescriptionId: String,
    val medicineName: String,
    val dosage: String,
    val timeSlot: String,      // e.g. "08:00 AM" (Morning), "01:30 PM" (Afternoon), "08:30 PM" (Night)
    val mealInstruction: String,
    val isTakenToday: Boolean,
    val streakDays: Int = 3
)

data class ConsentRequest(
    val id: String,
    val patientId: String,
    val requesterName: String,
    val requesterRole: String,
    val facilityName: String,
    val categories: List<String>, // "Medical Timeline", "Prescriptions", "Vitals", "Lab Reports"
    val durationHours: Int,
    val status: ConsentStatus,
    val grantedAt: String
)

enum class ConsentStatus {
    ACTIVE,
    REVOKED,
    EXPIRED
}

data class AuditLog(
    val id: String,
    val timestamp: String,
    val actorName: String,
    val actorRole: String,
    val action: String,
    val details: String
)

data class HealthStepMetrics(
    val stepsToday: Int = 6420,
    val stepGoal: Int = 10000,
    val caloriesBurned: Int = 312,
    val distanceKm: Double = 4.8,
    val waterGlasses: Int = 6,
    val sleepHours: Double = 7.5
)

data class CaseIntake(
    val id: String,
    val patientId: String,
    val timestamp: String,
    val intakeMode: String, // "Voice Intake (Bhashini AI)" or "Touch & Symptom Picker"
    val language: String,   // "Hindi", "English", "Hinglish", "Bengali", "Tamil", "Marathi"
    val voiceTranscript: String,
    val chiefSymptoms: List<String>,
    val symptomDuration: String,
    val severityLevel: String, // "Mild", "Moderate", "Severe"
    val allergies: List<String>,
    val currentMedicines: List<String>,
    val attachedRecordTitle: String? = null,
    val attachedRecordOcrText: String? = null,
    val isSynthesized: Boolean = true
)

data class DoctorReviewReport(
    val id: String,
    val intakeId: String,
    val patientId: String,
    val patientName: String,
    val abhaId: String,
    val generatedAt: String,
    val chiefComplaintSummary: String,
    val extractedSymptoms: List<String>,
    val severityLevel: String,
    val duration: String,
    val allergies: List<String>,
    val currentMedicines: List<String>,
    val vitalsSnapshot: String,
    val attachedReportTitle: String? = null,
    val attachedOcrSummary: String? = null,
    val clinicalImpression: String,
    val editableDoctorNotes: String,
    val provisionalDiagnosis: String,
    val isDoctorConfirmed: Boolean,
    val confirmedByDoctorName: String? = null,
    val confirmedAt: String? = null
)

data class FacilityCheckIn(
    val id: String,
    val facilityId: String,
    val facilityName: String,
    val department: String,
    val counterNumber: String,
    val tokenNumber: String,
    val timestamp: String,
    val estimatedWaitMinutes: Int,
    val status: String = "CONFIRMED", // "CONFIRMED", "IN_QUEUE", "COMPLETED"
    val abhaShared: String,
    val qrRawData: String,
    val securityChecksum: String
)

data class FacilityQrScanInfo(
    val facilityId: String,
    val facilityName: String,
    val department: String,
    val counterNumber: String,
    val rawQrPayload: String,
    val checksum: String
)

