package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
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

    @Update
    abstract suspend fun updateAsync(entity: SchoolMember): Int


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
         ORDER BY CASE(:sortOrder)
                WHEN $SORT_FIRST_NAME_ASC THEN Person.firstNames
                WHEN $SORT_LAST_NAME_ASC THEN Person.lastName
                ELSE ''
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_FIRST_NAME_DESC THEN Person.firstNames
                WHEN $SORT_LAST_NAME_DESC THEN Person.lastName
                ELSE ''
            END DESC
            """)
    abstract fun findAllActiveMembersBySchoolAndRoleUid(schoolUid: Long, role: Int,
                                                           sortOrder: Int,
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

    companion object {

        const val SORT_FIRST_NAME_ASC = 1

        const val SORT_FIRST_NAME_DESC = 2

        const val SORT_LAST_NAME_ASC = 3

        const val SORT_LAST_NAME_DESC = 4

    }

}
