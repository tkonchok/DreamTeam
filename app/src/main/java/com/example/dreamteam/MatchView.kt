package com.example.dreamteam

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TeamScore(name: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val displayName = if (name.isBlank()) "MY TEAM" else name
        Text(displayName.uppercase(), fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Black)
        Text(score.toString(), fontSize = 40.sp, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
fun MatchView(
    playerSquad: Squad,
    onMatchFinished: (playerGoals: Int, opponentGoals: Int) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val playersOnPitch = playerSquad.players.values.filterNotNull()
    val isSquadEmpty = playersOnPitch.isEmpty()

    val opponentSquad = remember {
        val botPlayers = PlayerGenerator.generateDefaultTeam()
        val squadMap = mutableMapOf<String, SoccerPlayer?>()
        botPlayers.forEachIndexed { index, player -> squadMap["BOT_$index"] = player }
        Squad(name = "BOT FC", players = squadMap)
    }

    val matchState = remember { MatchState(playerSquad, opponentSquad) }
    var lastDuelResult by remember { mutableStateOf<MatchDuelResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.landscape),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))

        if (isSquadEmpty) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("YOUR SQUAD IS EMPTY!", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onBack) { Text("BACK TO MENU") }
            }
        } else if (matchState.isFinished) {
            MatchResultScreen(matchState, onMatchFinished)
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                IconButton(onClick = { showExitConfirm = true }, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
                    Icon(Icons.Default.Close, null, tint = Color.White.copy(0.5f))
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // MAIN ARENA: Scores and Duel Cards
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // LEFT: User
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            TeamScore(name = playerSquad.name, score = matchState.playerGoals, color = Color.Cyan)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (lastDuelResult != null && !isProcessing) {
                                PlayerCard(player = lastDuelResult!!.playerPlayer, modifier = Modifier.size(140.dp, 210.dp), enableFullView = false)
                                Text("${lastDuelResult!!.playerValue}", color = if(lastDuelResult!!.playerWon) Color.Green else Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            } else {
                                DuelPlaceholder(isProcessing)
                            }
                        }

                        // MIDDLE: Result Info
                        Column(modifier = Modifier.width(120.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("VS", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White.copy(0.3f))
                            val currentMoment = matchState.getCurrentMoment()
                            if (currentMoment != null) {
                                Text(currentMoment.displayName, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFC107), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color(0xFFFFC107), modifier = Modifier.size(30.dp))
                            } else if (lastDuelResult != null) {
                                Text(text = if (lastDuelResult!!.playerWon) "GOAL!" else "SAVED!", fontSize = 28.sp, fontWeight = FontWeight.Black, color = if (lastDuelResult!!.playerWon) Color.Green else Color.Red)
                            }
                        }

                        // RIGHT: Opponent
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            TeamScore(name = opponentSquad.name, score = matchState.opponentGoals, color = Color.Red)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (lastDuelResult != null && !isProcessing) {
                                PlayerCard(player = lastDuelResult!!.opponentPlayer, modifier = Modifier.size(140.dp, 210.dp), enableFullView = false)
                                Text("${lastDuelResult!!.opponentValue}", color = if(!lastDuelResult!!.playerWon) Color.Red else Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            } else {
                                DuelPlaceholder(isProcessing)
                            }
                        }
                    }

                    // BOTTOM: Selection
                    val availablePlayers = playersOnPitch.filter { !matchState.usedPlayerIds.contains(it.id) }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(100.dp).padding(bottom = 4.dp)
                    ) {
                        items(availablePlayers, key = { it.id }) { player ->
                            MatchPlayerChoiceItem(player = player, onClick = {
                                if (!isProcessing) {
                                    scope.launch {
                                        isProcessing = true
                                        delay(1200)
                                        matchState.processTurn(player)
                                        lastDuelResult = matchState.duelResults.lastOrNull()
                                        isProcessing = false
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }

        if (showExitConfirm) {
            AlertDialog(
                onDismissRequest = { showExitConfirm = false },
                title = { Text("Quit Match?") },
                text = { Text("Forfeit and return to menu?") },
                confirmButton = { TextButton(onClick = onBack) { Text("QUIT", color = Color.Red) } },
                dismissButton = { TextButton(onClick = { showExitConfirm = false }) { Text("CANCEL") } }
            )
        }
    }
}

@Composable
fun DuelPlaceholder(isProcessing: Boolean) {
    Box(
        modifier = Modifier.size(140.dp, 210.dp).border(2.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp)).background(Color.White.copy(if(isProcessing) 0.1f else 0.05f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!isProcessing) Icon(Icons.Default.Person, null, tint = Color.White.copy(0.1f), modifier = Modifier.size(50.dp))
    }
}

@Composable
fun MatchPlayerChoiceItem(player: SoccerPlayer, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(90.dp).fillMaxHeight().clickable { onClick() },
        color = Color.White.copy(0.1f), shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.3f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(player.rating.toString(), fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFC107))
            Text(player.name, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MiniStat("P", player.pace); MiniStat("S", player.shooting); MiniStat("D", player.defending)
            }
        }
    }
}

@Composable
fun MiniStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 6.sp, color = Color.Gray)
        Text(value.toString(), fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MatchResultScreen(state: MatchState, onFinished: (Int, Int) -> Unit) {
    val win = state.playerGoals > state.opponentGoals
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = if(win) "VICTORY!" else "MATCH ENDED", fontSize = 48.sp, fontWeight = FontWeight.Black, color = if(win) Color.Green else Color.White)
            Text("${state.playerGoals} - ${state.opponentGoals}", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { onFinished(state.playerGoals, state.opponentGoals) }, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) {
                Text(if(win) "COLLECT 500 COINS" else "RETURN HOME", fontWeight = FontWeight.Black)
            }
        }
    }
}
