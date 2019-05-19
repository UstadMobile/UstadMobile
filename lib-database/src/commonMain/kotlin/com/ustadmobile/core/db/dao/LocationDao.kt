package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Location

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@Dao
@UmRepository
abstract class LocationDao : SyncableDao<Location, LocationDao> {


    @Insert
    abstract override fun insert(entity: Location): Long

    @Insert
    abstract override suspend fun insertAsync(entity: Location): Long

    @Update
    abstract override fun update(entity: Location)

    @Update
    abstract suspend fun updateAsync(entity: Location): Int

    @Query("SELECT * FROM Location WHERE locationUid = :uid")
    abstract override fun findByUid(uid: Long): Location?

    @Query("SELECT * FROM Location WHERE locationUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): Location?

    @Query("SELECT * FROM Location WHERE parentLocationUid = 0")
    abstract suspend fun findTopLocationsAsync(): List<Location>

    @Query("SELECT * FROM Location WHERE parentLocationUid = :uid")
    abstract suspend fun findAllChildLocationsForUidAsync(uid: Long): List<Location>

    @Query("SELECT * FROM Location WHERE title = :name")
    abstract suspend fun findByTitleAsync(name: String): List<Location>

}
