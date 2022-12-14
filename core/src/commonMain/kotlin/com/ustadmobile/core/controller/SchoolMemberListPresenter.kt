package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.SchoolMemberDao
import com.ustadmobile.core.db.dao.SchoolMemberDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.approvePendingSchoolMember
import com.ustadmobile.core.util.ext.enrollPersonToSchool
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_ROLE
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.SchoolMember
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI

class SchoolMemberListPresenter(context: Any, arguments: Map<String, String>,
                                view: SchoolMemberListView, di: DI, lifecycleOwner: LifecycleOwner)
    : UstadListPresenter<SchoolMemberListView, SchoolMember>(context, arguments, view, di, lifecycleOwner)
        , OnSortOptionSelected, OnSearchSubmitted {

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    var searchText: String? = null

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        mLoggedInPersonUid = accountManager.activeAccount.personUid
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
                if(arguments[ARG_FILTER_BY_ROLE]?.toInt() == Role.ROLE_SCHOOL_STUDENT_UID) {
                    Role.PERMISSION_SCHOOL_ADD_STUDENT
                }else {
                    Role.PERMISSION_SCHOOL_ADD_STAFF
                })
    }

    override suspend fun onLoadFromDb() {
        super.onLoadFromDb()
        updateListOnView()
    }

    private fun updateListOnView() {

        val schoolRole = arguments[ARG_FILTER_BY_ROLE]?.toInt() ?: 0

        val schoolUid: Long = arguments[UstadView.ARG_FILTER_BY_SCHOOLUID]?.toLong() ?: 0L

        if(arguments[ARG_FILTER_BY_ROLE]?.toInt() == Role.ROLE_SCHOOL_STUDENT_UID) {
            GlobalScope.launch(doorMainDispatcher()) {
                val hasAddStudentPermission = db.schoolDao.personHasPermissionWithSchool(
                        accountManager.activeAccount.personUid,
                        arguments[UstadView.ARG_FILTER_BY_SCHOOLUID]?.toLong() ?: 0L,
                        Role.PERMISSION_SCHOOL_ADD_STUDENT)
                view.takeIf { hasAddStudentPermission }?.pendingStudentList = db.schoolMemberDao
                        .findAllActiveMembersBySchoolAndRoleUid(
                                schoolUid, Role.ROLE_SCHOOL_STUDENT_PENDING_UID, selectedSortOption?.flag ?: 0,
                                searchText.toQueryLikeParam(), mLoggedInPersonUid
                        )
            }
        }

        view.list = repo.schoolMemberDao.findAllActiveMembersBySchoolAndRoleUid(
                schoolUid, schoolRole,
                selectedSortOption?.flag ?: 0, searchText.toQueryLikeParam(), mLoggedInPersonUid
        )
    }

    fun handleEnrolMember(schoolUid: Long, personUid: Long, role: Int) {
        GlobalScope.launch {
            repo.enrollPersonToSchool(schoolUid, personUid, role)
        }
    }

    override fun handleClickEntry(entry: SchoolMember) {

        when (mListMode) {
            ListViewMode.PICKER -> finishWithResult(safeStringify(di,
                ListSerializer(SchoolMember.serializer()), listOf(entry)))
            ListViewMode.BROWSER -> systemImpl.go(PersonDetailView.VIEW_NAME,
                    mapOf(UstadView.ARG_ENTITY_UID to entry.schoolMemberPersonUid.toString()), context)
        }

    }

    fun handleClickPendingRequest(member: SchoolMember, approved: Boolean) {
        GlobalScope.launch {
            if (approved) {
                repo.approvePendingSchoolMember(member)
            } else {
                repo.schoolMemberDao.updateAsync(member.also {
                    it.schoolMemberActive = false
                })
            }
        }

    }

    override fun handleClickCreateNewFab() {
        view.addMember()
    }

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        updateListOnView()
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {
        navigateForResult(
            NavigateForResultOptions(
                this,null,
                PersonListView.VIEW_NAME,
                Person::class,
                Person.serializer(),
                destinationResultKey,
                true,
                arguments = args?.toMutableMap() ?: mutableMapOf(),
            )
        )
    }


    override fun onSearchSubmitted(text: String?) {
        searchText = text
        updateListOnView()
    }

    companion object {

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.first_name, SchoolMemberDaoCommon.SORT_FIRST_NAME_ASC, true),
                SortOrderOption(MessageID.first_name, SchoolMemberDaoCommon.SORT_FIRST_NAME_DESC, false),
                SortOrderOption(MessageID.last_name, SchoolMemberDaoCommon.SORT_LAST_NAME_ASC, true),
                SortOrderOption(MessageID.last_name, SchoolMemberDaoCommon.SORT_LAST_NAME_DESC, false)
        )

    }
}