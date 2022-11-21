package com.ustadmobile.core.view

import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.DiscussionTopic

interface CourseDiscussionEditView: UstadEditView<CourseBlockWithEntity> {

    var blockTitleError: String?

    var startDate: Long
    var startTime: Long

    var timeZone: String?

    var postList: MutableLiveData<List<DiscussionPost>>?

    companion object {

        const val VIEW_NAME = "CourseDiscussionBlockEdit"

    }

}