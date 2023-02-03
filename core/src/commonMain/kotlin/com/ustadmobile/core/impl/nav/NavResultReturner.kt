package com.ustadmobile.core.impl.nav

import kotlinx.coroutines.flow.Flow

interface NavResultReturner {

    fun resultFlowForKey(key: String): Flow<NavResult>

    fun sendResult(result: NavResult)

}