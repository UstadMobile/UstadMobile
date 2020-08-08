package com.ustadmobile.core.controller

import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ClazzWorkDetailProgressListView
import com.ustadmobile.core.view.ClazzWorkSubmissionMarkingView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazzWorkProgress
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class ClazzWorkDetailProgressListPresenter(context: Any, arguments: Map<String, String>,
                           view: ClazzWorkDetailProgressListView, di: DI,
                           lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ClazzWorkDetailProgressListView,
        ClazzMemberWithClazzWorkProgress>(context, arguments, view, di, lifecycleOwner) {

    private var filterByClazzWorkUid: Long = -1


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        filterByClazzWorkUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong()?: -1
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        //We never add anything here.
        return false
    }

    private fun updateListOnView() {


        view.clazzWorkWithMetrics = repo.clazzWorkDao.findClazzWorkWithMetricsByClazzWorkUid(
                filterByClazzWorkUid)

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

    }

    override fun handleClickCreateNewFab() {
        //No New Fab here
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        //No sort here
    }
}