package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails

data class CourseDiscussionBlockEditUiState(
    val courseDiscussion: CourseDiscussion? = null,
    val posts: List<DiscussionPostWithDetails> = emptyList(),
    val courseBlockEditUiState: CourseBlockEditUiState = CourseBlockEditUiState(),
    val fieldsEnabled: Boolean = true,
    val courseDiscussionTitleError: String? = null,
    val courseDiscussionDescError: String? = null
)