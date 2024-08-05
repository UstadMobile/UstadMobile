package com.ustadmobile.core.domain.xapi.state

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.ustadmobile.core.domain.interop.HttpApiException

/**
 * The parameters that need to be provided by for the xAPI state resource for all requests EXCEPT
 * the get request to list available state ids
 */
data class XapiStateParams(
    val activityId: String,
    val agent: String,
    val registration: String?,
    val stateId: String,
) {
    val registrationUuid: Uuid? by lazy {
        registration?.let {
            try {
                uuidFrom(it)
            }catch(e: Throwable) {
                throw HttpApiException(400, "Registration uuid provided but not valid UUID")
            }
        }
    }
}