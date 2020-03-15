package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.PersonWithAssignmentMetrics

@UmDao
@UmRepository
@Dao
abstract class ClazzAssignmentDao : BaseDao<ClazzAssignment> {

    @Query("SELECT * FROM ClazzAssignment WHERE clazzAssignmentUid = :clazzAssignmentUid " +
            "AND CAST(clazzAssignmentInactive AS INTEGER) = 0")
    abstract fun findByUidAsync(clazzAssignmentUid: Long): ClazzAssignment?

    @Query("""
        SELECT ClazzAssignment.*,
        0 as totalStudents, 
        0 as startedStudents,
        0 as notStartedStudents,
        0 as completedStudents,
        0 as firstContentEntryUid,
        0 as clazzAssignmentProgress,
        '' as storiesTitle
         
         FROM ClazzAssignment WHERE clazzAssignmentUid = :clazzAssignmentUid 
        AND CAST(clazzAssignmentInactive AS INTEGER) = 0
    """)
    abstract fun findWithMetricsByUid(clazzAssignmentUid: Long): ClazzAssignmentWithMetrics?

    @Update
    abstract suspend fun updateAsync(entity: ClazzAssignment): Int

    @Query("""SELECT * FROM ClazzAssignment WHERE 
        clazzAssignmentClazzUid = :clazzUid 
        AND CAST(clazzAssignmentInactive AS INTEGER) = 0
    """)
    abstract fun findByClazzUidFactory(clazzUid: Long): DataSource.Factory<Int, ClazzAssignment>

    @Query("""SELECT * FROM ClazzAssignment WHERE 
        clazzAssignmentClazzUid = :clazzUid 
        AND CAST(clazzAssignmentInactive AS INTEGER) = 0
    """)
    abstract fun findByClazzUidFactorySync(clazzUid: Long): List<ClazzAssignment>

