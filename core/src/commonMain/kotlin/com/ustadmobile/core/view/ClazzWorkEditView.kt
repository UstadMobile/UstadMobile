package com.ustadmobile.core.view

import com.ustadmobile.core.controller.ClazzWorkEditPresenter
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptions
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer


interface ClazzWorkEditView: UstadEditView<ClazzWork> {

    var clazzWorkContent: DoorMutableLiveData<List<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>>?
    var clazzWorkQuizQuestionsAndOptions: DoorMutableLiveData<List<ClazzWorkQuestionAndOptions>>?
    var submissionTypeOptions: List<ClazzWorkEditPresenter.SubmissionOptionsMessageIdOption>?
    var timeZone: String

    companion object {

        const val VIEW_NAME = "ClazzWorkEditEditView"

    }

}