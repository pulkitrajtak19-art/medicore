package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.HealthRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HealthViewModel(
    private val repository: HealthRepository = HealthRepository()
) : ViewModel() {

    val currentUser: StateFlow<UserProfile?> = repository.currentUser
    val vitalsList: StateFlow<List<HealthVitals>> = repository.vitalsList
    val prescriptions: StateFlow<List<Prescription>> = repository.prescriptions
    val consultations: StateFlow<List<DoctorConsultation>> = repository.consultations
    val reminders: StateFlow<List<MedicationReminder>> = repository.reminders
    val consents: StateFlow<List<ConsentRequest>> = repository.consents
    val auditLogs: StateFlow<List<AuditLog>> = repository.auditLogs
    val stepMetrics: StateFlow<HealthStepMetrics> = repository.stepMetrics
    val caseIntakes: StateFlow<List<CaseIntake>> = repository.caseIntakes
    val doctorReviewReports: StateFlow<List<DoctorReviewReport>> = repository.doctorReviewReports
    val activeCheckIn: StateFlow<FacilityCheckIn?> = repository.activeCheckIn
    val checkInsHistory: StateFlow<List<FacilityCheckIn>> = repository.checkInsHistory

    fun setUserProfile(user: UserProfile) {
        repository.setUserProfile(user)
        syncToSupabase(user)
    }

    fun syncToSupabase(user: UserProfile? = null) {
        val targetUser = user ?: currentUser.value ?: return
        viewModelScope.launch {
            com.example.data.supabase.SupabaseClient.syncUserData(
                user = targetUser,
                vitals = vitalsList.value,
                prescriptions = prescriptions.value
            )
        }
    }

    fun login(emailOrPhone: String, role: UserRole, isPhone: Boolean) {
        repository.login(emailOrPhone, role, isPhone)
    }

    fun signUp(name: String, emailOrPhone: String, role: UserRole, age: Int, gender: String, bloodGroup: String, isPhone: Boolean) {
        repository.signUp(name, emailOrPhone, role, age, gender, bloodGroup, isPhone)
    }

    fun setUser(user: UserProfile) {
        repository.setUser(user)
    }

    fun updateUserProfile(
        age: Int,
        gender: String,
        heightCm: Double,
        weightKg: Double,
        allergies: String,
        bloodGroup: String,
        phone: String,
        city: String,
        emergencyContact: String
    ): UserProfile? {
        return repository.updateUserProfile(
            age, gender, heightCm, weightKg, allergies, bloodGroup, phone, city, emergencyContact
        )
    }

    fun switchRole(role: UserRole) {
        repository.switchRole(role)
    }

    fun logout() {
        repository.logout()
    }

    fun recordVitals(systolic: Int, diastolic: Int, pulse: Int, temp: Double, weight: Double, spo2: Int) {
        repository.recordVitals(systolic, diastolic, pulse, temp, weight, spo2)
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
        repository.createConsultationWithPrescription(
            patientId, doctorName, doctorSpecialty, clinicName, chiefComplaint, diagnosis, remarks, items
        )
    }

    fun dispensePrescription(prescriptionId: String, pharmacyName: String): Boolean {
        return repository.dispensePrescription(prescriptionId, pharmacyName)
    }

    fun toggleReminder(reminderId: String) {
        repository.toggleReminderTaken(reminderId)
    }

    fun markReminderTaken(reminderId: String) {
        repository.markReminderTaken(reminderId)
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
        return repository.saveCaseIntake(
            mode, language, transcript, symptoms, duration, severity, allergies, medicines, attachedRecordTitle, attachedRecordOcrText
        )
    }

    fun updateDoctorNotes(reportId: String, doctorNotes: String, diagnosis: String) {
        repository.updateDoctorNotes(reportId, doctorNotes, diagnosis)
    }

    fun confirmDoctorReview(reportId: String, doctorName: String) {
        repository.confirmDoctorReview(reportId, doctorName)
    }

    fun toggleConsent(consentId: String) {
        repository.toggleConsentStatus(consentId)
    }

    fun performFacilityCheckIn(
        facilityId: String,
        facilityName: String,
        department: String,
        counterNumber: String,
        rawQr: String
    ): FacilityCheckIn {
        return repository.performFacilityCheckIn(facilityId, facilityName, department, counterNumber, rawQr)
    }

    fun checkoutFacility() {
        repository.checkoutFacility()
    }

    fun addSteps(steps: Int = 500) {
        repository.addStepProgress(steps)
    }

    fun addWater() {
        repository.addWaterGlass()
    }
}
