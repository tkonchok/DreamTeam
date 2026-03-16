package com.example.dreamteam

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter

@Composable
fun PlayerCard(
    player: SoccerPlayer, 
    modifier: Modifier = Modifier,
    enableFullView: Boolean = true,
    onDoubleTapOverride: (() -> Unit)? = null
) {
    var rotated by remember { mutableStateOf(false) }
    var showFullView by remember { mutableStateOf(false) }
    
    // Simple Image Logic: Use player.imageUrl directly to avoid constant API hits
    val currentImageUrl = player.imageUrl

    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "cardFlip"
    )

    Box(
        modifier = modifier
            .width(200.dp)
            .height(300.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .pointerInput(player.id) {
                detectTapGestures(
                    onTap = { rotated = !rotated },
                    onDoubleTap = { 
                        if (onDoubleTapOverride != null) {
                            onDoubleTapOverride()
                        } else if (enableFullView) {
                            showFullView = true 
                        }
                    }
                )
            }
    ) {
        if (rotation <= 90f) {
            PlayerCardFront(player, currentImageUrl)
        } else {
            Box(Modifier.graphicsLayer { rotationY = 180f }) {
                PlayerCardBack(player)
            }
        }
    }

    if (showFullView && enableFullView) {
        Dialog(
            onDismissRequest = { showFullView = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PlayerCard(
                        player = player,
                        modifier = Modifier.size(width = 320.dp, height = 480.dp),
                        enableFullView = false,
                        onDoubleTapOverride = { showFullView = false }
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerCardFront(player: SoccerPlayer, imageUrl: String) {
    val (cardColors, borderColor) = getCardStyles(player)
    val isWhiteBall = player.ballType == BallType.WHITE
    val textColor = if (isWhiteBall) Color.Black else Color.White

    Card(
        modifier = Modifier.fillMaxSize().border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(cardColors))) {
            if (player.ballType == BallType.BLACK || player.ballType == BallType.GOLD) GlossyPattern()

            var showFallback by remember { mutableStateOf(false) }

            if (imageUrl.isNotEmpty() && !showFallback) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = player.name,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f).align(Alignment.TopCenter).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    onState = { if (it is AsyncImagePainter.State.Error) showFallback = true }
                )
            } else {
                PlayerAvatarFallback(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f).align(Alignment.TopCenter), player = player)
            }

            GlossOverlay()

            Column(modifier = Modifier.padding(12.dp)) {
                val shadow = if (!isWhiteBall) Shadow(Color.Black.copy(alpha = 0.5f), Offset(2f, 2f), 4f) else null
                Text(text = player.rating.toString(), style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = textColor, shadow = shadow))
                Text(text = player.position, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, if (isWhiteBall) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f)))).padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = player.name.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Black, color = textColor, maxLines = 1)
                Text(text = player.club, fontSize = 10.sp, color = textColor.copy(alpha = 0.9f), maxLines = 1)
                Text(text = player.league, fontSize = 9.sp, color = textColor.copy(alpha = 0.8f), fontWeight = FontWeight.Medium, maxLines = 1)
                Text(text = player.country, fontSize = 10.sp, color = textColor.copy(alpha = 0.7f), maxLines = 1)
            }
        }
    }
}

@Composable
fun PlayerCardBack(player: SoccerPlayer) {
    val (cardColors, borderColor) = getCardStyles(player)
    val isWhiteBall = player.ballType == BallType.WHITE
    val textColor = if (isWhiteBall) Color.Black else Color.White
    
    Card(
        modifier = Modifier.fillMaxSize().border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(cardColors.reversed())).padding(10.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "STATS", 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Black, 
                    color = textColor.copy(alpha = 0.6f),
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                StatRow("PAC", player.pace, textColor)
                StatRow("SHO", player.shooting, textColor)
                StatRow("PAS", player.passing, textColor)
                StatRow("DRI", player.dribbling, textColor)
                StatRow("DEF", player.defending, textColor)
                StatRow("PHY", player.physical, textColor)
                
                Spacer(modifier = Modifier.weight(1f))
                Text(text = player.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1)
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: Int, textColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, modifier = Modifier.width(30.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
        Box(modifier = Modifier.weight(1f).height(5.dp).background(if (textColor == Color.Black) Color.Black.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))) {
            Box(modifier = Modifier.fillMaxWidth(value / 100f).fillMaxHeight().background(if (textColor == Color.Black) Color(0xFF1B5E20) else Color.Yellow, RoundedCornerShape(4.dp)))
        }
        Text(text = value.toString(), modifier = Modifier.width(22.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor, textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

private fun getCardStyles(player: SoccerPlayer): Pair<List<Color>, Color> {
    return when (player.ballType) {
        BallType.BLACK -> listOf(Color(0xFF0F0F0F), Color(0xFF333333)) to Color(0xFF555555)
        BallType.GOLD -> listOf(Color(0xFFD4AF37), Color(0xFFF9E076)) to Color(0xFFFFD700)
        BallType.SILVER -> listOf(Color(0xFFAAAAAA), Color(0xFFDDDDDD)) to Color(0xFFC0C0C0)
        BallType.BRONZE -> listOf(Color(0xFF804A00), Color(0xFFCD7F32)) to Color(0xFF8B4513)
        BallType.WHITE -> listOf(Color(0xFFF0F0F0), Color(0xFFFFFFFF)) to Color(0xFFCCCCCC)
    }
}

@Composable
fun PlayerAvatarFallback(modifier: Modifier = Modifier, player: SoccerPlayer) {
    val skinTones = listOf(Color(0xFFFFDBAC), Color(0xFFF1C27D), Color(0xFFE0AC69), Color(0xFF8D5524), Color(0xFFC68642))
    val avatarColor = skinTones[player.id.hashCode().coerceAtLeast(0) % skinTones.size]
    Box(modifier = modifier.background(Color.Gray.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
        Icon(imageVector = Icons.Default.Person, contentDescription = "Avatar", modifier = Modifier.fillMaxSize(0.8f), tint = avatarColor.copy(alpha = 0.6f))
    }
}

@Composable
fun GlossOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(brush = Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent, Color.White.copy(alpha = 0.05f)), start = Offset(0f, 0f), end = Offset(size.width, size.height)))
    }
}

@Composable
fun GlossyPattern() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 1.dp.toPx()
        for (i in 0..size.width.toInt() step 40) {
            drawLine(color = Color.White.copy(alpha = 0.05f), start = Offset(i.toFloat(), 0f), end = Offset(i.toFloat() + 100f, size.height), strokeWidth = strokeWidth)
        }
    }
}
