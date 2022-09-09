package com.ustadmobile.core.view

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.*


interface DiscussionTopicDetailView: UstadDetailView<DiscussionTopic> {


    var posts: DataSourceFactory<Int, DiscussionPostWithDetails>?

    companion object {

        const val VIEW_NAME = "DiscussionTopicDetailView"


    }

}