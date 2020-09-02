package com.ustadmobile.door

import com.ustadmobile.door.entities.UpdateNotification

interface UpdateNotificationManager {

    fun onNewUpdateNotifications(notifications: List<UpdateNotification>)

    fun addUpdateNotificationListener(deviceId: Int, listener: UpdateNotificationListener)

    fun removeUpdateNotificationListener(deviceId: Int)

}