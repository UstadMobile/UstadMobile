package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*

@UmDao
@UmRepository
@Dao
abstract class SchoolMemberDao : BaseDao<SchoolMember> {


    @Query("SELECT * FROM SchoolMember WHERE schoolMemberUid = :schoolMemberUid " +
            " AND CAST(schoolMemberActive AS INTEGER) = 1")
    abstract fun findByUidAsync(schoolMemberUid: Long): SchoolMember?


    @Query("""
        SELECT * FROM SchoolMember WHERE schoolMemberSchoolUid = :schoolUid
        AND schoolMemberPersonUid = :personUid
        AND schoolMemberRole = :role
    """)
    abstract suspend fun findBySchoolAndPersonAndRole(schoolUid: Long, personUid: Long, role: Int): List<SchoolMember>


    @Query("""SELECT SchoolMember.*, Person.* FROM SchoolMember
        LEFT JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
        WHERE CAST(SchoolMember.schoolMemberActive AS INTEGER) = 1
        AND SchoolMember.schoolMemberSchoolUid = :schoolUid 
        AND SchoolMember.schoolMemberRole = :role
        AND CAST(Person.active AS INTEGER) = 1
        AND (Person.firstNames || ' ' || Person.lastName) LIKE :searchQuery
        ORDER BY Person.firstNames ASC""")
    abstract fun findAllActiveMembersDescBySchoolAndRoleUidAsc(schoolUid: Long, role: Int,
                                                               searchQuery: String)
            : DataSource.Factory<Int, SchoolMemberWithPerson>

    @Query("""SELECT SchoolMember.*, Person.* FROM SchoolMember
        LEFT JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
        WHERE CAST(SchoolMember.schoolMemberActive AS INTEGER) = 1
        AND SchoolMember.schoolMemberSchoolUid = :schoolUid 
        AND SchoolMember.schoolMemberRole = :role
        AND CAST(Person.active AS INTEGER) = 1
        AND (Person.firstNames || ' ' || Person.lastName) LIKE :searchQuery
        ORDER BY Person.firstNames DESC""")
    abstract fun findAllActiveMembersAscBySchoolAndRoleUidAsc(schoolUid: Long, role: Int,
                                                              searchQuery: String)
            : DataSource.Factory<Int, SchoolMemberWithPerson>

    @Query("""SELECT SchoolMember.*, Person.* FROM SchoolMember
        LEFT JOIN Person ON Person.personUid = SchoolMember.schoolMemberPersonUid
        WHERE CAST(SchoolMember.schoolMemberActive AS INTEGER) = 1
        AND SchoolMember.schoolMemberSchoolUid = :schoolUid 
        AND SchoolMember.schoolMemberRole = :role
        AND CAST(Person.active AS INTEGER) = 1
        AND (Person.firstNames || ' ' || Person.lastName) LIKE :searchQuery
        ORDER BY Person.firstNames DESC""")
    abstract suspend fun findAllTest(schoolUid: Long, role: Int, searchQuery: String): List<SchoolMemberWithPerson>

}
