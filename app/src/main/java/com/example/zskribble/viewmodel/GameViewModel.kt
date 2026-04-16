package com.example.zskribble.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zskribble.data.FirebaseManager
import com.example.zskribble.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {
    
    private val firebaseManager = FirebaseManager()
    
    private val _strokes = MutableStateFlow<List<Stroke>>(emptyList())
    val strokes: StateFlow<List<Stroke>> = _strokes.asStateFlow()
    
    private val _currentTool = MutableStateFlow(DrawingTool.default())
    val currentTool: StateFlow<DrawingTool> = _currentTool.asStateFlow()
    
    private val _currentRoom = MutableStateFlow<Room?>(null)
    val currentRoom: StateFlow<Room?> = _currentRoom.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _timeRemaining = MutableStateFlow(60)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()
    
    private var currentRoomCode: String? = null
    private var timerJob: Job? = null
    
    init {
        Log.d("GameViewModel", "ViewModel initialized")
    }
    
    fun createRoom(playerName: String) {
        Log.d("GameViewModel", "createRoom called with: $playerName")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                Log.d("GameViewModel", "Starting room creation...")
                
                firebaseManager.createRoom(playerName)
                    .onSuccess { room ->
                        Log.d("GameViewModel", "Room created successfully: ${room.code}")
                        currentRoomCode = room.code
                        observeRoom(room.code)
                    }
                    .onFailure { e ->
                        Log.e("GameViewModel", "Failed to create room", e)
                        _error.value = "Failed to create room: ${e.message}"
                    }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Exception in createRoom", e)
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun joinRoom(playerName: String, roomCode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            firebaseManager.joinRoom(roomCode, playerName)
                .onSuccess { room ->
                    currentRoomCode = roomCode
                    observeRoom(roomCode)
                }
                .onFailure { e ->
                    _error.value = e.message
                }
            _isLoading.value = false
        }
    }
    
    private fun observeRoom(roomCode: String) {
        viewModelScope.launch {
            firebaseManager.observeRoom(roomCode).collect { room ->
                val previousRound = _currentRoom.value?.gameState?.currentRound
                val newRound = room?.gameState?.currentRound
                
                // Clear strokes when round changes
                if (previousRound != null && newRound != null && previousRound != newRound) {
                    _strokes.value = emptyList()
                    Log.d("GameViewModel", "Round changed, clearing canvas")
                }
                
                _currentRoom.value = room
                
                // Start timer when game starts
                if (room?.state == RoomState.PLAYING && room.gameState != null) {
                    startRoundTimer(room.gameState!!)
                }
            }
        }
        
        // Observe strokes from other players
        viewModelScope.launch {
            firebaseManager.observeStrokes(roomCode).collect { stroke ->
                // Only add if not already in list (avoid duplicates)
                if (_strokes.value.none { it.id == stroke.id }) {
                    _strokes.value = _strokes.value + stroke
                    Log.d("GameViewModel", "Received stroke from Firebase: ${stroke.id}")
                }
            }
        }
        
        // Observe chat messages
        viewModelScope.launch {
            firebaseManager.observeMessages(roomCode).collect { message ->
                if (_messages.value.none { it.id == message.id }) {
                    _messages.value = _messages.value + message
                }
            }
        }
    }
    
    private fun startRoundTimer(gameState: GameState) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startTime = gameState.roundStartTime
            val duration = gameState.roundDuration
            
            while (true) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                val remaining = (duration - elapsed).toInt()
                
                if (remaining <= 0) {
                    _timeRemaining.value = 0
                    onRoundEnd()
                    break
                }
                
                _timeRemaining.value = remaining
                delay(1000)
            }
        }
    }
    
    private fun onRoundEnd() {
        viewModelScope.launch {
            currentRoomCode?.let { roomCode ->
                firebaseManager.nextRound(roomCode)
                _strokes.value = emptyList()
            }
        }
    }
    
    fun sendMessage(message: String) {
        viewModelScope.launch {
            currentRoomCode?.let { roomCode ->
                val userId = firebaseManager.getCurrentUserId() ?: return@launch
                val playerName = _currentRoom.value?.players?.get(userId)?.name ?: "Unknown"
                
                firebaseManager.submitGuess(roomCode, userId, playerName, message)
            }
        }
    }
    
    fun onStrokeComplete(stroke: Stroke) {
        // Add locally immediately for smooth drawing
        _strokes.value = _strokes.value + stroke
        
        // Then send to Firebase for other players
        viewModelScope.launch {
            try {
                currentRoomCode?.let { roomCode ->
                    Log.d("GameViewModel", "Sending stroke to Firebase: ${stroke.id}")
                    firebaseManager.sendStroke(roomCode, stroke)
                }
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error sending stroke", e)
            }
        }
    }
    
    fun onToolChange(tool: DrawingTool) {
        _currentTool.value = tool
    }
    
    fun onUndo() {
        if (_strokes.value.isNotEmpty()) {
            _strokes.value = _strokes.value.dropLast(1)
        }
    }
    
    fun onClear() {
        viewModelScope.launch {
            currentRoomCode?.let { roomCode ->
                firebaseManager.clearCanvas(roomCode)
            }
        }
        _strokes.value = emptyList()
    }
    
    fun startGame() {
        viewModelScope.launch {
            currentRoomCode?.let { roomCode ->
                // Clear local state
                _messages.value = emptyList()
                _strokes.value = emptyList()
                
                firebaseManager.startGame(roomCode)
            }
        }
    }
    
    fun leaveRoom() {
        viewModelScope.launch {
            currentRoomCode?.let { roomCode ->
                firebaseManager.leaveRoom(roomCode)
            }
        }
        
        // Clean up all state
        timerJob?.cancel()
        _currentRoom.value = null
        _strokes.value = emptyList()
        _messages.value = emptyList()
        _timeRemaining.value = 60
        _error.value = null
        currentRoomCode = null
    }
    
    fun getCurrentUserId(): String? = firebaseManager.getCurrentUserId()
}
