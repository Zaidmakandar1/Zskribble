package com.example.zskribble.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zskribble.model.Room
import com.example.zskribble.ui.components.ChatBox
import com.example.zskribble.ui.components.DrawingCanvas
import com.example.zskribble.ui.components.DrawingToolbar
import com.example.zskribble.viewmodel.GameViewModel

@Composable
fun GameScreen(
    room: Room,
    currentUserId: String,
    viewModel: GameViewModel = viewModel()
) {
    val strokes by viewModel.strokes.collectAsState()
    val currentTool by viewModel.currentTool.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val isDrawer = room.gameState?.currentDrawerId == currentUserId
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F7FA)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Modern Header
            ModernGameHeader(
                room = room,
                isDrawer = isDrawer,
                timeRemaining = timeRemaining,
                onLeaveRoom = { viewModel.leaveRoom() }
            )
            
            // Main content area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Drawing canvas with modern card style
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        DrawingCanvas(
                            strokes = strokes,
                            isDrawingEnabled = isDrawer,
                            currentTool = currentTool,
                            onStrokeComplete = viewModel::onStrokeComplete,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Bottom section
                    if (isDrawer) {
                        // Drawing tools for drawer
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            DrawingToolbar(
                                currentTool = currentTool,
                                onToolChange = viewModel::onToolChange,
                                onClear = viewModel::onClear,
                                onUndo = viewModel::onUndo,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        // Chat for guessers
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            ChatBox(
                                messages = messages,
                                onSendMessage = viewModel::sendMessage,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernGameHeader(
    room: Room, 
    isDrawer: Boolean, 
    timeRemaining: Int,
    onLeaveRoom: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Role and round info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isDrawer) "🎨 You're Drawing!" else "🤔 Guess the Word",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Round ${room.gameState?.currentRound ?: 1} of ${room.gameState?.totalRounds ?: 1}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                // Timer
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (timeRemaining <= 10) 
                        Color(0xFFFF5252) 
                    else 
                        Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${timeRemaining}s",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Leave button
                TextButton(
                    onClick = onLeaveRoom,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Leave", fontSize = 14.sp)
                }
            }
            
            // Word hint or word display
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDrawer) {
                        Text(
                            text = "Draw: ${room.gameState?.currentWord ?: ""}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                    } else {
                        Text(
                            text = room.gameState?.wordHint ?: "_ _ _ _ _",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 4.sp
                        )
                    }
                }
            }
        }
    }
}
