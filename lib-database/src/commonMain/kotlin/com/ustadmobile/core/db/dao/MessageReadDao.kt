package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.MessageRead

@DoorDao
@Repository
expect abstract class MessageReadDao: BaseDao<MessageRead>{



}