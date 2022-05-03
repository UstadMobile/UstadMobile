package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionTopicListDetail


interface CourseDiscussionDetailView: UstadDetailView<CourseDiscussion> {


    var topics: DoorDataSourceFactory<Int, DiscussionTopicListDetail>?

    companion object {

        const val VIEW_NAME = "CourseDiscussionDetailView"


    }

}