package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class PersonWithSessionsDisplay : Person() {

    var startDate: Long = 0L

    var contextRegistration: String? = null

    var duration: Long = 0

    var resultSuccess: Int = 0

    var resultComplete: Boolean = false

}