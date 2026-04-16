# Firebase Setup Instructions

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Name it "InkStorm" (or your preferred name)
4. Disable Google Analytics (optional)
5. Click "Create project"

## Step 2: Add Android App

1. In your Firebase project, click the Android icon
2. Enter package name: `com.example.zskribble`
3. Download `google-services.json`
4. Place it in `app/` directory (same level as `build.gradle.kts`)

## Step 3: Enable Firebase Services

### Enable Realtime Database
1. In Firebase Console, go to "Build" → "Realtime Database"
2. Click "Create Database"
3. Choose location (e.g., us-central1)
4. Start in "Test mode" for development
5. Click "Enable"

### Enable Authentication
1. Go to "Build" → "Authentication"
2. Click "Get started"
3. Enable "Anonymous" sign-in method
4. Click "Save"

## Step 4: Configure Database Rules (Development)

In Realtime Database → Rules tab, use these rules for development:

```json
{
  "rules": {
    "rooms": {
      "$roomCode": {
        ".read": true,
        ".write": true
      }
    },
    "strokes": {
      "$roomCode": {
        ".read": true,
        ".write": true
      }
    }
  }
}
```

⚠️ **Important**: These rules are for development only. For production, implement proper security rules.

## Step 5: Build and Run

```bash
./gradlew build
```

Then run the app on your Android device or emulator.

## Project Structure

```
InkStorm/
├── app/
│   ├── google-services.json  ← Place Firebase config here
│   ├── build.gradle.kts
│   └── src/
├── build.gradle.kts
└── FIREBASE_SETUP.md
```

## Testing

1. Run the app on two devices/emulators
2. Create a room on device 1
3. Note the 6-character room code
4. Join the room on device 2 using the code
5. Start the game and test drawing synchronization

## Troubleshooting

- **Build fails**: Ensure `google-services.json` is in the `app/` directory
- **Connection fails**: Check internet connection and Firebase project settings
- **Strokes not syncing**: Verify Realtime Database rules are set correctly
