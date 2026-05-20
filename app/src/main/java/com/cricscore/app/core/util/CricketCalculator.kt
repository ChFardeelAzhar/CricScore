package com.cricscore.app.core.util

import java.util.Locale
import kotlin.math.round

object CricketCalculator {

    fun calculateStrikeRate(runs: Int, balls: Int): Float {
        if (balls <= 0) return 0.0f
        val sr = (runs.toFloat() / balls) * 100f
        return round(sr * 10f) / 10f
    }

    fun calculateEconomyRate(runsConceded: Int, ballsBowled: Int): Float {
        if (ballsBowled <= 0) return 0.00f
        val econ = (runsConceded.toFloat() / ballsBowled) * 6f
        return round(econ * 100f) / 100f
    }

    fun calculateRunRate(runs: Int, ballsBowled: Int): Float {
        if (ballsBowled <= 0) return 0.00f
        val rr = (runs.toFloat() / ballsBowled) * 6f
        return round(rr * 100f) / 100f
    }

    fun ballsToOversString(balls: Int): String {
        val completedOvers = balls / 6
        val remainingBalls = balls % 6
        return String.format(Locale.US, "%d.%d", completedOvers, remainingBalls)
    }

    fun oversStringToBalls(overs: String): Int {
        if (overs.isBlank()) return 0
        return try {
            val parts = overs.split(".")
            if (parts.size == 1) {
                parts[0].toInt() * 6
            } else {
                val completedOvers = parts[0].toInt()
                val remainingBalls = parts[1].toInt()
                (completedOvers * 6) + remainingBalls
            }
        } catch (e: Exception) {
            0
        }
    }
}
