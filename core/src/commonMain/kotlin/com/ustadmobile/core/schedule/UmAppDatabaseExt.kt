package com.ustadmobile.core.schedule

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.lib.db.entities.Schedule
import org.kodein.di.DI


fun UmAppDatabase.setupScheduleSyncListener(site: Endpoint, di: DI) : UmAppDatabase{
    val listener = ScheduleSyncListener(site, di)
    (this as DoorDatabaseRepository).addSyncListener(Schedule::class, listener.scheduleListener)
    return this
}
