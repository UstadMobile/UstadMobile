package com.ustadmobile.core.db.dao

import com.ustadmobile.door.DoorDataSourceFactory
import androidx.room.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.Role

@Repository
@Dao
abstract class RoleDao : BaseDao<Role> {


    companion object {

        const val SORT_NAME_ASC = 1

        const val SORT_NAME_DESC = 2

    }
}
