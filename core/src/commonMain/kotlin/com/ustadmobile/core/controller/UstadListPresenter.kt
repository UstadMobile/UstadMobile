package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.view.ListViewAddMode
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class UstadListPresenter<V: UstadView, RT>(context: Any, arguments: Map<String, String>, view: V,
                                                    val lifecycleOwner: DoorLifecycleOwner,
                                                    val systemImpl: UstadMobileSystemImpl,
                                                    val db: UmAppDatabase, val repo: UmAppDatabase,
                                                    val activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadBaseController<V>(context, arguments, view) {

    protected var mListMode = ListViewMode.BROWSER

    protected var mLoggedInPersonUid: Long = 0

    protected var mSearchQuery: String = "%"

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        mListMode = ListViewMode.valueOf(
                arguments[UstadView.ARG_LISTMODE] ?: ListViewMode.BROWSER.toString())
        activeAccount.observeWithLifecycleOwner(lifecycleOwner, this::onAccountChanged)
    }

    protected open fun onAccountChanged(account: UmAccount?) {
        val listView = (view as? UstadListView<*, *>) ?: return

        GlobalScope.launch(doorMainDispatcher()) {
            val hasAddPermission = onCheckAddPermission(account)
            listView.addMode = when {
                hasAddPermission && mListMode == ListViewMode.BROWSER -> ListViewAddMode.FAB
                hasAddPermission && mListMode == ListViewMode.PICKER -> ListViewAddMode.FIRST_ITEM
                else -> ListViewAddMode.NONE
            }
        }
    }

    abstract fun handleClickEntry(entry: RT)

    /**
     * This should be implemented to check if the given user has permission to add entries (and hence
     * control what add buttons are or are not shown on the view)
     */
    abstract suspend fun onCheckAddPermission(account: UmAccount?): Boolean

    open fun handleClickSortOrder(sortOption: MessageIdOption) {

    }

    abstract fun handleClickCreateNew()


}