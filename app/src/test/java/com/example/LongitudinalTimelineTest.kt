package com.example

import com.example.data.model.*
import com.example.data.repository.HealthRepository
import com.example.ui.screens.TimelineEvent
import com.example.ui.screens.TimelineFilter
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LongitudinalTimelineTest {

    private lateinit var repository: HealthRepository

    @Before
    fun setup() {
        repository = HealthRepository()
    }

    @Test
    fun `longitudinal timeline aggregates all healthcare domains`() {
        val doctorReports = repository.doctorReviewReports.value
        val consultations = repository.consultations.value
        val prescriptions = repository.prescriptions.value
        val vitals = repository.vitalsList.value
        val facilityVisits = repository.checkInsHistory.value

        val events = mutableListOf<TimelineEvent>()
        doctorReports.forEach { events.add(TimelineEvent.DoctorReviewEvent(it)) }
        consultations.forEach { events.add(TimelineEvent.ConsultationEvent(it)) }
        prescriptions.forEach { events.add(TimelineEvent.PrescriptionEvent(it)) }
        vitals.forEach { events.add(TimelineEvent.VitalsEvent(it)) }
        facilityVisits.forEach { events.add(TimelineEvent.FacilityCheckInEvent(it)) }

        assertTrue("Longitudinal timeline must contain historical events", events.isNotEmpty())
        assertTrue("Contains doctor reviews", events.any { it is TimelineEvent.DoctorReviewEvent })
        assertTrue("Contains consultation encounters", events.any { it is TimelineEvent.ConsultationEvent })
        assertTrue("Contains prescriptions", events.any { it is TimelineEvent.PrescriptionEvent })
        assertTrue("Contains vitals checkpoints", events.any { it is TimelineEvent.VitalsEvent })
    }

    @Test
    fun `timeline search matches diagnosis, symptoms and doctor name`() {
        val doctorReports = repository.doctorReviewReports.value
        val consultations = repository.consultations.value

        val events = mutableListOf<TimelineEvent>()
        doctorReports.forEach { events.add(TimelineEvent.DoctorReviewEvent(it)) }
        consultations.forEach { events.add(TimelineEvent.ConsultationEvent(it)) }

        // Search by diagnosis "URTI" or "Pharyngitis"
        val query = "pharyngitis"
        val matches = events.filter { event ->
            when (event) {
                is TimelineEvent.DoctorReviewEvent -> {
                    event.report.provisionalDiagnosis.lowercase().contains(query) ||
                            event.report.chiefComplaintSummary.lowercase().contains(query)
                }
                is TimelineEvent.ConsultationEvent -> {
                    event.consultation.diagnosis.lowercase().contains(query)
                }
                else -> false
            }
        }
        assertTrue("Query for 'pharyngitis' should find matching medical records", matches.isNotEmpty())

        // Search by doctor name "Rajesh"
        val docQuery = "rajesh"
        val docMatches = events.filter { event ->
            when (event) {
                is TimelineEvent.DoctorReviewEvent -> {
                    event.report.confirmedByDoctorName?.lowercase()?.contains(docQuery) == true
                }
                is TimelineEvent.ConsultationEvent -> {
                    event.consultation.doctorName.lowercase().contains(docQuery)
                }
                else -> false
            }
        }
        assertTrue("Query for 'rajesh' should find doctor events", docMatches.isNotEmpty())
    }

    @Test
    fun `category filters properly separate timeline stream`() {
        val doctorReports = repository.doctorReviewReports.value
        val consultations = repository.consultations.value
        val prescriptions = repository.prescriptions.value
        val vitals = repository.vitalsList.value

        val events = mutableListOf<TimelineEvent>()
        doctorReports.forEach { events.add(TimelineEvent.DoctorReviewEvent(it)) }
        consultations.forEach { events.add(TimelineEvent.ConsultationEvent(it)) }
        prescriptions.forEach { events.add(TimelineEvent.PrescriptionEvent(it)) }
        vitals.forEach { events.add(TimelineEvent.VitalsEvent(it)) }

        val reviewEvents = events.filter { it.category == TimelineFilter.DOCTOR_REVIEW_SUMMARY }
        val consultationEvents = events.filter { it.category == TimelineFilter.CONSULTATION_HISTORY }
        val prescriptionEvents = events.filter { it.category == TimelineFilter.PRESCRIPTIONS }
        val vitalsEvents = events.filter { it.category == TimelineFilter.VITALS }

        assertEquals(doctorReports.size, reviewEvents.size)
        assertEquals(consultations.size, consultationEvents.size)
        assertEquals(prescriptions.size, prescriptionEvents.size)
        assertEquals(vitals.size, vitalsEvents.size)
    }

    @Test
    fun `voice touch intake tab title is configured correctly`() {
        val tab = com.example.ui.PatientTab.VoiceTouchIntake
        assertEquals("Voice / Touch Intake", tab.title)
    }
}
