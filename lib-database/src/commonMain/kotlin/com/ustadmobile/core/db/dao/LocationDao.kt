package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.AuditLog
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.lib.db.entities.LocationWithSubLocationCount
import kotlin.js.JsName

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, 
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class LocationDao : BaseDao<Location> {

    @Insert
    abstract override fun insert(entity: Location): Long

    @Update
    abstract override fun update(entity: Location)

    @Update
    abstract suspend fun updateAsync(entity: Location):Int

    @Insert
    abstract fun insertAuditLog(entity: AuditLog): Long

    @Query("SELECT * FROM Location WHERE CAST(locationActive AS INTEGER) = 1")
    abstract fun findAllActiveLocationsLive(): DoorLiveData<List<Location>>

    fun createAuditLog(toPersonUid: Long, fromPersonUid: Long) {
        val auditLog = AuditLog(fromPersonUid, Location.TABLE_ID, toPersonUid)
        insertAuditLog(auditLog)
    }

    fun insertLocation(entity: Location, loggedInPersonUid: Long) {
        val personUid = insert(entity)
        createAuditLog(personUid, loggedInPersonUid)
    }

    fun updateLocation(entity: Location, loggedInPersonUid: Long) {
        update(entity)
        createAuditLog(entity.locationUid, loggedInPersonUid)
    }

    @JsName("findByUid")
    @Query("SELECT * FROM Location WHERE locationUid = :uid")
    abstract fun findByUid(uid: Long): Location?

    @JsName("findByUidAsync")
    @Query("SELECT * FROM Location WHERE locationUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): Location?

    @Query("SELECT * FROM Location WHERE locationUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<Location?>

    @JsName("findTopLocationsAsync")
    @Query("SELECT * FROM Location WHERE parentLocationUid = 0 AND CAST(locationActive AS INTEGER) = 1")
    abstract suspend fun findTopLocationsAsync(): List<Location>

    @JsName("findAllChildLocationsForUidAsync")
    @Query("SELECT * FROM Location WHERE parentLocationUid = :uid AND CAST(locationActive AS INTEGER) = 1")
    abstract suspend fun findAllChildLocationsForUidAsync(uid: Long) : List<Location>

    @Query("SELECT * FROM Location WHERE parentLocationUid = :uid AND CAST(locationActive AS INTEGER) = 1" +
            " AND Location.locationUid != :suid ")
    abstract suspend fun findAllChildLocationsForUidExceptSelectedUidAsync(uid: Long, suid: Long)
            : List<Location>

    @JsName("findByTitleAsync")
    @Query("SELECT * FROM Location WHERE title = :name AND CAST(locationActive AS INTEGER) = 1")
    abstract suspend fun findByTitleAsync(name: String):List<Location>

    @Query("SELECT * FROM Location WHERE title LIKE :name AND CAST(locationActive AS INTEGER) = 1")
    abstract suspend fun findByTitleLikeAsync(name: String):List<Location>

    @Query("SELECT * FROM Location WHERE title = :name AND CAST(locationActive AS INTEGER) = 1")
    abstract fun findByTitle(name: String): List<Location>

    @Query("SELECT *, 0 AS subLocations  FROM Location WHERE parentLocationUid = 0 " +
            " AND CAST(locationActive AS INTEGER) = 1")
    abstract fun findAllTopLocationsWithCount(): DataSource.Factory<Int, LocationWithSubLocationCount>

    @Query("SELECT *, " +
            " (SELECT COUNT(*) FROM Location WHERE Location.parentLocationUid = LOC.locationUid) " +
            " AS subLocations  " +
            "FROM Location AS LOC WHERE CAST(LOC.locationActive AS INTEGER) = 1 ORDER BY LOC.title ASC")
    abstract fun findAllLocationsWithCount(): DataSource.Factory<Int, LocationWithSubLocationCount>

    @Query("UPDATE Location SET locationActive = 0 WHERE locationUid = :uid")
    abstract suspend fun inactivateLocationAsync(uid: Long):Int

}
