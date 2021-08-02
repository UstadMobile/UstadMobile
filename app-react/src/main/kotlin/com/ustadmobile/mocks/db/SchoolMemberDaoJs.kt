package com.ustadmobile.mocks.db

import androidx.paging.DataSource
import com.ustadmobile.core.db.dao.SchoolMemberDao
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.SchoolMember
import com.ustadmobile.lib.db.entities.SchoolMemberWithPerson

class SchoolMemberDaoJs: SchoolMemberDao() {
    override fun findByUidAsync(schoolMemberUid: Long): SchoolMember? {
        return ENTRIES.first { it.schoolMemberUid == schoolMemberUid }
    }

    override suspend fun updateAsync(entity: SchoolMember): Int {
        TODO("Not yet implemented")
    }

    override suspend fun findBySchoolAndPersonAndRole(
        schoolUid: Long,
        personUid: Long,
        role: Int,
        timeFilter: Long
    ): List<SchoolMember> {
        return ENTRIES.filter { it.schoolMemberSchoolUid == schoolUid
                && it.schoolMemberRole == role}
    }

    override fun findAllActiveMembersBySchoolAndRoleUid(
        schoolUid: Long,
        role: Int,
        sortOrder: Int,
        searchQuery: String,
        accountPersonUid: Long
    ): DataSource.Factory<Int, SchoolMemberWithPerson> {
        val entries = ENTRIES.filter { it.schoolMemberSchoolUid == schoolUid
                && it.schoolMemberRole == role}
        return DataSourceFactoryJs(entries)
    }

    override suspend fun findAllTest(
        schoolUid: Long,
        role: Int,
        searchQuery: String
    ): List<SchoolMemberWithPerson> {
        return ENTRIES.filter { it.schoolMemberSchoolUid == schoolUid
                && it.schoolMemberRole == role}
    }

    override fun insert(entity: SchoolMember): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: SchoolMember): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<SchoolMember>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<SchoolMember>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: SchoolMember) {
        TODO("Not yet implemented")
    }

    companion object {
        val ENTRIES = PersonDaoJs.ENTRIES.mapIndexed { index, it ->
            SchoolMemberWithPerson().apply {
                person = it
                schoolMemberUid = (index + 1).toLong()
                schoolMemberPersonUid = it.personUid
                schoolMemberSchoolUid = 1
                schoolMemberJoinDate = 1627569159000L
                schoolMemberRole = when {
                    it.admin && index < 1 -> Role.ROLE_SCHOOL_STAFF_UID
                    index <= 1 -> Role.ROLE_SCHOOL_STUDENT_UID
                    else -> Role.ROLE_SCHOOL_STUDENT_PENDING_UID
                }
            }
        }
    }
}