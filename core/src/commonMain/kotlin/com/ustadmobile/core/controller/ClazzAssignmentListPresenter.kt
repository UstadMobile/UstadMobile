package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ClazzAssignmentDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.view.ClazzAssignmentListView
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
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
    : UstadListPresenter<ClazzAssignmentListView, ClazzAssignment>(
        context, arguments, view, di, lifecycleOwner),
        ClazzAssignmentListItemListener by assignmentItemListener {


    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    var searchText: String? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        updateListOnView()
        assignmentItemListener.listViewMode = mListMode
        selectedSortOption = SORT_OPTIONS[0]
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        val clazzUid = arguments[UstadView.ARG_FILTER_BY_CLAZZUID]?.toLong() ?: 0L
        return db.clazzDao.personHasPermissionWithClazz(accountManager.activeAccount.personUid,
                clazzUid, Role.PERMISSION_ASSIGNMENT_UPDATE)
    }

    private fun updateListOnView() {
        /* TODO: Update the list on the view from the appropriate DAO query, e.g.
        view.list = when(sortOrder) {
            SortOrder.ORDER_NAME_ASC -> repo.daoName.findAllActiveClazzesSortByNameAsc(
                    searchQuery, loggedInPersonUid)
            SortOrder.ORDER_NAME_DSC -> repo.daoName.findAllActiveClazzesSortByNameDesc(
                    searchQuery, loggedInPersonUid)
        }
        */
    }

    override fun handleClickCreateNewFab() {
        val clazzUid = arguments[UstadView.ARG_FILTER_BY_CLAZZUID]?.toLong() ?: 0L

        val clazzAssignment: ClazzAssignment = ClazzAssignment().apply {
            clazzAssignmentClazzUid = clazzUid
        }
        val clazzWorkJson = Json.encodeToString(ClazzAssignment.serializer(), clazzAssignment)
        systemImpl.go(AssignmentEditView.VIEW_NAME,
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
                SortOrderOption(MessageID.deadline, ClazzAssignmentDao.SORT_DEADLINE_ASC, true),
                SortOrderOption(MessageID.deadline, ClazzAssignmentDao.SORT_DEADLINE_DESC, false),
                SortOrderOption(MessageID.title, ClazzAssignmentDao.SORT_TITLE_ASC, true),
                SortOrderOption(MessageID.title, ClazzAssignmentDao.SORT_TITLE_DESC, false),
                SortOrderOption(MessageID.score, ClazzAssignmentDao.SORT_SCORE_ASC, true),
                SortOrderOption(MessageID.score, ClazzAssignmentDao.SORT_SCORE_DESC, false)
        )
    }

}