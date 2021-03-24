package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class PersonWithStatementDisplay : Person() {

    var attempts: Int = 0

    var progress: Int = 0

    var score: Int = 0

    var startDate: Long = 0L

    var endDate: Long = Long.MAX_VALUE

    var duration: Long = 0L

}