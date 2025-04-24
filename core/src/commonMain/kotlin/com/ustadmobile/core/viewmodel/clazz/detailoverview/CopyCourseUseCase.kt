package com.ustadmobile.core.viewmodel.clazz.detailoverview

import com.ustadmobile.core.MR.strings.assignment
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel.ClazzAction
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CourseBlockAndDisplayDetails
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndAndTerminology
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CourseBlockPicture
import com.ustadmobile.lib.db.entities.CoursePicture
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class CopyCourseUseCase(
    private val repoOrDb: UmAppDatabase,
    private val accountManager: UstadAccountManager
) {

    @Serializable
    data class CopyCourseResult(
        val clazz: ClazzWithHolidayCalendarAndAndTerminology,
        val courseBlocks: List<CourseBlockAndEditEntities>,
        val schedules: List<Schedule>,
    )

    suspend operator fun invoke(
        clazz: Clazz,
        scheduleListVal: List<Schedule>,
    ): CopyCourseResult {
            val courseBlocksDb = repoOrDb.courseBlockDao().findAllCourseBlockByClazzUidAsync(
                clazzUid = clazz.clazzUid,
                includeInactive = false
            )

            val assignmentPeerAllocations = repoOrDb.peerReviewerAllocationDao()
                .getAllPeerReviewerAllocationsByClazzUid(
                    clazzUid = clazz.clazzUid,
                    includeInactive = false
                )

        val clazzWithHolidayCalendarAndAndTerminology = repoOrDb.clazzDao().findByUidWithHolidayCalendarAsync(clazz.clazzUid)  ?: ClazzWithHolidayCalendarAndAndTerminology()

        //  Map to CourseBlockAndEditEntities
            val courseBlocksMapped = courseBlocksDb.map {
                CourseBlockAndEditEntities(
                    courseBlock = it.courseBlock!!, //CourseBlock can't be null as per query
                    courseBlockPicture = it.courseBlockPicture ?: CourseBlockPicture(
                        cbpUid = it.courseBlock!!.cbUid
                    ),
                    contentEntry = it.contentEntry,
                    contentEntryLang = it.contentEntryLang,
                    assignment = it.assignment,
                    assignmentCourseGroupSetName = it.assignmentCourseGroupSetName,
                    assignmentPeerAllocations = assignmentPeerAllocations.filter { allocation ->
                        allocation.praAssignmentUid == it.assignment?.caUid
                    }
                )
        }

        val primaryKeyManager = repoOrDb.doorPrimaryKeyManager
        val newClazzUid = primaryKeyManager.nextIdAsync(Clazz.TABLE_ID)
        val currentPersonUid = accountManager.currentAccount.personUid
        val newScheduleUid = primaryKeyManager.nextIdAsync(Schedule.TABLE_ID)



        val courseBlocks = courseBlocksMapped.map { block ->

            val newCourseBlockUid = primaryKeyManager.nextIdAsync(CourseBlock.TABLE_ID)

            val copiedAssignment = block.assignment?.copy(
                caUid = primaryKeyManager.nextIdAsync(ClazzAssignment.TABLE_ID),
                caClazzUid = newClazzUid,
                caGroupUid = 0,
                caMarkingType = ClazzAssignment.MARKED_BY_COURSE_LEADER
            )
            block.copy(
                courseBlock = block.courseBlock.copy(
                    cbUid = newCourseBlockUid,
                    cbClazzUid = newClazzUid,
                    cbEntityUid = copiedAssignment?.caUid ?: 0
                ),
                assignment = copiedAssignment,

            )
        }
        clazz.shallowCopyTo(clazzWithHolidayCalendarAndAndTerminology)
        clazzWithHolidayCalendarAndAndTerminology.coursePicture = clazzWithHolidayCalendarAndAndTerminology.coursePicture?.copy(newClazzUid) ?: CoursePicture(newClazzUid)
        clazzWithHolidayCalendarAndAndTerminology.clazzUid = newClazzUid
        clazzWithHolidayCalendarAndAndTerminology.clazzOwnerPersonUid = currentPersonUid
        clazzWithHolidayCalendarAndAndTerminology.clazzUid = newClazzUid
        clazzWithHolidayCalendarAndAndTerminology.clazzOwnerPersonUid = currentPersonUid

        val schedules = scheduleListVal.map { originalSchedule ->
            originalSchedule.shallowCopy {
                scheduleUid = newScheduleUid
                scheduleClazzUid = clazz.clazzUid
            }
        }
        return CopyCourseResult(
            clazzWithHolidayCalendarAndAndTerminology,
            courseBlocks,
            schedules)
    }
}

private fun Clazz.shallowCopyTo(target: Clazz) {
    target.clazzName = this.clazzName
    target.clazzDesc = this.clazzDesc
    target.attendanceAverage = this.attendanceAverage
    target.clazzHolidayUMCalendarUid = this.clazzHolidayUMCalendarUid
    target.clazzScheuleUMCalendarUid = this.clazzScheuleUMCalendarUid
    target.isClazzActive = this.isClazzActive
    target.clazzLocationUid = this.clazzLocationUid
    target.clazzStartTime = this.clazzStartTime
    target.clazzEndTime = this.clazzEndTime
    target.clazzFeatures = this.clazzFeatures
    target.clazzSchoolUid = this.clazzSchoolUid
    target.clazzEnrolmentPolicy = this.clazzEnrolmentPolicy
    target.clazzTerminologyUid = this.clazzTerminologyUid
    target.clazzMasterChangeSeqNum = this.clazzMasterChangeSeqNum
    target.clazzLocalChangeSeqNum = this.clazzLocalChangeSeqNum
    target.clazzLastChangedBy = this.clazzLastChangedBy
    target.clazzLct = this.clazzLct
    target.clazzTimeZone = this.clazzTimeZone
    target.clazzStudentsPersonGroupUid = this.clazzStudentsPersonGroupUid
    target.clazzTeachersPersonGroupUid = this.clazzTeachersPersonGroupUid
    target.clazzPendingStudentsPersonGroupUid = this.clazzPendingStudentsPersonGroupUid
    target.clazzParentsPersonGroupUid = this.clazzParentsPersonGroupUid
    target.clazzCode = this.clazzCode
}

