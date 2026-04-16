# ✅ Quick Deployment Checklist

## Before You Start
- [ ] Test app thoroughly on multiple devices
- [ ] All features working (drawing, chat, rooms, scoring)
- [ ] No crashes or major bugs
- [ ] Firebase is properly configured

## 1. Generate Keystore (One-time)
```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```
- [ ] Save keystore file in safe location
- [ ] Remember passwords!

## 2. Create keystore.properties
```properties
storePassword=YOUR_PASSWORD
keyPassword=YOUR_PASSWORD
keyAlias=my-key-alias
storeFile=../my-release-key.jks
```
- [ ] File created in project root
- [ ] Added to .gitignore

## 3. Update App Info (app/build.gradle.kts)
- [ ] Change `applicationId` to unique package name
- [ ] Set `versionCode = 1`
- [ ] Set `versionName = "1.0.0"`

## 4. Build Release
```bash
./gradlew bundleRelease
```
- [ ] Build successful
- [ ] AAB file created: `app/build/outputs/bundle/release/app-release.aab`

## 5. Test Release Build
```bash
./gradlew assembleRelease
adb install app/build/outputs/apk/release/app-release.apk
```
- [ ] Installed on device
- [ ] All features tested
- [ ] No crashes

## 6. Google Play Console Setup
- [ ] Account created ($25 fee paid)
- [ ] New app created
- [ ] App name chosen

## 7. Store Listing
- [ ] App name
- [ ] Short description (80 chars)
- [ ] Full description (4000 chars)
- [ ] App icon (512x512)
- [ ] Feature graphic (1024x500)
- [ ] Screenshots (minimum 2)
- [ ] Category selected
- [ ] Contact email

## 8. App Content
- [ ] Privacy policy created and uploaded
- [ ] Content rating completed
- [ ] Data safety form filled
- [ ] Target audience set

## 9. Upload & Submit
- [ ] AAB uploaded
- [ ] Release notes written
- [ ] Countries selected
- [ ] Review and submit

## 10. Wait for Review
- [ ] Usually 1-3 days
- [ ] Check email for updates
- [ ] Fix any issues if rejected

---

## Quick Commands

```bash
# Build release AAB
./gradlew bundleRelease

# Build release APK (for testing)
./gradlew assembleRelease

# Install APK
adb install app/build/outputs/apk/release/app-release.apk

# Clean build
./gradlew clean
```

---

## Important Files Location

- **AAB**: `app/build/outputs/bundle/release/app-release.aab`
- **APK**: `app/build/outputs/apk/release/app-release.apk`
- **Keystore**: `my-release-key.jks` (keep safe!)
- **Config**: `keystore.properties` (don't commit!)

---

## For Updates

1. Increment `versionCode` in `build.gradle.kts`
2. Update `versionName`
3. Build new AAB
4. Upload to Play Console
5. Add release notes
6. Submit

---

## Need Help?
See `PLAY_STORE_DEPLOYMENT.md` for detailed instructions.
