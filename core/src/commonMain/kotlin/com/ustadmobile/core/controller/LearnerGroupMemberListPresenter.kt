package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.view.LearnerGroupMemberListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEARNER_GROUP_UID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.LearnerGroupMember
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class LearnerGroupMemberListPresenter(context: Any, arguments: Map<String, String>, view: LearnerGroupMemberListView,
                                      di: DI, lifecycleOwner: LifecycleOwner) :
        UstadListPresenter<LearnerGroupMemberListView, LearnerGroupMember>(context, arguments, view, di, lifecycleOwner) {

    var learnerGroupUid: Long = 0

    var contentEntryUid: Long = 0

    var clazzUid: Long = 0


    private val contentEntryOpener: ContentEntryOpener by di.on(accountManager.activeAccount).instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        learnerGroupUid = arguments[ARG_LEARNER_GROUP_UID]?.toLong() ?: 0L
        contentEntryUid = arguments[ARG_CONTENT_ENTRY_UID]?.toLong() ?: 0L
        clazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: 0L
        updateList()

    }

    private fun updateList() {
        view.list = repo.learnerGroupMemberDao.findLearnerGroupMembersByGroupIdAndEntry(
            learnerGroupUid, contentEntryUid)
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    override fun handleClickCreateNewFab() {

    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {}

    fun handleNewMemberToGroup(student: Person) {
        GlobalScope.launch(doorMainDispatcher()) {
            LearnerGroupMember().apply {
                learnerGroupMemberPersonUid = student.personUid
                learnerGroupMemberRole = LearnerGroupMember.PARTICIPANT_ROLE
                learnerGroupMemberLgUid = learnerGroupUid
                learnerGroupMemberUid = repo.learnerGroupMemberDao.insertAsync(this)
            }
            updateList()
        }
    }

    fun handleClickGroupSelectionDone() {
        GlobalScope.launch(doorMainDispatcher()) {
            try {
                contentEntryOpener.openEntry(context, contentEntryUid, true, false,
                        arguments[UstadView.ARG_NO_IFRAMES]?.toBoolean() ?: false, learnerGroupUid, clazzUid)
            } catch (e: Exception) {
                if (e is NoAppFoundException) {
                    view.showSnackBar(systemImpl.getString(MessageID.no_app_found, context))
                } else {
                    val message = e.message
                    if (message != null) {
                        view.showSnackBar(message)
                    }
                }
            }
        }
    }


}