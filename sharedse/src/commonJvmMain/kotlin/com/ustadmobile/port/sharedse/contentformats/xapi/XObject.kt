package com.ustadmobile.port.sharedse.contentformats.xapi

class XObject {

    var id: String? = null

    var definition: Definition? = null

    var objectType: String? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val `object` = o as XObject?

        if (if (id != null) id != `object`!!.id else `object`!!.id != null) return false
        return if (objectType != null) objectType == `object`.objectType else `object`.objectType == null
    }

    override fun hashCode(): Int {
        var result = if (id != null) id!!.hashCode() else 0
        result = 31 * result + if (objectType != null) objectType!!.hashCode() else 0
        return result
    }
}
