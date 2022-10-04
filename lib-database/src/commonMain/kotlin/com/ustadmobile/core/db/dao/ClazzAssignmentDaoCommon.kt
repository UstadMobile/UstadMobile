package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CourseBlock

object ClazzAssignmentDaoCommon {
    const val SUBMITTER_LIST_CTE = """
            WITH SubmitterList (submitterId, name)
            AS (SELECT DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid AS submitterId, 
                       Person.firstNames || ' ' || Person.lastName AS name
                  FROM ClazzEnrolment
                  
                       JOIN Person 
                       ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
                        
                       JOIN ClazzAssignment
                       ON ClazzAssignment.caUid = :assignmentUid

                       JOIN CourseBlock
                       ON CourseBlock.cbEntityUid = ClazzAssignment.caUid
                       AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                       
                 WHERE ClazzAssignment.caGroupUid = 0
                   AND clazzEnrolmentClazzUid = :clazzUid
                   AND clazzEnrolmentActive
                   AND clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                   AND CourseBlock.cbGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft
                   AND ClazzEnrolment.clazzEnrolmentDateJoined <= CourseBlock.cbGracePeriodDate
              GROUP BY submitterId, name
            UNION                 
             SELECT DISTINCT CourseGroupMember.cgmGroupNumber AS submitterId,
                    :group || ' ' || CourseGroupMember.cgmGroupNumber AS name  
               FROM CourseGroupMember
                    JOIN ClazzAssignment
                    ON ClazzAssignment.caUid = :assignmentUid
              WHERE CourseGroupMember.cgmSetUid = ClazzAssignment.caGroupUid
                AND ClazzAssignment.caGroupUid != 0
                AND CourseGroupMember.cgmGroupNumber != 0
           GROUP BY submitterId, name
            )
        """

    const val SORT_DEADLINE_ASC = 1

    const val SORT_DEADLINE_DESC = 2

    const val SORT_TITLE_ASC = 3

    const val SORT_TITLE_DESC = 4

    const val SORT_SCORE_ASC = 5

    const val SORT_SCORE_DESC = 6

    const val SORT_START_DATE_ASC = 7

    const val SORT_START_DATE_DESC = 8
}