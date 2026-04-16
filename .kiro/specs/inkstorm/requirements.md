# Requirements Document: InkStorm

## Introduction

InkStorm is a real-time multiplayer drawing and guessing game for Android where one player draws while others simultaneously guess the word. The game features room-based multiplayer with live drawing synchronization, chat functionality, scoring systems, and automatic round rotation. The system includes unique features like Phantom Stroke Rendering, Silent Mode rounds, and a Pressure Meter to differentiate from standard drawing games.

## Glossary

- **Game_System**: The complete InkStorm application including client and server components
- **Client**: The Android application running on a player's device
- **Server**: The backend Node.js service managing game state and communication
- **Room**: A game session identified by a 6-character code where players gather
- **Host**: The player who created the room and has management privileges
- **Drawer**: The player currently drawing in a round
- **Guesser**: A player attempting to guess the word being drawn
- **Stroke**: A continuous drawing path from touch-down to touch-up
- **Round**: A single drawing and guessing phase with one drawer
- **Game_Session**: Multiple rounds played consecutively in a room
- **Phantom_Stroke**: A stroke that renders in fragments over time
- **Silent_Mode**: A round type where only emoji reactions are allowed
- **Pressure_Meter**: A mechanic that reduces drawing speed over time

## Requirements

### Requirement 1: Room Creation and Management

**User Story:** As a player, I want to create and join game rooms, so that I can play with specific groups of people.

#### Acceptance Criteria

1. WHEN a player requests room creation, THE Game_System SHALL generate a unique 6-character alphanumeric room code
2. WHEN a room is created, THE Game_System SHALL assign the creator as the Host
3. WHEN a player provides a valid room code, THE Game_System SHALL add them to that room
4. WHEN a player provides an invalid room code, THE Game_System SHALL return an error message
5. WHEN a room reaches 8 players, THE Game_System SHALL prevent additional players from joining
6. WHEN the Host disconnects, THE Game_System SHALL assign Host privileges to another player in the room

### Requirement 2: Lobby Management

**User Story:** As a player in a lobby, I want to see who else is in the room and manage game settings, so that I can prepare for the game.

#### Acceptance Criteria

1. WHEN a player joins a room, THE Client SHALL display the list of all players currently in the room
2. WHEN a player joins or leaves the room, THE Game_System SHALL broadcast the updated player list to all room members
3. WHEN the Host starts the game, THE Game_System SHALL transition all players from lobby to game state
4. WHILE in the lobby, THE Client SHALL display the room code prominently
5. WHEN a player is in the lobby, THE Client SHALL provide a button to leave the room

### Requirement 3: Drawing Canvas

**User Story:** As a drawer, I want to draw on a canvas with touch input, so that I can illustrate the word for other players.

#### Acceptance Criteria

1. WHEN the Drawer touches the canvas, THE Client SHALL begin recording a stroke at the touch coordinates
2. WHILE the Drawer moves their finger on the canvas, THE Client SHALL continuously record stroke points
3. WHEN the Drawer lifts their finger, THE Client SHALL complete the stroke
4. WHEN a stroke is recorded, THE Client SHALL render it immediately on the local canvas
5. THE Client SHALL support stroke properties including color, width, and opacity
6. WHEN the Drawer selects the eraser tool, THE Client SHALL record erase strokes that remove previous content
7. WHEN the Drawer clears the canvas, THE Client SHALL remove all strokes

### Requirement 4: Real-Time Stroke Synchronization

**User Story:** As a guesser, I want to see the drawer's strokes in real-time, so that I can make informed guesses.

#### Acceptance Criteria

1. WHEN the Drawer creates a stroke, THE Client SHALL send stroke data to the Server within 30 milliseconds
2. WHEN the Server receives stroke data, THE Server SHALL broadcast it to all Guessers in the room
3. WHEN a Guesser receives stroke data, THE Client SHALL render the stroke on their canvas
4. WHEN network latency occurs, THE Client SHALL buffer and interpolate strokes to maintain smooth rendering
5. THE Game_System SHALL throttle stroke transmission to batches of 30 milliseconds to prevent network flooding
6. WHEN the Drawer uses the eraser, THE Game_System SHALL synchronize erase strokes to all Guessers

### Requirement 5: Word Selection and Security

**User Story:** As a player, I want the word selection to be fair and secure, so that the game remains challenging and fun.

#### Acceptance Criteria

