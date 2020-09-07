package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class SchoolListPresenter(context: Any, arguments: Map<String, String>, view: SchoolListView,
                          di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<SchoolListView, School>(context, arguments, view, di, lifecycleOwner) {

    var searchQuery = "%"
    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class SchoolListSortOption(val sortOrder: SortOrder, context: Any)
        : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { SchoolListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return db.entityRoleDao.userHasTableLevelPermission(account?.personUid ?: 0,
            School.TABLE_ID, Role.PERMISSION_SCHOOL_INSERT)
    }

    private fun updateListOnView() {
        view.list = when(currentSortOrder) {
            SortOrder.ORDER_NAME_ASC ->
                repo.schoolDao.findAllActiveSchoolWithMemberCountAndLocationNameAsc(searchQuery)
            SortOrder.ORDER_NAME_DSC ->
                repo.schoolDao.findAllActiveSchoolWithMemberCountAndLocationNameDesc(searchQuery)
        }
    }

    override fun handleClickEntry(entry: School) {
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(SchoolDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.schoolUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(SchoolEditView.VIEW_NAME, mapOf(), context)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? SchoolListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }
}