package com.example.dreamteam

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random

enum class MatchMomentType(val statName: String, val displayName: String) {
    MIDFIELD("PAS", "Round of Midfield Battle"),
    SUDDEN_ATTACK("DRI", "Round of Sudden Attack"),
    DEFENSIVE_STAND("DEF", "Round of Defensive Stand"),
    WING_SPRINT("PAC", "Round of Wing Sprint"),
    FINAL_SHOT("SHO", "Round of Final Strike")
}

data class MatchDuelResult(
    val playerValue: Int,
    val opponentValue: Int,
    val playerWon: Boolean,
    val playerPlayer: SoccerPlayer,
    val opponentPlayer: SoccerPlayer
)

class MatchState(
    val playerSquad: Squad,
    val opponentSquad: Squad
) {
    var currentMomentIndex by mutableStateOf(0)
    var playerGoals by mutableStateOf(0)
    var opponentGoals by mutableStateOf(0)
    var isFinished by mutableStateOf(false)
    
    val usedPlayerIds = mutableStateListOf<Int>()
    val usedOpponentIds = mutableStateListOf<Int>()
    val duelResults = mutableStateListOf<MatchDuelResult>()

    private val moments = listOf(
        MatchMomentType.MIDFIELD,
        MatchMomentType.SUDDEN_ATTACK,
        MatchMomentType.DEFENSIVE_STAND,
        MatchMomentType.WING_SPRINT,
        MatchMomentType.FINAL_SHOT
    )

    fun getCurrentMoment() = moments.getOrNull(currentMomentIndex)

    fun processTurn(playerSelected: SoccerPlayer) {
        val moment = getCurrentMoment() ?: return
        
        val opponentAvailable = opponentSquad.players.values.filterNotNull()
            .filter { !usedOpponentIds.contains(it.id) }
        
        val opponentSelected = opponentAvailable.maxByOrNull { getStatValue(it, moment) } ?: opponentAvailable.random()
        
        val pStat = getStatValue(playerSelected, moment)
        val oStat = getStatValue(opponentSelected, moment)
        
        val pRoll = pStat + Random.nextInt(1, 15)
        val oRoll = oStat + Random.nextInt(1, 15)
        
        val playerWon = pRoll >= oRoll
        if (playerWon) playerGoals++ else opponentGoals++
        
        duelResults.add(MatchDuelResult(pRoll, oRoll, playerWon, playerSelected, opponentSelected))
        usedPlayerIds.add(playerSelected.id)
        usedOpponentIds.add(opponentSelected.id)
        
        if (currentMomentIndex >= moments.size - 1) {
            isFinished = true
        } else {
            currentMomentIndex++
        }
    }

    private fun getStatValue(player: SoccerPlayer, moment: MatchMomentType): Int {
        return when (moment) {
            MatchMomentType.MIDFIELD -> player.passing
            MatchMomentType.SUDDEN_ATTACK -> player.dribbling
            MatchMomentType.DEFENSIVE_STAND -> player.defending
            MatchMomentType.WING_SPRINT -> player.pace
            MatchMomentType.FINAL_SHOT -> player.shooting
        }
    }
}