1. WHEN a round begins, THE Server SHALL randomly select a word from the word list
2. WHEN a word is selected, THE Server SHALL send the complete word only to the Drawer
3. WHEN a word is selected, THE Server SHALL send the word length and category to all Guessers
4. THE Server SHALL maintain a word list with at least 500 words across multiple difficulty levels
5. WHEN a word is used in a round, THE Server SHALL not reuse it in the same Game_Session

### Requirement 6: Chat and Guess Validation

**User Story:** As a guesser, I want to submit guesses through chat, so that I can earn points when correct.

#### Acceptance Criteria

1. WHEN a Guesser sends a chat message, THE Client SHALL transmit it to the Server
2. WHEN the Server receives a guess, THE Server SHALL validate it against the current word
3. WHEN a guess is correct, THE Server SHALL award points to the Guesser and notify all players
4. WHEN a guess is incorrect, THE Server SHALL broadcast it as a chat message to all players
5. WHEN a guess is correct, THE Game_System SHALL hide subsequent guesses from that player for the remainder of the round
6. THE Server SHALL perform case-insensitive comparison for guess validation
7. THE Server SHALL ignore leading and trailing whitespace in guesses

### Requirement 7: Scoring System

**User Story:** As a player, I want to earn points for correct guesses and successful drawings, so that I can compete with others.

#### Acceptance Criteria

1. WHEN a Guesser correctly guesses the word, THE Server SHALL award points based on remaining time
2. WHEN a Guesser guesses correctly first, THE Server SHALL award bonus points
3. WHEN at least one player guesses correctly, THE Server SHALL award points to the Drawer
4. WHEN the round ends, THE Server SHALL broadcast updated scores to all players
5. THE Client SHALL display a real-time scoreboard showing all player scores
6. WHEN a Game_Session ends, THE Client SHALL display final rankings

### Requirement 8: Round Timer and Management

**User Story:** As a player, I want rounds to have time limits, so that the game maintains good pacing.

#### Acceptance Criteria

1. WHEN a round begins, THE Server SHALL start a 60-second countdown timer
2. WHILE the timer is running, THE Server SHALL broadcast time updates every second
3. WHEN the timer reaches zero, THE Server SHALL end the round automatically
4. WHEN all Guessers have guessed correctly, THE Server SHALL end the round early
5. WHEN a round ends, THE Server SHALL reveal the correct word to all players
6. THE Client SHALL display the remaining time prominently during gameplay

### Requirement 9: Round Rotation and Turn Management

**User Story:** As a player, I want everyone to get a turn drawing, so that the game is fair and engaging.

#### Acceptance Criteria

1. WHEN a Game_Session starts, THE Server SHALL assign the first Drawer randomly
2. WHEN a round ends, THE Server SHALL assign the next player as Drawer in rotation order
3. WHEN all players have drawn once, THE Server SHALL complete one full round cycle
4. THE Server SHALL track which players have drawn in the current Game_Session
5. WHEN a Drawer disconnects during their turn, THE Server SHALL skip to the next player
6. WHEN a new round begins, THE Server SHALL clear the canvas for all players

### Requirement 10: Phantom Stroke Rendering

**User Story:** As a player, I want strokes to appear gradually in fragments, so that the game has a unique visual style and increased difficulty.

#### Acceptance Criteria

1. WHEN Phantom Stroke mode is enabled, THE Client SHALL render incoming strokes in fragments
2. WHILE rendering a Phantom Stroke, THE Client SHALL progressively reveal segments over 2 seconds
3. WHEN a stroke is complete, THE Client SHALL display the full stroke path
4. THE Client SHALL maintain smooth animation between stroke fragments
5. WHEN Phantom Stroke mode is disabled, THE Client SHALL render strokes immediately

### Requirement 11: Silent Mode Round

**User Story:** As a player, I want some rounds to restrict chat to emojis only, so that the game has varied difficulty and communication styles.

#### Acceptance Criteria

1. WHEN Silent Mode is active, THE Client SHALL disable text input in the chat
2. WHILE Silent Mode is active, THE Client SHALL provide an emoji picker interface
3. WHEN a player sends an emoji in Silent Mode, THE Server SHALL broadcast it to all players
4. WHEN Silent Mode is active, THE Server SHALL still validate guesses submitted through alternative means
5. WHEN Silent Mode ends, THE Client SHALL restore normal chat functionality

### Requirement 12: Pressure Meter

