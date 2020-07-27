package com.ustadmobile.port.sharedse.contentformats.xapi

class Actor {

    var name: String? = null

    var mbox: String? = null

    var mbox_sha1sum: String? = null

    var openid: String? = null

    var objectType: String? = null

    var members: List<Actor>? = null

    var account: Account? = null

    inner class Account {

        var name: String? = null

        var homePage: String? = null
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val actor = o as Actor?

        if (if (name != null) name != actor!!.name else actor!!.name != null) return false
        if (if (mbox != null) mbox != actor.mbox else actor.mbox != null) return false
        if (if (mbox_sha1sum != null) mbox_sha1sum != actor.mbox_sha1sum else actor.mbox_sha1sum != null)
            return false
        if (if (openid != null) openid != actor.openid else actor.openid != null) return false
        if (if (objectType != null) objectType != actor.objectType else actor.objectType != null)
            return false
        if (if (members != null) members != actor.members else actor.members != null) return false
        return if (account != null) account == actor.account else actor.account == null
    }

    override fun hashCode(): Int {
        var result = if (name != null) name!!.hashCode() else 0
        result = 31 * result + if (mbox != null) mbox!!.hashCode() else 0
        result = 31 * result + if (mbox_sha1sum != null) mbox_sha1sum!!.hashCode() else 0
        result = 31 * result + if (openid != null) openid!!.hashCode() else 0
        result = 31 * result + if (objectType != null) objectType!!.hashCode() else 0
        result = 31 * result + if (members != null) members!!.hashCode() else 0
        result = 31 * result + if (account != null) account!!.hashCode() else 0
        return result
    }
}
