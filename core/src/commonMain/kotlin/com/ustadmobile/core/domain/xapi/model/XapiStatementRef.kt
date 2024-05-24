package com.ustadmobile.core.domain.xapi.model

import kotlinx.serialization.Serializable

/**
 * As per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#object-is-statement
 */
@Serializable
data class XapiStatementRef(
    override val objectType: XapiObjectType,
    val id: String,
): XapiStatementObject

