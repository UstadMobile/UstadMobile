package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.VerbEntityListView
import com.ustadmobile.core.view.VerbEntityListView.Companion.ARG_EXCLUDE_VERBUIDS_LIST
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.db.entities.VerbDisplay
import org.kodein.di.DI

class VerbEntityListPresenter(context: Any, arguments: Map<String, String>, view: VerbEntityListView,
                              lifecycleOwner: DoorLifecycleOwner, di: DI)
    : UstadListPresenter<VerbEntityListView, VerbDisplay>(context, arguments, view, lifecycleOwner, di) {


    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    private var filterExcludeList = listOf<Long>()

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class VerbEntityListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        filterExcludeList = arguments[ARG_EXCLUDE_VERBUIDS_LIST]?.split(",")?.filter { it.isNotEmpty() }?.map { it.toLong() }
                ?: listOf()
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { VerbEntityListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    private fun updateListOnView() {
        view.list = when (currentSortOrder) {
            SortOrder.ORDER_NAME_ASC -> repo.verbDao.findAllVerbsAsc(filterExcludeList)
            SortOrder.ORDER_NAME_DSC -> repo.verbDao.findAllVerbsDesc(filterExcludeList)
        }
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? VerbEntityListSortOption)?.sortOrder ?: return
        if (sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }

    override fun handleClickCreateNewFab() {

    }

    override fun handleClickEntry(entry: VerbDisplay) {
        view.finishWithResult(listOf(entry))
    }
}