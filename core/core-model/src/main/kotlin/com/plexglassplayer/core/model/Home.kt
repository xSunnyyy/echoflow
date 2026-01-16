package com.plexglassplayer.core.model

import kotlinx.serialization.Serializable

sealed class HomeSection {
    data class ContinueListening(val maxItems: Int) : HomeSection()
    data class RecentlyAdded(val maxItems: Int) : HomeSection()
    data class RecentlyPlayed(val maxItems: Int) : HomeSection()
    data class Pinned(val pinnedIds: List<PinnedRef>) : HomeSection()
    data class PlaylistsRow(val maxItems: Int) : HomeSection()
}

@Serializable
data class HomeLayoutConfig(
    val sections: List<HomeSectionConfig>
)

@Serializable
data class HomeSectionConfig(
    val id: String,
    val type: String,
    val visible: Boolean,
    val order: Int,
    val settingsJson: String
)

@Serializable
data class PinnedRef(
    val type: String, // ARTIST|ALBUM|PLAYLIST
    val id: String
)
