package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.LanguageListView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class LanguageListPresenter(context: Any, arguments: Map<String, String>, view: LanguageListView,
                            di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<LanguageListView, Language>(context, arguments, view, di, lifecycleOwner) {


    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    private var loggedInPersonUid: Long = 0

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class LanguageListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.sortOptions = SortOrder.values().toList().map { LanguageListSortOption(it, context) }
        loggedInPersonUid = accountManager.activeAccount.personUid
        getAndSetList()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    private fun getAndSetList(sortOrder: SortOrder = currentSortOrder) {
        view.list = when(sortOrder) {
            SortOrder.ORDER_NAME_ASC -> repo.languageDao.publicLanguagesOrderByNameAsc()
            SortOrder.ORDER_NAME_DSC -> repo.languageDao.publicLanguagesOrderByNameDesc()
        }
    }


    override fun handleClickEntry(entry: Language) {
        view.finishWithResult(listOf(entry))
    }

    override fun handleClickCreateNewFab() {}

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? LanguageListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            getAndSetList(currentSortOrder)
        }
    }
}