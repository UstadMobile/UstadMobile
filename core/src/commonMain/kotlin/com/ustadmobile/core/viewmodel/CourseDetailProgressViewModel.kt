package com.ustadmobile.core.viewmodel

import com.ustadmobile.door.paging.PagingSource
import com.ustadmobile.lib.db.entities.Person

data class CourseDetailProgressUiState(

    val students: () -> PagingSource<Int, Person> = { EmptyPagingSource() },

    val results: List<String> = emptyList(),

    val fieldsEnabled: Boolean = true,

    )