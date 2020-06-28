package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzMemberAndClazzWorkWithSubmission
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionAndOptionWithResponse
import com.ustadmobile.lib.db.entities.ClazzWorkWithMetrics
import com.ustadmobile.lib.db.entities.CommentsWithPerson


interface ClazzWorkSubmissionMarkingView: UstadEditView<ClazzMemberAndClazzWorkWithSubmission> {

    var privateCommentsToPerson: DataSource.Factory<Int, CommentsWithPerson>?

    var clazzWorkQuizQuestionsAndOptionsWithResponse
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>?

    var markingLeft: Boolean

    var clazzWorkWithMetricsFlat : ClazzWorkWithMetrics?

    companion object {

        const val VIEW_NAME = "ClazzWorkSubmissionMarkingView"

    }

}