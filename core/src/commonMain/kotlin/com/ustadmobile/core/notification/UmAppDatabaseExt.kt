package com.ustadmobile.core.notification

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.NotificationSetting
import com.ustadmobile.lib.db.entities.Schedule
import org.kodein.di.DI

/**
 * Add a NotificationCheckerSyncListener
 */
fun UmAppDatabase.setupNotificationCheckerSyncListener(site: Endpoint, di: DI) {
    val listener = NotificationCheckerSyncListener(site, di)
    val thisRepo = this as DoorDatabaseRepository
    thisRepo.addSyncListener(Schedule::class, listener.scheduleListener)
    thisRepo.addSyncListener(Clazz::class, listener.clazzListener)
    thisRepo.addSyncListener(NotificationSetting::class, listener.notificationSettingListener)
}