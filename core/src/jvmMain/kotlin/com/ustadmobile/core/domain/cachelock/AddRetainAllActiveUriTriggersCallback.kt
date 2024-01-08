package com.ustadmobile.core.domain.cachelock

import com.ustadmobile.door.DoorDatabaseCallbackSync
import com.ustadmobile.door.DoorSqlDatabase
import com.ustadmobile.door.ext.dbType

class AddRetainAllActiveUriTriggersCallback: DoorDatabaseCallbackSync {

    val useCase = AddRetainAllActiveUriTriggersUseCase()
    override fun onCreate(db: DoorSqlDatabase) {
        useCase(db.dbType()).forEach { sql ->
            db.execSQL(sql)
        }
    }

    override fun onOpen(db: DoorSqlDatabase) {

    }
}