package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class AuditLogWithNames : AuditLog() {

    var actorName: String? = null
    var clazzName: String? = null
    var personName: String? = null
}
