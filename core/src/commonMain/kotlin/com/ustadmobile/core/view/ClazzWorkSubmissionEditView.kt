package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWorkSubmission
import com.ustadmobile.lib.db.entities.ClazzWorkSubmissionWithClazzWork


interface ClazzWorkSubmissionEditView: UstadEditView<ClazzWorkSubmissionWithClazzWork> {

    companion object {

        const val VIEW_NAME = "ClazzWorkSubmissionEditEditView"

    }

}