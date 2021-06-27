package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import com.ustadmobile.door.annotation.SyncableEntity

@Entity
@SyncableEntity(tableId = UserSession.TABLE_ID)
class UserSession {

    var usUid: Long = 0

    var usPersonUid: Long = 0

    var usClientNodeId: Int = 0

    var usServerNodeId: Int = 0





    companion object {

        const val TABLE_ID = 679

    }
}