    @Query("""
        SELECT ClazzAssignment.*, 
        
         (SELECT COUNT(*) FROM ClazzMember WHERE clazzMemberClazzUid = Clazz.clazzUid AND
         CAST(clazzMemberActive AS INTEGER) = 1 AND clazzMemberRole = 1
         ) as totalStudents,
         
         (
          SELECT COUNT(DISTINCT(personUid))
            FROM StatementEntity
            LEFT JOIN XObjectEntity ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid
            WHERE XObjectEntity.objectContentEntryUid IN 
              ( 
                SELECT ContentEntry.contentEntryUid FROM ClazzAssignmentContentJoin
                LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzAssignmentContentJoinContentUid
                WHERE clazzAssignmentContentJoinClazzAssignmentUid = ClazzAssignment.clazzAssignmentUid
                AND CAST(clazzAssignmentContentJoinInactive AS INTEGER) = 0
              )
            AND StatementEntity.personUid IN 
            (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid
            AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 AND ClazzMember.clazzMemberRole = 1) 
            AND StatementEntity.timestamp BETWEEN ClazzAssignment.clazzAssignmentStartDate AND ClazzAssignment.clazzAssignmentDueDate
            GROUP BY personUid 
          ) 
          as startedStudents,
          
          
          (
            (
            SELECT COUNT(*) FROM ClazzMember WHERE clazzMemberClazzUid = Clazz.clazzUid AND
            CAST(clazzMemberActive AS INTEGER) = 1 AND clazzMemberRole = 1
            ) -
        
            (
            SELECT COUNT(DISTINCT(personUid))
            FROM StatementEntity
            LEFT JOIN XObjectEntity ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid
            WHERE XObjectEntity.objectContentEntryUid IN 
              ( 
                SELECT ContentEntry.contentEntryUid FROM ClazzAssignmentContentJoin
                LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzAssignmentContentJoinContentUid
                WHERE clazzAssignmentContentJoinClazzAssignmentUid = ClazzAssignment.clazzAssignmentUid
                AND CAST(clazzAssignmentContentJoinInactive AS INTEGER) = 0
              )
            AND StatementEntity.personUid IN 
            (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid
            AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 AND ClazzMember.clazzMemberRole = 1) 
            AND StatementEntity.timestamp BETWEEN ClazzAssignment.clazzAssignmentStartDate AND ClazzAssignment.clazzAssignmentDueDate
            GROUP BY personUid 
            ) 
          )as notStartedStudents,
          
          (
                SELECT SUM(val) FROM (
            SELECT
            CASE WHEN (MAX(extensionProgress) > 99) THEN 1 else 0 END as val
            FROM StatementEntity
            LEFT JOIN XObjectEntity ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid
            WHERE XObjectEntity.objectContentEntryUid IN 
              ( 
                SELECT ContentEntry.contentEntryUid FROM ClazzAssignmentContentJoin
                LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzAssignmentContentJoinContentUid
                WHERE clazzAssignmentContentJoinClazzAssignmentUid = ClazzAssignment.clazzAssignmentUid
                AND CAST(clazzAssignmentContentJoinInactive AS INTEGER) = 0
              )
            AND StatementEntity.personUid IN 
            (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid
            AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 AND ClazzMember.clazzMemberRole = 1) 
            AND StatementEntity.timestamp BETWEEN ClazzAssignment.clazzAssignmentStartDate AND ClazzAssignment.clazzAssignmentDueDate
            GROUP BY personUid
            )
                
           ) as completedStudents,
          
          (
                SELECT SUM(progress)/
                    (SELECT count(*)
                    FROM ClazzAssignmentContentJoin
                    LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzAssignmentContentJoinContentUid
                    WHERE clazzAssignmentContentJoinClazzAssignmentUid = ClazzAssignment.clazzAssignmentUid
                    AND CAST(clazzAssignmentContentJoinInactive AS INTEGER) = 0) 
                FROM (
                    SELECT 
                    CASE WHEN
                    (
                        SELECT
                        MAX(extensionProgress)
                        FROM StatementEntity
                        LEFT JOIN XObjectEntity ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid
                        WHERE XObjectEntity.objectContentEntryUid = ContentEntry.contentEntryUid
                        AND StatementEntity.timestamp BETWEEN ClazzAssignment.clazzAssignmentStartDate AND ClazzAssignment.clazzAssignmentDueDate
                        
                    )  THEN
                    (
                        SELECT
                        MAX(extensionProgress)
                        FROM StatementEntity
                        LEFT JOIN XObjectEntity ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid
                        WHERE XObjectEntity.objectContentEntryUid = ContentEntry.contentEntryUid
                        AND StatementEntity.timestamp BETWEEN ClazzAssignment.clazzAssignmentStartDate AND ClazzAssignment.clazzAssignmentDueDate
        
                    ) ELSE 0 END as progress,
                    ContentEntry.contentEntryUid
                    FROM ClazzAssignmentContentJoin
                    LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzAssignmentContentJoinContentUid
                    WHERE clazzAssignmentContentJoinClazzAssignmentUid = ClazzAssignment.clazzAssignmentUid
                    AND CAST(clazzAssignmentContentJoinInactive AS INTEGER) = 0
                )
          ) 
          as clazzAssignmentProgress,
          
          "" as storiesTitle,
          (SELECT clazzAssignmentContentJoinContentUid FROM ClazzAssignmentContentJoin
          WHERE clazzAssignmentContentJoinClazzAssignmentUid = ClazzAssignment.clazzAssignmentUid
          ORDER BY clazzAssignmentContentJoinDateAdded ASC LIMIT 1) as firstContentEntryUid
         FROM ClazzAssignment
        LEFT JOIN Clazz ON Clazz.clazzUid = :clazzUid
        WHERE 
        clazzAssignmentClazzUid = Clazz.clazzUid AND CAST(clazzAssignmentInactive AS INTEGER) = 0
        
    """)
    abstract fun findWithMetricsByClazzUid(clazzUid: Long)
            : DataSource.Factory<Int, ClazzAssignmentWithMetrics>


    @Query(""" SELECT ClazzAssignment.*,
         0 as totalStudents, 
         0 as startedStudents, 
         0 as notStartedStudents, 
         0 as completedStudents,
         0 as clazzAssignmentProgress,
         "" as storiesTitle, 
         (SELECT clazzAssignmentContentJoinContentUid FROM ClazzAssignmentContentJoin
          WHERE clazzAssignmentContentJoinClazzAssignmentUid = ClazzAssignment.clazzAssignmentUid
          ORDER BY clazzAssignmentContentJoinDateAdded ASC LIMIT 1) as firstContentEntryUid
         FROM ClazzAssignment WHERE clazzAssignmentUid = :clazzAssignmentUid 
        AND CAST(clazzAssignmentInactive AS INTEGER) = 0""")
    abstract fun findWithMetricByUidAsync(clazzAssignmentUid: Long): ClazzAssignmentWithMetrics?

