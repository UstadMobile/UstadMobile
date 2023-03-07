package com.ustadmobile.core.viewmodel

import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.MessageWithPerson

data class DiscussionPostDetailUiState(
    val discussionPost: DiscussionPostWithDetails? = null,
    val replies: List<MessageWithPerson> = emptyList(),
    val messageReplyTitle: String? = null

)