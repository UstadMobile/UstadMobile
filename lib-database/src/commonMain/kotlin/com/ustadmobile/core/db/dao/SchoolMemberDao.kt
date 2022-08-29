package com.ustadmobile.core.db.dao

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.SchoolMemberDaoCommon.SORT_FIRST_NAME_ASC
import com.ustadmobile.core.db.dao.SchoolMemberDaoCommon.SORT_FIRST_NAME_DESC
import com.ustadmobile.core.db.dao.SchoolMemberDaoCommon.SORT_LAST_NAME_ASC
import com.ustadmobile.core.db.dao.SchoolMemberDaoCommon.SORT_LAST_NAME_DESC
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.SchoolMember.Companion.FROM_SCHOOLMEMBER_TO_SCOPEDGRANT_JOIN_ON_PERSON_OR_CLAZZ_PERMISSION_CLAUSE
import com.ustadmobile.lib.db.entities.SchoolMember.Companion.JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PERSON_OR_CLAZZ_PERMISSION_PT1
import com.ustadmobile.lib.db.entities.SchoolMember.Companion.JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2

@Repository
@DoorDao
expect abstract class SchoolMemberDao : BaseDao<SchoolMember> {

    @Query("""
     REPLACE INTO SchoolMemberReplicate(smPk, smDestination)
      SELECT DISTINCT SchoolMember.schoolMemberUid AS smPk,
             :newNodeId AS smDestination
        FROM UserSession
             JOIN PersonGroupMember
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             JOIN ScopedGrant
                  ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
                     AND (ScopedGrant.sgPermissions &  ${Role.PERMISSION_PERSON_SELECT}) > 0
             JOIN SchoolMember
                  ON $FROM_SCHOOLMEMBER_TO_SCOPEDGRANT_JOIN_ON_PERSON_OR_CLAZZ_PERMISSION_CLAUSE
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND SchoolMember.schoolMemberLct != COALESCE(
             (SELECT smVersionId
                FROM SchoolMemberReplicate
               WHERE smPk = SchoolMember.schoolMemberUid
                 AND smDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(smPk, smDestination) DO UPDATE
             SET smPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([SchoolMember::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO SchoolMemberReplicate(smPk, smDestination)
  SELECT DISTINCT SchoolMember.schoolMemberUid AS smUid,
         UserSession.usClientNodeId AS smDestination
    FROM ChangeLog
         JOIN SchoolMember
              ON ChangeLog.chTableId = ${SchoolMember.TABLE_ID}
                  AND ChangeLog.chEntityPk = SchoolMember.schoolMemberUid
         $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PERSON_OR_CLAZZ_PERMISSION_PT1
              ${Role.PERMISSION_PERSON_SELECT}
              $JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND SchoolMember.schoolMemberLct != COALESCE(
         (SELECT smVersionId
            FROM SchoolMemberReplicate
           WHERE smPk = SchoolMember.schoolMemberUid
             AND smDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(smPk, smDestination) DO UPDATE
     SET smPending = true
  */               
 """)
     @ReplicationRunOnChange([SchoolMember::class])
     @ReplicationCheckPendingNotificationsFor([SchoolMember::class])
     abstract suspend fun replicateOnChange()

    @Query("SELECT * FROM SchoolMember WHERE schoolMemberUid = :schoolMemberUid " +
            " AND CAST(schoolMemberActive AS INTEGER) = 1")
    abstract fun findByUidAsync(schoolMemberUid: Long): SchoolMember?

    @Update
    abstract suspend fun updateAsync(entity: SchoolMember): Int


    @Query("""
        SELECT * FROM SchoolMember WHERE schoolMemberSchoolUid = :schoolUid
        AND schoolMemberPersonUid = :personUid
        AND (:role = 0 OR schoolMemberRole = :role)
        AND (:timeFilter = 0 OR :timeFilter BETWEEN SchoolMember.schoolMemberJoinDate AND SchoolMember.schoolMemberLeftDate) 
        AND CAST(schoolMemberActive AS INTEGER) = 1
    """)
    abstract suspend fun findBySchoolAndPersonAndRole(schoolUid: Long, personUid: Long, role: Int,
        timeFilter: Long = 0): List<SchoolMember>

    @Query("""
        SELECT SchoolMember.*, Person.*
          FROM PersonGroupMember
                ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1} 
                ${Role.PERMISSION_PERSON_SELECT} 
                ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
          LEFT JOIN SchoolMember ON Person.personUid = SchoolMember.schoolMemberPersonUid 
         WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid 
         AND PersonGroupMember.groupMemberActive  
        AND SchoolMember.schoolMemberActive
        AND SchoolMember.schoolMemberSchoolUid = :schoolUid 
        AND SchoolMember.schoolMemberRole = :role
        AND Person.active
        AND (Person.firstNames || ' ' || Person.lastName) LIKE :searchQuery
        GROUP BY Person.personUid, SchoolMember.schoolMemberUid
         ORDER BY CASE(:sortOrder)
                WHEN $SORT_FIRST_NAME_ASC THEN Person.firstNames
                WHEN $SORT_LAST_NAME_ASC THEN Person.lastName
                ELSE ''
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_FIRST_NAME_DESC THEN Person.firstNames
                WHEN $SORT_LAST_NAME_DESC THEN Person.lastName
                ELSE ''
            END DESC
            """)
    abstract fun findAllActiveMembersBySchoolAndRoleUid(schoolUid: Long, role: Int,
                                                        sortOrder: Int,
                                                        searchQuery: String,
                                                        accountPersonUid: Long)
            : DataSourceFactory<Int, SchoolMemberWithPerson>

    @Query("""SELECT SchoolMember.*, Person.* FROM SchoolMember
        LEFT JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
        WHERE CAST(SchoolMember.schoolMemberActive AS INTEGER) = 1
        AND SchoolMember.schoolMemberSchoolUid = :schoolUid 
        AND SchoolMember.schoolMemberRole = :role
        AND CAST(Person.active AS INTEGER) = 1
        AND (Person.firstNames || ' ' || Person.lastName) LIKE :searchQuery
        ORDER BY Person.firstNames DESC""")
    abstract suspend fun findAllTest(schoolUid: Long, role: Int, searchQuery: String): List<SchoolMemberWithPerson>


}
