package com.ustadmobile.mocks.db

import androidx.paging.DataSource
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mocks.DoorLiveDataJs
import com.ustadmobile.mocks.db.DatabaseJs.Companion.ALLOW_ACCESS
import kotlin.js.Date


open class ClazzDaoJs: ClazzDao() {
    override fun findByUid(uid: Long): Clazz? {
        return ENTRIES.firstOrNull{it.clazzUid == uid}
    }

    override fun findByUidLive(uid: Long): DoorLiveData<Clazz?> {
        return DoorLiveDataJs(ENTRIES.firstOrNull{it.clazzUid == uid})
    }

    override suspend fun findByClazzCode(code: String): Clazz? {
        return ENTRIES.firstOrNull{it.clazzCode == code}
    }

    override fun findAllLive(): DoorLiveData<List<Clazz>> {
        return DoorLiveDataJs(ENTRIES)
    }

    override fun findAll(): List<Clazz> {
        return ENTRIES
    }

    override suspend fun findByUidAsync(uid: Long): Clazz? {
        return ENTRIES.firstOrNull{it.clazzUid == uid}
    }

    override suspend fun findByUidWithHolidayCalendarAsync(uid: Long): ClazzWithHolidayCalendarAndSchool? {
        TODO("Not yet implemented")
    }

    override suspend fun updateAsync(entity: Clazz): Int {
        TODO("Not yet implemented")
    }

    override suspend fun findAllClazzesBySchool(schoolUid: Long): List<Clazz> {
        TODO("Not yet implemented")
    }

    override fun findAllClazzesBySchoolLive(schoolUid: Long): DataSource.Factory<Int, Clazz> {
        TODO("Not yet implemented")
    }

    override suspend fun updateSchoolOnClazzUid(clazzUid: Long, schoolUid: Long) {
        TODO("Not yet implemented")
    }

    override fun findClazzesWithPermission(
        searchQuery: String,
        personUid: Long,
        excludeSelectedClazzList: List<Long>,
        excludeSchoolUid: Long,
        sortOrder: Int,
        filter: Int,
        currentTime: Long,
        permission: Long,
        selectedSchool: Long
    ): DataSource.Factory<Int, ClazzWithListDisplayDetails> {
        return DataSourceFactoryJs(ENTRIES)

    }

    override suspend fun getClassNamesFromListOfIds(ids: List<Long>): List<UidAndLabel> {
        TODO("Not yet implemented")
    }

    override fun findByClazzName(name: String): List<Clazz> {
        return ENTRIES.filter{it.clazzName == name}
    }

    override suspend fun updateClazzAttendanceAverageAsync(clazzUid: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun personHasPermissionWithClazz(
        accountPersonUid: Long,
        clazzUid: Long,
        permission: Long
    ): Boolean {
        return ALLOW_ACCESS
    }

    override fun getClazzWithDisplayDetails(
        clazzUid: Long,
        currentTime: Long
    ): DoorLiveData<ClazzWithDisplayDetails?> {
        val clazz = ENTRIES.firstOrNull{it.clazzUid == clazzUid}
            .unsafeCast<ClazzWithDisplayDetails>()
            .apply {
                clazzStartTime = 1626769537000L
                clazzEndTime = 1628769537000L
                clazzSchool = School().apply {
                    schoolName = "Sample school name"
                }
                clazzHolidayCalendar = HolidayCalendar().apply {
                    umCalendarName = "Sample calender name"

                }
        }
        return DoorLiveDataJs((clazz))
    }

    override fun findClazzesWithEffectiveHolidayCalendarAndFilter(filterUid: Long): List<ClazzWithHolidayCalendarAndSchool> {
        TODO("Not yet implemented")
    }

    override suspend fun getClazzWithSchool(clazzUid: Long): ClazzWithSchool? {
        TODO("Not yet implemented")
    }

    override fun insert(entity: Clazz): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: Clazz): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<Clazz>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<Clazz>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: Clazz) {
        TODO("Not yet implemented")
    }

    override suspend fun insertListAsync(entityList: List<Clazz>) {
        TODO("Not yet implemented")
    }

    override suspend fun updateListAsync(entityList: List<Clazz>) {
        TODO("Not yet implemented")
    }

    companion object {
        val ENTRIES = listOf(
            ClazzWithListDisplayDetails().apply {
                clazzUid = 1
                clazzName = "Class 1"
                clazzDesc = "In descriptive writing, the author does not just tell the reader what was seen, felt, tested, smelled, or heard. Rather, the author describes something from their own experience and, through careful choice of words and phrasing, makes it seem real"
                numStudents = 3
                numTeachers = 1
                teacherNames = "Jane Doe"
                lastRecorded = Date().getTime().toLong()
                clazzCode = "tyui"
                attendanceAverage = 0.0f
                clazzActiveEnrolment = ClazzEnrolment().apply {
                    clazzEnrolmentRole = ClazzEnrolment.ROLE_TEACHER
                }
            },
            ClazzWithListDisplayDetails().apply {
                clazzUid = 2
                clazzName = "Class 2 - French"
                clazzDesc = "Sample french class one description"
                numStudents = 30
                numTeachers = 3
                teacherNames = "June Doe"
                lastRecorded = Date().getTime().toLong()
                clazzCode = "xetyr"
                attendanceAverage = 0.9f
            },
            ClazzWithListDisplayDetails().apply {
                clazzUid = 3
                clazzName = "Class 3 - Swahili"
                clazzDesc = "In descriptive writing, the author does not just tell the reader what was seen, felt, tested, smelled, or heard. Rather, the author describes something from their own experience and, through careful choice of words and phrasing, makes it seem real"
                numStudents = 20
                numTeachers = 1
                teacherNames = "Sam John"
                lastRecorded = Date().getTime().toLong()
                clazzCode = "hnjyk"
                attendanceAverage = 0.7f
                clazzActiveEnrolment = ClazzEnrolment().apply {
                    clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT_PENDING
                }
            }
        )
    }

}