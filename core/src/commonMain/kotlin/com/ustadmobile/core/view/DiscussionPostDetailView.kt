package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.MessageWithPerson


interface DiscussionPostDetailView: UstadDetailView<DiscussionPostWithDetails> {

    var title: String?

    var replies: DoorDataSourceFactory<Int, MessageWithPerson>?

    companion object {

        const val VIEW_NAME = "DiscussionPostDetailView"


    }

}