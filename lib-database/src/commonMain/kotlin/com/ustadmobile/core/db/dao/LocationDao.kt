package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.lib.db.entities.UidAndLabel
import com.ustadmobile.door.annotation.Repository

@Repository
@Dao
abstract class LocationDao : BaseDao<Location> {

    @Update
    abstract suspend fun updateAsync(entity: Location): Int

    @Query("""
        SELECT * FROM Location WHERE locationUid = :uid AND CAST(locationActive AS INTEGER) = 1
    """)
    abstract suspend fun findByUidAsync(uid: Long): Location?

    @Query("""
        SELECT * FROM Location WHERE CAST(locationActive AS INTEGER) = 1
    """)
    abstract fun findAllLocations(): DataSource.Factory<Int, Location>

    @Query("""SELECT Location.locationUid AS uid, Location.locationTitle As labelName 
                    FROM Location WHERE locationUid IN (:locationList)""")
    abstract suspend fun getLocationsFromUids(locationList: List<Long>): List<UidAndLabel>



}
