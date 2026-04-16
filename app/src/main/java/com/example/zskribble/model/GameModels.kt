package com.example.zskribble.model

data class Player(
    val id: String = "",
    val name: String = "",
    val score: Int = 0,
    val isHost: Boolean = false,
    val hasGuessed: Boolean = false
)

data class Room(
    val code: String = "",
    val hostId: String = "",
    val players: Map<String, Player> = emptyMap(),
    val state: RoomState = RoomState.LOBBY,
    val gameState: GameState? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RoomState {
    LOBBY, PLAYING, FINISHED
}

data class GameState(
    val currentRound: Int = 1,
    val totalRounds: Int = 3,
    val currentDrawerId: String = "",
    val currentWord: String = "",
    val wordHint: String = "",
    val roundStartTime: Long = 0,
    val roundDuration: Int = 60,
    val scores: Map<String, Int> = emptyMap(),
    val guessedPlayers: List<String> = emptyList()
)

data class ChatMessage(
    val id: String = "",
    val playerId: String = "",
    val playerName: String = "",
    val content: String = "",
    val type: MessageType = MessageType.CHAT,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageType {
    CHAT, GUESS_CORRECT, GUESS_INCORRECT, SYSTEM
}
