package com.example.dreamteam

/**
 * Represents a formation layout.
 * Each position has a name and a relative (x, y) coordinate on the field (0.0 to 1.0).
 */
data class FormationPosition(
    val id: String,
    val label: String,
    val x: Float, // 0.0 (left) to 1.0 (right)
    val y: Float  // 0.0 (top/attack) to 1.0 (bottom/defense)
)

data class Formation(
    val name: String,
    val positions: List<FormationPosition>
)

object Formations {
    val FourFourTwo = Formation(
        name = "4-4-2",
        positions = listOf(
            FormationPosition("GK", "GK", 0.5f, 0.9f),
            FormationPosition("LB", "LB", 0.15f, 0.75f),
            FormationPosition("CB1", "CB", 0.38f, 0.75f),
            FormationPosition("CB2", "CB", 0.62f, 0.75f),
            FormationPosition("RB", "RB", 0.85f, 0.75f),
            FormationPosition("LM", "LM", 0.15f, 0.45f),
            FormationPosition("CM1", "CM", 0.38f, 0.45f),
            FormationPosition("CM2", "CM", 0.62f, 0.45f),
            FormationPosition("RM", "RM", 0.85f, 0.45f),
            FormationPosition("ST1", "ST", 0.35f, 0.2f),
            FormationPosition("ST2", "ST", 0.65f, 0.2f)
        )
    )

    val FourThreeThree = Formation(
        name = "4-3-3",
        positions = listOf(
            FormationPosition("GK", "GK", 0.5f, 0.9f),
            FormationPosition("LB", "LB", 0.15f, 0.75f),
            FormationPosition("CB1", "CB", 0.38f, 0.75f),
            FormationPosition("CB2", "CB", 0.62f, 0.75f),
            FormationPosition("RB", "RB", 0.85f, 0.75f),
            FormationPosition("CM1", "CM", 0.25f, 0.5f),
            FormationPosition("CM2", "CM", 0.5f, 0.5f),
            FormationPosition("CM3", "CM", 0.75f, 0.5f),
            FormationPosition("LW", "LW", 0.2f, 0.25f),
            FormationPosition("ST", "ST", 0.5f, 0.2f),
            FormationPosition("RW", "RW", 0.8f, 0.25f)
        )
    )

    val ThreeFiveTwo = Formation(
        name = "3-5-2",
        positions = listOf(
            FormationPosition("GK", "GK", 0.5f, 0.9f),
            FormationPosition("CB1", "CB", 0.25f, 0.75f),
            FormationPosition("CB2", "CB", 0.5f, 0.75f),
            FormationPosition("CB3", "CB", 0.75f, 0.75f),
            FormationPosition("LWB", "LWB", 0.15f, 0.55f),
            FormationPosition("RWB", "RWB", 0.85f, 0.55f),
            FormationPosition("CM1", "CM", 0.3f, 0.45f),
            FormationPosition("CM2", "CDM", 0.5f, 0.45f),
            FormationPosition("CM3", "CM", 0.7f, 0.45f),
            FormationPosition("ST1", "ST", 0.35f, 0.2f),
            FormationPosition("ST2", "ST", 0.65f, 0.2f)
        )
    )

    val FourTwoThreeOne = Formation(
        name = "4-2-3-1",
        positions = listOf(
            FormationPosition("GK", "GK", 0.5f, 0.9f),
            FormationPosition("LB", "LB", 0.15f, 0.75f),
            FormationPosition("CB1", "CB", 0.38f, 0.75f),
            FormationPosition("CB2", "CB", 0.62f, 0.75f),
            FormationPosition("RB", "RB", 0.85f, 0.75f),
            FormationPosition("CDM1", "CDM", 0.35f, 0.55f),
            FormationPosition("CDM2", "CDM", 0.65f, 0.55f),
            FormationPosition("LM", "LM", 0.15f, 0.35f),
            FormationPosition("CAM", "CAM", 0.5f, 0.35f),
            FormationPosition("RM", "RM", 0.85f, 0.35f),
            FormationPosition("ST", "ST", 0.5f, 0.15f)
        )
    )

    val FiveThreeTwo = Formation(
        name = "5-3-2",
        positions = listOf(
            FormationPosition("GK", "GK", 0.5f, 0.9f),
            FormationPosition("LWB", "LWB", 0.15f, 0.65f),
            FormationPosition("CB1", "CB", 0.33f, 0.78f),
            FormationPosition("CB2", "CB", 0.5f, 0.78f),
            FormationPosition("CB3", "CB", 0.67f, 0.78f),
            FormationPosition("RWB", "RWB", 0.85f, 0.65f),
            FormationPosition("CM1", "CM", 0.25f, 0.45f),
            FormationPosition("CM2", "CM", 0.5f, 0.45f),
            FormationPosition("CM3", "CM", 0.75f, 0.45f),
            FormationPosition("ST1", "ST", 0.35f, 0.2f),
            FormationPosition("ST2", "ST", 0.65f, 0.2f)
        )
    )

    val ThreeFourThree = Formation(
        name = "3-4-3",
        positions = listOf(
            FormationPosition("GK", "GK", 0.5f, 0.9f),
            FormationPosition("CB1", "CB", 0.25f, 0.75f),
            FormationPosition("CB2", "CB", 0.5f, 0.75f),
            FormationPosition("CB3", "CB", 0.75f, 0.75f),
            FormationPosition("LM", "LM", 0.15f, 0.5f),
            FormationPosition("CM1", "CM", 0.38f, 0.5f),
            FormationPosition("CM2", "CM", 0.62f, 0.5f),
            FormationPosition("RM", "RM", 0.85f, 0.5f),
            FormationPosition("LW", "LW", 0.2f, 0.25f),
            FormationPosition("ST", "ST", 0.5f, 0.2f),
            FormationPosition("RW", "RW", 0.8f, 0.25f)
        )
    )

    val AllFormations = listOf(FourFourTwo, FourThreeThree, ThreeFiveTwo, FourTwoThreeOne, FiveThreeTwo, ThreeFourThree)
}

/**
 * A Squad maps a position ID to a specific Player.
 */
data class Squad(
    val name: String,
    val formation: Formation = Formations.FourFourTwo,
    val players: Map<String, SoccerPlayer?> = emptyMap()
)
