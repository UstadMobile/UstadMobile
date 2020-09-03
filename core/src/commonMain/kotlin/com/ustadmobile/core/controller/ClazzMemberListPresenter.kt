package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ClazzMemberDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.approvePendingClazzMember
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_CLAZZUID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class ClazzMemberListPresenter(context: Any, arguments: Map<String, String>, view: ClazzMemberListView,
                               di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ClazzMemberListView, ClazzMember>(context, arguments, view, di, lifecycleOwner), OnSortOptionSelected, OnSearchSubmitted {


    var filterByClazzUid: Long = -1

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    override fun onCreate(savedState: Map<String, String>?) {
        filterByClazzUid = arguments[ARG_FILTER_BY_CLAZZUID]?.toLong() ?: -1
        super.onCreate(savedState)
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return false
    }

    override suspend fun onLoadFromDb() {
        super.onLoadFromDb()

        val activePersonUid = accountManager.activeAccount.personUid

        view.addStudentVisible = db.clazzDao.personHasPermissionWithClazz(activePersonUid,
                filterByClazzUid, Role.PERMISSION_CLAZZ_ADD_STUDENT)

        selectedSortOption = SORT_OPTIONS[0]
        updateListOnView()

        view.addTeacherVisible = db.clazzDao.personHasPermissionWithClazz(activePersonUid,
                filterByClazzUid, Role.PERMISSION_CLAZZ_ADD_TEACHER)
    }

    private fun updateListOnView(searchText: String? = null) {
        view.list = repo.clazzMemberDao.findByClazzUidAndRole(filterByClazzUid,
                ClazzMember.ROLE_TEACHER, selectedSortOption?.flag ?: 0,
                if (searchText.isNullOrEmpty()) "%%" else "%${searchText}%")
        view.studentList = repo.clazzMemberDao.findByClazzUidAndRole(filterByClazzUid,
                ClazzMember.ROLE_STUDENT, selectedSortOption?.flag ?: 0,
                if (searchText.isNullOrEmpty()) "%%" else "%${searchText}%")
        if (view.addStudentVisible) {
            view.pendingStudentList = db.clazzMemberDao.findByClazzUidAndRole(filterByClazzUid,
                    ClazzMember.ROLE_STUDENT_PENDING, selectedSortOption?.flag ?: 0,
                    if (searchText.isNullOrEmpty()) "%%" else "%${searchText}%")
        }
    }


    override fun handleClickEntry(entry: ClazzMember) {
        //Just go to PersonDetail - this view is not used as a picker
        systemImpl.go(PersonDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to entry.clazzMemberPersonUid.toString()), context)
    }

    fun handleClickPendingRequest(member: ClazzMember, approved: Boolean) {
        GlobalScope.launch {
            if (approved) {
                repo.approvePendingClazzMember(member)
            } else {
                repo.clazzMemberDao.updateAsync(member.also {
                    it.clazzMemberActive = false
                })
            }
        }

    }

    override fun handleClickCreateNewFab() {
        //there really isn't a fab here. There are buttons for add teacher and add student in the list itself
    }

    fun handleEnrolMember(person: Person, role: Int) {
        GlobalScope.launch {
            repo.enrolPersonIntoClazzAtLocalTimezone(person, filterByClazzUid, role)
        }
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
                SortOrderOption(MessageID.first_name, ClazzMemberDao.SORT_FIRST_NAME_ASC, true),
                SortOrderOption(MessageID.first_name, ClazzMemberDao.SORT_FIRST_NAME_DESC, false),
                SortOrderOption(MessageID.last_name, ClazzMemberDao.SORT_LAST_NAME_ASC, true),
                SortOrderOption(MessageID.last_name, ClazzMemberDao.SORT_LAST_NAME_DESC, false),
                SortOrderOption(MessageID.attendance, ClazzMemberDao.SORT_ATTENDANCE_ASC, true),
                SortOrderOption(MessageID.attendance, ClazzMemberDao.SORT_ATTENDANCE_DESC, false),
                SortOrderOption(MessageID.date_enroll, ClazzMemberDao.SORT_DATE_REGISTERED_ASC, true),
                SortOrderOption(MessageID.date_enroll, ClazzMemberDao.SORT_DATE_REGISTERED_DESC, false),
                SortOrderOption(MessageID.date_left, ClazzMemberDao.SORT_DATE_LEFT_ASC, true),
                SortOrderOption(MessageID.date_left, ClazzMemberDao.SORT_DATE_LEFT_DESC, false)
        )
    }
}