package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ContentEntry2DetailView
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_CONTENT_FILTER
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_RECYCLED_CONTENT
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_TITLE
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class ContentEntryList2Presenter(context: Any, arguments: Map<String, String>, view: ContentEntryList2View,
                                 di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ContentEntryList2View, ContentEntry>(context, arguments, view, di, lifecycleOwner) {


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
        view.sortOptions = SortOrder.values().toList().map { ContentEntryListSortOption(it, context) }
        contentFilter = arguments[ARG_CONTENT_FILTER].toString()
        parentEntryUidStack += arguments[ARG_PARENT_ENTRY_UID]?.toLong() ?: 0L
        loggedPersonUid = accountManager.activeAccount.personUid
        getAndSetList()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    private fun getAndSetList(sortOrder: SortOrder = currentSortOrder) {
        view.list  = when(contentFilter){
            ARG_LIBRARIES_CONTENT -> when(sortOrder){
                SortOrder.ORDER_NAME_ASC -> repo.contentEntryDao.getChildrenByParentUidWithCategoryFilterOrderByNameAsc(
                        parentEntryUid, 0, 0, loggedPersonUid)
                SortOrder.ORDER_NAME_DSC -> repo.contentEntryDao.getChildrenByParentUidWithCategoryFilterOrderByNameDesc(
                        parentEntryUid, 0, 0, loggedPersonUid)
            }
            ARG_DOWNLOADED_CONTENT -> when(sortOrder){
                SortOrder.ORDER_NAME_ASC -> repo.contentEntryDao.downloadedRootItemsAsc(loggedPersonUid)
                SortOrder.ORDER_NAME_DSC -> repo.contentEntryDao.downloadedRootItemsDesc(loggedPersonUid)
            }
            ARG_RECYCLED_CONTENT -> repo.contentEntryDao.recycledItems(personUid = loggedPersonUid)
            else -> null
        }
    }

    override fun handleClickEntry(entry: ContentEntry) {
        when{
            mListMode == ListViewMode.PICKER && !entry.leaf -> {
                this.parentEntryUidStack += entry.contentEntryUid
                showContentEntryListByParentUid()
            }

            mListMode == ListViewMode.PICKER && entry.leaf -> {
                view.finishWithResult(listOf(entry))
            }
            mListMode == ListViewMode.BROWSER -> {
                val args = if(entry.leaf) mapOf(ARG_ENTITY_UID to entry.contentEntryUid.toString())
                else mapOf( ARG_PARENT_ENTRY_UID to entry.contentEntryUid.toString(),
                        ARG_CONTENT_FILTER to ARG_LIBRARIES_CONTENT, ARG_PARENT_ENTRY_TITLE to entry.title)
                systemImpl.go(if(entry.leaf) ContentEntry2DetailView.VIEW_NAME
                else ContentEntryList2View.VIEW_NAME, args,context)
            }
        }
    }

    fun handleOnSelectClicked(entry: ContentEntry){
        when(entry.leaf){
            true -> handleClickEntry(entry)
            false -> view.finishWithResult(listOf(entry))
        }
    }


    private fun showContentEntryListByParentUid(){
        view.list = repo.contentEntryDao.getChildrenByParentUidWithCategoryFilterOrderByNameAsc(
                parentEntryUid, 0, 0, loggedPersonUid)
    }

    fun handleOnBackPressed(): Boolean{
        if(mListMode == ListViewMode.PICKER && parentEntryUidStack.count() > 1){
            parentEntryUidStack.removeAt(parentEntryUidStack.count() - 1)
            showContentEntryListByParentUid()
            return true
        }
        return false
    }

    fun handleDownloadStatusButtonClicked(entry: ContentEntry){
        view.downloadOptions = mapOf(ARG_PARENT_ENTRY_UID to entry.toString())
    }

    override fun handleClickCreateNewFab() {
       view.showContentEntryAddOptions(parentEntryUid)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? ContentEntryListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            getAndSetList(currentSortOrder)
        }
    }
}