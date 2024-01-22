package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.SyncNode
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ChatMember
import com.ustadmobile.lib.db.entities.UserSession

@DoorDao
@Repository
expect abstract class ChatMemberDao: BaseDao<ChatMember>{


}