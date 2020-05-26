package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWorkSubmission


interface ClazzWorkSubmissionEditView: UstadEditView<ClazzWorkSubmission> {

    companion object {

        const val VIEW_NAME = "ClazzWorkSubmissionEditEditView"

    }

}