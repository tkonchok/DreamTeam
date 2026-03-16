package com.example.dreamteam

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferMarketView(
    playerCoins: Int,
    collection: List<SoccerPlayer>,
    onBack: () -> Unit,
    onBuyPlayer: (SoccerPlayer) -> Unit,
    onSellPlayer: (SoccerPlayer) -> Unit,
    playerDao: PlayerDao
) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Buy", "Sell")
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val scope = rememberCoroutineScope()
    val marketPlayers = remember { mutableStateListOf<SoccerPlayer>() }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPosition by remember { mutableStateOf<String?>(null) }

    val coinColor = Color(0xFFFFC107) // Amber/Gold color for coins
    
    val positions = listOf("ALL", "GK", "CB", "LB", "RB", "CDM", "CM", "CAM", "LM", "RM", "LW", "RW", "ST")

    // Unified Refresh Function
    val refreshMarket = {
        scope.launch {
            isLoading = true
            val entities = if (searchQuery.length >= 2) {
                playerDao.searchPlayers(searchQuery)
            } else {
                playerDao.getTopPlayers()
            }
            
            val playersWithImages = entities.map { entity ->
                async {
                    val player = entity.toSoccerPlayer()
                    val fetchedUrl = ImageFetcher.fetchPlayerImage(player.name)
                    player.copy(imageUrl = fetchedUrl)
                }
            }.awaitAll()
            
            marketPlayers.clear()
            marketPlayers.addAll(playersWithImages.distinctBy { it.id })
            isLoading = false
        }
    }

    LaunchedEffect(searchQuery, tabIndex) {
        if (tabIndex == 0) {
            delay(if (searchQuery.isEmpty()) 0 else 500)
            refreshMarket()
        }
    }

    var sellSuccessMessage by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(tonalElevation = 3.dp, color = Color.Black.copy(alpha = 0.9f)) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = if (isLandscape) 4.dp else 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp), tint = Color.White)
                            }
                            Text(
                                "TRANSFER MARKET", 
                                color = Color.White,
                                fontSize = if (isLandscape) 14.sp else 18.sp, 
                                fontWeight = FontWeight.Black, 
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "$playerCoins C", 
                                color = coinColor, 
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isLandscape) 12.sp else 14.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            IconButton(onClick = { refreshMarket() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search database...", color = Color.Gray, fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(if (isLandscape) 40.dp else 50.dp),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                    focusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                        }

                        // Position Filter Bar
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(positions) { pos ->
                                FilterChip(
                                    selected = (selectedPosition == pos) || (selectedPosition == null && pos == "ALL"),
                                    onClick = { selectedPosition = if (pos == "ALL") null else pos },
                                    label = { Text(pos, fontSize = 10.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color.White,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color.White.copy(alpha = 0.1f),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }

                        TabRow(
                            selectedTabIndex = tabIndex,
                            modifier = Modifier.height(if (isLandscape) 36.dp else 48.dp),
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            divider = {}
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = tabIndex == index,
                                    onClick = { tabIndex = index },
                                    text = { Text(title, fontSize = if (isLandscape) 12.sp else 14.sp) }
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (isLoading && tabIndex == 0) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = coinColor)
                    }
                } else {
                    val rawPlayersToShow = if (tabIndex == 0) marketPlayers else collection
                    val playersToShow = rawPlayersToShow.filter { 
                        (it.name.contains(searchQuery, ignoreCase = true) || 
                         it.club.contains(searchQuery, ignoreCase = true) ||
                         it.league.contains(searchQuery, ignoreCase = true)) &&
                        (selectedPosition == null || it.position == selectedPosition)
                    }
                    
                    if (playersToShow.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No players found", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(playersToShow, key = { it.id }) { player ->
                                MarketPlayerItem(
                                    player = player,
                                    canAfford = tabIndex == 1 || playerCoins >= player.marketValue,
                                    isLandscape = isLandscape,
                                    coinColor = coinColor,
                                    actionLabel = if (tabIndex == 0) "BUY" else "SELL",
                                    onAction = {
                                        if (tabIndex == 0) {
                                            onBuyPlayer(player)
                                            marketPlayers.remove(player)
                                            sellSuccessMessage = "Purchased ${player.name}!"
                                        } else {
                                            val value = player.marketValue
                                            onSellPlayer(player)
                                            sellSuccessMessage = "Sold ${player.name} for $value coins!"
                                        }
                                    }
                                )
                            }
                            
                            if (tabIndex == 0) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                        TextButton(onClick = { refreshMarket() }) {
                                            Text("RELOAD LISTINGS", color = Color.Gray, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        sellSuccessMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { sellSuccessMessage = null },
                title = { Text("Success") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { sellSuccessMessage = null }) { Text("OK") }
                }
            )
        }
    }
}

@Composable
fun MarketPlayerItem(
    player: SoccerPlayer,
    canAfford: Boolean,
    isLandscape: Boolean,
    coinColor: Color,
    actionLabel: String,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(if (isLandscape) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (isLandscape) 45.dp else 55.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (player.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = player.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                } else {
                    Text(player.rating.toString(), fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.width(if (isLandscape) 8.dp else 16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = if (isLandscape) 14.sp else 16.sp, maxLines = 1)
                Text(
                    "${player.position} | ${player.rating} OVR", 
                    fontSize = if (isLandscape) 10.sp else 12.sp, 
                    color = Color.LightGray,
                    maxLines = 1
                )
                Text(
                    "${player.club} | ${player.league}",
                    fontSize = if (isLandscape) 9.sp else 11.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${player.marketValue} C", 
                    fontWeight = FontWeight.Black, 
                    color = if (canAfford) coinColor else Color.Red,
                    fontSize = if (isLandscape) 13.sp else 15.sp
                )
                Button(
                    onClick = onAction,
                    enabled = canAfford,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(if (isLandscape) 30.dp else 38.dp).padding(top = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (actionLabel == "BUY") Color.White else Color.Red.copy(alpha = 0.8f),
                        contentColor = Color.Black
                    )
                ) {
                    Text(actionLabel, fontSize = if (isLandscape) 10.sp else 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
