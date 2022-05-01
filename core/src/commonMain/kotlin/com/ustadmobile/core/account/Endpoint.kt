package com.ustadmobile.core.account

import com.ustadmobile.core.util.ext.requirePostfix
import kotlinx.serialization.Serializable

@Serializable
data class Endpoint(
    /**
     * Must end with a "/". Cannot automatically adjust this due to serialization requirements
     */
    val url: String
) {

    fun url(subUri: String): String {
        return url.requirePostfix("/") + subUri.removePrefix("/")
    }

}
