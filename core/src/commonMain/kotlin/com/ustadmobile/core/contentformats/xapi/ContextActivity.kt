package com.ustadmobile.port.sharedse.contentformats.xapi

import com.google.gson.annotations.JsonAdapter

@JsonAdapter(ContextDeserializer::class)
class ContextActivity {

    var parent: List<XObject>? = null

    var grouping: List<XObject>? = null

    var category: List<XObject>? = null

    var other: List<XObject>? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as ContextActivity?

        if (if (parent != null) parent != that!!.parent else that!!.parent != null) return false
        if (if (grouping != null) grouping != that.grouping else that.grouping != null)
            return false
        if (if (category != null) category != that.category else that.category != null)
            return false
        return if (other != null) other == that.other else that.other == null
    }

    override fun hashCode(): Int {
        var result = if (parent != null) parent!!.hashCode() else 0
        result = 31 * result + if (grouping != null) grouping!!.hashCode() else 0
        result = 31 * result + if (category != null) category!!.hashCode() else 0
        result = 31 * result + if (other != null) other!!.hashCode() else 0
        return result
    }
}
