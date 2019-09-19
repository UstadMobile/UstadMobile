package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ClazzWithNumStudents() : Clazz() {

    var numStudents: Int = 0
}
