package com.ustadmobile.core.db.dao

import androidx.room.Dao
import com.ustadmobile.core.db.dao.RoleDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Role

@UmDao(updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@Dao
@UmRepository
abstract class RoleDao : BaseDao<Role> {
    companion object {

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"
    }

}