**User Story:** As a drawer, I want my drawing speed to decrease over time, so that the game becomes progressively more challenging.

#### Acceptance Criteria

1. WHEN Pressure Meter is enabled, THE Client SHALL track the Drawer's stroke velocity
2. WHILE the round progresses, THE Client SHALL gradually reduce maximum stroke speed by 50% over 60 seconds
3. WHEN the Drawer attempts to draw faster than the current limit, THE Client SHALL throttle stroke point recording
4. THE Client SHALL display a visual indicator of the current Pressure Meter level
5. WHEN a new round begins, THE Client SHALL reset the Pressure Meter to full speed

### Requirement 13: Disconnection and Reconnection Handling

**User Story:** As a player, I want to reconnect to my game if I lose connection, so that temporary network issues don't ruin my experience.

#### Acceptance Criteria

1. WHEN a player disconnects, THE Server SHALL maintain their room membership for 60 seconds
2. WHEN a disconnected player reconnects within 60 seconds, THE Server SHALL restore their game state
3. WHEN a player reconnects, THE Client SHALL synchronize the current canvas state from the Server
4. WHEN a player disconnects for more than 60 seconds, THE Server SHALL remove them from the room
5. WHEN a player disconnects, THE Server SHALL notify all other players in the room
6. WHEN the Drawer disconnects, THE Server SHALL end the current round and proceed to the next

### Requirement 14: Anti-Cheat and Validation

**User Story:** As a player, I want the game to prevent cheating, so that competition remains fair.

#### Acceptance Criteria

1. WHEN a player sends more than 100 messages per minute, THE Server SHALL rate-limit their messages
2. WHEN the Server receives stroke data, THE Server SHALL validate that it originates from the current Drawer
3. WHEN a Guesser attempts to send stroke data, THE Server SHALL reject it
4. THE Server SHALL maintain authoritative game state for all scoring and validation
5. WHEN suspicious activity is detected, THE Server SHALL log it for review
6. THE Server SHALL validate that stroke coordinates are within canvas bounds

### Requirement 15: User Interface and Navigation

**User Story:** As a player, I want intuitive navigation between screens, so that I can easily access game features.

#### Acceptance Criteria

1. WHEN the app launches, THE Client SHALL display the home screen with options to create or join a room
2. WHEN a player creates or joins a room, THE Client SHALL navigate to the lobby screen
3. WHEN the game starts, THE Client SHALL navigate to the game screen
4. WHEN a Game_Session ends, THE Client SHALL display the results screen
5. THE Client SHALL provide a back button to return to the previous screen where appropriate
6. WHEN a player is in an active game, THE Client SHALL display a confirmation dialog before leaving

### Requirement 16: Drawing Tools and Controls

**User Story:** As a drawer, I want access to various drawing tools, so that I can create clear illustrations.

#### Acceptance Criteria

1. THE Client SHALL provide a color picker with at least 12 color options
2. THE Client SHALL provide brush size selection with at least 3 size options
3. THE Client SHALL provide an eraser tool that removes previous strokes
4. THE Client SHALL provide an undo button that removes the last stroke
5. THE Client SHALL provide a clear canvas button that removes all strokes
6. WHEN a tool is selected, THE Client SHALL provide visual feedback of the active tool
7. THE Client SHALL display drawing tools in an accessible toolbar

### Requirement 17: Network Communication Protocol

**User Story:** As a developer, I want a well-defined communication protocol, so that the client and server can reliably exchange data.

#### Acceptance Criteria

1. THE Game_System SHALL use WebSocket protocol for all real-time communication
2. WHEN a connection is established, THE Client SHALL authenticate with the Server
3. THE Game_System SHALL use JSON serialization for all message payloads
4. WHEN a message fails to send, THE Client SHALL retry up to 3 times with exponential backoff
5. THE Server SHALL acknowledge critical messages with confirmation responses
6. THE Game_System SHALL define distinct message types for each game event

### Requirement 18: State Management

**User Story:** As a developer, I want robust state management, so that the application remains consistent and predictable.

#### Acceptance Criteria

1. THE Client SHALL use ViewModel pattern to manage UI state
2. THE Client SHALL use StateFlow to expose state to UI components
3. WHEN state changes occur, THE Client SHALL update the UI reactively
4. THE Server SHALL maintain authoritative state for all game logic
5. WHEN state conflicts occur, THE Client SHALL defer to Server state
6. THE Client SHALL persist minimal state locally for reconnection scenarios