    @Query("""
    SELECT Person.*, 
        (
            SELECT 
            CASE WHEN
            (
                SELECT
                MIN(StatementEntity.timestamp)
                FROM StatementEntity
                LEFT JOIN XObjectEntity ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid
                WHERE XObjectEntity.objectContentEntryUid IN 
				( SELECT ContentEntry.contentEntryUid FROM ClazzAssignmentContentJoin
					LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzAssignmentContentJoinContentUid
					WHERE clazzAssignmentContentJoinClazzAssignmentUid = ClazzAssignment.clazzAssignmentUid
					AND CAST(clazzAssignmentContentJoinInactive AS INTEGER) = 0
				)
                AND StatementEntity.personUid = ClazzMember.clazzMemberPersonUid
				AND StatementEntity.timestamp BETWEEN :fromDate AND :endDate
                
            )  THEN
            (
                SELECT
                MIN(StatementEntity.timestamp)
                FROM StatementEntity
                LEFT JOIN XObjectEntity ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid
                WHERE XObjectEntity.objectContentEntryUid IN 
				( SELECT ContentEntry.contentEntryUid FROM ClazzAssignmentContentJoin
					LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzAssignmentContentJoinContentUid
					WHERE clazzAssignmentContentJoinClazzAssignmentUid = ClazzAssignment.clazzAssignmentUid
					AND CAST(clazzAssignmentContentJoinInactive AS INTEGER) = 0
				)
                AND StatementEntity.personUid = ClazzMember.clazzMemberPersonUid
				AND StatementEntity.timestamp BETWEEN :fromDate AND :endDate

            ) ELSE 0 END 
		) as startedDate,
        0 as finishedDate,
        (
        SELECT SUM(progress)/
            (SELECT count(*)
            FROM ClazzAssignmentContentJoin
            LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzAssignmentContentJoinContentUid
            WHERE clazzAssignmentContentJoinClazzAssignmentUid = ClazzAssignment.clazzAssignmentUid
            AND CAST(clazzAssignmentContentJoinInactive AS INTEGER) = 0) 
        FROM (
            SELECT 
            CASE WHEN
            (
                SELECT
                MAX(extensionProgress)
                FROM StatementEntity
                LEFT JOIN XObjectEntity ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid
                WHERE XObjectEntity.objectContentEntryUid = ContentEntry.contentEntryUid
                AND StatementEntity.personUid = ClazzMember.clazzMemberPersonUid
                AND StatementEntity.timestamp BETWEEN :fromDate AND :endDate
                
            )  THEN
            (
                SELECT
                MAX(extensionProgress)
                FROM StatementEntity
                LEFT JOIN XObjectEntity ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid
                WHERE XObjectEntity.objectContentEntryUid = ContentEntry.contentEntryUid
                AND StatementEntity.personUid = ClazzMember.clazzMemberPersonUid
                AND StatementEntity.timestamp BETWEEN :fromDate AND :endDate

            ) ELSE 0 END as progress,
            ContentEntry.contentEntryUid
            FROM ClazzAssignmentContentJoin
            LEFT JOIN ContentEntry ON ContentEntry.contentEntryUid = clazzAssignmentContentJoinContentUid
            WHERE clazzAssignmentContentJoinClazzAssignmentUid = ClazzAssignment.clazzAssignmentUid
            AND CAST(clazzAssignmentContentJoinInactive AS INTEGER) = 0
        )
        ) 
        as percentageCompleted
        FROM ClazzAssignment
        LEFT JOIN Clazz ON Clazz.clazzUid = ClazzAssignment.clazzAssignmentClazzUid
        LEFT JOIN ClazzMember ON ClazzMember.clazzMemberClazzUid = Clazz.clazzUid 
            AND ClazzMember.clazzMemberRole = ${ClazzMember.ROLE_STUDENT}
            AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1
        LEFT JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid 
            AND CAST(Person.active AS INTEGER) = 1
        WHERE CAST(clazzAssignmentInactive AS INTEGER ) = 0 
            AND clazzAssignmentUid = :clazzAssignmentUid 
    """)

    abstract fun findAllStudentsInAssignmentWithMetrics(clazzAssignmentUid: Long, fromDate: Long, endDate: Long)
        :DataSource.Factory<Int, PersonWithAssignmentMetrics>
}
