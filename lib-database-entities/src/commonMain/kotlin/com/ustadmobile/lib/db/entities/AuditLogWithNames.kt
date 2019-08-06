package com.ustadmobile.lib.db.entities

class AuditLogWithNames : AuditLog() {

    var actorName: String? = null
    var clazzName: String? = null
    var personName: String? = null
}
