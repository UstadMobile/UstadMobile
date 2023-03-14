package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.Message

data class DiscussionPostEditUiState(
    val discussionPost: DiscussionPost? = null,
    val fieldsEnabled: Boolean = true,
    val discussionPostTitleError: String? = null,
    val discussionPostDescError: String? = null
)