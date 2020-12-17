package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.core.util.ext.toListFilterOptions
import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleListDetail
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class SaleListPresenter(context: Any, arguments: Map<String, String>, view: SaleListView,
                        di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<SaleListView, Sale>(context, arguments, view, di, lifecycleOwner)
        , SaleListItemListener, OnSearchSubmitted  {

    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    var searchText: String? = null

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    override fun onSearchSubmitted(text: String?) {
        searchText = text
        updateListOnView()
    }

    class SaleListSortOption(val sortOrder: SortOrder,
                             context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { SaleListSortOption(it, context) }

        view.listFilterOptionChips = FILTER_OPTIONS.toListFilterOptions(context, di)
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    override fun onListFilterOptionSelected(filterOptionId: ListFilterIdOption) {
        super.onListFilterOptionSelected(filterOptionId)
        updateListOnView()
    }

    private fun updateListOnView() {
        val loggedInPersonUid = accountManager.activeAccount.personUid
        view.list = repo.saleDao.findAllSales(loggedInPersonUid,
                view.checkedFilterOptionChip?.optionId?:SaleDao.FILTER_ALL_SALES,
                searchText.toQueryLikeParam())
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(SaleEditView.VIEW_NAME, mapOf(), context)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? SaleListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }

    override fun handleClickEntry(entry: Sale) {
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> {

                systemImpl.go(SaleEditView.VIEW_NAME,
                        mapOf(UstadView.ARG_ENTITY_UID to entry.saleUid.toString()), context)
            }
        }
    }

    override fun onClickSale(sale: SaleListDetail) {
         systemImpl.go(SaleEditView.VIEW_NAME, mapOf(UstadView.ARG_ENTITY_UID to sale.saleUid.toString()), context)
    }

    companion object{
            val FILTER_OPTIONS = listOf(
                MessageID.all_sales to SaleDao.FILTER_ALL_SALES,
                MessageID.payments_due to SaleDao.FILTER_PAYMENTS_DUE_SALES)

    }
}