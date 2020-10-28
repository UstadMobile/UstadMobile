package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptionWithResponse
import com.ustadmobile.lib.db.entities.ClazzWorkWithSubmission
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer


interface ClazzWorkDetailOverviewView: UstadDetailView<ClazzWorkWithSubmission> {

    var clazzWorkContent
            :DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?

    //TODO: can we call these editableQuizQuestions (the submission is another entity)
    var quizSubmissionEdit
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>?

    //TODO: can we call these viewOnlyQuizQuestions (the submission is another entity)
    var quizSubmissionView
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>?
    var timeZone: String
    var clazzWorkPublicComments: DataSource.Factory<Int, CommentsWithPerson>?
    var clazzWorkPrivateComments: DataSource.Factory<Int, CommentsWithPerson>?
    var isStudent : Boolean

    companion object {

        const val VIEW_NAME = "ClazzWorkWithSubmissionDetailView"

    }

}