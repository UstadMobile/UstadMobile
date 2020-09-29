package com.ustadmobile.core.controller

import com.ustadmobile.core.view.LearnerGroupMemberListView
import com.ustadmobile.core.view.LearnerGroupMemberListView.Companion.ARG_LEARNER_GROUP_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.LearnerGroupMember
import com.ustadmobile.lib.db.entities.LearnerGroupMember.Companion.STUDENT_ROLE
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class LearnerGroupMemberListPresenter(context: Any, arguments: Map<String, String>, view: LearnerGroupMemberListView,
                                      di: DI, lifecycleOwner: DoorLifecycleOwner) :
        UstadListPresenter<LearnerGroupMemberListView, LearnerGroupMember>(context, arguments, view, di, lifecycleOwner) {

    var learnerGroupUid: Long = 0

    var contentEntryUid: Long = 0

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        learnerGroupUid = arguments[ARG_LEARNER_GROUP_UID]?.toLong() ?: 0L
        contentEntryUid = arguments[ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0L
        updateList()

    }

    private fun updateList() {
        GlobalScope.launch(doorMainDispatcher()) {
            view.list = repo.learnerGroupMemberDao.findLearnerGroupMembersByGroupIdAndEntry(learnerGroupUid, contentEntryUid)
        }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    override fun handleClickCreateNewFab() {

    }

    fun handleNewMemberToGroup(student: Person) {
        GlobalScope.launch(doorMainDispatcher()) {
            LearnerGroupMember().apply {
                learnerGroupMemberPersonUid = student.personUid
                learnerGroupMemberRole = STUDENT_ROLE
                learnerGroupMemberLgUid = learnerGroupUid
                learnerGroupMemberUid = repo.learnerGroupMemberDao.insertAsync(this)
            }
            updateList()
        }
    }


}