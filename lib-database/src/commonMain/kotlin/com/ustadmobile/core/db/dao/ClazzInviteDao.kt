package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ClazzInvite


@DoorDao
@Repository
expect abstract class ClazzInviteDao : BaseDao<ClazzInvite> {
}