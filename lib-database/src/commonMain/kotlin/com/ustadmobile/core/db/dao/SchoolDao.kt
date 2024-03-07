package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@Repository
@DoorDao
expect abstract class SchoolDao : BaseDao<School> {

    @Query("SELECT * FROM School WHERE schoolUid = :schoolUid AND CAST(schoolActive AS INTEGER) = 1")
    abstract suspend fun findByUidAsync(schoolUid: Long): School?

    @Query("""SELECT School.*, HolidayCalendar.* FROM School 
            LEFT JOIN HolidayCalendar ON School.schoolHolidayCalendarUid = HolidayCalendar.umCalendarUid
            WHERE School.schoolUid = :uid""")
    abstract suspend fun findByUidWithHolidayCalendarAsync(uid: Long): SchoolWithHolidayCalendar?


    @Query("SELECT * FROM School WHERE schoolCode = :code")
    abstract suspend fun findBySchoolCode(code: String): School?

    @Query("SELECT * FROM School WHERE schoolCode = :code")
    @Repository(Repository.METHOD_DELEGATE_TO_WEB)
    abstract suspend fun findBySchoolCodeFromWeb(code: String): School?


    @Update
    abstract suspend fun updateAsync(entity: School): Int


}
