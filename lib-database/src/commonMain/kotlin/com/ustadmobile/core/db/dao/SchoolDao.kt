package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.SchoolWithHolidayCalendar
import com.ustadmobile.lib.db.entities.SchoolWithMemberCountAndLocation

@UmDao
@UmRepository
@Dao
abstract class SchoolDao : BaseDao<School> {

    @Query("SELECT * FROM School WHERE schoolName=:schoolName AND CAST(schoolActive AS INTEGER) = 1")
    abstract fun findByNameAsync(schoolName: String): School?

    @Query("SELECT * FROM School WHERE schoolUid = :schoolUid AND CAST(schoolActive AS INTEGER) = 1")
    abstract fun findByUidAsync(schoolUid: Long): School?

    @Query("SELECT * FROM School WHERE CAST(schoolActive AS INTEGER) = 1 " +
            " AND schoolName LIKE :searchBit ORDER BY schoolName ASC")
    abstract fun findAllActiveSchoolsNameAsc(searchBit: String): DataSource.Factory<Int, School>


    @Query("""SELECT School.*, HolidayCalendar.* FROM School 
            LEFT JOIN HolidayCalendar ON School.schoolHolidayCalendarUid = HolidayCalendar.umCalendarUid
            WHERE School.schoolUid = :uid""")
    abstract suspend fun findByUidWithHolidayCalendarAsync(uid: Long): SchoolWithHolidayCalendar?


    @Query("""SELECT School.*, 
         0 as numStudents,
         0 as numTeachers, 
         '' as locationName 
         FROM School WHERE CAST(schoolActive AS INTEGER) = 1 
             AND schoolName LIKE :searchBit ORDER BY schoolName ASC""")
    abstract fun findAllActiveSchoolWithMemberCountAndLocationNameAsc(searchBit: String): DataSource.Factory<Int, SchoolWithMemberCountAndLocation>


    @Query("""SELECT School.*, 
         0 as numStudents,
         0 as numTeachers, 
         '' as locationName 
         FROM School WHERE CAST(schoolActive AS INTEGER) = 1 
             AND schoolName LIKE :searchBit ORDER BY schoolName DESC""")
    abstract fun findAllActiveSchoolWithMemberCountAndLocationNameDesc(searchBit: String): DataSource.Factory<Int, SchoolWithMemberCountAndLocation>


    @Update
    abstract suspend fun updateAsync(entity: School): Int

}
