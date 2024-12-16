package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
import com.ustadmobile.door.annotation.Repository.Companion.METHOD_DELEGATE_TO_WEB
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class PersonParentJoinDao {


    @Insert
    abstract suspend fun insertListAsync(entityList: List<PersonParentJoin>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(entity: PersonParentJoin): Long

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT PersonParentJoin.*, Person.*
          FROM PersonParentJoin
     LEFT JOIN Person ON Person.personUid = PersonParentJoin.ppjMinorPersonUid    
         WHERE PersonParentJoin.ppjUid = :uid
    """)
    abstract suspend fun findByUidWithMinorAsync(uid: Long): PersonParentJoinAndMinorPerson?

    @Query("""
        SELECT PersonParentJoin.*, Person.*
          FROM PersonParentJoin
     LEFT JOIN Person ON Person.personUid = PersonParentJoin.ppjMinorPersonUid    
         WHERE PersonParentJoin.ppjUid = :uid
    """)
    @Repository(METHOD_DELEGATE_TO_WEB)
    abstract suspend fun findByUidWithMinorAsyncFromWeb(uid: Long): PersonParentJoinAndMinorPerson?

    @Query("""
        SELECT PersonParentJoin.*
          FROM PersonParentJoin
         WHERE ppjMinorPersonUid = :minorPersonUid 
    """)
    abstract suspend fun findByMinorPersonUid(minorPersonUid: Long): List<PersonParentJoin>


    /**
     * Find classes for which a minor (child) is enroled where there is no parent enrolment for
     * the parent in the same class.
     */
    @Query("""
        SELECT PersonParentJoin.ppjParentPersonUid AS parentPersonUid,
               ChildEnrolment.clazzEnrolmentClazzUid AS clazzUid
          FROM PersonParentJoin
               JOIN ClazzEnrolment ChildEnrolment 
                    ON ChildEnrolment.clazzEnrolmentPersonUid = :minorPersonUid
                   AND (:clazzUidFilter = 0 OR ChildEnrolment.clazzEnrolmentClazzUid = :clazzUidFilter)
         WHERE PersonParentJoin.ppjMinorPersonUid = :minorPersonUid
           AND PersonParentJoin.ppjParentPersonUid != 0
           AND NOT EXISTS(
               SELECT clazzEnrolmentUid 
                 FROM ClazzEnrolment
                WHERE ClazzEnrolment.clazzEnrolmentPersonUid = PersonParentJoin.ppjParentPersonUid
                  AND ClazzEnrolment.clazzEnrolmentClazzUid = ChildEnrolment.clazzEnrolmentClazzUid
                  AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_PARENT}
                  AND CAST(ClazzEnrolment.clazzEnrolmentActive AS INTEGER) = 1)
    """)
    abstract suspend fun findByMinorPersonUidWhereParentNotEnrolledInClazz(
        minorPersonUid: Long,
        clazzUidFilter: Long
    ): List<ParentEnrolmentRequired>

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

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findByMinorPersonUid")
        )
    )
    @Query("""
        SELECT EXISTS(
               SELECT ppjUid
                 FROM PersonParentJoin
                WHERE ppjMinorPersonUid = :minorPersonUid
                  AND CAST(ppjInactive AS INTEGER) = 0
                  AND ppjStatus = ${PersonParentJoin.STATUS_APPROVED})
    """)
    abstract suspend fun isMinorApproved(minorPersonUid: Long) : Boolean


}