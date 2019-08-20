package com.ustadmobile.port.sharedse.contentformats.xapi

class Verb {

    var id: String? = null

    var display: Map<String, String>? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val verb = o as Verb?

        return if (id != null) id == verb!!.id else verb!!.id == null
    }

    override fun hashCode(): Int {
        return if (id != null) id!!.hashCode() else 0
    }
}
