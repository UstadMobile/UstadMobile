package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*


interface ClazzWorkDetailOverviewView: UstadDetailView<ClazzWorkWithSubmission> {

    var clazzWorkContent: DataSource.Factory<Int,ContentEntryWithMetrics>?
    var clazzWorkQuizQuestionsAndOptions: DataSource.Factory<Int,ClazzWorkQuestionAndOptions>?
    var timeZone: String
    var clazzWorkPublicComments: DataSource.Factory<Int, CommentsWithPerson>?
    var clazzWorkPrivateComments: DataSource.Factory<Int, CommentsWithPerson>?
    var studentMode : Boolean


    var clazzWorkWithSubmission: DoorLiveData<List<ClazzWorkWithSubmission>>?
    var contentEntries: DataSource.Factory<Int, ContentEntry>?


    companion object {

        const val VIEW_NAME = "ClazzWorkWithSubmissionDetailView"

    }

}