package com.cricscore.app.core.util

import com.cricscore.app.domain.model.BallType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OversHelperTest {

    @Test
    fun testIsLegalBall() {
        assertTrue(OversHelper.isLegalBall(BallType.NORMAL))
        assertTrue(OversHelper.isLegalBall(BallType.BYE))
        assertTrue(OversHelper.isLegalBall(BallType.LEG_BYE))
        assertFalse(OversHelper.isLegalBall(BallType.WIDE))
        assertFalse(OversHelper.isLegalBall(BallType.NO_BALL))
    }

    @Test
    fun testShouldSwitchStrikeNormal() {
        assertFalse(OversHelper.shouldSwitchStrike(0, 0, BallType.NORMAL))
        assertTrue(OversHelper.shouldSwitchStrike(1, 0, BallType.NORMAL))
        assertFalse(OversHelper.shouldSwitchStrike(2, 0, BallType.NORMAL))
        assertTrue(OversHelper.shouldSwitchStrike(3, 0, BallType.NORMAL))
        assertFalse(OversHelper.shouldSwitchStrike(4, 0, BallType.NORMAL))
        assertTrue(OversHelper.shouldSwitchStrike(5, 0, BallType.NORMAL))
        assertFalse(OversHelper.shouldSwitchStrike(6, 0, BallType.NORMAL))
    }

    @Test
    fun testShouldSwitchStrikeWide() {
        // Wide penalty is 1. If runsExtra = 1, runsRan = 0 (no strike switch)
        assertFalse(OversHelper.shouldSwitchStrike(0, 1, BallType.WIDE))
        // If runsExtra = 2, runsRan = 1 (strike switch)
        assertTrue(OversHelper.shouldSwitchStrike(0, 2, BallType.WIDE))
        // If runsExtra = 3, runsRan = 2 (no strike switch)
        assertFalse(OversHelper.shouldSwitchStrike(0, 3, BallType.WIDE))
    }

    @Test
    fun testShouldSwitchStrikeNoBall() {
        // No ball penalty is 1. If runsBatsman = 1, runsExtra = 1 (strike switch)
        assertTrue(OversHelper.shouldSwitchStrike(1, 1, BallType.NO_BALL))
        // If runsBatsman = 0, runsExtra = 2 (strike switch because runsExtra - 1 = 1)
        assertTrue(OversHelper.shouldSwitchStrike(0, 2, BallType.NO_BALL))
        // If runsBatsman = 0, runsExtra = 1 (no strike switch)
        assertFalse(OversHelper.shouldSwitchStrike(0, 1, BallType.NO_BALL))
    }
}
