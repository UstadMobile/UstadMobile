package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.*


interface DiscussionPostDetailView: UstadDetailView<DiscussionPost> {


    var replies: DoorDataSourceFactory<Int, MessageWithPerson>?

    companion object {

        const val VIEW_NAME = "DiscussionPostDetailView"


    }

}