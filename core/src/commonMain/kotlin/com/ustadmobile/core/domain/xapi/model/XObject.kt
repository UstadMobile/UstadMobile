package com.ustadmobile.core.domain.xapi.model

import kotlinx.serialization.Serializable

/**
 * XApi object as per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#244-object
 */
@Serializable
data class XObject(
    var id: String? = null,

    var definition: Definition? = null,

    var objectType: String? = null,

    var statementRefUid: Long = 0,
)

