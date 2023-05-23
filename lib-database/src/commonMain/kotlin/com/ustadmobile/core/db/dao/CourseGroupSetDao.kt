package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
import com.ustadmobile.door.paging.PagingSource
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession
import kotlinx.coroutines.flow.Flow

@Repository
@DoorDao
expect abstract class CourseGroupSetDao : BaseDao<CourseGroupSet> {

    @Query("""
     REPLACE INTO CourseGroupSetReplicate(cgsPk, cgsDestination)
      SELECT DISTINCT CourseGroupSet.cgsUid AS cgsUid,
             :newNodeId AS cgsDestination
        FROM UserSession
             JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_CLAZZ_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
             JOIN CourseGroupSet
                    ON CourseGroupSet.cgsClazzUid = Clazz.clazzUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND CourseGroupSet.cgsLct != COALESCE(
             (SELECT cgsVersionId
                FROM CourseGroupSetReplicate
               WHERE cgsPk = CourseGroupSet.cgsUid
                 AND cgsDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(cgsPk, cgsDestination) DO UPDATE
             SET cgsPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([CourseGroupSet::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO CourseGroupSetReplicate(cgsPk, cgsDestination)
  SELECT DISTINCT CourseGroupSet.cgsUid AS cgsUid,
         UserSession.usClientNodeId AS cgsDestination
    FROM ChangeLog
         JOIN CourseGroupSet
             ON ChangeLog.chTableId = ${CourseGroupSet.TABLE_ID}
                AND ChangeLog.chEntityPk = CourseGroupSet.cgsUid
         JOIN Clazz 
              ON Clazz.clazzUid = CourseGroupSet.cgsClazzUid 
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_ASSIGNMENT_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}  
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND CourseGroupSet.cgsLct != COALESCE(
         (SELECT cgsVersionId
            FROM CourseGroupSetReplicate
           WHERE cgsPk = CourseGroupSet.cgsUid
             AND cgsDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(cgsPk, cgsDestination) DO UPDATE
     SET cgsPending = true
  */               
 """)
    @ReplicationRunOnChange([CourseGroupSet::class])
    @ReplicationCheckPendingNotificationsFor([CourseGroupSet::class])
    abstract suspend fun replicateOnChange()



    @Update
    abstract suspend fun updateAsync(entity: CourseGroupSet): Int

    @Query("""
        SELECT *
         FROM CourseGroupSet
        WHERE cgsActive
          AND cgsClazzUid = :clazzUid
          AND ((:searchText = '%') OR (cgsName LIKE :searchText))
     ORDER BY CASE(:sortOrder)
              WHEN ${CourseGroupSetDaoConstants.SORT_NAME_ASC} THEN cgsName
              ELSE ''
              END ASC,
              CASE(:sortOrder)
              WHEN ${CourseGroupSetDaoConstants.SORT_NAME_DESC} THEN cgsName
              ELSE ''
              END DESC
    """)
    abstract fun findAllCourseGroupSetForClazz(
        clazzUid: Long,
        searchText: String,
        sortOrder: Int,
    ): PagingSource<Int, CourseGroupSet>


    @Query("""
        SELECT *
         FROM CourseGroupSet
        WHERE cgsActive
          AND cgsClazzUid = :clazzUid
     ORDER BY cgsName   
    """)
    abstract fun findAllCourseGroupSetForClazzList(clazzUid: Long): List<CourseGroupSet>

    @Query("""
        SELECT *
         FROM CourseGroupSet
        WHERE cgsActive
          AND cgsClazzUid = :clazzUid
     ORDER BY cgsName   
    """)
    abstract suspend fun findAllCourseGroupSetForClazzListAsync(clazzUid: Long): List<CourseGroupSet>

    @Query("""
        SELECT * 
         FROM CourseGroupSet 
        WHERE cgsUid = :uid
        """)
    abstract suspend fun findByUidAsync(uid: Long): CourseGroupSet?

    @Query("""
        SELECT * 
         FROM CourseGroupSet 
        WHERE cgsUid = :uid
        """)
    abstract suspend fun findByUidAsFlow(uid: Long): Flow<CourseGroupSet?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(entity: CourseGroupSet)



}