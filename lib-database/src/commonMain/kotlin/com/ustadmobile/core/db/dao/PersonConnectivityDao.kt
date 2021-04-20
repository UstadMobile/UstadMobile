package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.PersonConnectivity
import com.ustadmobile.lib.db.entities.PersonPicture


@Dao
@Repository
abstract class PersonConnectivityDao : BaseDao<PersonConnectivity> {

    @Query("""
        SELECT PersonConnectivity.* 
          FROM PersonConnectivity 
               LEFT JOIN Person
               ON Person.personUid = PersonConnectivity.pcPersonUid
        WHERE 
            pcPersonUid = :personUid
            AND :accountPersonUid 
                IN (${PersonDao.ENTITY_PERSONS_WITH_CONNECTIVITY_PERMISSION}) 
    """)
    abstract suspend fun getConnectivityStatusForPerson(accountPersonUid: Long,
                                                personUid: Long): List<PersonConnectivity>

    @Query("""
        UPDATE PersonConnectivity SET pcConStatus = :status,
            pcLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1) 
            WHERE pcUid = :uid
    """)
    abstract fun updateConnectivity(status: Int, uid: Long)

}