package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LanguageListPresenter(context: Any, arguments: Map<String, String>, view: LanguageListView,
                          lifecycleOwner: DoorLifecycleOwner, systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?>)
    : UstadListPresenter<LanguageListView, Language>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {


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
        loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
        insertTempData()
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

    private fun insertTempData(){
        GlobalScope.launch {
            db.clearAllTables()
            for(i in 1..20){
                val language = Language()
                language.langUid = i.toLong()
                language.name = "Language $i"
                language.iso_639_1_standard = if(i == 1) "en" else if(i == 2) "es" else "fr$i"
                repo.languageDao.insertAsync(language)
            }
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