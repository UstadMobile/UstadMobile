package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CourseBlock

object ClazzDaoCommon {
    const val SORT_CLAZZNAME_ASC = 1

    const val SORT_CLAZZNAME_DESC = 2

    const val SORT_ATTENDANCE_ASC = 3

    const val SORT_ATTENDANCE_DESC = 4

    const val FILTER_ACTIVE_ONLY = 1

    const val FILTER_CURRENTLY_ENROLLED = 5

    const val FILTER_PAST_ENROLLMENTS = 6

    const val SELECT_ACTIVE_CLAZZES = "SELECT * FROM Clazz WHERE CAST(isClazzActive AS INTEGER) = 1"


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
                       
                       LEFT JOIN PeerReviewerAllocation
                       ON PeerReviewerAllocation.praToMarkerSubmitterUid = Person.personUid 
                       AND PeerReviewerAllocation.praMarkerSubmitterUid = :personUid
                       AND praActive
                       
                 WHERE ClazzAssignment.caGroupUid = 0
                   AND clazzEnrolmentClazzUid = :clazzUid
                   AND clazzEnrolmentActive
                   AND clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                   AND CourseBlock.cbGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft
                   AND ClazzEnrolment.clazzEnrolmentDateJoined <= CourseBlock.cbGracePeriodDate
                   AND ((SELECT hasPermission FROM CtePermissionCheck)
                        OR (ClazzAssignment.caMarkingType = ${ClazzAssignment.MARKED_BY_PEERS}
                        AND PeerReviewerAllocation.praUid IS NOT NULL))
              GROUP BY submitterId, assignmentUid
            UNION                 
             SELECT DISTINCT CourseGroupMember.cgmGroupNumber AS submitterId,
                    ClazzAssignment.caUid AS assignmentUid
               FROM CourseGroupMember
                    JOIN ClazzAssignment
                    ON ClazzAssignment.caClazzUid = :clazzUid
                    
                    LEFT JOIN PeerReviewerAllocation
                    ON PeerReviewerAllocation.praToMarkerSubmitterUid = CourseGroupMember.cgmGroupNumber
                    AND PeerReviewerAllocation.praMarkerSubmitterUid = (SELECT CourseGroupMember.cgmGroupNumber 
                                                                          FROM CourseGroupMember 
                                                                          WHERE cgmSetUid = ClazzAssignment.caGroupUid
                                                                            AND cgmPersonUid = :personUid)
                    AND praActive
                    
              WHERE CourseGroupMember.cgmSetUid = ClazzAssignment.caGroupUid
                AND ClazzAssignment.caGroupUid != 0
                AND CourseGroupMember.cgmGroupNumber != 0
                AND ((SELECT hasPermission FROM CtePermissionCheck)
                        OR (ClazzAssignment.caMarkingType = ${ClazzAssignment.MARKED_BY_PEERS}
                        AND PeerReviewerAllocation.praUid IS NOT NULL))
           GROUP BY submitterId, assignmentUid
            )
        """
}