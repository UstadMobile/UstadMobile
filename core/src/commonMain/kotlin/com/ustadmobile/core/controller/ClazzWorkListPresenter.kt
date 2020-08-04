package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import kotlinx.serialization.json.Json
import org.kodein.di.DI

class ClazzWorkListPresenter(context: Any, arguments: Map<String, String>, view: ClazzWorkListView,
                             di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ClazzWorkListView, ClazzWork>(context, arguments, view, di, lifecycleOwner) {


    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class ClazzWorkListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { ClazzWorkListSortOption(it, context) }

    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        val clazzUid = arguments.get(UstadView.ARG_FILTER_BY_CLAZZUID)?.toLong()?:0L
        return db.clazzDao.personHasPermissionWithClazz(accountManager.activeAccount.personUid,
            clazzUid, Role.PERMISSION_CLAZZ_ASSIGNMENT_UPDATE)
    }

    private fun updateListOnView() {

        val clazzUid = arguments.get(UstadView.ARG_FILTER_BY_CLAZZUID)?.toLong()?:0L
        val loggedInPersonUid = accountManager.activeAccount.personUid
        val clazzMember: ClazzMember? =
                db.clazzMemberDao.findByPersonUidAndClazzUid(loggedInPersonUid, clazzUid)

        view.list = when (currentSortOrder) {
            SortOrder.ORDER_NAME_ASC -> repo.clazzWorkDao.findWithMetricsByClazzUidLiveAsc(
                    clazzUid, clazzMember?.clazzMemberRole?:ClazzMember.ROLE_STUDENT,
                    UMCalendarUtil.getDateInMilliPlusDays(0))
            SortOrder.ORDER_NAME_DSC -> repo.clazzWorkDao.findWithMetricsByClazzUidLiveDesc(
                    clazzUid, clazzMember?.clazzMemberRole?:ClazzMember.ROLE_STUDENT,
                    UMCalendarUtil.getDateInMilliPlusDays(0))
        }
    }

    override fun handleClickEntry(entry: ClazzWork) {
        when(mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(ClazzWorkDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.clazzWorkUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        val clazzUid = arguments.get(UstadView.ARG_FILTER_BY_CLAZZUID)?.toLong()?:0L

        val clazzWork: ClazzWork = ClazzWork().apply {
            clazzWorkClazzUid = clazzUid
        }
        val clazzWorkJson = Json.stringify(ClazzWork.serializer(), clazzWork)
        systemImpl.go(ClazzWorkEditView.VIEW_NAME,
                mapOf(UstadEditView.ARG_ENTITY_JSON to clazzWorkJson), context)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? ClazzWorkListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }
}