package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.SchoolMemberDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.enrollPersonToSchool
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.SchoolMemberListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.SchoolMember
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class SchoolMemberListPresenter(context: Any, arguments: Map<String, String>,
                                view: SchoolMemberListView, di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<SchoolMemberListView, SchoolMember>(context, arguments, view, di, lifecycleOwner)
        , OnSortOptionSelected, OnSearchSubmitted {

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    var searchText: String? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        selectedSortOption = SORT_OPTIONS[0]
        updateListOnView()
    }

    override fun onPause() {
        searchText = ""
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return db.schoolDao.personHasPermissionWithSchool(account?.personUid ?: 0L,
                arguments[UstadView.ARG_FILTER_BY_SCHOOLUID]?.toLong() ?: 0L,
                Role.PERMISSION_SCHOOL_UPDATE)
    }

    private fun updateListOnView() {

        val schoolRole = arguments[UstadView.ARG_FILTER_BY_ROLE]?.toInt() ?: 0

        val schoolUid: Long = arguments[UstadView.ARG_FILTER_BY_SCHOOLUID]?.toLong() ?: 0L

        view.list = repo.schoolMemberDao
                .findAllActiveMembersBySchoolAndRoleUid(schoolUid, schoolRole,
                        selectedSortOption?.flag ?: 0,
                       searchText.toQueryLikeParam())
    }

    fun handleEnrolMember(schoolUid: Long, personUid: Long, role: Int) {

        GlobalScope.launch {
            db.enrollPersonToSchool(schoolUid, personUid, role)
        }
    }

    override fun handleClickEntry(entry: SchoolMember) {

        when (mListMode) {
            ListViewMode.PICKER -> view.finishWithResult(listOf(entry))
            ListViewMode.BROWSER -> systemImpl.go(PersonDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.schoolMemberPersonUid.toString()), context)
        }

    }

    override fun handleClickCreateNewFab() {
        view.addMember()
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
                SortOrderOption(MessageID.first_name, SchoolMemberDao.SORT_FIRST_NAME_ASC, true),
                SortOrderOption(MessageID.first_name, SchoolMemberDao.SORT_FIRST_NAME_DESC, false),
                SortOrderOption(MessageID.last_name, SchoolMemberDao.SORT_LAST_NAME_ASC, true),
                SortOrderOption(MessageID.last_name, SchoolMemberDao.SORT_LAST_NAME_DESC, false)
        )

    }
}