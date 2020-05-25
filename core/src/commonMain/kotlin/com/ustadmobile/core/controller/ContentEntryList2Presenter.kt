package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_CONTENT_FILTER
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_DOWNLOADED_CONTENT
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_RECYCLED_CONTENT
import com.ustadmobile.core.view.ContentEntryList2View.ContentEntryListViewMode
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.UmAccount

class ContentEntryList2Presenter(context: Any, arguments: Map<String, String>, view: ContentEntryList2View,
                          lifecycleOwner: DoorLifecycleOwner, systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?>)
    : UstadListPresenter<ContentEntryList2View, ContentEntry>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {


    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    private var contentFilter = ARG_LIBRARIES_CONTENT

    private var parentUid: Long = 0L

    private var loggedPersonUid: Long = 0L

    private var viewMode: ContentEntryListViewMode = ContentEntryListViewMode.NORMAL

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class ContentEntryListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.sortOptions = SortOrder.values().toList().map { ContentEntryListSortOption(it, context) }
        contentFilter = arguments[ARG_CONTENT_FILTER].toString()
        parentUid = arguments[ARG_PARENT_ENTRY_UID]?.toLong() ?: 0L
        loggedPersonUid = UmAccountManager.getActivePersonUid(context)
        getAndSetList()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    private fun getAndSetList(sortOrder: SortOrder = currentSortOrder) {

        view.list  = when(contentFilter){
            ARG_LIBRARIES_CONTENT -> when(sortOrder){
                SortOrder.ORDER_NAME_ASC -> repo.contentEntryDao.getChildrenByParentUidWithCategoryFilterOrderByNameAsc(parentUid, 0, 0, loggedPersonUid)
                SortOrder.ORDER_NAME_DSC -> repo.contentEntryDao.getChildrenByParentUidWithCategoryFilterOrderByNameDesc(parentUid, 0, 0, loggedPersonUid)
            }
            ARG_DOWNLOADED_CONTENT -> when(sortOrder){
                SortOrder.ORDER_NAME_ASC -> repo.contentEntryDao.downloadedRootItemsAsc()
                SortOrder.ORDER_NAME_DSC -> repo.contentEntryDao.downloadedRootItemsDesc()
            }
            ARG_RECYCLED_CONTENT -> repo.contentEntryDao.recycledItems()
            else -> null
        }
    }

    override fun handleClickEntry(entry: ContentEntry) {
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(ContentEntry2DetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.contentEntryUid.toString()), context)
        }
    }

    fun checkPermissionAndHandleDownloadStatusButtonClicked(entry: ContentEntry){

    }

    fun checkAndGetContentType(type: String?): String{
        return view.checkAndGetContentType(type)
    }

    fun checkAndGetContentDrawable(type: String?): Int{
        return  view.checkAndGetContentDrawable(type)
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(ContentEntryAddOptionsView.VIEW_NAME,
                mapOf(ARG_PARENT_ENTRY_UID to parentUid.toString()), context)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? ContentEntryListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            getAndSetList(currentSortOrder)
        }
    }
}