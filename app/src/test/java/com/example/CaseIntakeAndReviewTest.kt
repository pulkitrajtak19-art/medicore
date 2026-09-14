package com.example

import com.example.data.repository.HealthRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CaseIntakeAndReviewTest {

    private lateinit var repository: HealthRepository

    @Before
    fun setup() {
        repository = HealthRepository()
    }

    @Test
    fun `case intake synthesis creates doctor review report`() {
        val initialReportCount = repository.doctorReviewReports.value.size

        val report = repository.saveCaseIntake(
            mode = "Voice (Bhashini AI Hindi)",
            language = "हिन्दी (Hindi)",
            transcript = "Mujhe tez bukhar hai aur gale me dard hai.",
            symptoms = listOf("High Fever", "Sore Throat"),
            duration = "2-3 Days",
            severity = "Moderate",
            allergies = listOf("Penicillin"),
            medicines = listOf("Paracetamol 650mg"),
            attachedRecordTitle = "Lab_Report.pdf",
            attachedRecordOcrText = "WBC 11,200/mcL"
        )

        assertNotNull(report)
        assertEquals(initialReportCount + 1, repository.doctorReviewReports.value.size)
        assertTrue(report.extractedSymptoms.contains("High Fever"))
        assertTrue(report.allergies.contains("Penicillin"))
        assertFalse(report.isDoctorConfirmed)
    }

    @Test
    fun `doctor confirms pre-consultation summary before action`() {
        val report = repository.doctorReviewReports.value.first()
        assertNotNull(report)

        repository.confirmDoctorReview(report.id, "Dr. Rajesh Sharma, MD")

        val updated = repository.doctorReviewReports.value.first { it.id == report.id }
        assertTrue(updated.isDoctorConfirmed)
        assertEquals("Dr. Rajesh Sharma, MD", updated.confirmedByDoctorName)
    }

    @Test
    fun `medicine mark taken locks state and cannot be unclicked`() {
        // Find or create an untaken reminder
        val untaken = repository.reminders.value.firstOrNull { !it.isTakenToday }
            ?: run {
                repository.toggleReminderTaken(repository.reminders.value.first().id)
                repository.reminders.value.first { !it.isTakenToday }
            }

        val initialStreak = untaken.streakDays
        repository.markReminderTaken(untaken.id)

        val afterTaken = repository.reminders.value.first { it.id == untaken.id }
        assertTrue(afterTaken.isTakenToday)
        assertEquals(initialStreak + 1, afterTaken.streakDays)

        // Calling markReminderTaken again should keep it taken (no unclick toggle)
        repository.markReminderTaken(untaken.id)
        val stillTaken = repository.reminders.value.first { it.id == untaken.id }
        assertTrue(stillTaken.isTakenToday)
        assertEquals(initialStreak + 1, stillTaken.streakDays)
    }
}
