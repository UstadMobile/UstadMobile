package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ClazzAssignmentDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class ClazzAssignmentListPresenter(context: Any, arguments: Map<String, String>, view: ClazzAssignmentListView,
                                   di: DI, lifecycleOwner: DoorLifecycleOwner,
                                   private val assignmentItemListener: DefaultClazzAssignmentListItemListener
                                   = DefaultClazzAssignmentListItemListener(view, ListViewMode.BROWSER,
                                           di.direct.instance(), context))
    : UstadListPresenter<ClazzAssignmentListView, ClazzAssignmentWithMetrics>(
        context, arguments, view, di, lifecycleOwner),
        ClazzAssignmentListItemListener by assignmentItemListener {

    private var clazzUid: Long = 0L

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    var searchText: String? = null

    private var clazzTimeZone: String? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        clazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0L
        assignmentItemListener.listViewMode = mListMode
        selectedSortOption = SORT_OPTIONS[0]
        GlobalScope.launch(doorMainDispatcher()) {
            mLoggedInPersonUid = accountManager.activeAccount.personUid
            repo.clazzAssignmentRollUpDao.cacheBestStatements(
                    clazzUid, 0,
                    0)

            clazzTimeZone = repo.clazzDao.getClazzWithSchool(clazzUid)?.effectiveTimeZone() ?: "UTC"
            view.clazzTimeZone = clazzTimeZone
            updateListOnView()
        }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return db.clazzDao.personHasPermissionWithClazz(accountManager.activeAccount.personUid,
                clazzUid, Role.PERMISSION_ASSIGNMENT_UPDATE)
    }

    private fun updateListOnView() {
        view.list = repo.clazzAssignmentDao.getAllAssignments(clazzUid, systemTimeInMillis(),
                mLoggedInPersonUid, selectedSortOption?.flag ?: 0,
                searchText.toQueryLikeParam(), Role.PERMISSION_ASSIGNMENT_VIEWSTUDENTPROGRESS)
    }

    override fun handleClickCreateNewFab() {
        val clazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0L
        systemImpl.go(ClazzAssignmentEditView.VIEW_NAME,
                mapOf(UstadView.ARG_CLAZZUID to clazzUid.toString()), context)
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
                SortOrderOption(MessageID.start_date, ClazzAssignmentDao.SORT_START_DATE_ASC, true),
                SortOrderOption(MessageID.start_date, ClazzAssignmentDao.SORT_START_DATE_DESC, false),
                SortOrderOption(MessageID.deadline, ClazzAssignmentDao.SORT_DEADLINE_ASC, true),
                SortOrderOption(MessageID.deadline, ClazzAssignmentDao.SORT_DEADLINE_DESC, false),
                SortOrderOption(MessageID.title, ClazzAssignmentDao.SORT_TITLE_ASC, true),
                SortOrderOption(MessageID.title, ClazzAssignmentDao.SORT_TITLE_DESC, false),
                SortOrderOption(MessageID.xapi_score, ClazzAssignmentDao.SORT_SCORE_ASC, true),
                SortOrderOption(MessageID.xapi_score, ClazzAssignmentDao.SORT_SCORE_DESC, false)
        )
    }

}