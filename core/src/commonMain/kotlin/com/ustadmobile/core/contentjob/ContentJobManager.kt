package com.ustadmobile.core.contentjob

import com.ustadmobile.core.account.Endpoint

/**
 * ContentJobManager will enqueue a ContentJob using WorkManager on Android or Quartz on JVM. The
 * implementation can then be retrieved via DI (or mocked in tests as required).
 */
interface ContentJobManager  {

    fun enqueueContentJob(endpoint: Endpoint, contentJobUid: Long)

    fun cancelContentJob(endpoint: Endpoint, contentJobUid: Long)

    companion object {

        const val KEY_ENDPOINT = "endpoint"

        const val KEY_CONTENTJOB_UID = "cjUid"

    }

}