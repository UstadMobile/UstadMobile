package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class LocationListPresenter(context: Any, arguments: Map<String, String>, view: LocationListView,
                            di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<LocationListView, Location>(context, arguments, view, di, lifecycleOwner),
        LocationListItemListener {

    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class LocationListSortOption(val sortOrder: SortOrder, context: Any)
        : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { LocationListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        //TODO:
        return true
    }

    private fun updateListOnView() {
        view.list = repo.locationDao.findAllLocations()
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(LocationEditView.VIEW_NAME, mapOf(), context)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? LocationListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }


    override fun handleClickEntry(entry: Location) {
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> {

                systemImpl.go(LocationEditView.VIEW_NAME,
                        mapOf(UstadView.ARG_ENTITY_UID to entry.locationUid.toString()), context)
            }
        }
    }

    override fun onClickLocation(location: Location) {
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(location))
            ListViewMode.BROWSER -> {

                systemImpl.go(LocationEditView.VIEW_NAME,
                        mapOf(UstadView.ARG_ENTITY_UID to location.locationUid.toString()), context)
            }
        }
    }
}