# Design Document: InkStorm

## Overview

InkStorm is a real-time multiplayer drawing and guessing game built with a client-server architecture. The Android client uses Jetpack Compose for UI, Ktor for WebSocket communication, and follows MVVM architecture. The Node.js server manages game state, validates actions, and coordinates real-time communication using Socket.IO and Redis for state storage.

The system prioritizes low-latency stroke synchronization, server-authoritative game logic to prevent cheating, and smooth user experience through reactive state management.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Android Clients                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Player 1   │  │   Player 2   │  │   Player N   │      │
│  │  (Drawer)    │  │  (Guesser)   │  │  (Guesser)   │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                  │                  │              │
│         └──────────────────┼──────────────────┘              │
│                            │                                 │
│                    WebSocket (Socket.IO)                     │
│                            │                                 │
└────────────────────────────┼─────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────┐
│                     Node.js Server                           │
│                            │                                 │
│  ┌─────────────────────────▼──────────────────────────┐     │
│  │           Socket.IO Server                         │     │
│  │  - Connection Management                           │     │
│  │  - Event Routing                                   │     │
│  │  - Room Broadcasting                               │     │
│  └─────────────────────────┬──────────────────────────┘     │
│                            │                                 │
│  ┌─────────────────────────▼──────────────────────────┐     │
│  │           Game Engine                              │     │
│  │  - Turn Management                                 │     │
│  │  - Guess Validation                                │     │
│  │  - Score Calculation                               │     │
│  │  - Round Timer                                     │     │
│  └─────────────────────────┬──────────────────────────┘     │
│                            │                                 │
│  ┌─────────────────────────▼──────────────────────────┐     │
│  │           Redis Store                              │     │
│  │  - Room State                                      │     │
│  │  - Player Data                                     │     │
│  │  - Game State                                      │     │
│  │  - Canvas State                                    │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

### Client Architecture (Android/Kotlin)

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ HomeScreen   │  │ LobbyScreen  │  │  GameScreen  │      │
│  │  (Compose)   │  │  (Compose)   │  │  (Compose)   │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                  │                  │              │
│         └──────────────────┼──────────────────┘              │
│                            │                                 │
└────────────────────────────┼─────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────┐
│                     ViewModel Layer                          │
│  ┌─────────────────────────▼──────────────────────────┐     │
│  │  ViewModels (StateFlow)                            │     │
│  │  - HomeViewModel                                   │     │
│  │  - LobbyViewModel                                  │     │
│  │  - GameViewModel                                   │     │
│  └─────────────────────────┬──────────────────────────┘     │
└────────────────────────────┼─────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────┐
│                      Domain Layer                            │
│  ┌─────────────────────────▼──────────────────────────┐     │
│  │  Use Cases / Repositories                          │     │
│  │  - RoomRepository                                  │     │
│  │  - GameRepository                                  │     │
│  │  - DrawingRepository                               │     │
│  └─────────────────────────┬──────────────────────────┘     │
└────────────────────────────┼─────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────┐
│                       Data Layer                             │
│  ┌─────────────────────────▼──────────────────────────┐     │
│  │  SocketManager (Ktor WebSocket)                    │     │
│  │  - Connection Management                           │     │
│  │  - Event Emission                                  │     │
│  │  - Event Listening                                 │     │
│  │  - Reconnection Logic                              │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### Android Client Components

#### 1. SocketManager

Manages WebSocket connection and communication with the server.

```kotlin
interface SocketManager {
    // Connection management
    fun connect(userId: String): Flow<ConnectionState>
    fun disconnect()
    fun isConnected(): Boolean
    
    // Event emission
    suspend fun emit(event: String, data: JsonObject)
    
    // Event listening
    fun <T> on(event: String): Flow<T>
    
    // Room operations
    suspend fun createRoom(): Result<RoomCode>
    suspend fun joinRoom(code: RoomCode): Result<Room>
    suspend fun leaveRoom()
    
    // Game operations
    suspend fun sendStroke(stroke: Stroke)
    suspend fun sendMessage(message: String)
    suspend fun startGame()
}

data class ConnectionState(
    val status: Status,
    val error: String? = null
) {
    enum class Status { CONNECTING, CONNECTED, DISCONNECTED, ERROR }
}
```

#### 2. DrawingCanvas

Composable component for touch-based drawing with stroke recording.

