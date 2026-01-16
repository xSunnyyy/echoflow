#!/bin/bash

# Plex Glass Player - APK Build Script
# This script builds a debug or release APK

set -e

echo "🎵 Plex Glass Player - APK Builder"
echo "=================================="
echo ""

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ Error: gradlew not found. Creating Gradle wrapper..."
    gradle wrapper --gradle-version 8.7
fi

# Make gradlew executable
chmod +x ./gradlew

# Ask for build type
echo "Select build type:"
echo "1) Debug APK (for testing)"
echo "2) Release APK (for distribution)"
read -p "Enter choice [1-2]: " choice

case $choice in
    1)
        echo ""
        echo "🔨 Building Debug APK..."
        ./gradlew assembleDebug

        echo ""
        echo "✅ Debug APK built successfully!"
        echo "📦 Location: app/build/outputs/apk/debug/app-debug.apk"
        echo ""
        echo "To install on connected device:"
        echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
        ;;
    2)
        echo ""
        echo "🔨 Building Release APK..."
        echo "⚠️  Note: Release APKs should be signed for distribution"

        # Check if keystore exists
        if [ -f "release.keystore" ]; then
            echo "✅ Found release.keystore"
        else
            echo ""
            echo "⚠️  WARNING: No release.keystore found!"
            echo "The APK will be unsigned and cannot be installed on most devices."
            echo ""
            read -p "Continue anyway? [y/N]: " continue
            if [ "$continue" != "y" ]; then
                echo "Build cancelled."
                exit 0
            fi
        fi

        ./gradlew assembleRelease

        echo ""
        echo "✅ Release APK built successfully!"
        echo "📦 Location: app/build/outputs/apk/release/app-release.apk"
        echo ""
        if [ ! -f "release.keystore" ]; then
            echo "⚠️  This APK is UNSIGNED and cannot be installed."
            echo "See BUILD.md for instructions on signing."
        fi
        ;;
    *)
        echo "❌ Invalid choice. Exiting."
        exit 1
        ;;
esac

echo ""
echo "🎉 Build complete!"
