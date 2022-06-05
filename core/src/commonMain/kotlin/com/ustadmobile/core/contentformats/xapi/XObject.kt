package com.ustadmobile.core.contentformats.xapi

class XObject {

    var id: String? = null

    var definition: Definition? = null

    var objectType: String? = null

    var statementRefUid: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as XObject

        if (id != other.id) return false
        if (objectType != other.objectType) return false
        if (statementRefUid != other.statementRefUid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (objectType?.hashCode() ?: 0)
        result = 31 * result + statementRefUid.hashCode()
        return result
    }


}
