package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Site
import kotlinx.coroutines.flow.Flow

@DoorDao
@Repository
expect abstract class SiteDao {

    @Query("SELECT * FROM Site LIMIT 1")
    abstract fun getSite(): Site?

    @HttpAccessible
    @Query("SELECT * FROM Site LIMIT 1")
    abstract suspend fun getSiteAsync(): Site?

    @Query("SELECT authSalt FROM Site LIMIT 1")
    abstract suspend fun getSiteAuthSaltAsync(): String?

    @HttpAccessible
    @Query("SELECT * FROM Site LIMIT 1")
    abstract fun getSiteAsFlow(): Flow<Site?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceAsync(site: Site): Long

    @Insert
    abstract fun insert(site: Site): Long

    @Insert
    abstract suspend fun insertAsync(site: Site): Long

    @Update
    abstract suspend fun updateAsync(workspace: Site)


}