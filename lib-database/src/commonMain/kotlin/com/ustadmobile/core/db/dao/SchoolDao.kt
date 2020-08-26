package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*

@UmDao
@UmRepository
@Dao
abstract class SchoolDao : BaseDao<School> {

    @Query("SELECT * FROM School WHERE schoolUid = :schoolUid AND CAST(schoolActive AS INTEGER) = 1")
    abstract fun findByUidAsync(schoolUid: Long): School?

    @Query("""SELECT School.*, HolidayCalendar.* FROM School 
            LEFT JOIN HolidayCalendar ON School.schoolHolidayCalendarUid = HolidayCalendar.umCalendarUid
            WHERE School.schoolUid = :uid""")
    abstract suspend fun findByUidWithHolidayCalendarAsync(uid: Long): SchoolWithHolidayCalendar?




    /** Check if a permission is present on a specific entity e.g. updateState/modify etc */
    @Query("SELECT EXISTS(SELECT 1 FROM School WHERE " +
            "School.schoolUid = :schoolUid AND :accountPersonUid IN " +
            "(${ENTITY_PERSONS_WITH_PERMISSION}))")
    abstract suspend fun personHasPermissionWithSchool(accountPersonUid: Long,
                                                       schoolUid: Long,
                                                      permission: Long) : Boolean

    @Query("""SELECT School.*, 
         (SELECT COUNT(*) FROM SchoolMember WHERE SchoolMember.schoolMemberSchoolUid = School.schoolUid AND 
         CAST(SchoolMember.schoolMemberActive AS INTEGER) = 1 
         AND SchoolMember.schoolMemberRole = ${SchoolMember.SCHOOL_ROLE_STUDENT}) as numStudents,
         (SELECT COUNT(*) FROM SchoolMember WHERE SchoolMember.schoolMemberSchoolUid = School.schoolUid AND 
         CAST(SchoolMember.schoolMemberActive AS INTEGER) = 1 
         AND SchoolMember.schoolMemberRole = ${SchoolMember.SCHOOL_ROLE_TEACHER}) as numTeachers, 
         '' as locationName,
          (SELECT COUNT(*) FROM Clazz WHERE Clazz.clazzSchoolUid = School.schoolUid AND CAST(Clazz.clazzUid AS INTEGER) = 1 ) as clazzCount
         FROM School WHERE CAST(schoolActive AS INTEGER) = 1 
             AND schoolName LIKE :searchBit ORDER BY schoolName ASC""")
    abstract fun findAllActiveSchoolWithMemberCountAndLocationNameAsc(searchBit: String): DataSource.Factory<Int, SchoolWithMemberCountAndLocation>


    @Query("""SELECT School.*, 
         (SELECT COUNT(*) FROM SchoolMember WHERE SchoolMember.schoolMemberSchoolUid = School.schoolUid AND 
         CAST(SchoolMember.schoolMemberActive AS INTEGER) = 1 
         AND SchoolMember.schoolMemberRole = ${SchoolMember.SCHOOL_ROLE_STUDENT}) as numStudents,
         (SELECT COUNT(*) FROM SchoolMember WHERE SchoolMember.schoolMemberSchoolUid = School.schoolUid AND 
         CAST(SchoolMember.schoolMemberActive AS INTEGER) = 1 
         AND SchoolMember.schoolMemberRole = ${SchoolMember.SCHOOL_ROLE_TEACHER}) as numTeachers, 
         '' as locationName,
          (SELECT COUNT(*) FROM Clazz WHERE Clazz.clazzSchoolUid = School.schoolUid AND CAST(Clazz.clazzUid AS INTEGER) = 1 ) as clazzCount 
         FROM School WHERE CAST(schoolActive AS INTEGER) = 1 
             AND schoolName LIKE :searchBit ORDER BY schoolName DESC""")
    abstract fun findAllActiveSchoolWithMemberCountAndLocationNameDesc(searchBit: String): DataSource.Factory<Int, SchoolWithMemberCountAndLocation>


    @Update
    abstract suspend fun updateAsync(entity: School): Int

    companion object {

        const val ENTITY_PERSONS_WITH_PERMISSION_PT1 = """
            SELECT DISTINCT Person.PersonUid FROM Person
            LEFT JOIN PersonGroupMember ON Person.personUid = PersonGroupMember.groupMemberPersonUid
            LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
            LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid
            WHERE 
            CAST(Person.admin AS INTEGER) = 1
            OR 
            (EntityRole.ertableId = ${School.TABLE_ID} AND 
            EntityRole.erEntityUid = School.schoolUid AND
            (Role.rolePermissions &  
        """

        const val ENTITY_PERSONS_WITH_PERMISSION_PT2 = ") > 0)"

        const val ENTITY_PERSONS_WITH_PERMISSION = "${ENTITY_PERSONS_WITH_PERMISSION_PT1} " +
                ":permission ${ENTITY_PERSONS_WITH_PERMISSION_PT2}"


    }

}
