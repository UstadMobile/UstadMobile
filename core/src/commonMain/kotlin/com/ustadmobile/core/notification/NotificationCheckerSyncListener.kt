package com.ustadmobile.core.notification

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.door.SyncEntitiesReceivedEvent
import com.ustadmobile.door.SyncListener
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.NotificationSetting
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

/**
 * Listen for entities that are incoming through the sync system. Pass these onto the
 * NotificationCheckerManager as required so that the appropriate notifications are generated
 * locally on this device.
 */
class NotificationCheckerSyncListener(val site: Endpoint, override val di: DI) : DIAware{

    private val notificationCheckersManager: NotificationCheckersManager by di.on(site).instance()

    val notificationSettingListener = object: SyncListener<NotificationSetting> {
        override fun onEntitiesReceived(evt: SyncEntitiesReceivedEvent<NotificationSetting>) {
            GlobalScope.launch {
                notificationCheckersManager.checkNotifications(evt.entitiesReceived)
            }
        }
    }

    val scheduleListener = object: SyncListener<Schedule> {
        override fun onEntitiesReceived(evt: SyncEntitiesReceivedEvent<Schedule>) {
            val clazzUids = evt.entitiesReceived.map { it.scheduleClazzUid }.distinct()
            GlobalScope.launch {
                clazzUids.forEach {clazzUid ->
                    notificationCheckersManager.invalidateClazzScheduleRelatedCheckers(clazzUid)
                }
            }
        }
    }

    val clazzListener = object: SyncListener<Clazz> {
        override fun onEntitiesReceived(evt: SyncEntitiesReceivedEvent<Clazz>) {
            GlobalScope.launch {
                evt.entitiesReceived.forEach {
                    notificationCheckersManager.invalidateClazzScheduleRelatedCheckers(it.clazzUid)
                }
            }
        }
    }


}