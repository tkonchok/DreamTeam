package com.example.dreamteam

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun RouletteScreen(
    onPlayersWon: (List<SoccerPlayer>) -> Unit, 
    onPlayerSold: (SoccerPlayer) -> Unit,
    onBack: () -> Unit,
    isFirstDraw: Boolean = true,
    pack: Pack = Pack(
        id = "FREE", 
        name = "Standard", 
        description = "", 
        costEnergy = 1, 
        minRating = 50, 
        maxRating = 95,
        odds = mapOf("Black" to "5%", "Gold" to "10%", "Silver" to "25%", "Bronze" to "30%", "White" to "30%"),
        bestPlayers = listOf("L. Messi", "C. Ronaldo", "K. Mbappe")
    ),
    playerDao: PlayerDao
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentState by remember { mutableStateOf("spinning") } 
    val wonPlayersList = remember { mutableStateListOf<SoccerPlayer>() }
    var isZapVisible by remember { mutableStateOf(false) }
    var sellSuccessMessage by remember { mutableStateOf<String?>(null) }

    val config = LocalConfiguration.current
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
    val ballSize = if (isLandscape) 65.dp else 80.dp
    val ballSizePx = with(androidx.compose.ui.platform.LocalDensity.current) { ballSize.toPx() }
    val spacingPx = ballSizePx * 1.3f
    val scrollOffset = remember { Animatable(0f) }
    val totalBallsCount = 2000 

    val displayBalls = remember(pack) {
        val list = List(totalBallsCount) {
            val chance = Random.nextInt(100)
            when {
                pack.minRating >= 80 -> if (chance < 20) BallType.BLACK else BallType.GOLD
                pack.minRating >= 70 -> if (chance < 10) BallType.BLACK else if (chance < 40) BallType.GOLD else BallType.SILVER
                else -> when (chance) {
                    in 0..4 -> BallType.BLACK
                    in 5..14 -> BallType.GOLD
                    in 15..34 -> BallType.SILVER
                    in 35..64 -> BallType.BRONZE
                    else -> BallType.WHITE
                }
            }
        }
        mutableStateListOf<BallType>().apply { addAll(list) }
    }

    LaunchedEffect(currentState) {
        if (currentState == "spinning") {
            scrollOffset.animateTo(
                targetValue = -(totalBallsCount * spacingPx),
                animationSpec = tween(300000, easing = LinearEasing)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = if (isLandscape) R.drawable.landscape else R.drawable.portrait),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

        if (currentState != "finished" || (wonPlayersList.isEmpty() && sellSuccessMessage == null)) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val containerWidthPx = constraints.maxWidth.toFloat()
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = currentState == "spinning") {
                            scope.launch {
                                val currentVelocity = scrollOffset.velocity
                                val currentScroll = scrollOffset.value
                                currentState = "slowing"
                                
                                // Resume main background music on stop
                                SoundManager.startPlaylist(context)
                                
                                val mainWinner = if (isFirstDraw) {
                                    playerDao.getRandomPlayer(90, 99)?.toSoccerPlayer() 
                                        ?: PlayerGenerator.generateRealLifePlayer(91, 91)
                                } else {
                                    playerDao.getRandomPlayer(pack.minRating, pack.maxRating)?.toSoccerPlayer()
                                        ?: PlayerGenerator.generateRealLifePlayer(pack.minRating, pack.maxRating)
                                }

                                val stoppingDistance = -currentVelocity * 2.0f 
                                val stopOffset = currentScroll - stoppingDistance
                                val targetIndex = (-stopOffset / spacingPx).roundToInt().coerceIn(0, totalBallsCount - 5)
                                displayBalls[targetIndex] = mainWinner.ballType
                                val finalTargetOffset = -(targetIndex * spacingPx - (containerWidthPx / 2) + (ballSizePx / 2))

                                val animJob = launch {
                                    scrollOffset.animateTo(
                                        targetValue = finalTargetOffset, 
                                        initialVelocity = currentVelocity,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 6f)
                                    )
                                    SoundManager.playSound("stop")
                                }

                                launch {
                                    val team = mutableListOf<SoccerPlayer>()
                                    team.add(mainWinner)
                                    if (isFirstDraw) {
                                        team.addAll(PlayerGenerator.generateDefaultTeam(excludeIds = setOf(mainWinner.id), excludeNames = setOf(mainWinner.name)))
                                    }

                                    val finalTeam = team.map { p ->
                                        async {
                                            try {
                                                val response = RetrofitClient.api.searchPlayer(p.name)
                                                val apiPlayer = response.player?.firstOrNull()
                                                if (apiPlayer != null) {
                                                    p.copy(imageUrl = apiPlayer.strCutout ?: apiPlayer.strThumb ?: "")
                                                } else { p.copy(imageUrl = "") }
                                            } catch (e: Exception) { p.copy(imageUrl = "") }
                                        }
                                    }.awaitAll()

                                    animJob.join() 
                                    
                                    if (mainWinner.ballType == BallType.BLACK) {
                                        SoundManager.playSound("zap")
                                        repeat(3) {
                                            isZapVisible = true
                                            delay(100)
                                            isZapVisible = false
                                            delay(100)
                                        }
                                    }
                                    
                                    wonPlayersList.addAll(finalTeam)
                                    currentState = "finished"
                                }
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (currentState == "spinning") {
                            Text("TAP TO STOP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                        
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                displayBalls.forEachIndexed { index, ballType ->
                                    val xPos = scrollOffset.value + (index * spacingPx)
                                    if (xPos > -spacingPx && xPos < containerWidthPx + spacingPx) {
                                        Image(
                                            painter = painterResource(id = ballType.getImageRes()),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(ballSize)
                                                .graphicsLayer { translationX = xPos }
                                                .align(Alignment.CenterStart)
                                        )
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(if (isLandscape) 85.dp else 100.dp)
                                    .border(4.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            )
                        }
                    }
                }
            }
        } else if (wonPlayersList.isNotEmpty()) {
            WinningView(
                players = wonPlayersList, 
                onCollect = { 
                    SoundManager.playSound("click")
                    onPlayersWon(wonPlayersList.toList())
                    wonPlayersList.clear()
                }, 
                onSell = { player ->
                    SoundManager.playSound("click")
                    val value = player.marketValue
                    onPlayerSold(player)
                    wonPlayersList.remove(player)
                    sellSuccessMessage = "Sold ${player.name} for $value coins!"
                },
                isFirstDraw = isFirstDraw
            )
        }

        if (sellSuccessMessage != null) {
            AlertDialog(
                onDismissRequest = { 
                    sellSuccessMessage = null
                    if (wonPlayersList.isEmpty()) onBack()
                },
                title = { Text("Player Sold") },
                text = { Text(sellSuccessMessage!!) },
                confirmButton = {
                    TextButton(onClick = { 
                        sellSuccessMessage = null
                        if (wonPlayersList.isEmpty()) onBack()
                    }) { Text("OK") }
                }
            )
        }

        if (isZapVisible) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.7f)))
        }
    }
}

