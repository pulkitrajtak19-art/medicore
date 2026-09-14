package com.example

import com.example.data.model.*
import com.example.data.repository.HealthRepository
import com.example.ui.viewmodel.HealthViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class HealthAppFeatureLoopTest {

    private lateinit var repository: HealthRepository
    private lateinit var viewModel: HealthViewModel

    @Before
    fun setUp() {
        repository = HealthRepository()
        viewModel = HealthViewModel(repository)
    }

    @Test
    fun `feature loop - authentication with multiple personas`() {
        // 1. Patient Login
        val patient = repository.login("priya.sharma@healthmail.in", UserRole.PATIENT, false)
        assertNotNull(patient)
        assertEquals(UserRole.PATIENT, patient.role)
        assertEquals("Priya Sharma", patient.name)
        assertTrue(patient.abhaId.startsWith("ABHA-"))

        // 2. Doctor Login
        val doctor = repository.login("dr.rajesh@jaipurcare.org", UserRole.DOCTOR, false)
        assertNotNull(doctor)
        assertEquals(UserRole.DOCTOR, doctor.role)
        assertEquals("Dr. Rajesh Sharma", doctor.name)

        // 3. Medicine Centre Login
        val pharmacy = repository.login("dispense@jaipurcentralpharma.in", UserRole.MEDICINE_CENTRE, false)
        assertNotNull(pharmacy)
        assertEquals(UserRole.MEDICINE_CENTRE, pharmacy.role)

        // 4. Sign Up Flow
        val newPatient = repository.signUp("Amit Kumar", "amit@healthmail.in", UserRole.PATIENT, 32, "Male", "A+", false)
        assertEquals("Amit Kumar", newPatient.name)
        assertEquals(UserRole.PATIENT, newPatient.role)
        assertEquals("A+", newPatient.bloodGroup)
        assertEquals(32, newPatient.age)
    }

    @Test
    fun `feature loop - early checkin vitals capture`() {
        repository.login("priya.sharma@healthmail.in", UserRole.PATIENT, false)
        val initialCount = repository.vitalsList.value.size

        // Record new vitals
        repository.recordVitals(
            systolic = 120,
            diastolic = 80,
            pulse = 74,
            temp = 98.6,
            weight = 62.0,
            spo2 = 99
        )

        val updatedVitals = repository.vitalsList.value
        assertEquals(initialCount + 1, updatedVitals.size)
        val latest = updatedVitals.first()
        assertEquals(120, latest.bpSystolic)
        assertEquals(80, latest.bpDiastolic)
        assertEquals(74, latest.pulseRate)
        assertEquals(98.6, latest.temperatureF, 0.01)
        assertEquals(62.0, latest.weightKg, 0.01)
        assertEquals(99, latest.spo2Percent)

        // Verify audit log generated
        val latestLog = repository.auditLogs.value.first()
        assertEquals("Vitals Captured", latestLog.action)
    }

    @Test
    fun `feature loop - doctor consultation and digital prescription creation`() {
        val initialConsCount = repository.consultations.value.size
        val initialRxCount = repository.prescriptions.value.size

        val prescriptionItems = listOf(
            PrescriptionItem(
                id = UUID.randomUUID().toString(),
                medicineName = "Amoxicillin 500mg",
                dosage = "1 Cap",
                frequency = "1 - 0 - 1",
                timing = "After Meals",
                durationDays = 5,
                quantity = 10,
                instructions = "Take after breakfast and dinner"
            )
        )

        repository.createConsultationWithPrescription(
            patientId = "patient_001",
            doctorName = "Dr. Rajesh Sharma",
            doctorSpecialty = "General Medicine",
            clinicName = "Jaipur Care Clinic",
            chiefComplaint = "Fever and sore throat",
            diagnosis = "Viral Pharyngitis",
            remarks = "Adequate rest and warm liquids advised",
            items = prescriptionItems
        )

        val updatedCons = repository.consultations.value
        val updatedRx = repository.prescriptions.value

        assertEquals(initialConsCount + 1, updatedCons.size)
        assertEquals(initialRxCount + 1, updatedRx.size)

        val latestCons = updatedCons.first()
        assertEquals("Viral Pharyngitis", latestCons.diagnosis)
        assertNotNull(latestCons.prescription)
        assertEquals(1, latestCons.prescription!!.items.size)
        assertFalse(latestCons.prescription!!.isDispensed)
    }

    @Test
    fun `feature loop - medicine centre dispensing and adherence scheduling`() {
        val initialRemindersCount = repository.reminders.value.size
        val pendingRx = repository.prescriptions.value.firstOrNull { !it.isDispensed }

        val targetRxId = pendingRx?.id ?: run {
            repository.createConsultationWithPrescription(
                patientId = "patient_001",
                doctorName = "Dr. Rajesh Sharma",
                doctorSpecialty = "General Medicine",
                clinicName = "Jaipur Care Clinic",
                chiefComplaint = "Cough",
                diagnosis = "Bronchitis",
                remarks = "Rx issued",
                items = listOf(
                    PrescriptionItem(
                        id = UUID.randomUUID().toString(),
                        medicineName = "Levocetirizine 5mg",
                        dosage = "1 Tab",
                        frequency = "0 - 0 - 1",
                        timing = "Bedtime",
                        durationDays = 5,
                        quantity = 5
                    )
                )
            )
            repository.prescriptions.value.first().id
        }

        // Dispense prescription
        val dispensed = repository.dispensePrescription(targetRxId, "Jaipur Central Dispensary")
        assertTrue(dispensed)

        val updatedTargetRx = repository.prescriptions.value.first { it.id == targetRxId }
        assertTrue(updatedTargetRx.isDispensed)
        assertEquals("Jaipur Central Dispensary", updatedTargetRx.dispensedByCentre)
        assertNotNull(updatedTargetRx.dispensedDate)

        // Check reminders were scheduled
        assertTrue(repository.reminders.value.size > initialRemindersCount)

        // Check audit log
        val latestLog = repository.auditLogs.value.first()
        assertEquals("Medicines Dispensed", latestLog.action)
    }

    @Test
    fun `feature loop - adherence reminder toggle and streak tracking`() {
        val firstReminder = repository.reminders.value.first()
        val initialStatus = firstReminder.isTakenToday
        val initialStreak = firstReminder.streakDays

        repository.toggleReminderTaken(firstReminder.id)

        val updated = repository.reminders.value.first { it.id == firstReminder.id }
        assertEquals(!initialStatus, updated.isTakenToday)
    }

    @Test
    fun `feature loop - privacy consent toggle and audit logging`() {
        val firstConsent = repository.consents.value.first()
        val initialStatus = firstConsent.status

        repository.toggleConsentStatus(firstConsent.id)

        val updated = repository.consents.value.first { it.id == firstConsent.id }
        val expectedStatus = if (initialStatus == ConsentStatus.ACTIVE) ConsentStatus.REVOKED else ConsentStatus.ACTIVE
        assertEquals(expectedStatus, updated.status)

        val latestLog = repository.auditLogs.value.first()
        assertTrue(latestLog.action.contains("Consent"))
    }

    @Test
    fun `feature loop - step metrics and water tracker updates`() {
        val initialSteps = repository.stepMetrics.value.stepsToday
        val initialWater = repository.stepMetrics.value.waterGlasses

        repository.addStepProgress(500)
        assertEquals(initialSteps + 500, repository.stepMetrics.value.stepsToday)

        repository.addWaterGlass()
        assertEquals(initialWater + 1, repository.stepMetrics.value.waterGlasses)
    }

    @Test
    fun `feature loop - role switching and logout`() {
        repository.login("priya.sharma@healthmail.in", UserRole.PATIENT, false)
        assertEquals(UserRole.PATIENT, repository.currentUser.value?.role)

        repository.switchRole(UserRole.DOCTOR)
        assertEquals(UserRole.DOCTOR, repository.currentUser.value?.role)

        repository.switchRole(UserRole.MEDICINE_CENTRE)
        assertEquals(UserRole.MEDICINE_CENTRE, repository.currentUser.value?.role)

        repository.logout()
        assertNull(repository.currentUser.value)
    }
}
