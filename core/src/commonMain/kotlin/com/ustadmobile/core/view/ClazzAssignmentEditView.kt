package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer


interface ClazzAssignmentEditView: UstadEditView<ClazzAssignment> {

    var timeZone: String?

    var clazzAssignmentContent: DoorMutableLiveData<List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>>?

    companion object {

        const val VIEW_NAME = "ClazzAssignmentEditView"

    }

}