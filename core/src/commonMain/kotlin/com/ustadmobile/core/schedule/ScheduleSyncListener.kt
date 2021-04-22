package com.ustadmobile.core.schedule

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.SyncListener
import com.ustadmobile.door.SyncEntitiesReceivedEvent
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

/**
 * SyncListener that will handle when an incoming Schedule change requires a recheck of ClazzLog
 * generation. E.g. When a clazz is created on a client device, the Clazz and Schedule will be
 * sync'd up to the server. However the server needs to set a scheduler task to create future
 * ClazzLogs according to the schedule.
 */
class ScheduleSyncListener(val site: Endpoint, val di: DI) {

    val clazzLogCreatorManager: ClazzLogCreatorManager by di.on(site).instance()

    val db: UmAppDatabase by di.on(site).instance(tag = DoorTag.TAG_DB)

    val scheduleListener = object: SyncListener<Schedule> {
        override fun onEntitiesReceived(evt: SyncEntitiesReceivedEvent<Schedule>) {
            GlobalScope.launch {
                val clazzUids = evt.entitiesReceived.map { it.scheduleClazzUid }.distinct()
                clazzUids.chunked(100).forEach { clazzUidChunk ->
                    db.clazzDao.getClazzesWithSchool(clazzUidChunk).forEach { clazzWithSchool ->
                        clazzLogCreatorManager.requestClazzLogCreationForToday(clazzWithSchool, site.url)
                    }
                }
            }
        }
    }

}
