package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_CONTENT_FILTER
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_RECYCLED_CONTENT
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class ContentEntryList2Presenter(context: Any, arguments: Map<String, String>, view: ContentEntryList2View,
                                 di: DI, lifecycleOwner: DoorLifecycleOwner,
                                 val contentEntryListItemListener: DefaultContentEntryListItemListener
                                 = DefaultContentEntryListItemListener(view = view, context = context, di = di))
    : UstadListPresenter<ContentEntryList2View, ContentEntry>(context, arguments, view, di, lifecycleOwner),
        ContentEntryListItemListener by contentEntryListItemListener {

    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    private var contentFilter = ARG_LIBRARIES_CONTENT

    private var loggedPersonUid: Long = 0L

    private val parentEntryUidStack = mutableListOf<Long>()

    private val parentEntryUid: Long
        get() = parentEntryUidStack.lastOrNull() ?: 0L

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class ContentEntryListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        contentEntryListItemListener.mListMode = mListMode
        contentEntryListItemListener.presenter = this
        view.sortOptions = SortOrder.values().toList().map { ContentEntryListSortOption(it, context) }
        contentFilter = arguments[ARG_CONTENT_FILTER].toString()
        parentEntryUidStack += arguments[ARG_PARENT_ENTRY_UID]?.toLong() ?: 0L
        loggedPersonUid = accountManager.activeAccount.personUid
        getAndSetList()
        GlobalScope.launch(doorMainDispatcher()) {
            if (contentFilter == ARG_LIBRARIES_CONTENT) {
                view.editOptionVisible = onCheckUpdatePermission()
            }
        }

    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return db.entityRoleDao.userHasTableLevelPermission(accountManager.activeAccount.personUid,
                Role.PERMISSION_CONTENT_INSERT)
    }

    suspend fun onCheckUpdatePermission(): Boolean {
        return db.entityRoleDao.userHasTableLevelPermission(accountManager.activeAccount.personUid,
                Role.PERMISSION_CONTENT_UPDATE)
    }

    private fun getAndSetList(sortOrder: SortOrder = currentSortOrder, showHidden: Boolean = false) {
        view.list = when (contentFilter) {
            ARG_LIBRARIES_CONTENT -> when (sortOrder) {
                SortOrder.ORDER_NAME_ASC -> repo.contentEntryDao.getChildrenByParentUidWithCategoryFilterOrderByNameAsc(
                        parentEntryUid, 0, 0, loggedPersonUid, showHidden)
                SortOrder.ORDER_NAME_DSC -> repo.contentEntryDao.getChildrenByParentUidWithCategoryFilterOrderByNameDesc(
                        parentEntryUid, 0, 0, loggedPersonUid, showHidden)
            }
            ARG_DOWNLOADED_CONTENT -> when (sortOrder) {
                SortOrder.ORDER_NAME_ASC -> db.contentEntryDao.downloadedRootItemsAsc(loggedPersonUid)
                SortOrder.ORDER_NAME_DSC -> db.contentEntryDao.downloadedRootItemsDesc(loggedPersonUid)
            }
            ARG_RECYCLED_CONTENT -> repo.contentEntryDao.recycledItems(personUid = loggedPersonUid)
            else -> null
        }

        GlobalScope.launch(doorMainDispatcher()) {
            db.takeIf {parentEntryUid != 0L } ?.contentEntryDao?.findTitleByUidAsync(parentEntryUid)?.let {titleStr ->
                view.takeIf { titleStr.isNotBlank() }?.title = titleStr
            }
        }
    }

    override suspend fun onCheckListSelectionOptions(account: UmAccount?): List<SelectionOption> {
        return listOf(SelectionOption.MOVE, SelectionOption.HIDE)
    }



    override fun handleClickSelectionOption(selectedItem: List<ContentEntry>, option: SelectionOption) {
        GlobalScope.launch(doorMainDispatcher()) {
            when (option) {
                SelectionOption.MOVE -> {

                }
                SelectionOption.HIDE -> {
                    repo.contentEntryDao.toggleVisibilityContentEntryItems(true, selectedItem.map { it.contentEntryUid })
                }
                SelectionOption.UNHIDE -> {
                    repo.contentEntryDao.toggleVisibilityContentEntryItems(false, selectedItem.map { it.contentEntryUid })
                }
            }
        }
    }


    /**
     * Handles when the user clicks a "folder" in picker mode
     */
    fun openContentEntryBranchPicker(entry: ContentEntry) {
        this.parentEntryUidStack += entry.contentEntryUid
        showContentEntryListByParentUid()
    }

    private fun showContentEntryListByParentUid(){
        view.list = repo.contentEntryDao.getChildrenByParentUidWithCategoryFilterOrderByNameAsc(
                parentEntryUid, 0, 0, loggedPersonUid, false)
    }

    fun handleOnBackPressed(): Boolean{
        if(mListMode == ListViewMode.PICKER && parentEntryUidStack.count() > 1){
            parentEntryUidStack.removeAt(parentEntryUidStack.count() - 1)
            showContentEntryListByParentUid()
            return true
        }
        return false
    }

    override fun handleClickCreateNewFab() {
        view.showContentEntryAddOptions(parentEntryUid)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? ContentEntryListSortOption)?.sortOrder ?: return
        if (sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            getAndSetList(currentSortOrder)
        }
    }

    fun handleClickEditFolder() {
        systemImpl.go(ContentEntryEdit2View.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to parentEntryUid.toString()), context)
    }

    fun handleClickShowHiddenItems() {
        getAndSetList(showHidden = true)
    }
}