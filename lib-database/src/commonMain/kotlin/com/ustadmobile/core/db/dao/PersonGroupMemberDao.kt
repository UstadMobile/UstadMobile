package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.PersonGroupMember

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, 
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class PersonGroupMemberDao : BaseDao<PersonGroupMember> {

    @Query("SELECT * FROM PersonGroupMember WHERE groupMemberPersonUid = :personUid")
    abstract suspend fun findAllGroupWherePersonIsIn(personUid: Long) : List<PersonGroupMember>

}
