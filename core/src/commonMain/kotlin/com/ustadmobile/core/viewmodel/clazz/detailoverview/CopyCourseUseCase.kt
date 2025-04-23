package com.ustadmobile.core.viewmodel.clazz.detailoverview

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.composites.CourseBlockAndDisplayDetails
import com.ustadmobile.lib.db.composites.CourseBlockAndEditEntities
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndAndTerminology
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.CoursePicture
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import io.github.aakira.napier.Napier
import kotlinx.serialization.Serializable

class CopyCourseUseCase(
    private val repoOrDb: UmAppDatabase,
    private val accountManager: UstadAccountManager
) {

    suspend operator fun invoke(
        clazz: Clazz,
        courseBlockListVal: List<CourseBlockAndEditEntities>,
        scheduleListVal: List<Schedule>,
        coursePicture: CoursePicture?
    ): CopyCourseResult {

        val primaryKeyManager = repoOrDb.doorPrimaryKeyManager
        val newClazzUid = primaryKeyManager.nextIdAsync(Clazz.TABLE_ID)
        val currentPersonUid = accountManager.currentAccount.personUid
        val newScheduleUid = primaryKeyManager.nextIdAsync(Schedule.TABLE_ID)

        clazz.clazzUid = newClazzUid
        clazz.clazzOwnerPersonUid = currentPersonUid

        val courseBlocks = courseBlockListVal.map { block ->

            val newCourseBlockUid = primaryKeyManager.nextIdAsync(CourseBlock.TABLE_ID)

            val copiedAssignment = block.assignment?.copy(
                caUid = primaryKeyManager.nextIdAsync(ClazzAssignment.TABLE_ID),
                caClazzUid = newClazzUid,
            )

            block.copy(
                courseBlock = block.courseBlock.copy(
                    cbUid = newCourseBlockUid,
                    cbClazzUid = newClazzUid
                ),
                assignment = copiedAssignment,

            )
        }

        val copiedCoursePicture =  if(coursePicture?.coursePictureUri !=  null ) {
            coursePicture.copy(
                coursePictureUid = newClazzUid,
                coursePictureLct = systemTimeInMillis(),
                coursePictureUri = coursePicture.coursePictureUri,
            )
        } else {
            CoursePicture()
        }
        val clazzWithHolidayCalendarAndAndTerminology = ClazzWithHolidayCalendarAndAndTerminology()
        clazzWithHolidayCalendarAndAndTerminology.coursePicture = copiedCoursePicture

        clazzWithHolidayCalendarAndAndTerminology.clazzUid = newClazzUid

        clazzWithHolidayCalendarAndAndTerminology.clazzOwnerPersonUid = currentPersonUid

        clazz.shallowCopyTo(clazzWithHolidayCalendarAndAndTerminology)


        val schedules = scheduleListVal.map { originalSchedule ->
            originalSchedule.shallowCopy {
                scheduleUid = newScheduleUid
                scheduleClazzUid = clazz.clazzUid
            }
        }
        return CopyCourseResult(
            clazzWithHolidayCalendarAndAndTerminology,
            courseBlocks,
            schedules,
            copiedCoursePicture
        )
    }
}

fun Clazz.shallowCopyTo(target: Clazz) {
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

@Serializable
data class CopyCourseResult(
    val clazz: ClazzWithHolidayCalendarAndAndTerminology,
    val courseBlocks: List<CourseBlockAndEditEntities>,
    val schedules: List<Schedule>,
    val coursePicture: CoursePicture?
)
