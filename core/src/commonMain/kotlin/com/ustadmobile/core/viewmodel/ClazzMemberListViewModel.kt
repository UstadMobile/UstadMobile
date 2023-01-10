package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.PersonWithClazzEnrolmentDetails

data class ClazzMemberListUiState(

    val studentList: List<PersonWithClazzEnrolmentDetails> = emptyList(),

    val pendingStudentList: List<PersonWithClazzEnrolmentDetails> = emptyList(),

    val addTeacherVisible: Boolean = false,

    val addStudentVisible: Boolean = false,

    val termMap: Map<String, String> = emptyMap()

) {
    fun courseBlockStateFor(couresBlockWithEntity: CourseBlockWithEntity): ClazzEditUiState.CourseBlockUiState {
        return ClazzEditUiState.CourseBlockUiState(couresBlockWithEntity)
    }
}