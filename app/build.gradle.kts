plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.plexglassplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.plexglassplayer"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders["appAuthRedirectScheme"] = "plexglassplayer"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    /* ----------------------------
     * Core modules
     * ---------------------------- */
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-model"))
    implementation(project(":core:core-network"))
    implementation(project(":core:core-db"))
    implementation(project(":core:core-util"))

    /* ----------------------------
     * Data modules
     * ---------------------------- */
    implementation(project(":data:plex-auth"))
    implementation(project(":data:plex-api"))
    implementation(project(":data:repositories"))
    implementation(project(":data:cache"))

    /* ----------------------------
     * Domain
     * ---------------------------- */
    implementation(project(":domain"))

    /* ----------------------------
     * Features
     * ---------------------------- */
    implementation(project(":features:feature-auth"))
    implementation(project(":features:feature-server"))
    implementation(project(":features:feature-home"))
    implementation(project(":features:feature-library"))
    implementation(project(":features:feature-search"))
    implementation(project(":features:feature-playback"))
    implementation(project(":features:feature-downloads"))
    implementation(project(":features:feature-settings"))

    /* ----------------------------
     * AndroidX Core
     * ---------------------------- */
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    /* ----------------------------
     * Compose
     * ---------------------------- */
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    /* ----------------------------
     * 🔊 Media3 (REQUIRED for PlaybackService in :app)
     * ---------------------------- */
    implementation("androidx.media3:media3-session:1.4.1")
    implementation("androidx.media3:media3-exoplayer:1.4.1")

    /* ----------------------------
     * 🧠 Hilt
     * ---------------------------- */
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    /* ----------------------------
     * 🛠 WorkManager + Hilt integration (FIXES NonExistentClass)
     * ---------------------------- */
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    /* ----------------------------
     * Timber
     * ---------------------------- */
    implementation(libs.timber)

    /* ----------------------------
     * Debug
     * ---------------------------- */
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    /* ----------------------------
     * Testing
     * ---------------------------- */
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
