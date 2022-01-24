package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.ContentPlugin
import org.kodein.di.DI


expect suspend fun ContentPlugin.withWifiLock(context: Any, block: suspend () -> Unit)

expect suspend fun deleteFilesForContentJob(jobId: Long, di: DI, endpoint: Endpoint): Int