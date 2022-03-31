package com.ustadmobile.core.util.ext

import com.ustadmobile.core.contentjob.ContentPlugin


expect suspend fun ContentPlugin.withWifiLock(context: Any, block: suspend () -> Unit)
