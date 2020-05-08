package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_CLAZZUID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.UmAccount

class ClazzMemberListPresenter(context: Any, arguments: Map<String, String>, view: ClazzMemberListView,
                          lifecycleOwner: DoorLifecycleOwner, systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?>)
    : UstadListPresenter<ClazzMemberListView, ClazzMember>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {


    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    var filterByClazzUid: Long = -1

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class ClazzMemberListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        filterByClazzUid = arguments[ARG_FILTER_BY_CLAZZUID]?.toLong() ?: -1
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { ClazzMemberListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    private fun updateListOnView() {
        view.list = repo.clazzMemberDao.findByClazzUidAndRole(filterByClazzUid,
                ClazzMember.ROLE_TEACHER)
        view.studentList = repo.clazzMemberDao.findByClazzUidAndRole(filterByClazzUid,
            ClazzMember.ROLE_STUDENT)
        view.addStudentVisible = true
        view.addTeacherVisible = true

    }

    override fun handleClickEntry(entry: ClazzMember) {
        /* TODO: Add code to go to the appropriate detail view or make a selection
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(ClazzMemberDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to uid, context)
        }
        */
    }

    override fun handleClickCreateNewFab() {
        /* TODO: Add code to go to the edit view when the user clicks the new item FAB. This is only
         * called when the fab is clicked, not if the first item is create new item (e.g. picker mode).
         * That has to be handled at a platform level to use prepareCall etc.
        systemImpl.go(ClazzMemberEditView.VIEW_NAME, mapOf(), context)
         */
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? ClazzMemberListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }
}