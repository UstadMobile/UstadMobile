package com.ustadmobile.core.controller

import com.ustadmobile.core.MR
import kotlin.jvm.JvmStatic

object TerminologyKeys {

    const val TEACHER_KEY = "Teacher"
    const val STUDENT_KEY = "Student"
    const val TEACHERS_KEY = "Teachers"
    const val STUDENTS_KEY = "Students"
    const val ADD_TEACHER_KEY = "AddTeacher"
    const val ADD_STUDENT_KEY = "AddStudent"


    @JvmStatic
    val TERMINOLOGY_ENTRY_MESSAGE_ID = mapOf(
        TEACHER_KEY to MR.strings.teacher,
        STUDENT_KEY to MR.strings.student,
        TEACHERS_KEY to MR.strings.teachers_literal,
        STUDENTS_KEY to MR.strings.students,
        ADD_TEACHER_KEY to MR.strings.add_a_teacher,
        ADD_STUDENT_KEY to MR.strings.add_a_student
    )


}