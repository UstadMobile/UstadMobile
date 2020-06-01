package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptions
import com.ustadmobile.lib.db.entities.ClazzWorkWithSubmission
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.ContentEntryWithMetrics


interface ClazzWorkWithSubmissionDetailView: UstadDetailView<ClazzWorkWithSubmission> {

    var clazzWorkContent: DataSource.Factory<Int,ContentEntryWithMetrics>?
    var clazzWorkQuizQuestionsAndOptions: DataSource.Factory<Int,ClazzWorkQuestionAndOptions>?
    var timeZone: String
    var clazzWorkPublicComments: DataSource.Factory<Int, CommentsWithPerson>?
    var clazzWorkPrivateComments: DataSource.Factory<Int, CommentsWithPerson>?
    var studentMode : Boolean

    companion object {

        const val VIEW_NAME = "ClazzWorkWithSubmissionDetailView"

    }

}