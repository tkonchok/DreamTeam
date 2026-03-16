package com.example.dreamteam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionView(players: List<SoccerPlayer>, onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedBallType by remember { mutableStateOf<BallType?>(null) }
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val filteredPlayers = remember(players, searchQuery, selectedBallType) {
        players.filter { player ->
            val matchesSearch = player.name.contains(searchQuery, ignoreCase = true) ||
                               player.club.contains(searchQuery, ignoreCase = true) ||
                               player.country.contains(searchQuery, ignoreCase = true)
            val matchesBallType = selectedBallType == null || player.ballType == selectedBallType
            matchesSearch && matchesBallType
        }
    }

    Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(tonalElevation = 3.dp, color = Color.Black.copy(alpha = 0.8f)) {
                    Column {
                        if (!isLandscape) {
                            TopAppBar(
                                title = { Text("My Collection (${filteredPlayers.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                                navigationIcon = {
                                    IconButton(onClick = onBack) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp), tint = Color.White)
                                }
                                Text("COLLECTION (${filteredPlayers.size})", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                            }
                        }
                        FilterBar(
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            selectedBallType = selectedBallType,
                            onBallTypeSelect = { selectedBallType = it },
                            isLandscape = isLandscape
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (filteredPlayers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No players found", color = Color.Gray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = if (isLandscape) 120.dp else 160.dp),
                        contentPadding = PaddingValues(if (isLandscape) 8.dp else 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredPlayers, key = { it.id }) { player ->
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.7f)) {
                                PlayerCard(player = player, modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedBallType: BallType?,
    onBallTypeSelect: (BallType?) -> Unit,
    isLandscape: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = if (isLandscape) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search...", fontSize = 12.sp, color = Color.Gray) },
            modifier = Modifier.weight(1f).height(if (isLandscape) 40.dp else 56.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray) },
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
        
        Spacer(modifier = Modifier.width(8.dp))
        
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedBallType == null,
                    onClick = { onBallTypeSelect(null) },
                    label = { Text("All", fontSize = 10.sp) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedLabelColor = Color.Black,
                        selectedContainerColor = Color.White,
                        labelColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                )
            }
            items(BallType.entries.toTypedArray()) { ballType ->
                FilterChip(
                    selected = selectedBallType == ballType,
                    onClick = { onBallTypeSelect(ballType) },
                    label = { Text(ballType.name, fontSize = 10.sp) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedLabelColor = Color.Black,
                        selectedContainerColor = Color.White,
                        labelColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}
