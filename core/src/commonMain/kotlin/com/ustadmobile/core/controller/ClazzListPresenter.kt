package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.PersonListView.Companion.ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role.Companion.PERMISSION_CLAZZ_INSERT
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


    enum class SortOrder(val messageId: Int, val sortOrderCode: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc, ClazzDao.SORT_CLAZZNAME_ASC),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc, ClazzDao.SORT_CLAZZNAME_DESC),
        ORDER_ATTENDANCE_ASC(MessageID.attendance_low_to_high, ClazzDao.SORT_ATTENDANCE_ASC),
        ORDER_ATTENDANCE_DESC(MessageID.attendance_high_to_low, ClazzDao.SORT_ATTENDANCE_DESC)
    }

    class ClazzListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        filterExcludeMembersOfSchool = arguments[ARG_FILTER_EXCLUDE_MEMBERSOFSCHOOL]?.toLong() ?: 0L
        clazzList2ItemListener.listViewMode = mListMode

        loggedInPersonUid = accountManager.activeAccount.personUid
        view.sortOptions = SortOrder.values().toList().map { ClazzListSortOption(it, context) }
        updateList()
    }

    private fun updateList() {
        view.list = repo.clazzDao.findClazzesWithPermission(searchQuery,
                loggedInPersonUid, filterExcludeMembersOfSchool, currentSortOrder.sortOrderCode)
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return repo.clazzDao.personHasPermission(loggedInPersonUid, PERMISSION_CLAZZ_INSERT)
    }

    private fun getAndSetList(sortOrder: SortOrder) {
        currentSortOrder = sortOrder
        updateList()
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