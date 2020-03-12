package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.*
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role.Companion.PERMISSION_CLAZZ_INSERT
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ClazzList2Presenter(context: Any, arguments: Map<String, String>, view: ClazzList2View,
                          val db: UmAppDatabase, val dbRepo: UmAppDatabase,
                          val systemImpl: UstadMobileSystemImpl)
    : UstadBaseController<ClazzList2View>(context, arguments, view) {

    var searchQuery: String = "%"

    var loggedInPersonUid = 0L

    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc),
        ORDER_ATTENDANCE_ASC(MessageID.attendance_low_to_high),
        ORDER_ATTENDANCE_DESC(MessageID.attendance_high_to_low)
    }

    class ClazzListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
        getAndSetList(SortOrder.ORDER_NAME_ASC)
        view.sortOptions = SortOrder.values().toList()
        checkPermissions()
    }

    fun checkPermissions() {
        GlobalScope.launch(doorMainDispatcher()) {
            view.addButtonVisible = dbRepo.clazzDao.personHasPermission(loggedInPersonUid,
                    PERMISSION_CLAZZ_INSERT)
        }
    }

    private fun getAndSetList(sortOrder: SortOrder) {
        view.clazzList = when(sortOrder) {
            SortOrder.ORDER_ATTENDANCE_ASC -> dbRepo.clazzDao.findAllActiveClazzesSortByNameAsc(
                    searchQuery, loggedInPersonUid)
            SortOrder.ORDER_ATTENDANCE_DESC -> dbRepo.clazzDao.findAllActiveClazzesSortByNameDesc(
                    searchQuery, loggedInPersonUid)
            SortOrder.ORDER_NAME_ASC -> dbRepo.clazzDao.findAllActiveClazzesSortByNameAsc(
                    searchQuery, loggedInPersonUid)
            SortOrder.ORDER_NAME_DSC -> dbRepo.clazzDao.findAllActiveClazzesSortByNameDesc(
                    searchQuery, loggedInPersonUid)
        }
    }

    fun handleClickClazz(clazz: Clazz) {
        val args = mapOf(UstadView.ARG_CLAZZ_UID to clazz.clazzUid.toString())
        systemImpl.go(ClazzDetailView.VIEW_NAME, args, context)
    }

    fun handleClickAddClazz() {
        systemImpl.go(ClazzEdit2View.VIEW_NAME, mapOf(), context)
    }

    fun handleClickSortOrder(sortOrder: SortOrder) {
        if(sortOrder != currentSortOrder) {
            getAndSetList(sortOrder)
            currentSortOrder = sortOrder
        }

    }

}