```kotlin
@Composable
fun DrawingCanvas(
    strokes: List<Stroke>,
    isDrawingEnabled: Boolean,
    currentTool: DrawingTool,
    onStrokeComplete: (Stroke) -> Unit,
    modifier: Modifier = Modifier
)

data class DrawingTool(
    val type: ToolType,
    val color: Color,
    val strokeWidth: Float,
    val opacity: Float
) {
    enum class ToolType { BRUSH, ERASER }
}

data class Stroke(
    val id: String,
    val points: List<Point>,
    val tool: DrawingTool,
    val timestamp: Long
)

data class Point(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f
)
```

#### 3. GameViewModel

Manages game state and coordinates between UI and data layer.

```kotlin
class GameViewModel(
    private val socketManager: SocketManager,
    private val gameRepository: GameRepository
) : ViewModel() {
    
    private val _gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    
    private val _strokes = MutableStateFlow<List<Stroke>>(emptyList())
    val strokes: StateFlow<List<Stroke>> = _strokes.asStateFlow()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    fun onStrokeDrawn(stroke: Stroke)
    fun onMessageSent(message: String)
    fun onToolSelected(tool: DrawingTool)
    fun onClearCanvas()
    fun onUndo()
}

sealed class GameState {
    object Idle : GameState()
    object Lobby : GameState()
    data class Playing(
        val round: Round,
        val players: List<Player>,
        val currentDrawer: Player,
        val timeRemaining: Int
    ) : GameState()
    data class RoundEnd(
        val word: String,
        val scores: Map<String, Int>
    ) : GameState()
    data class GameEnd(
        val finalScores: List<PlayerScore>
    ) : GameState()
}
```

#### 4. StrokeRenderer

Handles rendering of strokes with support for Phantom Stroke mode.

```kotlin
interface StrokeRenderer {
    fun render(
        canvas: Canvas,
        strokes: List<Stroke>,
        mode: RenderMode
    )
    
    fun renderPhantomStroke(
        canvas: Canvas,
        stroke: Stroke,
        progress: Float // 0.0 to 1.0
    )
}

enum class RenderMode {
    IMMEDIATE,
    PHANTOM,
    INTERPOLATED
}
```

#### 5. StrokeSynchronizer

Batches and throttles stroke data for network transmission.

```kotlin
class StrokeSynchronizer(
    private val socketManager: SocketManager,
    private val throttleMs: Long = 30
) {
    private val strokeBuffer = mutableListOf<Stroke>()
    
    suspend fun queueStroke(stroke: Stroke)
    private suspend fun flushBuffer()
}
```

### Server Components

#### 1. RoomManager

Manages room lifecycle, player membership, and room state.

```kotlin
interface RoomManager {
    fun createRoom(): RoomCode
    fun joinRoom(code: RoomCode, player: Player): Result<Room>
    fun leaveRoom(code: RoomCode, playerId: String)
    fun getRoom(code: RoomCode): Room?
    fun assignHost(code: RoomCode, playerId: String)
    fun removePlayer(code: RoomCode, playerId: String)
}

data class Room(
    val code: RoomCode,
    val host: String,
    val players: List<Player>,
    val state: RoomState,
    val gameState: GameState?,
    val createdAt: Long
)

enum class RoomState {
    LOBBY,
    PLAYING,
    FINISHED
}

typealias RoomCode = String // 6-character alphanumeric
```

#### 2. GameEngine

Orchestrates game logic, turn management, and scoring.

```kotlin
interface GameEngine {
    fun startGame(roomCode: RoomCode)
    fun startRound(roomCode: RoomCode): Round
    fun endRound(roomCode: RoomCode)
    fun validateGuess(roomCode: RoomCode, playerId: String, guess: String): GuessResult
    fun calculateScore(timeRemaining: Int, guessOrder: Int): Int
    fun selectNextDrawer(roomCode: RoomCode): Player
    fun selectWord(): Word
}

data class Round(
    val number: Int,
    val drawer: Player,
    val word: Word,
    val startTime: Long,
    val duration: Int, // seconds
    val guesses: List<Guess>
)

data class Word(
    val text: String,
    val category: String,
    val difficulty: Difficulty
)

enum class Difficulty {
    EASY, MEDIUM, HARD
}

data class Guess(
    val playerId: String,
    val guess: String,
    val timestamp: Long,
    val correct: Boolean
)

sealed class GuessResult {
    data class Correct(val points: Int) : GuessResult()
    object Incorrect : GuessResult()
    object AlreadyGuessed : GuessResult()
}
```

#### 3. CanvasStateManager

Maintains and synchronizes canvas state across clients.

