package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class PersonWithStatementDisplay {

    var personUid: Long = 0

    var firstNames: String? = null

    var lastName: String? = null

    var attempts: Int = 0

    var progress: Int = 0

    var score: Int = 0

    var startDate: Long = 0L

    var endDate: Long = Long.MAX_VALUE

    var duration: Long = 0L

}