package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.PersonGroupEditView
import com.ustadmobile.core.view.PersonGroupListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.lib.db.entities.PersonGroupWithMemberCount
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class PersonGroupListPresenter(context: Any, arguments: Map<String, String>, view: PersonGroupListView,
        di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<PersonGroupListView, PersonGroup>(context, arguments, view, di,
        lifecycleOwner), PersonGroupListItemListener {


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
      view.list = repo.personGroupDao.getAllGroupsLive()
    }

    override fun handleClickEntry(entry: PersonGroup) {
        /* TODO: Add code to go to the appropriate detail view or make a selection
        */
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(PersonGroupEditView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.groupUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(PersonGroupEditView.VIEW_NAME, mapOf(), context)
    }

    override fun handleClickSortOrder(sortOption: IdOption) {
        val sortOrder = (sortOption as? PersonGroupListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }

    override fun onClickGroup(group: PersonGroupWithMemberCount) {
        //TODO :This
    }
}