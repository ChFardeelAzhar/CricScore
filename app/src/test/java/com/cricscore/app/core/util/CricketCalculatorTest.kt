package com.cricscore.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CricketCalculatorTest {

    @Test
    fun testBallsToOversString() {
        assertEquals("0.0", CricketCalculator.ballsToOversString(0))
        assertEquals("0.1", CricketCalculator.ballsToOversString(1))
        assertEquals("0.5", CricketCalculator.ballsToOversString(5))
        assertEquals("1.0", CricketCalculator.ballsToOversString(6))
        assertEquals("1.1", CricketCalculator.ballsToOversString(7))
        assertEquals("2.4", CricketCalculator.ballsToOversString(16))
    }

    @Test
    fun testOversStringToBalls() {
        assertEquals(0, CricketCalculator.oversStringToBalls("0"))
        assertEquals(0, CricketCalculator.oversStringToBalls("0.0"))
        assertEquals(1, CricketCalculator.oversStringToBalls("0.1"))
        assertEquals(5, CricketCalculator.oversStringToBalls("0.5"))
        assertEquals(6, CricketCalculator.oversStringToBalls("1.0"))
        assertEquals(7, CricketCalculator.oversStringToBalls("1.1"))
        assertEquals(16, CricketCalculator.oversStringToBalls("2.4"))
        assertEquals(30, CricketCalculator.oversStringToBalls("5"))
    }

    @Test
    fun testCalculateEconomyRate() {
        assertEquals(0f, CricketCalculator.calculateEconomyRate(0, 0), 0.001f)
        assertEquals(6.0f, CricketCalculator.calculateEconomyRate(24, 24), 0.001f)
        assertEquals(8.25f, CricketCalculator.calculateEconomyRate(33, 24), 0.001f)
        assertEquals(9.0f, CricketCalculator.calculateEconomyRate(15, 10), 0.001f) // 1.4 overs = 10 balls
    }

    @Test
    fun testCalculateStrikeRate() {
        assertEquals(0f, CricketCalculator.calculateStrikeRate(0, 0), 0.001f)
        assertEquals(100f, CricketCalculator.calculateStrikeRate(10, 10), 0.001f)
        assertEquals(150f, CricketCalculator.calculateStrikeRate(30, 20), 0.001f)
        assertEquals(200f, CricketCalculator.calculateStrikeRate(12, 6), 0.001f)
    }

    @Test
    fun testCalculateRunRate() {
        assertEquals(0f, CricketCalculator.calculateRunRate(0, 0), 0.001f)
        assertEquals(6.0f, CricketCalculator.calculateRunRate(36, 36), 0.001f)
        assertEquals(10.0f, CricketCalculator.calculateRunRate(10, 6), 0.001f)
    }
}
