package com.example.dreamteam

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementView(
    squads: List<Squad>,
    activeSquad: Squad,
    collection: List<SoccerPlayer>,
    onBack: () -> Unit,
    onActiveSquadChanged: (Squad) -> Unit,
    onSquadsChanged: (List<Squad>) -> Unit
) {
    var showSelectionDialog by remember { mutableStateOf<String?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showLoadDialog by remember { mutableStateOf(false) }
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Team Management", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(activeSquad.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showLoadDialog = true }) {
                        Text("LOAD")
                    }
                    TextButton(onClick = { showSaveDialog = true }) {
                        Text("SAVE")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.8f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Row(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Left Sidebar: Formation and Squad Tools
            Surface(
                color = Color.DarkGray.copy(alpha = 0.2f),
                modifier = Modifier.width(180.dp).fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text("FORMATION", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedCard(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(activeSquad.formation.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            Formations.AllFormations.forEach { formation ->
                                DropdownMenuItem(
                                    text = { Text(formation.name) },
                                    onClick = {
                                        onActiveSquadChanged(activeSquad.copy(formation = formation))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("SQUAD STATS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray)
                    val totalRating = activeSquad.players.values.filterNotNull().sumOf { it.rating }
                    val avgRating = if (activeSquad.players.values.filterNotNull().isNotEmpty()) totalRating / activeSquad.players.values.filterNotNull().size else 0
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Team OVR: $avgRating", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Yellow)
                    Text("Players: ${activeSquad.players.values.count { it != null }}/11", fontSize = 12.sp)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { onActiveSquadChanged(Squad(name = "New Squad")) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CLEAR", fontSize = 12.sp)
                    }
                }
            }

            // Main Field
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                SoccerPitch(activeSquad, onSlotClick = { showSelectionDialog = it })
            }
        }

        if (showSelectionDialog != null) {
            PlayerSelectionDialog(
                collection = collection,
                currentSquad = activeSquad,
                onDismiss = { showSelectionDialog = null },
                onSelect = { player ->
                    val newPlayers = activeSquad.players.toMutableMap()
                    // Remove player if already in squad at different position
                    val existingPos = activeSquad.players.filter { it.value?.id == player.id }.keys.firstOrNull()
                    if (existingPos != null) newPlayers[existingPos] = null
                    
                    newPlayers[showSelectionDialog!!] = player
                    onActiveSquadChanged(activeSquad.copy(players = newPlayers))
                    showSelectionDialog = null
                }
            )
        }

        if (showSaveDialog) {
            var squadName by remember { mutableStateOf(activeSquad.name) }
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Save Squad") },
                text = {
                    OutlinedTextField(
                        value = squadName,
                        onValueChange = { squadName = it },
                        label = { Text("Squad Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (squadName.isNotBlank()) {
                            val newSquad = activeSquad.copy(name = squadName)
                            val newList = squads.filter { it.name != squadName } + newSquad
                            onSquadsChanged(newList)
                            onActiveSquadChanged(newSquad)
                            showSaveDialog = false
                        }
                    }) { Text("SAVE") }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) { Text("CANCEL") }
                }
            )
        }

        if (showLoadDialog) {
            AlertDialog(
                onDismissRequest = { showLoadDialog = false },
                title = { Text("Load Squad") },
                text = {
                    Box(modifier = Modifier.height(300.dp).width(300.dp)) {
                        if (squads.isEmpty()) {
                            Text("No saved squads", modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn {
                                items(squads) { squad ->
                                    ListItem(
                                        headlineContent = { Text(squad.name) },
                                        supportingContent = { Text(squad.formation.name) },
                                        trailingContent = {
                                            IconButton(onClick = { 
                                                val newSquads = squads.filter { it.name != squad.name }
                                                onSquadsChanged(newSquads)
                                                // If we just deleted the active squad, reset view to a clean slate
                                                if (activeSquad.name == squad.name) {
                                                    onActiveSquadChanged(Squad(name = "New Squad"))
                                                }
                                            }) {
                                                Icon(Icons.Default.Delete, null, tint = Color.Red)
                                            }
                                        },
                                        modifier = Modifier.clickable {
                                            onActiveSquadChanged(squad)
                                            showLoadDialog = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLoadDialog = false }) { Text("CLOSE") }
                }
            )
        }
    }
}

@Composable
fun SoccerPitch(squad: Squad, onSlotClick: (String) -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20))
    ) {
        val fieldWidth = maxWidth
        val fieldHeight = maxHeight

        PitchLines()

        squad.formation.positions.forEach { pos ->
            val xOffset = pos.x * fieldWidth.value
            val yOffset = pos.y * fieldHeight.value
            
            PositionSlot(
                position = pos,
                player = squad.players[pos.id],
                modifier = Modifier.offset(x = xOffset.dp - 30.dp, y = yOffset.dp - 40.dp),
                onClick = { onSlotClick(pos.id) }
            )
        }
    }
}

@Composable
fun PositionSlot(
    position: FormationPosition,
    player: SoccerPlayer?,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (player != null) {
                AsyncImage(
                    model = player.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(4.dp).clip(CircleShape)
                )
            } else {
                Text("+", color = Color.White, fontSize = 20.sp)
            }
        }
        Text(
            text = player?.name ?: position.label,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun PitchLines() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = Stroke(width = 2.dp.toPx())
        val white = Color.White.copy(alpha = 0.4f)
        
        drawRect(white, style = stroke)
        drawLine(white, start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), strokeWidth = 2.dp.toPx())
        drawCircle(white, radius = size.width / 6, center = Offset(size.width / 2, size.height / 2), style = stroke)
        
        val boxWidth = size.width * 0.6f
        val boxHeight = size.height * 0.15f
        drawRect(white, topLeft = Offset((size.width - boxWidth)/2, 0f), size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight), style = stroke)
        drawRect(white, topLeft = Offset((size.width - boxWidth)/2, size.height - boxHeight), size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight), style = stroke)
    }
}

@Composable
fun PlayerSelectionDialog(
    collection: List<SoccerPlayer>,
    currentSquad: Squad,
    onDismiss: () -> Unit,
    onSelect: (SoccerPlayer) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Player") },
        text = {
            Column(modifier = Modifier.height(300.dp)) {
                androidx.compose.foundation.lazy.LazyColumn {
                    // Filter out players already in this squad position, but show them if they are in other positions (allowing swaps)
                    items(collection) { player ->
                        val isInSquad = currentSquad.players.values.any { it?.id == player.id }
                        ListItem(
                            headlineContent = { Text(player.name, fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("${player.position} | OVR: ${player.rating} | ${player.club}") },
                            leadingContent = {
                                if (isInSquad) Icon(Icons.Default.Check, null, tint = Color.Green)
                                else Box(Modifier.size(40.dp).background(Color.Gray.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                    Text(player.position.take(2), fontSize = 12.sp)
                                }
                            },
                            modifier = Modifier.clickable { onSelect(player) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
