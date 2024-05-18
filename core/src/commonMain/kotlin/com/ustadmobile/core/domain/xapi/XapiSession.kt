package com.ustadmobile.core.domain.xapi

import com.ustadmobile.core.account.Endpoint

/**
 * Represents an xAPI session e.g. where a given content entry is opened for a given user
 */
data class XapiSession(
    val endpoint: Endpoint,
    val accountPersonUid: Long,
    val accountUsername: String,
    val clazzUid: Long,
    val contentEntryUid: Long,
)

