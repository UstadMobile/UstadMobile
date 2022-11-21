package com.ustadmobile.core.view

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionTopicListDetail
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails


interface CourseDiscussionDetailView: UstadDetailView<CourseDiscussion> {

    var posts: DataSourceFactory<Int, DiscussionPostWithDetails>?

    companion object {

        const val VIEW_NAME = "CourseDiscussionDetailView"


    }

}