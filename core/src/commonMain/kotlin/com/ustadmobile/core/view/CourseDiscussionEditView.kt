package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.DiscussionTopic

interface CourseDiscussionEditView: UstadEditView<CourseBlockWithEntity> {

    var blockTitleError: String?

    var startDate: Long
    var startTime: Long

    var timeZone: String?

    var topicList: DoorMutableLiveData<List<DiscussionTopic>>?

    companion object {

        const val VIEW_NAME = "CourseDiscussionBlockEdit"

    }

}