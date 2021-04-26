package com.ustadmobile.core.controller

import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_PERSON_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.ContentEntryWithAttemptsSummary
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class ClazzAssignmentDetailStudentProgressPresenter(context: Any, arguments: Map<String, String>, view: ClazzAssignmentDetailStudentProgressView,
                                                    di: DI, lifecycleOwner: DoorLifecycleOwner,
                                                    private val clazzAssignmentItemListener: DefaultClazzAssignmentDetailStudentProgressItemListener = DefaultClazzAssignmentDetailStudentProgressItemListener(view, ListViewMode.BROWSER, di.direct.instance(), context))
    : UstadListPresenter<ClazzAssignmentDetailStudentProgressView, ContentEntryWithAttemptsSummary>(context, arguments, view, di, lifecycleOwner), ClazzAssignmentDetailStudentProgressItemListener by clazzAssignmentItemListener {

    var selectedPersonUid: Long = 0

    var selectedClazzAssignmentUid: Long= 0

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        clazzAssignmentItemListener.listViewMode = mListMode
        selectedPersonUid = arguments[ARG_PERSON_UID]?.toLong() ?: 0
        selectedClazzAssignmentUid = arguments[ARG_CLAZZ_ASSIGNMENT_UID]?.toLong() ?: 0
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    private fun updateListOnView() {
        /** TODO
         * student
         * get all entries with attempts
         * score
         * private comments
        */
    }

    override fun handleClickCreateNewFab() {

    }

    fun onClickEntry(entry: ContentEntryWithAttemptsSummary){

    }

    fun handleAddComment(commentsWithPerson: CommentsWithPerson){

    }

}