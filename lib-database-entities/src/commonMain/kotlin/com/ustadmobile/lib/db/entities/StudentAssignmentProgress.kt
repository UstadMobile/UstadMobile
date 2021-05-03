package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class StudentAssignmentProgress {

    var notSubmittedStudents : Int = 0

    var submittedStudents : Int = 0

    var completedStudents : Int = 0

}