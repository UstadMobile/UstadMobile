package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint

/**
 * Enqueue a content job. This uses WorkManager on Android, or Quartz on JVM
 */
interface ContentJobManager  {

    fun enqueueContentJob(endpoint: Endpoint, contentJobUid: Long)

    companion object {

        const val KEY_ENDPOINT = "endpoint"

        const val KEY_CONTENTJOB_UID = "cjUid"

    }

}