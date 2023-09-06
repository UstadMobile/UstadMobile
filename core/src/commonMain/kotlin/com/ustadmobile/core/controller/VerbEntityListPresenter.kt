package com.ustadmobile.core.controller

import com.ustadmobile.core.MR
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.VerbEntityListView
import com.ustadmobile.core.view.VerbEntityListView.Companion.ARG_EXCLUDE_VERBUIDS_LIST
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.db.entities.VerbDisplay
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI

class VerbEntityListPresenter(context: Any, arguments: Map<String, String>, view: VerbEntityListView,
                              di: DI,lifecycleOwner: LifecycleOwner)
    : UstadListPresenter<VerbEntityListView, VerbDisplay>(context, arguments, view, di, lifecycleOwner) {


    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    private var filterExcludeList = listOf<Long>()

    enum class SortOrder(val stringResource: StringResource) {
        ORDER_NAME_ASC(MR.strings.sort_by_name_asc),
        ORDER_NAME_DSC(MR.strings.sort_by_name_desc)
    }

    class VerbEntityListSortOption(
        val sortOrder: SortOrder,
        context: Any,
        di: DI
    ) : MessageIdOption(sortOrder.stringResource, context, di = di, code = 42/*this needs fixed when converted to MVVM*/)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        filterExcludeList = arguments[ARG_EXCLUDE_VERBUIDS_LIST]?.split(",")?.filter { it.isNotEmpty() }?.map { it.toLong() }
                ?: listOf()
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { VerbEntityListSortOption(it, context, di) }
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

    override fun handleClickSortOrder(sortOption: IdOption) {
        val sortOrder = (sortOption as? VerbEntityListSortOption)?.sortOrder ?: return
        if (sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }

    override fun handleClickCreateNewFab() {

    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {}

    override fun handleClickEntry(entry: VerbDisplay) {
        finishWithResult(safeStringify(di, ListSerializer(VerbDisplay.serializer()), listOf(entry)))
    }
}