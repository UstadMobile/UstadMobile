package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*

interface ClazzWorkSubmissionMarkingView: UstadEditView<PersonWithClazzWorkAndSubmission> {

    var privateComments: DataSource.Factory<Int, CommentsWithPerson>?

    var editableQuizQuestions
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>?

    var viewOnlyQuizQuestions
            : DoorMutableLiveData<List<ClazzWorkQuestionAndOptionWithResponse>>?

    var isMarkingFinished: Boolean

    var clazzWorkMetrics : ClazzWorkWithMetrics?

    var showRecordForStudent: Boolean

    var showSubmissionHeading: Boolean

    var showSimpleTwoButton: Boolean

    var setQuizEditList : Boolean

    var showShortTextSubmission: Boolean

    var showShortTextResult: Boolean

    companion object {

        const val VIEW_NAME = "ClazzWorkSubmissionMarkingView"

    }

}