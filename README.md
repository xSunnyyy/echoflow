# Plex Glass Player 🎵

A modern Android music player for Plex with a stunning frosted-glass UI inspired by visionOS, built with Jetpack Compose and Material You.

![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-blue.svg)
![MinSDK](https://img.shields.io/badge/MinSDK-26-orange.svg)

## ✨ Features

### Current (MVP)
- ✅ **Plex OAuth Authentication** - Secure PIN-based authentication with system browser
- ✅ **Multi-Server Support** - Browse and select from your available Plex servers
- ✅ **Music Library Browsing** - Paginated lists of artists, albums, and tracks
- ✅ **Search** - Debounced search across your entire music library
- ✅ **Frosted Glass UI** - Beautiful translucent design with Material You dynamic colors
- ✅ **Clean Architecture** - MVI pattern with multi-module structure

### Coming Soon
- 🚧 **Media Playback** - ExoPlayer integration with queue management
- 🚧 **Offline Downloads** - Manual track/album downloads via WorkManager
- 🚧 **Now Playing Screen** - Full-screen playback controls
- 🚧 **Mini Player** - Persistent bottom player
- 🚧 **Customizable Home** - Drag-and-drop sections

## 🏗️ Architecture

Built with **Clean Architecture** + **MVI** pattern across 18 modules:

```
app/                  # Main application
core/                 # Core infrastructure (UI, DB, Network)
data/                 # Data layer (API, Auth, Repositories)
domain/               # Business logic (Use Cases)
features/             # Feature modules (Auth, Library, Search, etc.)
```

## 🛠️ Tech Stack

- **Kotlin**, **Jetpack Compose**, **Material 3**
- **Hilt**, **Retrofit**, **Room**, **DataStore**
- **Media3 (ExoPlayer)**, **Coil**, **WorkManager**
- **kotlinx.serialization**, **Timber**

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 26+
- JDK 17
- Plex account with music library

### Setup
```bash
git clone https://github.com/xSunnyyy/echoflow.git
cd echoflow
./gradlew assembleDebug
```

### First Run
1. Sign in with Plex (browser OAuth)
2. Select your server
3. Browse your music library

## 🎨 Design System

Frosted-glass aesthetic with Material You:
- Translucent surfaces with blur (Android 12+)
- Dynamic color from wallpaper
- Smooth animations

## 📖 Documentation

See [IMPLEMENTATION.md](IMPLEMENTATION.md) for detailed architecture and implementation status.

## 🗺️ Roadmap

### Current Sprint
- [ ] ExoPlayer playback integration
- [ ] Now Playing screen
- [ ] Mini-player component

### Next
- [ ] WorkManager downloads
- [ ] Customizable home
- [ ] Settings screen

---

**Built with ❤️ using Kotlin and Jetpack Compose**