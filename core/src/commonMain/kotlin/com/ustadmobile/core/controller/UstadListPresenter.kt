package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.OnListFilterOptionSelectedListener
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

abstract class UstadListPresenter<V: UstadListView<RT, *>, RT>(context: Any, arguments: Map<String, String>,
                                                               view: V, di: DI,
                                                    val lifecycleOwner: DoorLifecycleOwner)
    : UstadBaseController<V>(context, arguments, view, di), DIAware, OnSortOptionSelected,
        OnSearchSubmitted, OnListFilterOptionSelectedListener {

    protected var mListMode = ListViewMode.BROWSER

    protected var mLoggedInPersonUid: Long = 0

    protected var mSearchQuery: String = "%"

    val accountManager: UstadAccountManager by instance()

    val systemImpl: UstadMobileSystemImpl by instance()

    val db: UmAppDatabase by on(accountManager.activeAccount).instance(tag = TAG_DB)

    val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_REPO)

    open val sortOptions: List<SortOrderOption>
        get() = listOf()

    protected var selectedSortOption: SortOrderOption? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        mListMode = ListViewMode.valueOf(
                arguments[UstadView.ARG_LISTMODE] ?: ListViewMode.BROWSER.toString())
        GlobalScope.launch(doorMainDispatcher()) {
            onLoadFromDb()
        }
    }

    suspend open fun onLoadFromDb() {
        val listView = (view as? UstadListView<*, *>) ?: return
        val hasAddPermission = onCheckAddPermission(accountManager.activeAccount)
        listView.addMode = when {
            hasAddPermission && mListMode == ListViewMode.BROWSER -> ListViewAddMode.FAB
            hasAddPermission && mListMode == ListViewMode.PICKER -> ListViewAddMode.FIRST_ITEM
            else -> ListViewAddMode.NONE
        }

        listView.selectionOptions = onCheckListSelectionOptions(accountManager.activeAccount)
    }

    /**
     * This method will handle calling finishWithResult on the view as the default course of action
     * when the the mListMode is PICKER.
     *
     * If ListMode is BROWSE then the child implementation should call systemImpl.go itself to direct
     * the user to the detail view (or otherwise)
     */
    open fun handleClickEntry(entry: RT) {
        view.takeIf { mListMode == ListViewMode.PICKER }?.finishWithResult(listOf(entry))
    }

    open fun handleClickSelectionOption(selectedItem: List<RT>, option: SelectionOption) {

    }

    /**
     * This should be implemented to check if the given user has permission to add entries (and hence
     * control what add buttons are or are not shown on the view)
     */
    abstract suspend fun onCheckAddPermission(account: UmAccount?): Boolean

    /**
     * Override this method to set the options that a user will see if they start making selections
     */
    open suspend fun onCheckListSelectionOptions(account: UmAccount?): List<SelectionOption> {
        return listOf()
    }

    @Deprecated("Use onSortOptionSelected")
    open fun handleClickSortOrder(sortOption: MessageIdOption) {

    }

    override fun onClickSort(sortOption: SortOrderOption) {
        selectedSortOption = sortOption
    }
    override fun onSearchSubmitted(text: String?) {

    }

    /**
     * This can be overriden by the child class to udpate the query on the basis
     * of the selected filterOption
     */
    override fun onListFilterOptionSelected(filterOptionId: ListFilterIdOption) {

    }

    abstract fun handleClickCreateNewFab()

    open fun handleSelectionOptionChanged(t: List<RT>){

    }


}