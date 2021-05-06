package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ClazzWorkDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.ClazzWorkDetailProgressListView
import com.ustadmobile.core.view.ClazzWorkSubmissionMarkingView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzWorkProgress
import com.ustadmobile.lib.db.entities.UmAccount
import org.kodein.di.DI

class ClazzWorkDetailProgressListPresenter(context: Any, arguments: Map<String, String>,
                                           view: ClazzWorkDetailProgressListView, di: DI,
                                           lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ClazzWorkDetailProgressListView,
        ClazzEnrolmentWithClazzWorkProgress>(context, arguments, view, di, lifecycleOwner),
        OnSortOptionSelected, OnSearchSubmitted {

    private var filterByClazzWorkUid: Long = -1

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    var searchText: String? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        filterByClazzWorkUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: -1
        selectedSortOption = SORT_OPTIONS[0]
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        //We never add anything here.
        return false
    }

    override suspend fun onLoadFromDb() {
        super.onLoadFromDb()
        updateListOnView()
    }

    private fun updateListOnView() {

        view.list = repo.clazzWorkDao.findStudentProgressByClazzWork(
                filterByClazzWorkUid,
                selectedSortOption?.flag ?: ClazzWorkDao.SORT_FIRST_NAME_ASC,
                searchText.toQueryLikeParam(), systemTimeInMillis())
    }

    override fun handleClickEntry(entry: ClazzEnrolmentWithClazzWorkProgress) {

        val personUid = entry.personUid
        val clazzWorkUid = filterByClazzWorkUid

        systemImpl.go(ClazzWorkSubmissionMarkingView.VIEW_NAME,
                mapOf(UstadView.ARG_CLAZZWORK_UID to clazzWorkUid.toString(),
                        UstadView.ARG_PERSON_UID to personUid.toString()),
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
        searchText = text
        updateListOnView()
    }


    companion object {

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.first_name, ClazzWorkDao.SORT_FIRST_NAME_ASC, true),
                SortOrderOption(MessageID.first_name, ClazzWorkDao.SORT_FIRST_NAME_DESC, false),
                SortOrderOption(MessageID.last_name, ClazzWorkDao.SORT_LAST_NAME_ASC, true),
                SortOrderOption(MessageID.last_name, ClazzWorkDao.SORT_LAST_NAME_DESC, false),
                SortOrderOption(MessageID.student_progress, ClazzWorkDao.SORT_CONTENT_PROGRESS_ASC, true),
                SortOrderOption(MessageID.student_progress, ClazzWorkDao.SORT_CONTENT_PROGRESS_DESC, false),
                SortOrderOption(MessageID.status, ClazzWorkDao.SORT_STATUS_ASC, true),
                SortOrderOption(MessageID.status, ClazzWorkDao.SORT_STATUS_DESC, false)
        )
    }
}