package com.ustadmobile.core.domain.xapi

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.xapi.model.XapiAccount
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.model.XapiObjectType
import kotlinx.serialization.Serializable

/**
 * Represents an xAPI session e.g. where a given content entry is opened for a given user. Used where
 * an xAPI session happens within the app itself (e.g. not involving external content).
 *
 * @param endpoint The system endpoint (NOT the local xapi endpoint e.g. a localhost url for the content etc)
 */
@Serializable
data class XapiSession(
    val endpoint: Endpoint,
    val accountPersonUid: Long,
    val accountUsername: String,
    val clazzUid: Long,
    val cbUid: Long = 0,
    val contentEntryUid: Long = 0,
    val rootActivityId: String? = null,
) {

    val agent: XapiAgent
        get() = XapiAgent(
            account = XapiAccount(
                homePage = endpoint.url,
                name = accountUsername
            ),
            objectType = XapiObjectType.Agent
        )

}

