package com.ustadmobile.mocks.db

import androidx.paging.DataSource
import com.ustadmobile.core.db.dao.SchoolDao
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.lib.db.entities.SchoolWithMemberCountAndLocation

class SchoolDaoJs: SchoolDao() {

    override suspend fun findByUidAsync(schoolUid: Long): School? {
        TODO("Not yet implemented")
    }

    override suspend fun findByUidWithHolidayCalendarAsync(uid: Long): SchoolWithHolidayCalendar? {
        TODO("Not yet implemented")
    }

    override suspend fun findBySchoolCode(code: String): School? {
        TODO("Not yet implemented")
    }

    override suspend fun personHasPermissionWithSchool(
        accountPersonUid: Long,
        schoolUid: Long,
        permission: Long
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun findAllActiveSchoolWithMemberCountAndLocationName(
        searchBit: String,
        personUid: Long,
        permission: Long,
        sortOrder: Int
    ): DataSource.Factory<Int, SchoolWithMemberCountAndLocation> {
        TODO("Not yet implemented")
    }

    override suspend fun updateAsync(entity: School): Int {
        TODO("Not yet implemented")
    }

    override fun insert(entity: School): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: School): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<School>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<School>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: School) {
        TODO("Not yet implemented")
    }

    companion object {
        val ENTRIES = listOf(
            SchoolWithHolidayCalendar().apply {
                schoolUid = 1
                schoolName = "Sample school name 1"
                schoolDesc = "Sample school description to be shown"
                holidayCalendar = HolidayCalendarDaoJs.ENTRIES[0]
            },
            SchoolWithHolidayCalendar().apply {
                schoolUid = 2
                schoolName = "Sample school name 2"
                schoolDesc = "Sample school description two to be shown"
                holidayCalendar = HolidayCalendarDaoJs.ENTRIES[1]
            },
            SchoolWithHolidayCalendar().apply {
                schoolUid = 3
                schoolName = "Sample school name 3"
                schoolDesc = "Sample school description three to be shown"
                holidayCalendar = HolidayCalendarDaoJs.ENTRIES[2]
            }
        )
    }
}