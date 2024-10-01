package com.ustadmobile.core.account

import io.ktor.http.Url
import kotlinx.serialization.Serializable

/**
 * Represents an Endpoint. The Ustad app support multiple users on multiple endpoints.
 *
 */
@Serializable
data class LearningSpace(
    /**
     * Must end with a "/". Cannot automatically adjust this due to serialization requirements
     */
    val url: String
) {

    /**
     * Local account endpoints are non-reachable internal IP addresses/domains that are generated
     * for local device-only accounts.
     */
    val isLocal: Boolean by lazy(LazyThreadSafetyMode.NONE) {
        Url(url).host.endsWith(".local")
    }

}
