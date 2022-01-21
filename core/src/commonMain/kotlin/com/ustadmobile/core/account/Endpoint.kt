package com.ustadmobile.core.account

import kotlinx.serialization.Serializable

@Serializable
data class Endpoint(
    /**
     * Must end with a "/". Cannot automatically adjust this due to serialization requirements
     */
    val url: String
) {
    init {
//        if(!url.endsWith("/"))
//            throw IllegalArgumentException("Endpoint url must end with / !")
    }
}
