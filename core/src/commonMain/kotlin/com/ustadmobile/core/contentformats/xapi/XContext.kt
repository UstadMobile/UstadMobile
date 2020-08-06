package com.ustadmobile.core.contentformats.xapi

class XContext {

    var instructor: Actor? = null

    var registration: String? = null

    var language: String? = null

    var platform: String? = null

    var revision: String? = null

    var team: Actor? = null

    var statement: XObject? = null

    var contextActivities: ContextActivity? = null

    var extensions: Map<String, String>? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || this::class != o::class) return false

        val xContext = o as XContext?

        if (if (instructor != null) instructor != xContext!!.instructor else xContext!!.instructor != null)
            return false
        if (if (registration != null) registration != xContext.registration else xContext.registration != null)
            return false
        if (if (language != null) language != xContext.language else xContext.language != null)
            return false
        if (if (platform != null) platform != xContext.platform else xContext.platform != null)
            return false
        if (if (revision != null) revision != xContext.revision else xContext.revision != null)
            return false
        if (if (team != null) team != xContext.team else xContext.team != null) return false
        if (if (statement != null) statement != xContext.statement else xContext.statement != null)
            return false
        if (if (contextActivities != null) contextActivities != xContext.contextActivities else xContext.contextActivities != null)
            return false
        return if (extensions != null) extensions == xContext.extensions else xContext.extensions == null
    }

    override fun hashCode(): Int {
        var result = if (instructor != null) instructor!!.hashCode() else 0
        result = 31 * result + if (registration != null) registration!!.hashCode() else 0
        result = 31 * result + if (language != null) language!!.hashCode() else 0
        result = 31 * result + if (platform != null) platform!!.hashCode() else 0
        result = 31 * result + if (revision != null) revision!!.hashCode() else 0
        result = 31 * result + if (team != null) team!!.hashCode() else 0
        result = 31 * result + if (statement != null) statement!!.hashCode() else 0
        result = 31 * result + if (contextActivities != null) contextActivities!!.hashCode() else 0
        result = 31 * result + if (extensions != null) extensions!!.hashCode() else 0
        return result
    }
}
