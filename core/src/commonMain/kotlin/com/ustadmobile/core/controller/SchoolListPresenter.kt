package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.UmAccount
import io.ktor.http.parseAndSortContentTypeHeader

class SchoolListPresenter(context: Any, arguments: Map<String, String>, view: SchoolListView,
                          lifecycleOwner: DoorLifecycleOwner, systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?>)
    : UstadListPresenter<SchoolListView, School>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {


    var searchQuery = "%%"
    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class SchoolListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { SchoolListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        //TODO: This
        return true
    }

    private fun updateListOnView() {
        /* TODO: Update the list on the view from the appropriate DAO query, e.g.*/
        view.list = when(currentSortOrder) {
            SortOrder.ORDER_NAME_ASC -> repo.schoolDao.findAllActiveSchoolWithMemberCountAndLocationNameAsc(
                    searchQuery)
            SortOrder.ORDER_NAME_DSC -> repo.schoolDao.findAllActiveSchoolWithMemberCountAndLocationNameDesc(
                    searchQuery)
        }
    }

    override fun handleClickEntry(entry: School) {
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            //TODO: This
//            ListViewMode.BROWSER -> systemImpl.go(SchoolDetailView.VIEW_NAME,
//                    mapOf(UstadView.ARG_ENTITY_UID to entry.schoolUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(SchoolEditView.VIEW_NAME, mapOf(), context)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? SchoolListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }
}