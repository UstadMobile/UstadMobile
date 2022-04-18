package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.*


interface DiscussionTopicDetailView: UstadDetailView<DiscussionTopic> {


    var posts: DoorDataSourceFactory<Int, DiscussionPostWithDetails>?

    companion object {

        const val VIEW_NAME = "DiscussionTopicDetailView"


    }

}