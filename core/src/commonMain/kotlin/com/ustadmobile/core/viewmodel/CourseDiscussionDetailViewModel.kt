package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails

data class CourseDiscussionDetailUiState(
    val courseDiscussion: CourseDiscussion? = null,
    val posts: List<DiscussionPostWithDetails> = emptyList(),

)