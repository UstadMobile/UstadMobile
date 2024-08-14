package com.ustadmobile.core.account

import kotlinx.serialization.Serializable

/**
 * Represents an Endpoint. The Ustad app support multiple users on multiple endpoints.
 *
 */
@Serializable
data class Endpoint(
    /**
     * Must end with a "/". Cannot automatically adjust this due to serialization requirements
     */
    val url: String
) {

    /**
     * Local account endpoints are non-reachable internal IP addresses/domains that are generated
     * for local device-only accounts.
     */
    val isLocal
        get() = url.startsWith("http://169")
}
