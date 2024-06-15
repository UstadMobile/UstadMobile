package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.dao.CourseGroupMemberDaoCommon.CASE_CLAZZ_UID_WHEN_NOT_ZERO_USEIT_ELSE_LOOKUP_CGS_UID_SQL
import com.ustadmobile.core.db.dao.CourseGroupMemberDaoCommon.FIND_BY_COURSEGROUPSET_AND_CLAZZ_SQL
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.composites.CourseGroupMemberAndPerson
import com.ustadmobile.lib.db.composites.PersonAndPicture
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.Flow

@Repository
@DoorDao
expect abstract class CourseGroupMemberDao: BaseDao<CourseGroupMember> {


    @Query("""
        SELECT CourseGroupMember.*
          FROM CourseGroupMember
         WHERE cgmSetUid = :groupSetUid 
    """)
    abstract suspend fun findByGroupSetUidAsync(groupSetUid: Long): List<CourseGroupMember>

    @Query("""
        SELECT * 
          FROM CourseGroupMember
         WHERE cgmPersonUid = :studentUid 
          AND cgmSetUid = :groupSetUid
         LIMIT 1
    """)
    abstract suspend fun findByPersonUid(groupSetUid: Long, studentUid: Long): CourseGroupMember?

    @Insert
    abstract suspend fun insertListAsync(entityList: List<CourseGroupMember>)

    @Update
    abstract suspend fun updateListAsync(entityList: List<CourseGroupMember>)

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findByCourseGroupSetAndClazz"
            ),
            HttpServerFunctionCall(
                functionName = "findByCourseGroupSetAndClazzAsFlowPersons",
            ),
            HttpServerFunctionCall(
                functionName = "findByCourseGroupSetAndClazzAsFlowEnrolments"
            ),
        )
    )
    @Query(FIND_BY_COURSEGROUPSET_AND_CLAZZ_SQL)
    /**
     * @param cgsUid CourseGroupSetUid - might be 0 if not created yet
     * @param clazzUid ClazzUid, required if the coursegroupset does not exist yet, otherwise may be 0
     * @param time the current time (used to determine if enrolments are active)
     * @param activeFilter if 1, then only return active members.
     * @param accountPersonUid the currently active user (used for permission checks)
     */
    abstract suspend fun findByCourseGroupSetAndClazz(
        cgsUid: Long,
        clazzUid: Long,
        time: Long,
        activeFilter: Int,
        accountPersonUid: Long,
    ): List<CourseGroupMemberAndName>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findByCourseGroupSetAndClazzAsFlow"
            ),
            HttpServerFunctionCall(
                functionName = "findByCourseGroupSetAndClazzAsFlowPersons",
            ),
            HttpServerFunctionCall(
                functionName = "findByCourseGroupSetAndClazzAsFlowEnrolments"
            ),
        )
    )
    @Query(FIND_BY_COURSEGROUPSET_AND_CLAZZ_SQL)
    @QueryLiveTables(arrayOf("ClazzEnrolment", "Person", "PersonPicture", "CourseGroupMember", "CourseGroupSet"))
    abstract fun findByCourseGroupSetAndClazzAsFlow(
        cgsUid: Long,
        clazzUid: Long,
        time: Long,
        activeFilter: Int,
        accountPersonUid: Long,
    ): Flow<List<CourseGroupMemberAndName>>

    @Query("""
        SELECT Person.*, PersonPicture.*
          FROM Person
               LEFT JOIN PersonPicture
                         ON PersonPicture.personPictureUid = Person.personUid
         WHERE Person.personUid IN
               (SELECT DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid
                  FROM ClazzEnrolment
                 WHERE ClazzEnrolment.clazzEnrolmentClazzUid = $CASE_CLAZZ_UID_WHEN_NOT_ZERO_USEIT_ELSE_LOOKUP_CGS_UID_SQL)
    """)
    abstract suspend fun findByCourseGroupSetAndClazzAsFlowPersons(
        clazzUid: Long,
        cgsUid: Long,
    ): List<PersonAndPicture>

    @Query("""
        SELECT ClazzEnrolment.*
          FROM ClazzEnrolment
         WHERE ClazzEnrolment.clazzEnrolmentClazzUid = $CASE_CLAZZ_UID_WHEN_NOT_ZERO_USEIT_ELSE_LOOKUP_CGS_UID_SQL
    """)
    abstract suspend fun findByCourseGroupSetAndClazzAsFlowEnrolments(
        clazzUid: Long,
        cgsUid: Long,
    ): List<ClazzEnrolment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertListAsync(list: List<CourseGroupMember>)


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT CourseGroupMember.*, Person.*
          FROM CourseGroupMember
               JOIN Person 
                    ON Person.personUid = CourseGroupMember.cgmPersonUid
         WHERE (    CourseGroupMember.cgmSetUid = :courseGroupSetUid
                AND CourseGroupMember.cgmGroupNumber = :groupNum)
           AND (    /* Grant permission where the active person is in the group */ 
                    EXISTS(SELECT 1
                             FROM CourseGroupMember CourseGroupMemberInternal
                            WHERE CourseGroupMemberInternal.cgmSetUid = :courseGroupSetUid
                              AND CourseGroupMemberInternal.cgmPersonUid = :accountPersonUid)
                    /* Grant permission where the activepersonuid is in a group assigned to mark this group */
                 OR EXISTS(SELECT 1
                             FROM PeerReviewerAllocation
                            WHERE PeerReviewerAllocation.praAssignmentUid = :assignmentUid
                              AND PeerReviewerAllocation.praMarkerSubmitterUid = :groupNum
                              AND EXISTS(SELECT 1
                                           FROM CourseGroupMember CourseGroupMemberInternal
                                          WHERE CourseGroupMemberInternal.cgmSetUid = PeerReviewerAllocation.praMarkerSubmitterUid
                                            AND CourseGroupMemberInternal.cgmPersonUid = :accountPersonUid)) 
                    /* Grant permission where the active person has the select person permission for the class */                        
                 OR (${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1} ${PermissionFlags.PERSON_VIEW}
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2} ${PermissionFlags.PERSON_VIEW}
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3})    
               )
               
    """)
    abstract suspend fun findByCourseGroupSetAndGroupNumAsync(
        courseGroupSetUid: Long,
        groupNum: Int,
        clazzUid: Long,
        assignmentUid: Long,
        accountPersonUid: Long,
    ): List<CourseGroupMemberAndPerson>

}