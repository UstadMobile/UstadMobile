package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.LanguageDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class LanguageListPresenter(context: Any, arguments: Map<String, String>, view: LanguageListView,
                            di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<LanguageListView, Language>(context, arguments, view, di, lifecycleOwner),
        OnSearchSubmitted, OnSortOptionSelected {

    private var loggedInPersonUid: Long = 0

    private var searchText: String? = null

    private var currentSortOrder: Int = LanguageDao.SORT_LANGNAME_ASC

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS


    override fun onClickSort(sortOption: SortOrderOption) {
        currentSortOrder = sortOption.flag
        getAndSetList()
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        loggedInPersonUid = accountManager.activeAccount.personUid
        getAndSetList()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    override fun onSearchSubmitted(text: String?) {
        searchText = text
        getAndSetList()
    }

    private fun getAndSetList() {
        view.list = repo.languageDao.findLanguagesAsSource(currentSortOrder, searchText.toQueryLikeParam())
    }


    override fun handleClickEntry(entry: Language) {
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(LanguageEditView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.langUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(LanguageEditView.VIEW_NAME, mapOf(), context)
    }

    companion object {
        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.name, LanguageDao.SORT_LANGNAME_ASC, true),
                SortOrderOption(MessageID.name, LanguageDao.SORT_LANGNAME_DESC, false))
    }

}