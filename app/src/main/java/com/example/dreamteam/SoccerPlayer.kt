package com.example.dreamteam
import kotlin.random.Random

enum class BallType {
    BLACK, GOLD, SILVER, BRONZE, WHITE;

    fun getImageRes(): Int {
        return when (this) {
            BLACK -> R.drawable.ball_black
            GOLD -> R.drawable.ball_gold
            SILVER -> R.drawable.ball_silver
            BRONZE -> R.drawable.ball_bronze
            WHITE -> R.drawable.ball_white
        }
    }
}

data class SoccerPlayer(
    val id: Int,
    val name: String,
    val rating: Int,
    val position: String,
    val club: String,
    val league: String,
    val country: String,
    val imageUrl: String,
    val pace: Int,
    val shooting: Int,
    val passing: Int,
    val dribbling: Int,
    val defending: Int,
    val physical: Int
) {
    val ballType: BallType
        get() = when {
            rating >= 83 -> BallType.BLACK
            rating >= 79 -> BallType.GOLD
            rating >= 70 -> BallType.SILVER
            rating >= 60 -> BallType.BRONZE
            else -> BallType.WHITE
        }

    val marketValue: Int
        get() {
            val base = (rating * rating) / 2
            return when (ballType) {
                BallType.BLACK -> base * 10
                BallType.GOLD -> base * 5
                BallType.SILVER -> base * 2
                BallType.BRONZE -> base
                BallType.WHITE -> base / 2
            }
        }
}

object PlayerGenerator {
    private val whitePlayers = listOf(
        "K. Miura", "T. Davies", "M. Johnston", "E. Palmer-Brown", "S. Vines", "G. Bello", "J. Sands", "T. Buchanan",
        "D. Mihailovic", "K. Paredes", "C. Clark", "B. Duke", "J. Ferreira", "R. Pepi", "C. Cowell", "G. Slonina",
        "N. McGlynn", "P. Aaronson", "J. McGlynn", "K. Sullivan", "O. Wolff", "B. Cremaschi"
    )
    private val bronzePlayers = listOf(
        "L. O'Brien", "C. Richards", "M. Turner", "D. Yedlin", "K. Acosta", "S. Lletget", "P. Arriola", "G. Zardes",
        "A. Long", "W. Zimmerman", "M. Robinson", "K. Miller", "S. Moore", "K. Rosenberry", "J. Gressel", "D. Nagbe",
        "S. Ebobisse", "B. Wood", "H. Wright", "J. Sargent", "T. Boyd", "K. Lewis-Potter"
    )
    private val silverPlayers = listOf(
        "C. Pulisic", "W. McKennie", "T. Adams", "G. Reyna", "F. Balogun", "Y. Musah", "A. Robinson", "S. Dest",
        "J. Steffen", "E. Horvath", "C. Carter-Vickers", "E. Palmer-Brown", "L. de la Torre", "B. Aaronson", "M. Tillman",
        "T. Weah", "J. Morris", "B. Vazquez", "D. McGuire", "N. Gioacchini"
    )
    private val goldPlayers = listOf(
        "L. Messi", "C. Ronaldo", "K. Mbappe", "E. Haaland", "K. De Bruyne", "M. Salah", "V. Junior", "J. Bellingham", 
        "H. Kane", "R. Lewandowski", "L. Martinez", "A. Griezmann", "Neymar Jr", "H. Son", "B. Silva", "Rodri",
        "J. Musiala", "P. Foden", "L. Yamal", "X. Simons", "F. Wirtz", "A. Davies", "R. Araujo", "W. Saliba"
    )

    fun generateDefaultTeam(excludeIds: Set<Int> = emptySet(), excludeNames: Set<String> = emptySet()): List<SoccerPlayer> {
        val positions = listOf("GK", "CB", "CB", "LB", "RB", "CM", "CM", "LM", "RM", "ST", "ST")
        val team = mutableListOf<SoccerPlayer>()
        val usedNames = excludeNames.toMutableSet()
        val usedIds = excludeIds.toMutableSet()
        
        positions.forEach { pos ->
            var player: SoccerPlayer
            var attempts = 0
            do {
                player = generateRealLifePlayer(minRating = 50, maxRating = 59, pos = pos)
                attempts++
            } while ((usedIds.contains(player.id) || usedNames.contains(player.name)) && attempts < 100)
            
            team.add(player)
            usedIds.add(player.id)
            usedNames.add(player.name)
        }
        return team
    }

    fun generateRealLifePlayer(minRating: Int = 50, maxRating: Int = 99, pos: String? = null): SoccerPlayer {
        val rating = Random.nextInt(minRating, maxRating + 1)
        val pool = when {
            rating >= 83 -> goldPlayers
            rating >= 79 -> goldPlayers
            rating >= 70 -> silverPlayers
            rating >= 60 -> bronzePlayers
            else -> whitePlayers
        }
        
        val name = pool.random()
        // Stable ID based on name to help uniqueness check
        val id = Math.abs(name.hashCode() + Random.nextInt(1000000))
        
        return SoccerPlayer(
            id = id,
            name = name,
            rating = rating,
            position = pos ?: listOf("GK", "CB", "LB", "RB", "CM", "CDM", "CAM", "LM", "RM", "ST", "LW", "RW").random(),
            club = listOf("Inter Miami", "Al Nassr", "Real Madrid", "Man City", "Bayern Munich", "PSG", "Liverpool", "Arsenal", "Barcelona").random(),
            league = listOf("MLS", "Saudi Pro League", "La Liga", "Premier League", "Bundesliga", "Ligue 1", "Serie A").random(),
            country = "World",
            imageUrl = "",
            pace = Random.nextInt(rating - 15, rating + 5).coerceIn(1, 99),
            shooting = Random.nextInt(rating - 20, rating + 5).coerceIn(1, 99),
            passing = Random.nextInt(rating - 15, rating + 5).coerceIn(1, 99),
            dribbling = Random.nextInt(rating - 15, rating + 5).coerceIn(1, 99),
            defending = Random.nextInt(10, 90).coerceIn(1, 99),
            physical = Random.nextInt(rating - 15, rating + 5).coerceIn(1, 99)
        )
    }

    fun generateMarketPlayer(): SoccerPlayer = generateRealLifePlayer(60, 92)
}
