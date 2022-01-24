package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ClazzAssignmentWithTimezone: ClazzAssignment() {

    var effectiveTimeZone: String? = null

}