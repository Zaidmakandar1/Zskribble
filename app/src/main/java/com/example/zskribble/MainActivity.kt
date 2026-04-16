package com.example.zskribble

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zskribble.model.RoomState
import com.example.zskribble.ui.screens.*
import com.example.zskribble.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var showSplash by remember { mutableStateOf(true) }
                
                if (showSplash) {
                    SplashScreen(onTimeout = { showSplash = false })
                } else {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    val viewModel: GameViewModel = viewModel()
    val currentRoom by viewModel.currentRoom.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUserId = viewModel.getCurrentUserId()
    
    Log.d("MainActivity", "Room: $currentRoom, Loading: $isLoading, Error: $error")
    
    when {
        currentRoom == null -> {
            HomeScreen(
                onCreateRoom = { playerName ->
                    Log.d("MainActivity", "Creating room for: $playerName")
                    viewModel.createRoom(playerName)
                },
                onJoinRoom = { playerName, roomCode ->
                    Log.d("MainActivity", "Joining room: $roomCode")
                    viewModel.joinRoom(playerName, roomCode)
                },
                isLoading = isLoading,
                error = error
            )
        }
        currentRoom?.state == RoomState.LOBBY -> {
            LobbyScreen(
                room = currentRoom!!,
                currentUserId = currentUserId ?: "",
                onStartGame = { viewModel.startGame() },
                onLeaveRoom = { viewModel.leaveRoom() }
            )
        }
        currentRoom?.state == RoomState.PLAYING -> {
            GameScreen(
                room = currentRoom!!,
                currentUserId = currentUserId ?: "",
                viewModel = viewModel
            )
        }
        currentRoom?.state == RoomState.FINISHED -> {
            ResultsScreen(
                room = currentRoom!!,
                onPlayAgain = { viewModel.startGame() },
                onLeaveRoom = { viewModel.leaveRoom() }
            )
        }
        else -> {
            DrawingTestScreen()
        }
    }
}