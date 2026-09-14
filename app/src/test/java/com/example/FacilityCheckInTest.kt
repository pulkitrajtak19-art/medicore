package com.example

import com.example.data.repository.HealthRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FacilityCheckInTest {

    private lateinit var repository: HealthRepository

    @Before
    fun setup() {
        repository = HealthRepository()
    }

    @Test
    fun `patient check-in creates active check-in and queue token`() {
        assertNull(repository.activeCheckIn.value)
        val initialHistoryCount = repository.checkInsHistory.value.size

        val checkIn = repository.performFacilityCheckIn(
            facilityId = "FAC-AIIMS-DELHI",
            facilityName = "AIIMS New Delhi - Main Hospital",
            department = "Cardiology & General Medicine OPD",
            counterNumber = "OPD Registration Counter 04",
            rawQr = "ABDM://FACILITY?id=FAC-AIIMS-DELHI&dept=Cardiology"
        )

        assertNotNull(checkIn)
        assertNotNull(repository.activeCheckIn.value)
        assertEquals("FAC-AIIMS-DELHI", repository.activeCheckIn.value?.facilityId)
        assertTrue(checkIn.tokenNumber.startsWith("OPD-"))
        assertEquals("CONFIRMED", checkIn.status)
        assertEquals(initialHistoryCount + 1, repository.checkInsHistory.value.size)
    }

    @Test
    fun `patient checkout closes active check-in session`() {
        repository.performFacilityCheckIn(
            facilityId = "FAC-APOLLO-BLR",
            facilityName = "Apollo Hospital Bangalore",
            department = "General Medicine",
            counterNumber = "Desk 02",
            rawQr = "ABDM://FACILITY?id=FAC-APOLLO-BLR"
        )

        assertNotNull(repository.activeCheckIn.value)

        repository.checkoutFacility()

        assertNull(repository.activeCheckIn.value)
    }
}
