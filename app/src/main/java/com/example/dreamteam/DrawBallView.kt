package com.example.dreamteam

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration

data class Pack(
    val id: String,
    val name: String,
    val description: String,
    val costCoins: Int = 0,
    val costEnergy: Int = 0,
    val minRating: Int,
    val maxRating: Int,
    val odds: Map<String, String>,
    val bestPlayers: List<String>,
    val specificPool: List<String>? = null
)

@Composable
fun DrawBallView(
    playerProfile: PlayerProfile,
    onBack: () -> Unit,
    onDrawComplete: (SoccerPlayer) -> Unit,
    onSpendCoins: (Int) -> Unit,
    onSpendEnergy: (Int) -> Unit,
    playerDao: PlayerDao
) {
    var isRouletteActive by remember { mutableStateOf(false) }
    var wonPlayers by remember { mutableStateOf<List<SoccerPlayer>>(emptyList()) }
    var selectedPack by remember { mutableStateOf<Pack?>(null) }
    var showPackInfo by remember { mutableStateOf<Pack?>(null) }
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val packs = listOf(
        Pack(
            "FREE", "Standard Pack", "All players included.", 
            costEnergy = 1, minRating = 50, maxRating = 95,
            odds = mapOf("Black" to "5%", "Gold" to "10%", "Silver" to "25%", "Bronze" to "30%", "White" to "30%"),
            bestPlayers = listOf("L. Messi", "C. Ronaldo", "K. Mbappe")
        ),
        Pack(
            "ARGENTINA", "Albiceleste Pack", "Top Argentinian stars.", 
            costCoins = 1000, minRating = 79, maxRating = 95,
            odds = mapOf("Black" to "15%", "Gold" to "35%", "Silver" to "50%"),
            bestPlayers = listOf("L. Messi", "Lautaro Martinez", "E. Fernandez"),
            specificPool = listOf("Lionel Messi", "Angel Di Maria", "Lautaro Martinez", "Enzo Fernandez", "Julian Alvarez")
        ),
        Pack(
            "MADRID", "Galacticos Pack", "Real Madrid's finest.", 
            costCoins = 1500, minRating = 82, maxRating = 95,
            odds = mapOf("Black" to "25%", "Gold" to "75%"),
            bestPlayers = listOf("Vinicius Jr", "J. Bellingham", "K. Mbappe"),
            specificPool = listOf("Vinicius Junior", "Jude Bellingham", "Kylian Mbappe", "Federico Valverde", "Rodrygo")
        ),
        Pack(
            "LEGENDS", "Elite Pack", "The best of the best.", 
            costCoins = 2500, minRating = 85, maxRating = 99,
            odds = mapOf("Black" to "100%"),
            bestPlayers = listOf("Rodri", "E. Haaland", "K. De Bruyne")
        )
    )

    if (isRouletteActive && selectedPack != null) {
        RouletteScreen(
            onPlayersWon = { players ->
                wonPlayers = players
                isRouletteActive = false
            },
            onPlayerSold = { soldPlayer ->
                onSpendCoins(-soldPlayer.marketValue)
                wonPlayers = wonPlayers.filter { it.id != soldPlayer.id }
            },
            onBack = onBack,
            isFirstDraw = false,
            pack = selectedPack!!,
            playerDao = playerDao
        )
    } else if (wonPlayers.isNotEmpty()) {
        WinningView(
            players = wonPlayers,
            onCollect = {
                wonPlayers.forEach { onDrawComplete(it) }
                wonPlayers = emptyList()
                onBack()
            },
            onSell = { soldPlayer ->
                onSpendCoins(-soldPlayer.marketValue)
                wonPlayers = wonPlayers.filter { it.id != soldPlayer.id }
                if (wonPlayers.isEmpty()) {
                    onBack()
                }
            },
            isFirstDraw = false
        )
    } else {
        val pagerState = rememberPagerState(pageCount = { packs.size })

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = if (isLandscape) 16.dp else 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${playerProfile.coins} ", color = Color(0xFFFFC107), fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text("Coins", color = Color.White, fontSize = 12.sp)
                        }
                        EnergyBarWithTimer(playerProfile, isLandscape)
                    }
                }

                Text(
                    "CHOOSE YOUR PACK",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = if (isLandscape) 22.sp else 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 20.dp))

                Box(modifier = Modifier.height(if (isLandscape) 220.dp else 450.dp).fillMaxWidth()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = if (isLandscape) 150.dp else 48.dp),
                        pageSpacing = 16.dp
                    ) { page ->
                        PackCard(
                            pack = packs[page],
                            canAfford = if (packs[page].costEnergy > 0) playerProfile.energy >= packs[page].costEnergy else playerProfile.coins >= packs[page].costCoins,
                            isLandscape = isLandscape,
                            onOpen = {
                                if (packs[page].costEnergy > 0) onSpendEnergy(packs[page].costEnergy) else onSpendCoins(packs[page].costCoins)
                                selectedPack = packs[page]
                                isRouletteActive = true
                            },
                            onInfo = { showPackInfo = packs[page] }
                        )
                    }
                }
            }
        }

        if (showPackInfo != null) {
            PackInfoDialog(pack = showPackInfo!!, onDismiss = { showPackInfo = null })
        }
    }
}

@Composable
fun EnergyBarWithTimer(profile: PlayerProfile, isLandscape: Boolean) {
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Energy: ", color = Color.White, fontSize = if (isLandscape) 12.sp else 14.sp)
            repeat(PlayerProfile.MAX_ENERGY) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 1.dp)
                        .size(width = if (isLandscape) 12.dp else 20.dp, height = if (isLandscape) 6.dp else 10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (index < profile.energy) Color(0xFF00FF00) else Color.DarkGray)
                )
            }
        }
        if (profile.energy < PlayerProfile.MAX_ENERGY) {
            Text(
                text = "Next: ${profile.formatRemainingTime()}",
                color = Color.Gray,
                fontSize = if (isLandscape) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PackCard(pack: Pack, canAfford: Boolean, isLandscape: Boolean, onOpen: () -> Unit, onInfo: () -> Unit) {
    Surface(
        color = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, if (canAfford) Color.White.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box {
            IconButton(onClick = onInfo, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White.copy(alpha = 0.7f))
            }
            
            Column(
                modifier = Modifier.padding(if (isLandscape) 12.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(if (isLandscape) 60.dp else 120.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(pack.name.take(1), fontSize = if (isLandscape) 30.sp else 60.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 24.dp))
                
                Text(pack.name, fontSize = if (isLandscape) 18.sp else 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                
                Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 32.dp))
                
                val priceLabel = if (pack.costEnergy > 0) "${pack.costEnergy} Energy" else "${pack.costCoins} Coins"
                
                Button(
                    onClick = onOpen,
                    enabled = canAfford,
                    modifier = Modifier.fillMaxWidth().height(if (isLandscape) 40.dp else 56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (canAfford) Color.White else Color.DarkGray)
                ) {
                    Text(if (canAfford) "OPEN ($priceLabel)" else "LOCKED", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = if (isLandscape) 12.sp else 14.sp)
                }
            }
        }
    }
}

@Composable
fun PackInfoDialog(pack: Pack, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(pack.name, fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("PROBABILITIES", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                pack.odds.forEach { (tier, chance) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(tier, fontSize = 14.sp)
                        Text(chance, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("TOP PLAYERS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                pack.bestPlayers.forEach { name ->
                    Text("• $name", fontSize = 14.sp, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE") }
        }
    )
}
