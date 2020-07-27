package com.ustadmobile.port.sharedse.contentformats.xapi

class Statement {

    var actor: Actor? = null

    var verb: Verb? = null

    var `object`: XObject? = null

    var subStatement: Statement? = null

    var result: Result? = null

    var context: XContext? = null

    var timestamp: String? = null

    var stored: String? = null

    var authority: Actor? = null

    var version: String? = null

    var id: String? = null

    var attachments: List<Attachment>? = null

    var objectType: String? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val statement = o as Statement?

        if (if (actor != null) actor != statement!!.actor else statement!!.actor != null) return false
        if (if (verb != null) verb != statement.verb else statement.verb != null) return false
        if (if (`object` != null) `object` != statement.`object` else statement.`object` != null)
            return false
        if (if (subStatement != null) subStatement != statement.subStatement else statement.subStatement != null)
            return false
        if (if (result != null) result != statement.result else statement.result != null)
            return false
        if (if (context != null) context != statement.context else statement.context != null)
            return false
        if (if (authority != null) authority != statement.authority else statement.authority != null)
            return false
        return if (objectType != null) objectType == statement.objectType else statement.objectType == null
    }

    override fun hashCode(): Int {
        var result1 = if (actor != null) actor!!.hashCode() else 0
        result1 = 31 * result1 + if (verb != null) verb!!.hashCode() else 0
        result1 = 31 * result1 + if (`object` != null) `object`!!.hashCode() else 0
        result1 = 31 * result1 + if (subStatement != null) subStatement!!.hashCode() else 0
        result1 = 31 * result1 + if (result != null) result!!.hashCode() else 0
        result1 = 31 * result1 + if (context != null) context!!.hashCode() else 0
        result1 = 31 * result1 + if (authority != null) authority!!.hashCode() else 0
        result1 = 31 * result1 + if (objectType != null) objectType!!.hashCode() else 0
        return result1
    }
}
