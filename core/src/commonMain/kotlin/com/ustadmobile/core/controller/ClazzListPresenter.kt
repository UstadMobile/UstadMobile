package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class ClazzListPresenter(context: Any, arguments: Map<String, String>, view: ClazzList2View,
                         di: DI, lifecycleOwner: DoorLifecycleOwner,
                         private val clazzList2ItemListener:
                          DefaultClazzListItemListener = DefaultClazzListItemListener(view, ListViewMode.BROWSER, context, di))
    : UstadListPresenter<ClazzList2View, Clazz>(context, arguments, view,  di, lifecycleOwner), ClazzListItemListener by clazzList2ItemListener {

    var searchQuery: String = "%"

    var loggedInPersonUid = 0L

    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    private var filterExcludeMembersOfSchool : Long = 0

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc),
        ORDER_ATTENDANCE_ASC(MessageID.attendance_low_to_high),
        ORDER_ATTENDANCE_DESC(MessageID.attendance_high_to_low)
    }

    class ClazzListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        filterExcludeMembersOfSchool = arguments[ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL]?.toLong() ?: 0L
        clazzList2ItemListener.listViewMode = mListMode

        loggedInPersonUid = accountManager.activeAccount.personUid
        getAndSetList(SortOrder.ORDER_NAME_ASC)
        view.sortOptions = SortOrder.values().toList().map { ClazzListSortOption(it, context) }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        //return repo.clazzDao.personHasPermission(loggedInPersonUid, PERMISSION_CLAZZ_INSERT)
        return true
    }

    private fun getAndSetList(sortOrder: SortOrder) {
        view.list = when(sortOrder) {
            SortOrder.ORDER_ATTENDANCE_ASC -> repo.clazzDao.findAllActiveClazzesSortByNameAsc(
                    searchQuery, loggedInPersonUid, filterExcludeMembersOfSchool)
            SortOrder.ORDER_ATTENDANCE_DESC -> repo.clazzDao.findAllActiveClazzesSortByNameDesc(
                    searchQuery, loggedInPersonUid, filterExcludeMembersOfSchool)
            SortOrder.ORDER_NAME_ASC -> repo.clazzDao.findAllActiveClazzesSortByNameAsc(
                    searchQuery, loggedInPersonUid, filterExcludeMembersOfSchool)
            SortOrder.ORDER_NAME_DSC -> repo.clazzDao.findAllActiveClazzesSortByNameDesc(
                    searchQuery, loggedInPersonUid, filterExcludeMembersOfSchool)
        }
    }

    override fun handleClickCreateNewFab() {
        systemImpl.go(ClazzEdit2View.VIEW_NAME, mapOf(), context)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? ClazzListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            getAndSetList(sortOrder)
        }
    }
}