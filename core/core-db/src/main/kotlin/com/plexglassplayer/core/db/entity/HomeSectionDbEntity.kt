package com.plexglassplayer.core.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.plexglassplayer.core.model.HomeSectionConfig

@Entity(tableName = "home_sections")
data class HomeSectionDbEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val visible: Boolean,
    val order: Int,
    val settingsJson: String
)

fun HomeSectionDbEntity.toModel(): HomeSectionConfig {
    return HomeSectionConfig(
        id = id,
        type = type,
        visible = visible,
        order = order,
        settingsJson = settingsJson
    )
}

fun HomeSectionConfig.toDbEntity(): HomeSectionDbEntity {
    return HomeSectionDbEntity(
        id = id,
        type = type,
        visible = visible,
        order = order,
        settingsJson = settingsJson
    )
}