```kotlin
interface CanvasStateManager {
    fun addStroke(roomCode: RoomCode, stroke: Stroke)
    fun getStrokes(roomCode: RoomCode): List<Stroke>
    fun clearCanvas(roomCode: RoomCode)
    fun undoLastStroke(roomCode: RoomCode, playerId: String)
}
```

#### 4. TimerManager

Manages round timers with server-authoritative time tracking.

```kotlin
interface TimerManager {
    fun startTimer(roomCode: RoomCode, duration: Int, onTick: (Int) -> Unit, onComplete: () -> Unit)
    fun stopTimer(roomCode: RoomCode)
    fun getRemainingTime(roomCode: RoomCode): Int
}
```

#### 5. AntiCheatValidator

Validates player actions to prevent cheating.

```kotlin
interface AntiCheatValidator {
    fun validateStroke(roomCode: RoomCode, playerId: String, stroke: Stroke): Boolean
    fun validateMessage(playerId: String, message: String): Boolean
    fun checkRateLimit(playerId: String): Boolean
    fun validateCoordinates(stroke: Stroke, canvasBounds: Rect): Boolean
}
```

## Data Models

### Core Models

```kotlin
data class Player(
    val id: String,
    val name: String,
    val score: Int = 0,
    val isHost: Boolean = false,
    val isConnected: Boolean = true,
    val hasGuessed: Boolean = false
)

data class ChatMessage(
    val id: String,
    val playerId: String,
    val playerName: String,
    val content: String,
    val type: MessageType,
    val timestamp: Long
)

enum class MessageType {
    CHAT,
    GUESS_CORRECT,
    GUESS_INCORRECT,
    SYSTEM,
    EMOJI
}

data class PlayerScore(
    val player: Player,
    val score: Int,
    val rank: Int
)
```

### WebSocket Event Models

```kotlin
// Client -> Server Events
sealed class ClientEvent {
    data class CreateRoom(val playerId: String, val playerName: String) : ClientEvent()
    data class JoinRoom(val playerId: String, val playerName: String, val roomCode: RoomCode) : ClientEvent()
    object LeaveRoom : ClientEvent()
    object StartGame : ClientEvent()
    data class SendStroke(val stroke: Stroke) : ClientEvent()
    data class SendMessage(val message: String) : ClientEvent()
    object ClearCanvas : ClientEvent()
    object UndoStroke : ClientEvent()
}

// Server -> Client Events
sealed class ServerEvent {
    data class RoomCreated(val roomCode: RoomCode, val room: Room) : ServerEvent()
    data class RoomJoined(val room: Room) : ServerEvent()
    data class PlayerJoined(val player: Player) : ServerEvent()
    data class PlayerLeft(val playerId: String) : ServerEvent()
    data class GameStarted(val round: Round) : ServerEvent()
    data class RoundStarted(val round: Round, val wordHint: String) : ServerEvent()
    data class StrokeReceived(val stroke: Stroke) : ServerEvent()
    data class MessageReceived(val message: ChatMessage) : ServerEvent()
    data class GuessCorrect(val playerId: String, val points: Int) : ServerEvent()
    data class TimerTick(val timeRemaining: Int) : ServerEvent()
    data class RoundEnded(val word: String, val scores: Map<String, Int>) : ServerEvent()
    data class GameEnded(val finalScores: List<PlayerScore>) : ServerEvent()
    data class CanvasCleared : ServerEvent()
    data class StrokeUndone(val strokeId: String) : ServerEvent()
    data class Error(val message: String) : ServerEvent()
}
```

### Redis Data Structures

