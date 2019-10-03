package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ReportMasterItem {

    var clazzName: String? = null
    var clazzUid: String? = null
    var firstNames: String? = null
    var lastName: String? = null
    var personUid: Long = 0
    var daysPresent: Int = 0
    var daysAbsent: Int = 0
    var daysPartial: Int = 0
    var clazzDays: Int = 0
    var dateLeft: Long = 0
    var isClazzMemberActive: Boolean = false
    var gender: Int = 0
    var dateOfBirth: Long = 0
}
