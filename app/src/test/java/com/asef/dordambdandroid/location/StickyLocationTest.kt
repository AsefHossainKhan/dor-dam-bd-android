package com.asef.dordambdandroid.location

import com.asef.dordambdandroid.data.remote.models.places.PlacePathStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// SPEC: spec-5f5a30 — carried over for 2 hours or until ~1 km of movement.
class StickyLocationTest {

    private val savedAt = 1_000_000_000L
    private val shop = GeoPoint(23.7510, 90.3935)
    private val sticky = StickyLocation(
        location = ChosenLocation(listOf(PlacePathStep(3, "Karwan Bazar", "market")), source = LocationSource.SUGGESTED),
        savedAtMs = savedAt,
        savedFix = shop,
    )
    private val minute = 60_000L

    @Test
    fun withinTwoHoursAndNearby_isPrefilled() {
        assertTrue(sticky.isValid(savedAt + 119 * minute, GeoPoint(23.7540, 90.3935))) // ~330 m
    }

    @Test
    fun afterTwoHours_expires() {
        assertFalse(sticky.isValid(savedAt + 121 * minute, shop))
    }

    @Test
    fun movedMoreThanAboutOneKm_expires() {
        assertFalse(sticky.isValid(savedAt + 10 * minute, GeoPoint(23.7620, 90.3935))) // ~1.2 km
    }

    @Test
    fun withoutACurrentFix_onlyTheTimeLimitApplies() {
        assertTrue(sticky.isValid(savedAt + 10 * minute, null))
    }

    @Test
    fun aClockGoingBackwards_expires() {
        assertFalse(sticky.isValid(savedAt - minute, shop))
    }

    @Test
    fun carriedOver_isMarkedAuto() {
        assertEquals(LocationSource.AUTO, sticky.carriedOver().source)
        assertEquals(sticky.location.path, sticky.carriedOver().path)
    }

    @Test
    fun distance_isRoughlyRight() {
        val d = distanceMetres(GeoPoint(23.7510, 90.3935), GeoPoint(23.7600, 90.3935))
        assertEquals(1000.0, d, 10.0)
    }
}
