package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*

interface ClazzWorkSubmissionMarkingView: UstadEditView<ClazzMemberAndClazzWorkWithSubmission> {

    var privateComments: DataSource.Factory<Int, CommentsWithPerson>?

    var quizSubmissionEditData
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>?

    var quizSubmissionViewData
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>?

    var isMarkingFinished: Boolean

    var clazzWorkMetrics : ClazzWorkWithMetrics?

    var updatedSubmission : ClazzWorkWithSubmission?

    companion object {

        const val VIEW_NAME = "ClazzWorkSubmissionMarkingView"

    }

}