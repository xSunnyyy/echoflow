# Building Plex Glass Player APKs

This guide explains how to build APKs for the Plex Glass Player native Android app.

**⚠️ Important**: This is a **native Kotlin Android app**, not a React Native app. You cannot use Expo.dev or similar React Native build services.

## Table of Contents

1. [Local Build (Gradle)](#local-build-gradle)
2. [Android Studio Build](#android-studio-build)
3. [GitHub Actions CI/CD](#github-actions-cicd)
4. [Signing Release APKs](#signing-release-apks)
5. [Troubleshooting](#troubleshooting)

## Local Build (Gradle)

### Prerequisites

- JDK 17 installed
- Android SDK installed (via Android Studio or command-line tools)
- Git

### Quick Build

```bash
# Debug APK (for testing)
./gradlew assembleDebug

# Release APK (unsigned)
./gradlew assembleRelease
```

### Using the Build Script

```bash
# Run the interactive build script
./build-apk.sh
```

The script will:
1. Ask for build type (Debug or Release)
2. Build the APK
3. Show the output location
4. Provide installation instructions

### Output Locations

- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`

### Installing the APK

```bash
# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk

# Install on specific device
adb -s <device-id> install app/build/outputs/apk/debug/app-debug.apk
```

## Android Studio Build

### Steps

1. **Open project** in Android Studio
2. **Select build variant**:
   - `Build` → `Select Build Variant`
   - Choose `debug` or `release`
3. **Build APK**:
   - `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
4. **Find APK**:
   - Click "locate" in the notification
   - Or navigate to `app/build/outputs/apk/`

### Build AAB (for Google Play)

```bash
# Build Android App Bundle
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
```

## GitHub Actions CI/CD

Create automated builds on every commit using GitHub Actions.

### Setup

Create `.github/workflows/build.yml`:

```yaml
name: Build APK

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Setup Android SDK
      uses: android-actions/setup-android@v3

    - name: Cache Gradle packages
      uses: actions/cache@v4
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Build Debug APK
      run: ./gradlew assembleDebug

    - name: Upload Debug APK
      uses: actions/upload-artifact@v4
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

### Download Built APK

1. Go to **Actions** tab in GitHub
2. Click on the latest workflow run
3. Download the artifact

## Signing Release APKs

Release APKs must be signed before distribution.

### 1. Generate Keystore

```bash
keytool -genkey -v \
  -keystore release.keystore \
  -alias plex-glass-player \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Answer the prompts and **remember your passwords**!

### 2. Configure Signing

Create `keystore.properties` in project root:

```properties
storePassword=YOUR_KEYSTORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=plex-glass-player
storeFile=../release.keystore
```

**⚠️ Never commit this file to Git!**

Add to `.gitignore`:
```
keystore.properties
release.keystore
```

### 3. Update app/build.gradle.kts

```kotlin
android {
    // ... existing config

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = java.util.Properties()
                keystoreProperties.load(keystorePropertiesFile.inputStream())

                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... rest of release config
        }
    }
}
```

### 4. Build Signed APK

```bash
./gradlew assembleRelease
```

The signed APK will be at:
`app/build/outputs/apk/release/app-release.apk`

## Alternative Build Services for Native Android

Since Expo doesn't work for native Android, here are alternatives:

### 1. **Bitrise** (Recommended)
- https://bitrise.io
- Free tier available
- Native Android support
- Easy GitHub integration
- Automatic builds on commit

### 2. **CircleCI**
- https://circleci.com
- Good free tier
- Supports native Android
- Fast builds

### 3. **GitHub Actions** (Free)
- Built into GitHub
- Unlimited for public repos
- See workflow above

### 4. **GitLab CI**
- https://gitlab.com
- Free CI/CD included
- Native Android support

### 5. **Codemagic**
- https://codemagic.io
- Designed for Flutter/Native apps
- Free tier for open source

## Build Variants

### Debug Build
```bash
./gradlew assembleDebug
```

**Characteristics:**
- Includes debugging symbols
- Not optimized
- Can be debugged
- Larger file size
- **Not signed** (works on dev devices)

### Release Build
```bash
./gradlew assembleRelease
```

**Characteristics:**
- Optimized with ProGuard/R8
- Smaller file size
- Better performance
- **Must be signed** for installation

## Troubleshooting

### "gradlew not found"

```bash
gradle wrapper --gradle-version 8.7
chmod +x gradlew
```

### "SDK not found"

Set `ANDROID_HOME` environment variable:

```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### "Build failed with exception"

1. Clean build:
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

2. Check Java version:
   ```bash
   java -version  # Should be 17
   ```

3. Update Gradle:
   ```bash
   ./gradlew wrapper --gradle-version 8.7
   ```

### "Unsigned APK cannot be installed"

You need to sign the release APK (see [Signing](#signing-release-apks)) or use debug build:

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## File Size Optimization

### Enable R8 Shrinking

Already configured in `app/build.gradle.kts`:

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        proguardFiles(...)
    }
}
```

### Split APKs by ABI

Reduce APK size by building separate APKs for each CPU architecture:

```kotlin
android {
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = false
        }
    }
}
```

This creates multiple APKs (one per architecture) instead of a universal APK.

## Continuous Deployment

### To Google Play (Manual)

1. Build signed AAB:
   ```bash
   ./gradlew bundleRelease
   ```

2. Upload to Play Console:
   - https://play.google.com/console
   - Create app listing
   - Upload AAB from `app/build/outputs/bundle/release/`

### To Internal Testing (GitHub Releases)

GitHub Actions workflow for auto-releases:

```yaml
- name: Create Release
  uses: softprops/action-gh-release@v1
  if: startsWith(github.ref, 'refs/tags/')
  with:
    files: app/build/outputs/apk/release/app-release.apk
```

## Summary

**For Development:**
```bash
./build-apk.sh  # Interactive
# or
./gradlew assembleDebug
```

**For Release:**
1. Set up keystore (one-time)
2. Configure signing (one-time)
3. Build: `./gradlew assembleRelease`

**For CI/CD:**
- Use GitHub Actions (free, included)
- Or Bitrise/CircleCI/Codemagic

**NOT Compatible:**
- ❌ Expo.dev (React Native only)
- ❌ Ionic Appflow (Ionic/Cordova only)
- ❌ Capacitor build services (Hybrid apps only)

---

For questions, see the main [README.md](README.md) or open an issue.
