pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PlexGlassPlayer"

include(":app")

// Core modules
include(":core:core-ui")
include(":core:core-model")
include(":core:core-network")
include(":core:core-db")
include(":core:core-util")

// Data modules
include(":data:plex-auth")
include(":data:plex-api")
include(":data:repositories")
include(":data:cache")

// Domain module
include(":domain")

// Feature modules
include(":features:feature-auth")
include(":features:feature-server")
include(":features:feature-home")
include(":features:feature-library")
include(":features:feature-search")
include(":features:feature-playback")
include(":features:feature-downloads")
include(":features:feature-settings")
