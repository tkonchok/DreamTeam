package com.example.dreamteam

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.dreamteam.ui.theme.DreamTeamTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force Landscape and Immersive Full Screen
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()

        enableEdgeToEdge()

        setContent {
             DreamTeamTheme {
                val context = LocalContext.current
                val storage = remember { UserStorage(context) }
                val database = remember { AppDatabase.getDatabase(context) }
                val playerDao = database.playerDao()
                
                var currentScreen by remember { mutableStateOf("BOOT") }
                var playerProfile by remember { mutableStateOf(storage.getProfile()) }
                var collection by remember { mutableStateOf(storage.getCollection()) }
                var savedSquads by remember { mutableStateOf(storage.getSquads()) }
                var activeSquad by remember { mutableStateOf(storage.getSquads().firstOrNull() ?: Squad(name = "My Dream Team")) }

                // Initialize Audio
                LaunchedEffect(Unit) {
                    SoundManager.init(context)
                }

                // Manage Music State based on Screen
                LaunchedEffect(currentScreen) {
                    when (currentScreen) {
                        "ROULETTE" -> SoundManager.startDrawBallMusic(context)
                        "BOOT", "ENTER" -> SoundManager.stopBgMusic()
                        else -> SoundManager.startPlaylist(context)
                    }
                    hideSystemBars()
                }

                // Energy Real-time Refill Ticker
                LaunchedEffect(playerProfile) {
                    while (true) {
                        delay(1000) // Tick every second
                        playerProfile?.let { profile ->
                            val (newEnergy, newTime) = profile.getUpdatedEnergy()
                            if (newEnergy != profile.energy) {
                                playerProfile = profile.copy(energy = newEnergy, lastEnergyRefillTime = newTime)
                            }
                        }
                    }
                }

                // Seed database from players.json if empty
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        if (playerDao.getCount() == 0) {
                            try {
                                val jsonString = context.assets.open("players.json").bufferedReader().use { it.readText() }
                                val listType = object : TypeToken<List<PlayerEntity>>() {}.type
                                val players: List<PlayerEntity> = Gson().fromJson(jsonString, listType)
                                playerDao.insertAll(players)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                // Persistence triggers
                LaunchedEffect(playerProfile) {
                    playerProfile?.let { storage.saveProfile(it) }
                }
                LaunchedEffect(collection) {
                    storage.saveCollection(collection)
                }
                LaunchedEffect(savedSquads) {
                    storage.saveSquads(savedSquads)
                }

                val scaffoldColor = if (currentScreen == "BOOT" || currentScreen == "ROULETTE" || currentScreen == "DRAW_BALL" || currentScreen == "MARKET" || currentScreen == "COLLECTION" || currentScreen == "MATCH") Color.Black else Color.Transparent

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = scaffoldColor,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    val isFullView = currentScreen == "BOOT" || currentScreen == "ENTER" || currentScreen == "ROULETTE" || currentScreen == "DRAW_BALL" || currentScreen == "COLLECTION" || currentScreen == "MARKET" || currentScreen == "MATCH"
                    val contentModifier = if (isFullView) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.padding(innerPadding).fillMaxSize()
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (currentScreen == "ENTER") {
                            Image(
                                painter = painterResource(id = R.drawable.home_screen),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (currentScreen != "BOOT" && currentScreen != "MARKET" && currentScreen != "COLLECTION" && currentScreen != "MATCH") {
                            AppBackground()
                        }

                        Box(modifier = contentModifier) {
                            when (currentScreen) {
                                "BOOT" -> {
                                    BootScreen(onTimeout = { 
                                        currentScreen = "ENTER" 
                                    })
                                }
                                "ENTER" -> EnterGameScreen(onEnter = {
                                    SoundManager.playSound("cheer")
                                    currentScreen = if (playerProfile == null) "SETUP" else "HOME"
                                })
                                "SETUP" -> SetupScreen(onProfileCreated = { newProfile: PlayerProfile ->
                                    playerProfile = newProfile
                                    currentScreen = "ROULETTE"
                                })
                                "ROULETTE" -> RouletteScreen(
                                    onPlayersWon = { wonPlayers ->
                                        collection = (collection + wonPlayers).distinctBy { it.id }
                                        playerProfile = playerProfile?.copy(hasCompletedFirstDraw = true)
                                        currentScreen = "HOME"
                                    }, 
                                    onPlayerSold = { soldPlayer ->
                                        playerProfile = playerProfile?.copy(
                                            coins = (playerProfile?.coins ?: 0) + soldPlayer.marketValue
                                        )
                                        if (collection.isEmpty()) {
                                            currentScreen = "HOME"
                                        }
                                    },
                                    onBack = { currentScreen = "HOME" },
                                    isFirstDraw = true,
                                    playerDao = playerDao
                                )
                                "DRAW_BALL" -> playerProfile?.let { profile ->
                                    DrawBallView(
                                        playerProfile = profile,
                                        onBack = { currentScreen = "HOME" },
                                        onDrawComplete = { wonPlayer ->
                                            collection = (collection + wonPlayer).distinctBy { it.id }
                                        },
                                        onSpendCoins = { amount ->
                                            playerProfile = playerProfile?.copy(coins = (playerProfile?.coins ?: 0) - amount)
                                        },
                                        onSpendEnergy = { amount ->
                                            playerProfile = playerProfile?.copy(energy = (playerProfile?.energy ?: 0) - amount)
                                        },
                                        playerDao = playerDao
                                    )
                                }
                                "HOME" -> HomeScreen(
                                    profile = playerProfile,
                                    collectionSize = collection.size,
                                    onNavigate = { screen: String -> 
                                        SoundManager.playSound("click")
                                        currentScreen = screen 
                                    }
                                )
                                "MATCH" -> MatchView(
                                    playerSquad = activeSquad,
                                    onMatchFinished = { playerGoals, opponentGoals ->
                                        if (playerGoals > opponentGoals) {
                                            SoundManager.playSound("celebration")
                                            playerProfile = playerProfile?.copy(coins = (playerProfile?.coins ?: 0) + 500)
                                        }
                                        currentScreen = "HOME"
                                    },
                                    onBack = { currentScreen = "HOME" }
                                )
                                "SETTINGS" -> playerProfile?.let { profile ->
                                    SettingsScreen(
                                        profile = profile,
                                        onProfileUpdate = { updatedProfile: PlayerProfile -> playerProfile = updatedProfile },
                                        onBack = { currentScreen = "HOME" },
                                        onClearData = {
                                            storage.clearAll()
                                            playerProfile = null
                                            collection = emptyList()
                                            savedSquads = emptyList()
                                            currentScreen = "BOOT"
                                        }
                                    )
                                }
                                "COLLECTION" -> CollectionView(
                                    players = collection,
                                    onBack = { currentScreen = "HOME" }
                                )
                                "TEAM" -> TeamManagementView(
                                    squads = savedSquads,
                                    activeSquad = activeSquad,
                                    collection = collection,
                                    onBack = { currentScreen = "HOME" },
                                    onActiveSquadChanged = { activeSquad = it },
                                    onSquadsChanged = { savedSquads = it }
                                )
                                "MARKET" -> TransferMarketView(
                                    playerCoins = playerProfile?.coins ?: 0,
                                    collection = collection,
                                    onBack = { currentScreen = "HOME" },
                                    onBuyPlayer = { player ->
                                        if ((playerProfile?.coins ?: 0) >= player.marketValue) {
                                            playerProfile = playerProfile?.copy(coins = (playerProfile?.coins ?: 0) - player.marketValue)
                                            collection = (collection + player).distinctBy { it.id }
                                        }
                                    },
                                    onSellPlayer = { player ->
                                        playerProfile = playerProfile?.copy(coins = (playerProfile?.coins ?: 0) + player.marketValue)
                                        collection = collection.filter { it.id != player.id }
                                        val newSavedSquads = savedSquads.map { squad ->
                                            val newPlayers = squad.players.filter { it.value?.id != player.id }
                                            squad.copy(players = newPlayers)
                                        }
                                        savedSquads = newSavedSquads
                                        if (activeSquad.players.values.any { it?.id == player.id }) {
                                            activeSquad = activeSquad.copy(players = activeSquad.players.filter { it.value?.id != player.id })
                                        }
                                    },
                                    playerDao = playerDao
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
fun AppBackground() {
    val configuration = LocalConfiguration.current
    val backgroundImage = R.drawable.landscape 

    Image(
        painter = painterResource(id = backgroundImage),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
}

@Composable
fun BootScreen(onTimeout: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(1500))
        delay(1500)
        alpha.animateTo(0f, animationSpec = tween(1000))
        onTimeout()
    }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Image(painter = painterResource(id = R.drawable.ic_boot_logo), contentDescription = null, modifier = Modifier.size(280.dp).alpha(alpha.value))
    }
}

@Composable
fun EnterGameScreen(onEnter: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse), label = "alpha")
    Box(modifier = Modifier.fillMaxSize().clickable { onEnter() }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "DREAM TEAM", fontSize = 56.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(40.dp))
            Text(text = "TAP TO START", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.alpha(alpha))
        }
    }
}

@Composable
fun SetupScreen(onProfileCreated: (PlayerProfile) -> Unit) {
    var nameInput by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(color = Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.medium, modifier = Modifier.padding(24.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Pick Your Username", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color.White, unfocusedBorderColor = Color.Gray))
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { if (nameInput.isNotBlank()) onProfileCreated(PlayerProfile(username = nameInput)) }, enabled = nameInput.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                    Text("Create Profile")
                }
            }
        }
    }
}

@Composable
fun HomeScreen(profile: PlayerProfile?, collectionSize: Int, onNavigate: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text(text = profile?.username ?: "", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "$collectionSize Players", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
        }
        Text(text = "${profile?.coins} Coins", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFC107), modifier = Modifier.align(Alignment.TopEnd))
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            val buttonModifier = Modifier.width(260.dp).padding(vertical = 6.dp)
            Button(onClick = { onNavigate("MATCH") }, modifier = buttonModifier) { Text("PLAY MATCH", fontWeight = FontWeight.Bold) }
            Button(onClick = { onNavigate("DRAW_BALL") }, modifier = buttonModifier) { Text("DRAW BALL", fontWeight = FontWeight.Bold) }
            Button(onClick = { onNavigate("COLLECTION") }, modifier = buttonModifier) { Text("COLLECTION", fontWeight = FontWeight.Bold) }
            Button(onClick = { onNavigate("TEAM") }, modifier = buttonModifier) { Text("TEAM MANAGEMENT", fontWeight = FontWeight.Bold) }
            Button(onClick = { onNavigate("MARKET") }, modifier = buttonModifier) { Text("TRANSFER MARKET", fontWeight = FontWeight.Bold) }
        }
        IconButton(onClick = { onNavigate("SETTINGS") }, modifier = Modifier.align(Alignment.BottomStart)) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun SettingsScreen(profile: PlayerProfile, onProfileUpdate: (PlayerProfile) -> Unit, onBack: () -> Unit, onClearData: () -> Unit) {
    var newUsername by remember { mutableStateOf(profile.username) }
    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(color = Color.Black.copy(alpha = 0.85f), shape = MaterialTheme.shapes.large, modifier = Modifier.padding(16.dp).fillMaxHeight(0.9f).width(400.dp)) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("SETTINGS", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = newUsername, 
                    onValueChange = { newUsername = it }, 
                    label = { Text("Change Username") }, 
                    singleLine = true, 
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = Color.White, unfocusedLabelColor = Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { onProfileUpdate(profile.copy(username = newUsername)) },
                    modifier = Modifier.fillMaxWidth()
                ) { 
                    Text("SAVE USERNAME") 
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onClearData, 
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth()
                ) { 
                    Text("RESET GAME & CLEAR DATA", color = Color.White) 
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { 
                    Text("BACK TO MENU", color = Color.White, fontWeight = FontWeight.Bold) 
                }
            }
        }
    }
}
