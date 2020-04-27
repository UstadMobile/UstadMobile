package com.ustadmobile.core.contentformats.har

import kotlinx.serialization.Serializable

@Serializable
class HarNameValuePair(val name: String, val value: String){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as HarNameValuePair

        if (name != other.name) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}