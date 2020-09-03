package com.ustadmobile.door

import com.ustadmobile.door.entities.UpdateNotification

class UpdateNotificationManagerImpl: UpdateNotificationManager {

    //TODO: Use thread safe versions on JVM via expect / actual
    val notificationListeners: MutableMap<Int, MutableList<UpdateNotificationListener>> = mutableMapOf()

    override fun onNewUpdateNotifications(notifications: List<UpdateNotification>) {
        notifications.forEach {notification ->
            notificationListeners[notification.pnDeviceId]?.forEach {listener ->
                listener.onNewUpdate(notification)
            }
        }
    }

    override fun addUpdateNotificationListener(deviceId: Int, listener: UpdateNotificationListener) {
        notificationListeners.getOrPut(deviceId) { mutableListOf() }.add(listener)
    }

    override fun removeUpdateNotificationListener(deviceId: Int, listener: UpdateNotificationListener) {
        notificationListeners[deviceId]?.remove(listener)
    }


}