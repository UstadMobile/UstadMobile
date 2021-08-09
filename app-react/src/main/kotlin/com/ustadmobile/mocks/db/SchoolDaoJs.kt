package com.ustadmobile.mocks.db

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.core.db.dao.SchoolDao
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.lib.db.entities.SchoolWithMemberCountAndLocation

class SchoolDaoJs: SchoolDao() {

    override suspend fun findByUidAsync(schoolUid: Long): School? {
        return ENTRIES.first { it.schoolUid == schoolUid }
    }

    override suspend fun findByUidWithHolidayCalendarAsync(uid: Long): SchoolWithHolidayCalendar? {
        return ENTRIES.first { it.schoolUid == uid }
    }

    override suspend fun findBySchoolCode(code: String): School? {
        return ENTRIES.first { it.schoolCode == code }
    }

    override suspend fun personHasPermissionWithSchool(
        accountPersonUid: Long,
        schoolUid: Long,
        permission: Long
    ): Boolean {
        return true
    }

    override fun findAllActiveSchoolWithMemberCountAndLocationName(
        searchBit: String,
        personUid: Long,
        permission: Long,
        sortOrder: Int
    ): DoorDataSourceFactory<Int, SchoolWithMemberCountAndLocation> {
        val entries = ENTRIES.map {
            it.unsafeCast<SchoolWithMemberCountAndLocation>().apply {
                numStudents = (it.schoolUid * 12).toInt()
                numTeachers = (it.schoolUid * 2).toInt()
                locationName = "Sample location ${it.schoolUid}"
                clazzCount = (it.schoolUid * 4).toInt()

            }
        }
        return DataSourceFactoryJs(entries)
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
                schoolAddress = "Sample address 1, Dubai"
                holidayCalendar = HolidayCalendarDaoJs.ENTRIES[0]
            },
            SchoolWithHolidayCalendar().apply {
                schoolUid = 2
                schoolName = "Sample school name 2"
                schoolDesc = "Sample school description two to be shown"
                schoolAddress = "Sample address 2, Tanzania"
                holidayCalendar = HolidayCalendarDaoJs.ENTRIES[1]
            },
            SchoolWithHolidayCalendar().apply {
                schoolUid = 3
                schoolName = "Sample school name 3"
                schoolDesc = "Sample school description three to be shown"
                schoolAddress = "Sample address 3, Kenya"
                holidayCalendar = HolidayCalendarDaoJs.ENTRIES[2]
            }
        )
    }
}