```typescript
// Room data stored in Redis
interface RedisRoom {
    code: string;
    host: string;
    players: string[]; // Player IDs
    state: 'lobby' | 'playing' | 'finished';
    gameState: RedisGameState | null;
    createdAt: number;
    expiresAt: number;
}

interface RedisGameState {
    currentRound: number;
    totalRounds: number;
    currentDrawer: string;
    currentWord: string;
    roundStartTime: number;
    roundDuration: number;
    scores: Record<string, number>;
    guessedPlayers: string[];
    drawOrder: string[];
}

interface RedisPlayer {
    id: string;
    name: string;
    roomCode: string;
    score: number;
    isHost: boolean;
    lastActivity: number;
}

interface RedisCanvasState {
    roomCode: string;
    strokes: RedisStroke[];
}

interface RedisStroke {
    id: string;
    playerId: string;
    points: Array<{x: number, y: number, pressure: number}>;
    tool: {
        type: 'brush' | 'eraser';
        color: string;
        strokeWidth: number;
        opacity: number;
    };
    timestamp: number;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property 1: Room Code Uniqueness

*For any* set of room creation requests, all generated room codes should be unique 6-character alphanumeric strings.

**Validates: Requirements 1.1**

### Property 2: Host Assignment Correctness

*For any* room, the host should always be correctly assigned: the creator on room creation, and another player when the current host disconnects.

**Validates: Requirements 1.2, 1.6**

### Property 3: Room Joining Validation

*For any* room code provided by a player, the system should add them to the room if the code is valid and return an error if invalid.

**Validates: Requirements 1.3, 1.4**

### Property 4: Room Capacity Enforcement

*For any* room with 8 players, attempting to add a 9th player should be rejected.

**Validates: Requirements 1.5**

### Property 5: Player List Synchronization

*For any* room, when a player joins or leaves, all clients in that room should receive the updated player list.

**Validates: Requirements 2.1, 2.2**

### Property 6: Game State Transition

*For any* room in lobby state, when the host starts the game, all players should transition to playing state.

**Validates: Requirements 2.3**

### Property 7: Stroke Lifecycle Correctness

*For any* touch sequence (down, move, up), the client should correctly start a stroke, record all points, complete the stroke, and render it immediately.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4**

### Property 8: Stroke Properties Completeness

*For any* recorded stroke, it should contain color, width, and opacity properties.

**Validates: Requirements 3.5**

### Property 9: Canvas Modification Operations

*For any* canvas with strokes, erase operations should remove targeted strokes, clear should remove all strokes, and undo should remove the last stroke.

**Validates: Requirements 3.6, 3.7, 16.4, 16.5**

### Property 10: Stroke Synchronization

*For any* stroke created by the drawer (including erase strokes), all guessers in the room should receive and render that stroke.

**Validates: Requirements 4.2, 4.3, 4.6**

### Property 11: Stroke Batching

*For any* rapid sequence of strokes, the system should batch them into groups with at least 30ms intervals to prevent flooding.

**Validates: Requirements 4.5**

### Property 12: Word Selection

*For any* round start, the server should assign a word from the word list to that round.

**Validates: Requirements 5.1**

### Property 13: Word Information Distribution

*For any* selected word, the drawer should receive the complete word while guessers should receive only the word length and category.

**Validates: Requirements 5.2, 5.3**

### Property 14: Word Uniqueness Per Session

*For any* game session, no word should be used more than once across all rounds.

**Validates: Requirements 5.5**

### Property 15: Message Transmission

*For any* chat message sent by a guesser, the server should receive it.

**Validates: Requirements 6.1**

### Property 16: Guess Validation Normalization

*For any* guess, the server should perform case-insensitive comparison and ignore leading/trailing whitespace when validating against the word.

**Validates: Requirements 6.2, 6.6, 6.7**

### Property 17: Guess Result Handling

*For any* guess, if correct, the guesser should receive points and all players should be notified; if incorrect, it should be broadcast as a chat message.

**Validates: Requirements 6.3, 6.4**

### Property 18: Repeat Guess Prevention

*For any* player who has guessed correctly in a round, subsequent guesses from that player should be hidden or rejected for the remainder of the round.

**Validates: Requirements 6.5**

### Property 19: Score Calculation

*For any* correct guess, points should be awarded based on remaining time, with bonus points for the first correct guesser, and points awarded to the drawer when at least one player guesses correctly.

**Validates: Requirements 7.1, 7.2, 7.3**

### Property 20: Score Synchronization

*For any* round end, all players should receive updated scores.

**Validates: Requirements 7.4**

### Property 21: Scoreboard Display

*For any* game state, the client should display a scoreboard containing all players and their current scores.

**Validates: Requirements 7.5**

### Property 22: Final Rankings

*For any* completed game session, the client should display final rankings sorted by score.

**Validates: Requirements 7.6**

### Property 23: Timer Initialization

*For any* round start, the server should initialize a 60-second countdown timer.

**Validates: Requirements 8.1**

### Property 24: Timer Updates

*For any* active round timer, the server should broadcast time updates at regular intervals.

**Validates: Requirements 8.2**

### Property 25: Round Ending Conditions

*For any* round, it should end when the timer reaches zero OR when all guessers have guessed correctly, and the correct word should be revealed to all players.

**Validates: Requirements 8.3, 8.4, 8.5**

### Property 26: Drawer Rotation

*For any* game session, the drawer should rotate through all player