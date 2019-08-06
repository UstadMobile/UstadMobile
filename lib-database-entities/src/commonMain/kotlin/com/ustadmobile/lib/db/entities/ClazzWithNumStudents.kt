package com.ustadmobile.lib.db.entities

class ClazzWithNumStudents() : Clazz() {

    var numStudents: Int = 0

    var numTeachers: Int = 0

    var teacherNames: String? = null

    var lastRecorded: Long = 0
}
