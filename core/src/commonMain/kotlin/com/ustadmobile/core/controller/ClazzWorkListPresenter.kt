package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ClazzWorkDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.kodein.di.DI

class ClazzWorkListPresenter(context: Any, arguments: Map<String, String>, view: ClazzWorkListView,
                             di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ClazzWorkListView, ClazzWork>(context, arguments, view, di, lifecycleOwner)
        , OnSortOptionSelected, OnSearchSubmitted {

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    var searchText: String? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        GlobalScope.launch(doorMainDispatcher()) {
            selectedSortOption = SORT_OPTIONS[0]
            updateListOnView()
        }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        val clazzUid = arguments.get(UstadView.ARG_FILTER_BY_CLAZZUID)?.toLong() ?: 0L
        return db.clazzDao.personHasPermissionWithClazz(accountManager.activeAccount.personUid,
                clazzUid, Role.PERMISSION_CLAZZWORK_UPDATE)
    }

    private suspend fun updateListOnView() {

        val clazzUid = arguments[UstadView.ARG_FILTER_BY_CLAZZUID]?.toLong() ?: 0L
        val loggedInPersonUid = accountManager.activeAccount.personUid
        val clazzEnrolment: ClazzEnrolment? =
                db.clazzEnrolmentDao.findByPersonUidAndClazzUidAsync(loggedInPersonUid, clazzUid)

        view.list = repo.clazzWorkDao.findWithMetricsByClazzUidLive(
                clazzUid, clazzEnrolment?.clazzEnrolmentRole ?: ClazzEnrolment.ROLE_STUDENT,
                UMCalendarUtil.getDateInMilliPlusDays(0), selectedSortOption?.flag ?: 0,
                searchText.toQueryLikeParam())
    }

    override fun handleClickEntry(entry: ClazzWork) {
        when (mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(ClazzWorkDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.clazzWorkUid.toString()), context)
        }
    }

    override fun handleClickCreateNewFab() {
        val clazzUid = arguments.get(UstadView.ARG_FILTER_BY_CLAZZUID)?.toLong() ?: 0L

        val clazzWork: ClazzWork = ClazzWork().apply {
            clazzWorkClazzUid = clazzUid
        }
        val clazzWorkJson = Json.stringify(ClazzWork.serializer(), clazzWork)
        systemImpl.go(ClazzWorkEditView.VIEW_NAME,
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
                SortOrderOption(MessageID.deadline, ClazzWorkDao.SORT_DEADLINE_ASC, true),
                SortOrderOption(MessageID.deadline, ClazzWorkDao.SORT_DEADLINE_DESC, false),
                SortOrderOption(MessageID.visible_from_date, ClazzWorkDao.SORT_VISIBLE_FROM_ASC, true),
                SortOrderOption(MessageID.visible_from_date, ClazzWorkDao.SORT_VISIBLE_FROM_DESC, false),
                SortOrderOption(MessageID.title, ClazzWorkDao.SORT_TITLE_ASC, true),
                SortOrderOption(MessageID.title, ClazzWorkDao.SORT_TITLE_DESC, false)
        )
    }
}