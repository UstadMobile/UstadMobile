package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.LanguageDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI

class LanguageListPresenter(context: Any, arguments: Map<String, String>, view: LanguageListView,
                            di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<LanguageListView, Language>(context, arguments, view, di, lifecycleOwner),
        OnSearchSubmitted, OnSortOptionSelected {

    private var loggedInPersonUid: Long = 0

    private var searchText: String? = null

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS


    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
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
        view.list = repo.languageDao.findLanguagesAsSource(
                selectedSortOption?.flag ?: LanguageDao.SORT_LANGNAME_ASC,
                searchText.toQueryLikeParam())
    }


    override fun handleClickEntry(entry: Language) {
        when(mListMode) {
            ListViewMode.PICKER -> finishWithResult(safeStringify(di,
                ListSerializer(Language.serializer()), listOf(entry)))
            ListViewMode.BROWSER -> systemImpl.go(LanguageEditView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.langUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        navigateForResult(
            NavigateForResultOptions(this,
                null,
                LanguageEditView.VIEW_NAME, Language::class,
                Language.serializer(),
                SAVEDSTATE_KEY_LANGUAGE
            )
        )
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {
        handleClickCreateNewFab()
    }

    override suspend fun onCheckListSelectionOptions(account: UmAccount?): List<SelectionOption> {
        return listOf(SelectionOption.HIDE)
    }

    override fun handleClickSelectionOption(selectedItem: List<Language>, option: SelectionOption) {
        GlobalScope.launch(doorMainDispatcher()) {
            when (option) {
                SelectionOption.HIDE -> {
                    repo.languageDao.toggleVisibilityLanguage(true,
                            selectedItem.map { it.langUid })
                    view.showSnackBar(systemImpl.getString(MessageID.action_hidden, context), {

                        GlobalScope.launch(doorMainDispatcher()){
                            repo.languageDao.toggleVisibilityLanguage(false,
                                    selectedItem.map { it.langUid })
                        }

                    }, MessageID.undo)
                }
            }
        }
    }

    companion object {
        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.name, LanguageDao.SORT_LANGNAME_ASC, true),
                SortOrderOption(MessageID.name, LanguageDao.SORT_LANGNAME_DESC, false),
                SortOrderOption(MessageID.two_letter_code, LanguageDao.SORT_TWO_LETTER_ASC, true),
                SortOrderOption(MessageID.two_letter_code, LanguageDao.SORT_TWO_LETTER_DESC, false),
                SortOrderOption(MessageID.three_letter_code, LanguageDao.SORT_THREE_LETTER_ASC, true),
                SortOrderOption(MessageID.three_letter_code, LanguageDao.SORT_THREE_LETTER_DESC, false))

        const val SAVEDSTATE_KEY_LANGUAGE = "Language"
    }

}