package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ClazzWorkDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.SortOrderOption
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
        ClazzMemberWithClazzWorkProgress>(context, arguments, view, di, lifecycleOwner),
        OnSortOptionSelected, OnSearchSubmitted {

    private var filterByClazzWorkUid: Long = -1

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        filterByClazzWorkUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: -1
        selectedSortOption = SORT_OPTIONS[0]
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        //We never add anything here.
        return false
    }

    private fun updateListOnView(searchText: String? = null) {

        view.clazzWorkWithMetrics = repo.clazzWorkDao.findClazzWorkWithMetricsByClazzWorkUid(
                filterByClazzWorkUid)

        view.list = repo.clazzWorkDao.findStudentProgressByClazzWork(
                filterByClazzWorkUid,
                selectedSortOption?.flag ?: ClazzWorkDao.SORT_FIRST_NAME_ASC,
                searchText)

    }

    override fun handleClickEntry(entry: ClazzMemberWithClazzWorkProgress) {

        val clazzMemberUid = entry.mClazzMember?.clazzMemberUid ?: 0L
        val clazzWorkUid = filterByClazzWorkUid

        systemImpl.go(ClazzWorkSubmissionMarkingView.VIEW_NAME,
                mapOf(UstadView.ARG_CLAZZWORK_UID to clazzWorkUid.toString(),
                        UstadView.ARG_CLAZZMEMBER_UID to clazzMemberUid.toString()),
                context)

    }

    override fun handleClickCreateNewFab() {
        //No New Fab here
    }

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        updateListOnView()
    }


    override fun onSearchSubmitted(text: String?) {
        updateListOnView(text)
    }


    companion object {

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.first_name, ClazzWorkDao.SORT_FIRST_NAME_ASC, true),
                SortOrderOption(MessageID.first_name, ClazzWorkDao.SORT_FIRST_NAME_DESC, false),
                SortOrderOption(MessageID.last_name, ClazzWorkDao.SORT_LAST_NAME_ASC, true),
                SortOrderOption(MessageID.last_name, ClazzWorkDao.SORT_LAST_NAME_DESC, false),
                SortOrderOption(MessageID.student_progress, ClazzWorkDao.SORT_CONTENT_PROGRESS_ASC, true),
                SortOrderOption(MessageID.student_progress, ClazzWorkDao.SORT_CONTENT_PROGRESS_DESC, false),
                SortOrderOption(MessageID.student_progress, ClazzWorkDao.SORT_STATUS_ASC, true),
                SortOrderOption(MessageID.student_progress, ClazzWorkDao.SORT_STATUS_DESC, false)
        )
    }
}