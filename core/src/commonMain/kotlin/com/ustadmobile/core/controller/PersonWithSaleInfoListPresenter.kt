package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class PersonWithSaleInfoListPresenter(context: Any, arguments: Map<String, String>, view: PersonWithSaleInfoListView,
                                      di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<PersonWithSaleInfoListView, PersonWithSaleInfo>(context, arguments, view,
        di, lifecycleOwner), PersonWithSaleInfoListItemListener {

    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class PersonWithSaleInfoListSortOption(val sortOrder: SortOrder, context: Any) :
            MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { PersonWithSaleInfoListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
//        TODO("check on add permission for this account: e.g. " +
//                "repo.clazzDao.personHasPermission(loggedInPersonUid, PERMISSION_CLAZZ_INSERT)")
        return true
    }

    private fun updateListOnView() {
        //TODO: add sorting
        val loggedInPersonUid = accountManager.activeAccount.personUid
        view.list = repo.saleDao.findAllPersonWithSaleInfo(loggedInPersonUid)
    }

    override fun handleClickCreateNewFab() {

        systemImpl.go(PersonEditView.VIEW_NAME, mapOf(UstadView.ARG_FILTER_PERSON_WE to "true"),
                context)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? PersonWithSaleInfoListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }

    override fun onClickPersonWithSaleInfo(personWithSaleInfo: PersonWithSaleInfo) {
        systemImpl.go(PersonDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to personWithSaleInfo.personUid.toString()), context)
    }

    fun handleClickAddLE(){

        systemImpl.go(PersonEditView.VIEW_NAME, mapOf(UstadView.ARG_FILTER_PERSON_LE to "true"), context)
    }

    fun handleClickAddProducer(){

        systemImpl.go(PersonEditView.VIEW_NAME, mapOf(UstadView.ARG_FILTER_PERSON_WE to "true"), context)
    }

    fun handleClickAddCustomer(){

        systemImpl.go(PersonEditView.VIEW_NAME, mapOf(UstadView.ARG_FILTER_PERSON_CUSTOMER to "true"), context)
    }
}