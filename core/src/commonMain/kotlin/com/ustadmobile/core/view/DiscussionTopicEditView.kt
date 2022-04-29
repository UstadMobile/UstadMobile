package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.DiscussionTopic

interface DiscussionTopicEditView: UstadEditView<DiscussionTopic> {

    var blockTitleError: String?

    companion object {

        const val VIEW_NAME = "DiscussionTopicEdit"

    }

}