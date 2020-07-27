package com.ustadmobile.core.contentformats.xapi

class Verb {

    var id: String? = null

    var display: Map<String, String>? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Verb

        if (id != other.id) return false
        if (display != other.display) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (display?.hashCode() ?: 0)
        return result
    }


}
