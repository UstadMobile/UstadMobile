package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toListFilterOptions
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI

class PersonWithSaleInfoListPresenter(context: Any, arguments: Map<String, String>,
                                      view: PersonWithSaleInfoListView,
                                      di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<PersonWithSaleInfoListView, PersonWithSaleInfo>(context, arguments, view,
        di, lifecycleOwner), PersonWithSaleInfoListItemListener, OnSearchSubmitted {

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

    class PersonWithSaleInfoListSortOption(val sortOrder: SortOrder, context: Any) :
            MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map {
            PersonWithSaleInfoListSortOption(it, context) }

        val loggedInPersonUid = accountManager.activeAccount.personUid
        GlobalScope.launch {


            val loggedInPerson = withTimeoutOrNull(2000) {

                repo.personDao.findByUidAsync(loggedInPersonUid)

            }?:Person()

            view.runOnUiThread(Runnable {
                view.showAddLE(loggedInPerson.admin)
                if(loggedInPerson.admin){
                    view.listFilterOptionChips = FILTER_OPTIONS.toListFilterOptions(context, di)
                }else{
                    view.listFilterOptionChips = FILTER_OPTIONS_LE.toListFilterOptions(context, di)
                }
            })
        }

    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    private fun updateListOnView() {
        val loggedInPersonUid = accountManager.activeAccount.personUid
        view.list = repo.saleDao.findAllPersonWithSaleInfo(loggedInPersonUid,
            view.checkedFilterOptionChip?.optionId?:SaleDao.FILTER_ALL,
                searchText.toQueryLikeParam())
    }

    override fun handleClickCreateNewFab() {

        systemImpl.go(PersonEditView.VIEW_NAME, mapOf(UstadView.ARG_FILTER_PERSON_WE to "true"),
                context)
    }

    override fun onClickPersonWithSaleInfo(personWithSaleInfo: PersonWithSaleInfo) {
        systemImpl.go(PersonDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to personWithSaleInfo.personUid.toString()),
                context)
    }

    fun handleClickAddLE(){

        systemImpl.go(PersonEditView.VIEW_NAME, mapOf(UstadView.ARG_FILTER_PERSON_LE to "true"),
                context)
    }

    fun handleClickAddProducer(){

        systemImpl.go(PersonEditView.VIEW_NAME, mapOf(UstadView.ARG_FILTER_PERSON_WE to "true"),
                context)
    }

    fun handleClickAddCustomer(){

        systemImpl.go(PersonEditView.VIEW_NAME,
                mapOf(UstadView.ARG_FILTER_PERSON_CUSTOMER to "true"), context)
    }

    override fun onListFilterOptionSelected(filterOptionId: ListFilterIdOption) {
        super.onListFilterOptionSelected(filterOptionId)
        updateListOnView()
    }

    companion object{
        val FILTER_OPTIONS = listOf(
                MessageID.all to SaleDao.FILTER_ALL,
                MessageID.le  to SaleDao.FILTER_LE_ONLY,
                MessageID.we  to SaleDao.FILTER_WE_ONLY,
                MessageID.customer  to SaleDao.FILTER_CUSTOMER_ONLY)

        val FILTER_OPTIONS_LE = listOf(
                MessageID.all to SaleDao.FILTER_ALL,
                MessageID.we  to SaleDao.FILTER_WE_ONLY,
                MessageID.customer  to SaleDao.FILTER_CUSTOMER_ONLY
        )
    }
}