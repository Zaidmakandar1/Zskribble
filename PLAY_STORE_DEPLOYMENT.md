# 🚀 Google Play Store Deployment Guide

## Complete Step-by-Step Guide to Publishing Your Drawing Game

---

## 📋 Pre-Deployment Checklist

### 1. **Prepare Your App**

#### A. Update App Information
Edit `app/build.gradle.kts`:

```kotlin
android {
    namespace = "com.example.zskribble"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yourcompany.inkstorm"  // Change this to unique ID
        minSdk = 24
        targetSdk = 34
        versionCode = 1        // Increment for each release
        versionName = "1.0.0"  // User-facing version
    }
}
```

#### B. Create App Icon
- Size: 512x512 px (for Play Store)
- Format: PNG with transparency
- Place in: `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`
- Use Android Studio's Image Asset Studio: Right-click `res` → New → Image Asset

#### C. Add Privacy Policy
- Required by Google Play
- Host on a public URL (GitHub Pages, your website, etc.)
- Must include:
  - What data you collect
  - How you use Firebase
  - User rights

---

## 🔐 Step 1: Generate Signing Key

### Create Keystore File

Open terminal in your project root:

```bash
# Windows (PowerShell)
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias

# You'll be asked:
# - Password (remember this!)
# - Your name
# - Organization
# - City, State, Country
```

**IMPORTANT**: 
- Save the keystore file (`my-release-key.jks`) in a SAFE location
- NEVER commit it to Git
- If you lose it, you can NEVER update your app!

### Configure Signing in Gradle

Create `keystore.properties` in project root:

```properties
storePassword=YOUR_KEYSTORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=my-key-alias
storeFile=../my-release-key.jks
```

Add to `.gitignore`:
```
keystore.properties
*.jks
*.keystore
```

Update `app/build.gradle.kts`:

```kotlin
// At the top, before android block
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    // ... existing config ...
    
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

---

## 📦 Step 2: Build Release APK/AAB

### Option A: Build AAB (Recommended - Required for Play Store)

```bash
# In Android Studio:
# Build → Generate Signed Bundle / APK → Android App Bundle → Next
# Select your keystore → Enter passwords → Release → Finish

# Or via command line:
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
```

### Option B: Build APK (For testing)

```bash
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

---

## 🧪 Step 3: Test Your Release Build

### Install and Test APK

```bash
# Install on connected device
adb install app/build/outputs/apk/release/app-release.apk

# Test thoroughly:
# ✓ All features work
# ✓ No crashes
# ✓ Firebase works
# ✓ Drawing syncs properly
# ✓ Room creation/joining works
```

---

## 🏪 Step 4: Create Google Play Console Account

### A. Sign Up
1. Go to: https://play.google.com/console
2. Sign in with Google account
3. Pay **$25 one-time registration fee**
4. Complete account details

### B. Create App
1. Click "Create app"
2. Fill in:
   - App name: "InkStorm" (or your chosen name)
   - Default language: English
   - App or game: Game
   - Free or paid: Free
3. Accept declarations
4. Click "Create app"

---

## 📝 Step 5: Complete Store Listing

### A. Main Store Listing

Navigate to: **Grow → Store presence → Main store listing**

Fill in:
- **App name**: InkStorm - Drawing & Guessing Game
- **Short description** (80 chars):
  ```
  Real-time multiplayer drawing game. Draw, guess, and compete with friends!
  ```
- **Full description** (4000 chars):
  ```
  🎨 InkStorm - The Ultimate Drawing & Guessing Game!

  Join the fun in this real-time multiplayer drawing game where creativity meets competition!

  🎮 HOW TO PLAY:
  • Create or join a room with friends
  • Take turns drawing words while others guess
  • Earn points for correct guesses
  • Fastest guessers get bonus points!

  ✨ FEATURES:
  • Real-time multiplayer gameplay
  • Easy room creation with 6-digit codes
  • Multiple drawing tools and colors
  • Live chat for guessing
  • Automatic turn rotation
  • Score tracking and rankings
  • Modern, intuitive interface

  🏆 PERFECT FOR:
  • Playing with friends and family
  • Ice breakers and party games
  • Testing your drawing skills
  • Quick fun gaming sessions

  📱 SIMPLE & FUN:
  No registration required - just create a room and start playing!

  Download now and unleash your inner artist! 🎨
  ```

### B. Graphics Assets (REQUIRED)

You need to create:

1. **App Icon** (512 x 512 px)
   - PNG, 32-bit
   - No transparency

2. **Feature Graphic** (1024 x 500 px)
   - JPG or PNG
   - Showcases your app

3. **Screenshots** (At least 2, max 8)
   - Phone: 16:9 or 9:16 ratio
   - Minimum: 320px
   - Maximum: 3840px
   - Show: Home screen, Lobby, Game screen, Results

4. **Optional but Recommended**:
   - Promo video (YouTube link)
   - TV banner (1280 x 720 px)

