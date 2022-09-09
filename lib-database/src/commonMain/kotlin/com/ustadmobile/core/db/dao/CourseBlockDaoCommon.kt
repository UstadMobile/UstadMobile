package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CourseBlock

object CourseBlockDaoCommon {

    const val SUBMITTER_LIST_IN_CLAZZ_CTE = """
            SubmitterList (submitterId, assignmentUid)
            AS (SELECT DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid AS submitterId,
                       ClazzAssignment.caUid AS assignmentUid
                  
                  FROM ClazzEnrolment
                  
                       JOIN Person 
                       ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
                        
                       JOIN ClazzAssignment
                       ON ClazzAssignment.caClazzUid = :clazzUid

                       JOIN CourseBlock
                       ON CourseBlock.cbEntityUid = ClazzAssignment.caUid
                       AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                       
                 WHERE ClazzAssignment.caGroupUid = 0
                   AND clazzEnrolmentClazzUid = :clazzUid
                   AND clazzEnrolmentActive
                   AND clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                   AND CourseBlock.cbGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft
                   AND ClazzEnrolment.clazzEnrolmentDateJoined <= CourseBlock.cbGracePeriodDate
              GROUP BY submitterId, assignmentUid
            UNION                 
             SELECT DISTINCT CourseGroupMember.cgmGroupNumber AS submitterId,
                    ClazzAssignment.caUid AS assignmentUid
               FROM CourseGroupMember
                    JOIN ClazzAssignment
                    ON ClazzAssignment.caClazzUid = :clazzUid
              WHERE CourseGroupMember.cgmSetUid = ClazzAssignment.caGroupUid
                AND ClazzAssignment.caGroupUid != 0
                AND CourseGroupMember.cgmGroupNumber != 0
           GROUP BY submitterId, assignmentUid
            )
        """
}