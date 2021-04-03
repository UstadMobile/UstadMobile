package com.ustadmobile.core.notification

import com.ustadmobile.core.account.Endpoint
import org.kodein.di.DI
import org.kodein.di.DIAware

class NotificationManager(val endpoint: Endpoint, override val di: DI): DIAware {

    /**
     * Check the given notification. See what type of notification it is, and use the appropriate
     * checker to generate feedentry(s) etc.
     */
    suspend fun checkNotifications(notificationPrefUids: List<Long>) {

    }

}