@Composable
fun WinningView(
    players: List<SoccerPlayer>, 
    onCollect: () -> Unit,
    onSell: (SoccerPlayer) -> Unit,
    isFirstDraw: Boolean
) {
    var currentIndex by remember { mutableStateOf(0) }
    val lazyListState = rememberLazyListState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Surface(color = Color.Black.copy(alpha = 0.9f), modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (players.size > 1) "SQUAD SIGNED!" else "NEW SIGNING!", 
                color = Color.White, 
                fontSize = if (isLandscape) 24.sp else 32.sp, 
                fontWeight = FontWeight.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (players.size > 1) {
                Box(modifier = Modifier.height(if (isLandscape) 260.dp else 350.dp).fillMaxWidth()) {
                    LazyRow(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = (configuration.screenWidthDp.dp - (if (isLandscape) 180.dp else 200.dp).value.dp) / 2),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        flingBehavior = rememberSnapFlingBehavior(lazyListState)
                    ) {
                        items(players, key = { it.id }) { player ->
                            PlayerCard(
                                player = player, 
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(if (isLandscape) 180.dp else 200.dp)
                            )
                        }
                    }
                }
                
                LaunchedEffect(lazyListState.firstVisibleItemIndex) {
                    if (players.isNotEmpty()) {
                        currentIndex = lazyListState.firstVisibleItemIndex.coerceIn(0, players.size - 1)
                    }
                }

                Text(
                    text = "(${currentIndex + 1}/${players.size}) Swipe to view",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                PlayerCard(player = players[0], modifier = Modifier.height(if (isLandscape) 260.dp else 300.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            val safeIndex = currentIndex.coerceIn(0, players.size.minus(1).coerceAtLeast(0))
            val currentPlayer = if (players.isNotEmpty()) players[safeIndex] else null
            
            if (currentPlayer != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    Button(
                        onClick = { onCollect() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(150.dp).height(42.dp)
                    ) {
                        Text(if (players.size > 1) "COLLECT ALL" else "COLLECT", fontWeight = FontWeight.Bold)
                    }

                    if (!isFirstDraw) {
                        OutlinedButton(
                            onClick = { onSell(currentPlayer) },
                            border = BorderStroke(2.dp, Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.width(150.dp).height(42.dp)
                        ) {
                            Text("SELL: ${currentPlayer.marketValue}", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
