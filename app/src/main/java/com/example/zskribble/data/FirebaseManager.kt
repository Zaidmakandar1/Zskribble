package com.example.zskribble.data

import android.util.Log
import com.example.zskribble.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class FirebaseManager {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val roomsRef = database.getReference("rooms")
    private val strokesRef = database.getReference("strokes")
    private val messagesRef = database.getReference("messages")
    
    init {
        Log.d("FirebaseManager", "Initialized")
    }
    
    // Generate 6-character room code
    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
    
    // Anonymous authentication
    suspend fun signInAnonymously(): String {
        Log.d("FirebaseManager", "Signing in anonymously...")
        val result = auth.signInAnonymously().await()
        val userId = result.user?.uid ?: throw Exception("Failed to sign in")
        Log.d("FirebaseManager", "Signed in with ID: $userId")
        return userId
    }
    
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    // Create room
    suspend fun createRoom(playerName: String): Result<Room> {
        return try {
            Log.d("FirebaseManager", "Creating room for: $playerName")
            val userId = getCurrentUserId() ?: signInAnonymously()
            val roomCode = generateRoomCode()
            
            val player = Player(
                id = userId,
                name = playerName,
                isHost = true
            )
            
            val room = Room(
                code = roomCode,
                hostId = userId,
                players = mapOf(userId to player)
            )
            
            Log.d("FirebaseManager", "Saving room to Firebase: $roomCode")
            roomsRef.child(roomCode).setValue(room).await()
            Log.d("FirebaseManager", "Room created successfully: $roomCode")
            Result.success(room)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error creating room", e)
            Result.failure(e)
        }
    }
    
    // Join room
    suspend fun joinRoom(roomCode: String, playerName: String): Result<Room> {
        return try {
            val userId = getCurrentUserId() ?: signInAnonymously()
            val snapshot = roomsRef.child(roomCode).get().await()
            
            if (!snapshot.exists()) {
                return Result.failure(Exception("Room not found"))
            }
            
            val player = Player(
                id = userId,
                name = playerName
            )
            
            roomsRef.child(roomCode).child("players").child(userId).setValue(player).await()
            
            val room = snapshot.getValue(Room::class.java)
                ?: return Result.failure(Exception("Failed to parse room"))
            
            Result.success(room)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Observe room changes
    fun observeRoom(roomCode: String): Flow<Room?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshot.getValue(Room::class.java)
                trySend(room)
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        roomsRef.child(roomCode).addValueEventListener(listener)
        
        awaitClose {
            roomsRef.child(roomCode).removeEventListener(listener)
        }
    }
    
    // Send stroke
    suspend fun sendStroke(roomCode: String, stroke: Stroke) {
        strokesRef.child(roomCode).push().setValue(stroke).await()
    }
    
    // Observe strokes
    fun observeStrokes(roomCode: String): Flow<Stroke> = callbackFlow {
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(Stroke::class.java)?.let { trySend(it) }
            }
            
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        strokesRef.child(roomCode).addChildEventListener(listener)
        
        awaitClose {
            strokesRef.child(roomCode).removeEventListener(listener)
        }
    }
    
    // Clear canvas
    suspend fun clearCanvas(roomCode: String) {
        strokesRef.child(roomCode).removeValue().await()
    }
    
    // Leave room
    suspend fun leaveRoom(roomCode: String) {
        val userId = getCurrentUserId() ?: return
        
        try {
            // Get current room state
            val snapshot = roomsRef.child(roomCode).get().await()
            val room = snapshot.getValue(Room::class.java) ?: return
            
            // Remove player
            roomsRef.child(roomCode).child("players").child(userId).removeValue().await()
            
            // Check if room is now empty
            val remainingPlayers = room.players.filter { it.key != userId }
            
            if (remainingPlayers.isEmpty()) {
                // Delete the room if no players left
                roomsRef.child(roomCode).removeValue().await()
                strokesRef.child(roomCode).removeValue().await()
                messagesRef.child(roomCode).removeValue().await()
            } else if (room.hostId == userId) {
                // Reassign host to first remaining player
                val newHostId = remainingPlayers.keys.first()
                roomsRef.child(roomCode).child("hostId").setValue(newHostId).await()
                roomsRef.child(roomCode).child("players").child(newHostId).child("isHost").setValue(true).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error leaving room", e)
        }
    }
    
    // Start game
    suspend fun startGame(roomCode: String) {
        try {
            Log.d("FirebaseManager", "Starting game for room: $roomCode")
            val snapshot = roomsRef.child(roomCode).get().await()
            val room = snapshot.getValue(Room::class.java) ?: run {
                Log.e("FirebaseManager", "Room not found")
                return
            }
            
            val playerIds = room.players.keys.toList()
            Log.d("FirebaseManager", "Players in room: $playerIds")
            
            if (playerIds.isEmpty()) {
                Log.e("FirebaseManager", "No players in room")
                return
            }
            
            val drawerId = playerIds.random()
            val word = com.example.zskribble.data.WordDatabase.getRandomWord()
            val wordHint = com.example.zskribble.data.WordDatabase.getWordHint(word)
            
            Log.d("FirebaseManager", "Selected drawer: $drawerId, word: $word")
            
            val gameState = GameState(
                currentRound = 1,
                totalRounds = playerIds.size,
                currentDrawerId = drawerId,
                currentWord = word,
                wordHint = wordHint,
                roundStartTime = System.currentTimeMillis(),
                roundDuration = 60,
                scores = emptyMap(),
                guessedPlayers = emptyList()
            )
            
            // Clear previous game data
            strokesRef.child(roomCode).removeValue().await()
            database.getReference("messages").child(roomCode).removeValue().await()
            
            roomsRef.child(roomCode).child("state").setValue(RoomState.PLAYING).await()
            roomsRef.child(roomCode).child("gameState").setValue(gameState).await()
            
            Log.d("FirebaseManager", "Game started successfully")
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Error starting game", e)
        }
    }
    
    // Send chat message
    suspend fun sendMessage(roomCode: String, message: ChatMessage) {
        database.getReference("messages").child(roomCode).push().setValue(message).await()
    }
    
    // Observe messages
    fun observeMessages(roomCode: String): Flow<ChatMessage> = callbackFlow {
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(ChatMessage::class.java)?.let { trySend(it) }
            }
            
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        database.getReference("messages").child(roomCode).addChildEventListener(listener)
        
        awaitClose {
            database.getReference("messages").child(roomCode).removeEventListener(listener)
        }
    }
    
    // Submit guess
    suspend fun submitGuess(roomCode: String, playerId: String, playerName: String, guess: String): Boolean {
        val snapshot = roomsRef.child(roomCode).get().await()
        val room = snapshot.getValue(Room::class.java) ?: return false
        val gameState = room.gameState ?: return false
        
        val isCorrect = guess.equals(gameState.currentWord, ignoreCase = true)
        
        val message = ChatMessage(
            id = System.currentTimeMillis().toString(),
            playerId = playerId,
            playerName = playerName,
            content = if (isCorrect) "guessed correctly! 🎉" else guess,
            type = if (isCorrect) MessageType.GUESS_CORRECT else MessageType.CHAT
        )
        
        sendMessage(roomCode, message)
        
        if (isCorrect && !gameState.guessedPlayers.contains(playerId)) {
            // Award points
            val timeElapsed = (System.currentTimeMillis() - gameState.roundStartTime) / 1000
            val points = maxOf(100 - timeElapsed.toInt() * 2, 10)
            
            val currentScore = gameState.scores[playerId] ?: 0
            val updatedScores = gameState.scores + (playerId to currentScore + points)
            val updatedGuessed = gameState.guessedPlayers + playerId
            
            roomsRef.child(roomCode).child("gameState/scores").setValue(updatedScores).await()
            roomsRef.child(roomCode).child("gameState/guessedPlayers").setValue(updatedGuessed).await()
            
            // Check if all players (except drawer) have guessed
            val totalPlayers = room.players.size
            val guessersCount = totalPlayers - 1 // Exclude drawer
            
            if (updatedGuessed.size >= guessersCount && guessersCount > 0) {
                // All players guessed! End round early
                Log.d("FirebaseManager", "All players guessed! Ending round early")
                nextRound(roomCode)
            }
        }
        
        return isCorrect
    }
    
    // Next round
    suspend fun nextRound(roomCode: String) {
        val snapshot = roomsRef.child(roomCode).get().await()
        val room = snapshot.getValue(Room::class.java) ?: return
        val gameState = room.gameState ?: return
        
        val playerIds = room.players.keys.toList()
        val currentIndex = playerIds.indexOf(gameState.currentDrawerId)
        val nextIndex = (currentIndex + 1) % playerIds.size
        
        if (gameState.currentRound >= gameState.totalRounds) {
            // Game over
            roomsRef.child(roomCode).child("state").setValue(RoomState.FINISHED).await()
            return
        }
        
        val word = com.example.zskribble.data.WordDatabase.getRandomWord()
        val wordHint = com.example.zskribble.data.WordDatabase.getWordHint(word)
        
        val newGameState = gameState.copy(
            currentRound = gameState.currentRound + 1,
            currentDrawerId = playerIds[nextIndex],
            currentWord = word,
            wordHint = wordHint,
            roundStartTime = System.currentTimeMillis(),
            guessedPlayers = emptyList()
        )
        
        roomsRef.child(roomCode).child("gameState").setValue(newGameState).await()
        strokesRef.child(roomCode).removeValue().await()
    }
}
