package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.PersonGroupEditView
import com.ustadmobile.core.view.PersonGroupListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI

class PersonGroupListPresenter(context: Any, arguments: Map<String, String>, view: PersonGroupListView,
        di: DI, lifecycleOwner: LifecycleOwner)
    : UstadListPresenter<PersonGroupListView, PersonGroup>(context, arguments, view, di, lifecycleOwner) {


    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class PersonGroupListSortOption(
        val sortOrder: SortOrder,
        context: Any,
        di: DI
    ) : MessageIdOption(sortOrder.messageId, context, di = di)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { PersonGroupListSortOption(it, context, di) }
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
            ListViewMode.PICKER -> finishWithResult(
                safeStringify(di, ListSerializer(PersonGroup.serializer()), listOf(entry)))
            ListViewMode.BROWSER -> systemImpl.go(PersonGroupEditView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.groupUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(PersonGroupEditView.VIEW_NAME, mapOf(), context)
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {}

    override fun handleClickSortOrder(sortOption: IdOption) {
        val sortOrder = (sortOption as? PersonGroupListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }
}