package com.cricscore.app.core.util

import com.cricscore.app.domain.model.BallType

object OversHelper {

    fun isLegalBall(ballType: BallType): Boolean {
        return ballType != BallType.WIDE && ballType != BallType.NO_BALL
    }

    /**
     * Determines if the batsmen switch strike based on runs scored or run on the delivery.
     * Note: This does NOT include end of over strike rotation.
     */
    fun shouldSwitchStrike(runsBatsman: Int, runsExtra: Int, ballType: BallType): Boolean {
        return when (ballType) {
            BallType.NORMAL -> {
                runsBatsman % 2 != 0
            }
            BallType.BYE, BallType.LEG_BYE -> {
                runsExtra % 2 != 0
            }
            BallType.WIDE -> {
                // Wide penalty is 1 run. Any additional runs are byes they ran.
                val runsRan = runsExtra - 1
                runsRan > 0 && runsRan % 2 != 0
            }
            BallType.NO_BALL -> {
                // No ball penalty is 1 run. Runs can come from batsman hitting (runsBatsman)
                // or running byes (runsExtra - 1).
                val runsRan = (runsExtra - 1).coerceAtLeast(0)
                (runsBatsman + runsRan) % 2 != 0
            }
        }
    }
}
