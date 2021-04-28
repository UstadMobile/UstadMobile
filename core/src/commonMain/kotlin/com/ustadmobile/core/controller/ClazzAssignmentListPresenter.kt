package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ClazzAssignmentDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
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


    private var clazzEnrolment: ClazzEnrolment? = null
    private var clazzUid: Long = 0L
    private var progressPermission = false

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    var searchText: String? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        clazzUid = arguments[UstadView.ARG_FILTER_BY_CLAZZUID]?.toLong() ?: 0L
        assignmentItemListener.listViewMode = mListMode
        selectedSortOption = SORT_OPTIONS[0]
        GlobalScope.launch(doorMainDispatcher()) {
            val loggedInPersonUid = accountManager.activeAccount.personUid
            clazzEnrolment = db.clazzEnrolmentDao.findByPersonUidAndClazzUidAsync(loggedInPersonUid, clazzUid)
            progressPermission = db.clazzDao.personHasPermissionWithClazz(accountManager.activeAccount.personUid, clazzUid,
                    Role.PERMISSION_ASSIGNMENT_VIEWSTUDENTPROGRESS)
            updateListOnView()
        }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return db.clazzDao.personHasPermissionWithClazz(accountManager.activeAccount.personUid,
                clazzUid, Role.PERMISSION_ASSIGNMENT_UPDATE)
    }

    private suspend fun updateListOnView() {
        view.list = repo.clazzAssignmentDao.getAllAssignments(clazzUid,
                selectedSortOption?.flag ?: 0,
                searchText.toQueryLikeParam())
    }

    override fun handleClickCreateNewFab() {
        val clazzUid = arguments[UstadView.ARG_FILTER_BY_CLAZZUID]?.toLong() ?: 0L

        val clazzAssignment: ClazzAssignment = ClazzAssignment().apply {
            caClazzUid = clazzUid
        }
        val clazzWorkJson = Json.encodeToString(ClazzAssignment.serializer(), clazzAssignment)
        systemImpl.go(ClazzAssignmentEditView.VIEW_NAME,
                mapOf(UstadEditView.ARG_ENTITY_JSON to clazzWorkJson), context)
    }

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        GlobalScope.launch(doorMainDispatcher()) {
            updateListOnView()
        }
    }


    override fun onSearchSubmitted(text: String?) {
        GlobalScope.launch(doorMainDispatcher()) {
            searchText = text
            updateListOnView()
        }
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