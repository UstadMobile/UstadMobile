package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ClazzLogWithTimeZone : ClazzLog() {

    var timeZone: String? = null

}