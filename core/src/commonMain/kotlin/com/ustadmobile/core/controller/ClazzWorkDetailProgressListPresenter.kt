package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ClazzWorkDetailProgressListView
import com.ustadmobile.core.view.ClazzWorkSubmissionMarkingView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazzWorkProgress
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class ClazzWorkDetailProgressListPresenter(context: Any, arguments: Map<String, String>,
                           view: ClazzWorkDetailProgressListView,
                           lifecycleOwner: DoorLifecycleOwner, systemImpl: UstadMobileSystemImpl,
                           db: UmAppDatabase, repo: UmAppDatabase,
                           activeAccount: DoorLiveData<UmAccount?>)
    : UstadListPresenter<ClazzWorkDetailProgressListView,
        ClazzMemberWithClazzWorkProgress>(context, arguments, view, lifecycleOwner, systemImpl,
            db, repo, activeAccount) {


    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    private var filterByClazzWorkUid: Long = -1

    class ClazzMemberWithClazzWorkProgressListSortOption(val sortOrder: SortOrder, context: Any)
        : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        filterByClazzWorkUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong()?: -1
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        //TODO: This
        return false
    }

    private fun updateListOnView() {

        GlobalScope.launch {
        val clazzWorkWithMetrics =
                repo.clazzWorkDao.findClazzWorkWithMetricsByClazzWorkUidAsync(
                        filterByClazzWorkUid)

            view.runOnUiThread(Runnable {
                view.clazzWorkWithMetricsFlat = clazzWorkWithMetrics
            })
        }

        view.list = repo.clazzWorkDao.findStudentProgressByClazzWork(
                filterByClazzWorkUid)

    }

    override fun handleClickEntry(entry: ClazzMemberWithClazzWorkProgress) {

        val clazzMemberUid = entry.mClazzMember?.clazzMemberUid?:0L
        val clazzWorkUid = filterByClazzWorkUid

        systemImpl.go(ClazzWorkSubmissionMarkingView.VIEW_NAME,
                mapOf(UstadView.ARG_CLAZZWORK_UID to clazzWorkUid.toString(),
                        UstadView.ARG_CLAZZMEMBER_UID to clazzMemberUid.toString()),
                context)

        //super.handleClickEntry(entry)
    }

    override fun handleClickCreateNewFab() {
        //No New Fab here
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? ClazzMemberWithClazzWorkProgressListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }
}