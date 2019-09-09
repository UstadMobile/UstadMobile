package com.ustadmobile.lib.db.entities

class ClazzWithEnrollment : Clazz() {

    var personUid: Long = 0

    var enrolled: Boolean? = null

    var numStudents: Int = 0
}
