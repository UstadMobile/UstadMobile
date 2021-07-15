package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.PersonParentJoinWithMinorPerson

@Dao
@Repository
abstract class PersonParentJoinDao {

    @Insert
    abstract suspend fun insertAsync(entity: PersonParentJoin): Long

    @Query("""
        SELECT PersonParentJoin.*, Person.*
          FROM PersonParentJoin
     LEFT JOIN Person ON Person.personUid = PersonParentJoin.ppjMinorPersonUid    
         WHERE PersonParentJoin.ppjUid = :uid
    """)
    abstract suspend fun findByUidWithMinorAsync(uid: Long): PersonParentJoinWithMinorPerson?

    @Query("""
        SELECT PersonParentJoin.*
          FROM PersonParentJoin
         WHERE ppjMinorPersonUid = :minorPersonUid 
    """)
    abstract suspend fun findByMinorPersonUid(minorPersonUid: Long): PersonParentJoin?

    @Query("""
        SELECT EXISTS(
               SELECT ppjUid
                 FROM PersonParentJoin
                WHERE ppjMinorPersonUid = :minorPersonUid
                      AND ppjParentPersonUid = :userPersonUid
                      AND CAST(ppjInactive AS INTEGER) = 0)
    """)
    abstract suspend fun isParentOf(userPersonUid: Long, minorPersonUid: Long): Boolean

    @Update
    abstract suspend fun updateAsync(personParentJoin: PersonParentJoin)

}