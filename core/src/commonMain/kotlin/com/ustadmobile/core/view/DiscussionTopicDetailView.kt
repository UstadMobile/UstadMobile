package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.DiscussionTopicListDetail
import com.ustadmobile.lib.db.entities.MessageWithPerson


interface DiscussionTopicDetailView: UstadDetailView<CourseDiscussion> {


    var posts: DoorDataSourceFactory<Int, DiscussionPostWithDetails>?

    companion object {

        const val VIEW_NAME = "DiscussionTopicDetailView"


    }

}