package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzWorkSubmissionWithPerson


interface ClazzWorkSubmissionWithPersonListView: UstadListView<ClazzWorkSubmissionWithPerson, ClazzWorkSubmissionWithPerson> {

    companion object {
        const val VIEW_NAME = "ClazzWorkSubmissionWithPersonListView"
    }

}