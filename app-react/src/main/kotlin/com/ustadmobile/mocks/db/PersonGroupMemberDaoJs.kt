package com.ustadmobile.mocks.db

import com.ustadmobile.core.db.dao.PersonGroupMemberDao
import com.ustadmobile.lib.db.entities.PersonGroupMember
import com.ustadmobile.mocks.db.PersonDaoJs.Companion.ENTRIES

class PersonGroupMemberDaoJs: PersonGroupMemberDao() {
    override suspend fun findAllGroupWherePersonIsIn(personUid: Long): List<PersonGroupMember> {
        TODO("Not yet implemented")
    }

    override suspend fun checkPersonBelongsToGroup(
        groupUid: Long,
        personUid: Long
    ): List<PersonGroupMember> {
        return GROUP_MEMBERS
    }

    override suspend fun moveGroupAsync(personUid: Long, newGroup: Long, oldGroup: Long): Int {
        TODO("Not yet implemented")
    }

    override suspend fun setGroupMemberToInActive(personUid: Long, groupUid: Long) {
        TODO("Not yet implemented")
    }

    override fun insert(entity: PersonGroupMember): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: PersonGroupMember): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<PersonGroupMember>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<PersonGroupMember>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: PersonGroupMember) {
        TODO("Not yet implemented")
    }

    companion object {
        val GROUP_MEMBERS = listOf(
            PersonGroupMember().apply {
                groupMemberUid = 1
                groupMemberPersonUid = ENTRIES.first().personUid
            }
        )
    }
}