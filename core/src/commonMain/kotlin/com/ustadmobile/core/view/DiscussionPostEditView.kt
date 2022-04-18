package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.DiscussionPost

interface DiscussionPostEditView: UstadEditView<DiscussionPost> {

    companion object {

        const val VIEW_NAME = "DiscussionPostEdit"

    }

}