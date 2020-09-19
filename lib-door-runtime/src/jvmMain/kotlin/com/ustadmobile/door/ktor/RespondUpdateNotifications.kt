package com.ustadmobile.door.ktor

import com.github.aakira.napier.Napier
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.door.UpdateNotificationListener
import com.ustadmobile.door.ServerUpdateNotificationManager
import com.ustadmobile.door.entities.UpdateNotification
import com.ustadmobile.door.ext.DoorTag
import io.ktor.application.ApplicationCall
import io.ktor.response.cacheControl
import io.ktor.response.respondTextWriter
import kotlinx.coroutines.channels.Channel
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.kodein.di.on

/**
 * This is used by the generated server application to dispatch update notifications as a
 * server sent events stream. It will
 * 1) Get the UpdateNotificationManager using the DI
 */
suspend fun ApplicationCall.respondUpdateNotifications(repo: DoorDatabaseSyncRepository) {
    response.cacheControl(io.ktor.http.CacheControl.NoCache(null))
    val deviceId = request.queryParameters["deviceId"]?.toInt() ?: 0
    val channel = Channel<UpdateNotification>(capacity = Channel.UNLIMITED)

    val logPrefix = "[respondUpdateNotification device $deviceId]"
    Napier.d("$logPrefix connected",  tag = DoorTag.LOG_TAG)

    val updateManager: ServerUpdateNotificationManager by di().on(this).instance()

    val listener = object: UpdateNotificationListener {
        override fun onNewUpdate(notification: UpdateNotification) {
            channel.offer(notification)
        }
    }

    updateManager.addUpdateNotificationListener(deviceId, listener)

    repo.findPendingUpdateNotifications(deviceId).forEach {
        channel.offer(it)
    }

    try {
        respondTextWriter(contentType = io.ktor.http.ContentType.Text.EventStream) {
            Napier.d("$logPrefix say HELO", tag = DoorTag.LOG_TAG)
            write("id: 0\nevent: HELO\n\n")
            flush()
            for(notification in channel) {
                write("id: ${notification.pnUid}\n")
                write("event: UPDATE\n")
                write("data: ${notification.pnTableId} ${notification.pnTimestamp}\n\n")
                flush()
                Napier.d("$logPrefix:Sent event for table ${notification.pnTableId}",
                        tag = DoorTag.LOG_TAG)
            }
        }
    }finally {
        Napier.d("respondUpdateNotifications done: close",  tag = DoorTag.LOG_TAG)
        updateManager.removeUpdateNotificationListener(deviceId, listener)
        channel.close()
    }
}
