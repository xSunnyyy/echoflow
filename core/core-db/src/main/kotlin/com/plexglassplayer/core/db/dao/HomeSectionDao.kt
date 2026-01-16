package com.plexglassplayer.core.db.dao

import androidx.room.*
import com.plexglassplayer.core.db.entity.HomeSectionDbEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeSectionDao {

    @Query("SELECT * FROM home_sections ORDER BY `order` ASC")
    fun getAllSections(): Flow<List<HomeSectionDbEntity>>

    @Query("SELECT * FROM home_sections WHERE visible = 1 ORDER BY `order` ASC")
    fun getVisibleSections(): Flow<List<HomeSectionDbEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: HomeSectionDbEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<HomeSectionDbEntity>)

    @Update
    suspend fun updateSection(section: HomeSectionDbEntity)

    @Delete
    suspend fun deleteSection(section: HomeSectionDbEntity)

    @Query("DELETE FROM home_sections")
    suspend fun deleteAll()
}