### C. Categorization
- **App category**: Games → Casual
- **Tags**: drawing, multiplayer, party game, guessing game
- **Content rating**: Complete questionnaire (likely PEGI 3/Everyone)

### D. Contact Details
- Email: your-support@email.com
- Privacy Policy URL: (required!)
- Website: (optional)

---

## 🔒 Step 6: Content Rating

1. Go to: **Policy → App content → Content rating**
2. Click "Start questionnaire"
3. Answer questions honestly:
   - No violence
   - No user-generated content (drawings are temporary)
   - No social features beyond gameplay
4. Submit for rating
5. You'll get: PEGI 3, ESRB Everyone, etc.

---

## 🛡️ Step 7: Complete App Content Declarations

### A. Privacy Policy
- **Required**: Yes
- Upload your privacy policy URL

### B. Data Safety
Navigate to: **Policy → App content → Data safety**

Declare what data you collect:
- ✓ User IDs (Firebase Anonymous Auth)
- ✓ App interactions (gameplay data)
- Data is encrypted in transit
- Users can request deletion

### C. Ads Declaration
- Does your app contain ads? **No** (unless you added them)

### D. Target Audience
- Target age: Everyone (3+)

---

## 🚀 Step 8: Upload Your App Bundle

### A. Create Release
1. Go to: **Release → Production → Create new release**
2. Upload your AAB file: `app-release.aab`
3. Release name: "1.0.0" (matches versionName)
4. Release notes:
   ```
   🎉 Initial Release!
   
   • Real-time multiplayer drawing game
   • Create/join rooms with friends
   • Multiple drawing tools
   • Live chat and scoring
   • Beautiful modern UI
   ```

### B. Review Release
- Check all warnings
- Fix any issues
- Countries: Select all or specific countries

---

## ✅ Step 9: Submit for Review

### Pre-Submission Checklist:
- ✓ Store listing complete
- ✓ Graphics uploaded
- ✓ Content rating received
- ✓ Privacy policy added
- ✓ Data safety completed
- ✓ App bundle uploaded
- ✓ Release notes written

### Submit:
1. Click "Review release"
2. Review all sections
3. Click "Start rollout to Production"

### Review Time:
- Usually: 1-3 days
- Can take up to 7 days
- You'll get email notifications

---

## 📊 Step 10: Post-Launch

### A. Monitor Performance
- Check crash reports
- Read user reviews
- Monitor Firebase analytics

### B. Update Your App
When you need to update:

1. Increment version in `build.gradle.kts`:
   ```kotlin
   versionCode = 2        // Always increment
   versionName = "1.0.1"  // Update version
   ```

2. Build new AAB
3. Go to: **Release → Production → Create new release**
4. Upload new AAB
5. Add release notes
6. Submit

---

## 🎯 Quick Command Reference

```bash
# Build release AAB
./gradlew bundleRelease

# Build release APK
./gradlew assembleRelease

# Install APK on device
adb install app/build/outputs/apk/release/app-release.apk

# Check app size
ls -lh app/build/outputs/bundle/release/app-release.aab

# Clean build
./gradlew clean
```

---

## ⚠️ Common Issues & Solutions

### Issue: "App not signed"
**Solution**: Make sure you configured signing in `build.gradle.kts`

### Issue: "Version code must be higher"
**Solution**: Increment `versionCode` in `build.gradle.kts`

### Issue: "Privacy policy required"
**Solution**: Create and host a privacy policy, add URL to store listing

### Issue: "Screenshots required"
**Solution**: Take at least 2 screenshots of your app

### Issue: "Content rating incomplete"
**Solution**: Complete the content rating questionnaire

---

## 💡 Pro Tips

1. **Test on Multiple Devices**: Use Firebase Test Lab (free tier available)
2. **Beta Testing**: Use internal/closed testing track before production
3. **Staged Rollout**: Release to 10% → 50% → 100% of users
4. **App Size**: Keep under 100MB for better downloads
5. **Keywords**: Use relevant keywords in description for ASO
6. **Updates**: Release updates regularly to maintain ranking
7. **Reviews**: Respond to user reviews promptly

---

## 📱 Alternative: Direct APK Distribution

If you want to distribute without Play Store:

1. Build APK: `./gradlew assembleRelease`
2. Share APK file directly
3. Users must enable "Install from unknown sources"
4. No automatic updates
5. Less trust from users

---

## 🔗 Useful Links

- [Google Play Console](https://play.google.com/console)
- [Android Developer Docs](https://developer.android.com/distribute)
- [Privacy Policy Generator](https://www.privacypolicygenerator.info/)
- [App Icon Generator](https://romannurik.github.io/AndroidAssetStudio/)
- [Screenshot Maker](https://screenshots.pro/)

---

## 📞 Need Help?

- Google Play Support: https://support.google.com/googleplay/android-developer
- Android Developers Community: https://developer.android.com/community

---

**Good luck with your launch! 🚀**
