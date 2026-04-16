package com.example.zskribble.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    val colorAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "color"
    )
    
    LaunchedEffect(Unit) {
        delay(2500)
        onTimeout()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6366F1),
                        Color(0xFF8B5CF6),
                        Color(0xFFEC4899)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated drawing icon
            Canvas(
                modifier = Modifier.size(120.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = size.minDimension / 3
                
                // Draw animated brush stroke
                val path = Path().apply {
                    val progress = animatedProgress
                    moveTo(centerX - radius, centerY)
                    
                    // Create a wavy brush stroke
                    for (i in 0..100) {
                        val t = i / 100f * progress
                        val x = centerX - radius + (radius * 2 * t)
                        val y = centerY + kotlin.math.sin(t * 6f) * radius * 0.5f
                        lineTo(x, y)
                    }
                }
                
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(
                        width = 12f,
                        cap = StrokeCap.Round
                    )
                )
                
                // Draw pencil tip
                val tipX = centerX - radius + (radius * 2 * animatedProgress)
                val tipY = centerY + kotlin.math.sin(animatedProgress * 6f) * radius * 0.5f
                
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = 16f,
                    center = Offset(tipX, tipY)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "InkStorm",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Draw • Guess • Win",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) { index ->
                    val delay = index * 200
                    val dotAnimation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = FastOutSlowInEasing, delayMillis = delay),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .offset(y = (-20 * dotAnimation).dp)
                            .background(
                                color = Color.White.copy(alpha = 0.7f + 0.3f * dotAnimation),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }
        }
    }
}
