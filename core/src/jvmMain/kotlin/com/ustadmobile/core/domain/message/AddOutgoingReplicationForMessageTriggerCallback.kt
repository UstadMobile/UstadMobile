package com.ustadmobile.core.domain.message

import com.ustadmobile.door.DoorDatabaseCallbackSync
import com.ustadmobile.door.DoorSqlDatabase
import com.ustadmobile.door.ext.dbType

class AddOutgoingReplicationForMessageTriggerCallback : DoorDatabaseCallbackSync{

    override fun onCreate(db: DoorSqlDatabase) {
        GenerateOutgoingReplicationForMessageTriggerUseCase().invoke(db.dbType()).forEach { sql ->
            db.execSQL(sql)
        }
    }

    override fun onOpen(db: DoorSqlDatabase) {

    }
}