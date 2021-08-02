package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ClazzEnrolmentWithLeavingReason : ClazzEnrolment() {

    @Embedded
    var leavingReason: LeavingReason? = null

    var timeZone : String? = null

}