package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.AuditLog
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.lib.db.entities.LocationWithSubLocationCount

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, 
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class LocationDao : BaseDao<Location> {

    @Insert
    abstract override fun insert(entity: Location): Long

    @Insert
    abstract fun insertAsync(entity: Location, resultObject: UmCallback<Long>)

    @Update
    abstract override fun update(entity: Location)

    @Update
    abstract fun updateAsync(entity: Location, resultObject: UmCallback<Int>)

    @Insert
    abstract fun insertAuditLog(entity: AuditLog): Long

    @Query("SELECT * FROM Location WHERE locationActive = 1")
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

    @Query("SELECT * FROM Location WHERE locationUid = :uid")
    abstract fun findByUid(uid: Long): Location

    @Query("SELECT * FROM Location WHERE locationUid = :uid")
    abstract fun findByUidAsync(uid: Long, resultObject: UmCallback<Location>)

    @Query("SELECT * FROM Location WHERE locationUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<Location>

    @Query("SELECT * FROM Location WHERE parentLocationUid = 0 AND locationActive = 1")
    abstract fun findTopLocationsAsync(resultList: UmCallback<List<Location>>)

    @Query("SELECT * FROM Location WHERE parentLocationUid = :uid AND locationActive = 1")
    abstract fun findAllChildLocationsForUidAsync(uid: Long,
                                                  resultList: UmCallback<List<Location>>)

    @Query("SELECT * FROM Location WHERE parentLocationUid = :uid AND locationActive = 1" +
            " AND Location.locationUid != :suid ")
    abstract fun findAllChildLocationsForUidExceptSelectedUidAsync(uid: Long, suid: Long,
                                                                   resultList: UmCallback<List<Location>>)

    @Query("SELECT * FROM Location WHERE title = :name AND locationActive = 1")
    abstract fun findByTitleAsync(name: String, resultList: UmCallback<List<Location>>)

    @Query("SELECT * FROM Location WHERE title = :name AND locationActive = 1")
    abstract fun findByTitle(name: String): List<Location>

    @Query("SELECT *, 0 AS subLocations  FROM Location WHERE parentLocationUid = 0")
    abstract fun findAllTopLocationsWithCount(): DataSource.Factory<Int, LocationWithSubLocationCount>

    @Query("SELECT *, " +
            " (SELECT COUNT(*) FROM Location WHERE Location.parentLocationUid = LOC.locationUid) " +
            " AS subLocations  " +
            "FROM Location AS LOC ORDER BY LOC.title ASC")
    abstract fun findAllLocationsWithCount(): DataSource.Factory<Int, LocationWithSubLocationCount>


    @Query("UPDATE Location SET locationActive = 0 WHERE locationUid = :uid")
    abstract fun inactivateLocationAsync(uid: Long, resultObject: UmCallback<Int>)


}
