package com.ustadmobile.core.domain.assignment.submitmark

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.coursegroup.CreateXapiGroupForCourseGroupUseCase
import com.ustadmobile.core.domain.xapi.model.VERB_COMPLETED
import com.ustadmobile.core.domain.xapi.model.XapiActivityStatementObject
import com.ustadmobile.core.domain.xapi.model.XapiActor
import com.ustadmobile.core.domain.xapi.model.XapiContext
import com.ustadmobile.core.domain.xapi.model.XapiObjectType
import com.ustadmobile.core.domain.xapi.model.XapiResult
import com.ustadmobile.core.domain.xapi.model.XapiStatement
import com.ustadmobile.core.domain.xapi.model.XapiVerb
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.core.util.ext.roundTo
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.util.ext.toXapiAgent
import com.ustadmobile.core.viewmodel.UstadViewModel.Companion.ARG_CLAZZUID
import com.ustadmobile.core.viewmodel.UstadViewModel.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailViewModel
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ext.shallowCopy

/**
 * Handle submitting a mark. Record the mark in the repository, and then generate an appropriate
 * Xapi statement.
 *
 * To report a student's score based on Xapi statements; one can use:
 * - The latest score for the student (generally the right way to handle assignments e.g. if a mark is updated)
 * - The best score achieved for the student(generally the right way to handle self-directed content)
 *
 * How do we decide on the XapiStatement(s) to determine the mark. Could be
 *   Assignment: latest mark (when marked by teacher) or average of latest distinct marks (peer marking)
 *   best mark: Xapi stuff
 *
 * Statement should roughly follow final example from:
 * https://xapi.com/statements-101/
 *
 * e.g. actor = the student, verb = completed,
 */
class SubmitMarkUseCase(
    private val repo: UmAppDatabase,
    private val endpoint: Endpoint,
    private val createXapiGroupUseCase: CreateXapiGroupForCourseGroupUseCase,
    private val xapiStatementResource: XapiStatementResource,
) {

    suspend operator fun invoke(
        activeUserPerson: Person,
        assignment: ClazzAssignment,
        clazzUid: Long,
        submitterUid: Long,
        draftMark: CourseAssignmentMark,
        submissions: List<CourseAssignmentSubmission>,
        courseBlock: CourseBlock,
    ) {
        val applyPenalty = submissions.isNotEmpty() &&
            (submissions.maxOf { it.casTimestamp }) > courseBlock.cbDeadlineDate

        val activeUserSubmitterUid = repo.clazzAssignmentDao.getSubmitterUid(
            assignmentUid = assignment.caUid,
            clazzUid = clazzUid,
            accountPersonUid = activeUserPerson.personUid,
        )


        //Xapi Actor object representing the one who is marking the assignment.
        val instructorActor: XapiActor = if(assignment.caGroupUid == 0L) {
            activeUserPerson.toXapiAgent(endpoint)
        } else {
            createXapiGroupUseCase(
                groupSetUid = assignment.caGroupUid,
                groupNum = activeUserSubmitterUid.toInt(),
                clazzUid = assignment.caClazzUid,
                assignmentUid = assignment.caUid,
                accountPersonUid = activeUserPerson.personUid,
            )
        }

        val statementActor: XapiActor = if(assignment.caGroupUid == 0L) {
            repo.personDao.findByUidAsync(submitterUid)?.toXapiAgent(endpoint)
                ?: throw IllegalStateException("Could not find person for $submitterUid")
        }else {
            createXapiGroupUseCase(
                groupSetUid = assignment.caGroupUid,
                groupNum = submitterUid.toInt(),
                clazzUid = assignment.caClazzUid,
                assignmentUid = assignment.caUid,
                accountPersonUid = submitterUid,
            )
        }

        val markToRecord = draftMark.shallowCopy {
            camAssignmentUid = assignment.caUid
            camSubmitterUid = submitterUid
            camMarkerSubmitterUid = activeUserSubmitterUid
            camMarkerPersonUid = activeUserPerson.personUid
            camMaxMark = courseBlock.cbMaxPoints ?: 0f
            camClazzUid = clazzUid
            if(applyPenalty) {
                camPenalty = (camMark * (courseBlock.cbLateSubmissionPenalty.toFloat()/100f))
                    .roundTo(2)
                camMark -= camPenalty
            }
        }

        repo.courseAssignmentMarkDao.insertAsync(markToRecord)

        val activityId = UstadUrlComponents(
            viewName = ClazzAssignmentDetailViewModel.DEST_NAME,
            endpoint = endpoint.url,
            queryString = mapOf(
                ARG_CLAZZUID to clazzUid.toString(),
                ARG_ENTITY_UID to assignment.caUid.toString(),
            ).toQueryString()
        ).fullUrl()

        val stmt = XapiStatement(
            actor = statementActor,
            verb = XapiVerb(
                id = VERB_COMPLETED
            ),
            `object` = XapiActivityStatementObject(
                objectType = XapiObjectType.Activity,
                id = activityId,
            ),
            context = XapiContext(
                instructor = instructorActor,
            ),
            result = XapiResult(
                completion = true,
                success = true,
                score = XapiResult.Score(
                    scaled = draftMark.camMark / draftMark.camMaxMark,
                    raw = draftMark.camMark,
                    min = 0f,
                    max = draftMark.camMaxMark,
                )
            )
        )
        xapiStatementResource.post(
            statements = listOf(stmt),
            xapiSession = XapiSession(
                endpoint = endpoint,
                accountPersonUid = activeUserPerson.personUid,
                accountUsername = activeUserPerson.username!!,
                clazzUid = clazzUid,
                cbUid = courseBlock.cbUid,
                rootActivityId = activityId,
            )
        )

    }
}