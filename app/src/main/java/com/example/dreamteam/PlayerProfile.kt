package com.example.dreamteam

/**
 * This data class represents the player's profile.
 * Energy refills every 6 minutes, taking 30 minutes to reach full capacity (5).
 */
data class PlayerProfile(
    val username: String,
    val coins: Int = 1000, 
    val hasCompletedFirstDraw: Boolean = false,
    val energy: Int = 5,
    val lastEnergyRefillTime: Long = System.currentTimeMillis()
) {
    companion object {
        const val MAX_ENERGY = 5
        // 30 minutes to get full (from 0 to 5) = 6 minutes per energy
        const val REFILL_INTERVAL_MS = 6 * 60 * 1000L 
    }

    /**
     * Calculates updated energy based on time passed.
     */
    fun getUpdatedEnergy(): Pair<Int, Long> {
        if (energy >= MAX_ENERGY) return energy to System.currentTimeMillis()
        
        val currentTime = System.currentTimeMillis()
        val timePassed = currentTime - lastEnergyRefillTime
        val refillsEarned = (timePassed / REFILL_INTERVAL_MS).toInt()
        
        if (refillsEarned > 0) {
            val newEnergy = (energy + refillsEarned).coerceAtMost(MAX_ENERGY)
            // If full, reset timer to current. If not full, advance timer by increments used.
            val newTime = if (newEnergy == MAX_ENERGY) currentTime else lastEnergyRefillTime + (refillsEarned * REFILL_INTERVAL_MS)
            return newEnergy to newTime
        }
        return energy to lastEnergyRefillTime
    }

    /**
     * Returns remaining milliseconds until the next +1 energy.
     */
    fun getTimeUntilNextRefill(): Long {
        if (energy >= MAX_ENERGY) return 0
        val currentTime = System.currentTimeMillis()
        val timePassed = currentTime - lastEnergyRefillTime
        return (REFILL_INTERVAL_MS - (timePassed % REFILL_INTERVAL_MS)).coerceAtLeast(0)
    }
    
    /**
     * Formats the countdown timer as MM:SS
     */
    fun formatRemainingTime(): String {
        val ms = getTimeUntilNextRefill()
        if (ms <= 0) return "FULL"
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
