package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*


interface ClazzWorkDetailOverviewView: UstadDetailView<ClazzWorkWithSubmission> {

    var clazzWorkContent
            :DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?
    var clazzWorkQuizQuestionsAndOptionsWithResponse
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>?
    var timeZone: String
    var clazzWorkPublicComments: DataSource.Factory<Int, CommentsWithPerson>?
    var clazzWorkPrivateComments: DataSource.Factory<Int, CommentsWithPerson>?
    var studentMode : Boolean


    companion object {

        const val VIEW_NAME = "ClazzWorkWithSubmissionDetailView"

    }

}