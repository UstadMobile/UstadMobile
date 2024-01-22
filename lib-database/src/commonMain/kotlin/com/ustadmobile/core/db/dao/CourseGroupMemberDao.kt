package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.CourseGroupMemberDaoCommon.CASE_CLAZZ_UID_WHEN_NOT_ZERO_USEIT_ELSE_LOOKUP_CGS_UID_SQL
import com.ustadmobile.core.db.dao.CourseGroupMemberDaoCommon.FIND_BY_COURSEGROUPSET_AND_CLAZZ_SQL
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.composites.PersonAndPicture
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.Flow

@Repository
@DoorDao
expect abstract class CourseGroupMemberDao: BaseDao<CourseGroupMember> {


    @Query("""
        SELECT Person.*, CourseGroupMember.* 
          FROM Person
               JOIN ClazzEnrolment 
               ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
               AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
               AND ClazzEnrolment.clazzEnrolmentOutcome = ${ClazzEnrolment.OUTCOME_IN_PROGRESS}
               
               LEFT JOIN CourseGroupMember
               ON CourseGroupMember.cgmPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
               AND CourseGroupMember.cgmSetUid = :setUid
               
         WHERE clazzEnrolmentClazzUid = :clazzUid
      ORDER BY Person.firstNames
    """)
    abstract suspend fun findByGroupSetAsync(setUid: Long, clazzUid: Long): List<CourseGroupMemberPerson>

    @Query("""
        SELECT CourseGroupMember.*
          FROM CourseGroupMember
         WHERE cgmSetUid = :groupSetUid 
    """)
    abstract suspend fun findByGroupSetUidAsync(groupSetUid: Long): List<CourseGroupMember>


    @Query("""
        SELECT Person.*, CourseGroupMember.* 
          FROM Person
               JOIN ClazzEnrolment 
               ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
               AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT} 
               AND ClazzEnrolment.clazzEnrolmentOutcome = ${ClazzEnrolment.OUTCOME_IN_PROGRESS}
               
               LEFT JOIN CourseGroupMember
               ON CourseGroupMember.cgmPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
               AND CourseGroupMember.cgmSetUid = :setUid
               
         WHERE clazzEnrolmentClazzUid = :clazzUid
      ORDER BY CourseGroupMember.cgmGroupNumber, Person.firstNames
    """)
    abstract suspend fun findByGroupSetOrderedAsync(setUid: Long, clazzUid: Long): List<CourseGroupMemberPerson>

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
     */
    abstract suspend fun findByCourseGroupSetAndClazz(
        cgsUid: Long,
        clazzUid: Long,
        time: Long,
        activeFilter: Int,
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
    abstract fun findByCourseGroupSetAndClazzAsFlow(
        cgsUid: Long,
        clazzUid: Long,
        time: Long,
        activeFilter: Int,
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

}