package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzInvite
import com.ustadmobile.lib.db.entities.ClazzLog


@DoorDao
@Repository
expect abstract class ClazzInviteDao : BaseDao<ClazzInvite> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replace(entity: ClazzInvite): Long
}