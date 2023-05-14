package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.*

object ClazzAssignmentDaoCommon {

    const val SUBMITTER_LIST_WITHOUT_ASSIGNMENT_CTE = """
             WITH SubmitterList (submitterId, name)
            AS (SELECT DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid AS submitterId, 
                       Person.firstNames || ' ' || Person.lastName AS name
                  FROM ClazzEnrolment
                  
                       JOIN Person 
                       ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
                       
                 WHERE :groupUid = 0 
                   AND clazzEnrolmentClazzUid = :clazzUid
                   AND clazzEnrolmentActive
                   AND clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
              GROUP BY submitterId, name
            UNION                 
             SELECT DISTINCT CourseGroupMember.cgmGroupNumber AS submitterId,
                    :group || ' ' || CourseGroupMember.cgmGroupNumber AS name  
               FROM CourseGroupMember
                    JOIN CourseGroupSet
                    ON CourseGroupSet.cgsUid = :groupUid
              WHERE CourseGroupMember.cgmSetUid = CourseGroupSet.cgsUid
                AND CourseGroupMember.cgmGroupNumber != 0
           GROUP BY submitterId, name
            )
        """

    const val ASSIGNMENT_PERMISSION = """
            WITH AssignmentPermission (hasPermission) AS
            (SELECT EXISTS(
               SELECT PrsGrpMbr.groupMemberPersonUid
                  FROM Clazz
                       ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                          ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}
                          ${Clazz.JOIN_FROM_SCOPEDGRANT_TO_PERSONGROUPMEMBER}
                 WHERE Clazz.clazzUid = :clazzUid
                   AND PrsGrpMbr.groupMemberPersonUid = :loggedInPersonUid))
        """



    const val SUBMITTER_LIST_CTE = """
            SubmitterList (submitterId, name)
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
                       
                       LEFT JOIN PeerReviewerAllocation
                       ON PeerReviewerAllocation.praToMarkerSubmitterUid = Person.personUid 
                       AND PeerReviewerAllocation.praMarkerSubmitterUid = :submitterUid
                       AND praActive
                          
                 WHERE ClazzAssignment.caGroupUid = 0
                   AND clazzEnrolmentClazzUid = :clazzUid
                   AND clazzEnrolmentActive
                   AND clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                   AND CourseBlock.cbGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft
                   AND ClazzEnrolment.clazzEnrolmentDateJoined <= CourseBlock.cbGracePeriodDate
                   AND ((SELECT hasPermission FROM AssignmentPermission)
                        OR (ClazzAssignment.caMarkingType = ${ClazzAssignment.MARKED_BY_PEERS}
                        AND PeerReviewerAllocation.praUid IS NOT NULL))
              GROUP BY submitterId, name
            UNION                 
             SELECT DISTINCT CourseGroupMember.cgmGroupNumber AS submitterId,
                    :group || ' ' || CourseGroupMember.cgmGroupNumber AS name  
               FROM CourseGroupMember
                    JOIN ClazzAssignment
                    ON ClazzAssignment.caUid = :assignmentUid
                    
                    LEFT JOIN PeerReviewerAllocation
                    ON PeerReviewerAllocation.praToMarkerSubmitterUid = CourseGroupMember.cgmGroupNumber
                    AND PeerReviewerAllocation.praMarkerSubmitterUid = :submitterUid
                    AND praActive
                    
              WHERE CourseGroupMember.cgmSetUid = ClazzAssignment.caGroupUid
                AND ClazzAssignment.caGroupUid != 0
                AND CourseGroupMember.cgmGroupNumber != 0
                AND ((SELECT hasPermission FROM AssignmentPermission) 
                     OR (ClazzAssignment.caMarkingType = ${ClazzAssignment.MARKED_BY_PEERS}
                     AND PeerReviewerAllocation.praUid IS NOT NULL))
           GROUP BY submitterId, name
            )
        """

    const val SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL = """
        SELECT CASE
                    WHEN (SELECT caGroupUid
                            FROM ClazzAssignment
                           WHERE caUid = :assignmentUid) = 0 THEN :accountPersonUid
                    ELSE COALESCE(
                          (SELECT CourseGroupMember.cgmGroupNumber
                             FROM CourseGroupMember
                            WHERE CourseGroupMember.cgmSetUid = 
                                  (SELECT caGroupUid
                                     FROM ClazzAssignment
                                    WHERE caUid = :assignmentUid)
                              AND CourseGroupMember.cgmPersonUid = :accountPersonUid), -1)
                    END
    """


    const val SORT_DEADLINE_ASC = 1

    const val SORT_DEADLINE_DESC = 2

    const val SORT_TITLE_ASC = 3

    const val SORT_TITLE_DESC = 4

}