package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.lib.db.entities.UmAccount

class PersonGroupListPresenter(context: Any, arguments: Map<String, String>, view: PersonGroupListView,
                          lifecycleOwner: DoorLifecycleOwner, systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?>)
    : UstadListPresenter<PersonGroupListView, PersonGroup>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {


    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class PersonGroupListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { PersonGroupListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    private fun updateListOnView() {
        /* TODO: Update the list on the view from the appropriate DAO query, e.g.
        view.list = when(sortOrder) {
            SortOrder.ORDER_NAME_ASC -> repo.daoName.findAllActiveClazzesSortByNameAsc(
                    searchQuery, loggedInPersonUid)
            SortOrder.ORDER_NAME_DSC -> repo.daoName.findAllActiveClazzesSortByNameDesc(
                    searchQuery, loggedInPersonUid)q
        }
        */
    }

    override fun handleClickEntry(entry: PersonGroup) {
        /* TODO: Add code to go to the appropriate detail view or make a selection
        */
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(PersonGroupEditView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.groupPersonUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(PersonGroupEditView.VIEW_NAME, mapOf(), context)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? PersonGroupListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }
}