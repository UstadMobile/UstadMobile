package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*

interface ClazzWorkSubmissionMarkingView: UstadEditView<ClazzMemberAndClazzWorkWithSubmission> {

    var privateComments: DataSource.Factory<Int, CommentsWithPerson>?

    //TODO: can we call these editableQuizQuestions (the submission is another entity)
    var quizSubmissionEditData
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>?

    //TODO: can we call these viewOnlyQuizQuestions (the submission is another entity)
    var quizSubmissionViewData
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>?

    var isMarkingFinished: Boolean

    var clazzWorkMetrics : ClazzWorkWithMetrics?

    //TODO: why do we have updated submission when we also have the submission as part of the main entity here?
    var updatedSubmission : ClazzWorkWithSubmission?

    //TODO: as the thinking here should be done by the presenter, let's have a variable for
    // visibility of the recordForStudent button etc.

    //eg var recordForStudentVisible

    companion object {

        const val VIEW_NAME = "ClazzWorkSubmissionMarkingView"

    }

}