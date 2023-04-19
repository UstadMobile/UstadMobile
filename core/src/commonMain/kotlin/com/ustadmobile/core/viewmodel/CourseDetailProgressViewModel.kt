package com.ustadmobile.core.viewmodel

import com.ustadmobile.door.paging.PagingSource
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.Person

data class CourseDetailProgressUiState(

    val students: () -> PagingSource<Int, PersonWithResults> = { EmptyPagingSource() },

    val courseBlocks: List<CourseBlock> = emptyList(),

    val fieldsEnabled: Boolean = true,

)

data class PersonWithResults(

    val person: Person,

    val results: List<StudentResult>

)

data class StudentResult(

    val personUid: Long,

    val courseBlockUid: Long,

    val clazzUid: Long,

    val completed: Boolean

)
