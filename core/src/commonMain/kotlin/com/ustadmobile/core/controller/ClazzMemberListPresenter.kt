package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ClazzEnrollmentDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.approvePendingClazzEnrollment
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.toListFilterOptions
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_CLAZZUID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzEnrollment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class ClazzMemberListPresenter(context: Any, arguments: Map<String, String>, view: ClazzMemberListView,
                               di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ClazzMemberListView, ClazzEnrollment>(context, arguments, view, di, lifecycleOwner), OnSortOptionSelected, OnSearchSubmitted {


    private var filterByClazzUid: Long = -1

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    var searchText: String? = null

    override fun onCreate(savedState: Map<String, String>?) {
        filterByClazzUid = arguments[ARG_FILTER_BY_CLAZZUID]?.toLong() ?: -1
        super.onCreate(savedState)
    }

    override fun onPause() {
        searchText = ""
        updateListOnView()
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
        view.listFilterOptionChips = FILTER_OPTIONS.toListFilterOptions(context, di)
        updateListOnView()

        view.addTeacherVisible = db.clazzDao.personHasPermissionWithClazz(activePersonUid,
                filterByClazzUid, Role.PERMISSION_CLAZZ_ADD_TEACHER)
    }

    private fun updateListOnView() {
        view.list = repo.clazzEnrollmentDao.findByClazzUidAndRole(filterByClazzUid,
                ClazzEnrollment.ROLE_TEACHER, selectedSortOption?.flag ?: 0,
                searchText.toQueryLikeParam(),view.checkedFilterOptionChip?.optionId ?: 0,
                systemTimeInMillis())
        view.studentList = repo.clazzEnrollmentDao.findByClazzUidAndRole(filterByClazzUid,
                ClazzEnrollment.ROLE_STUDENT, selectedSortOption?.flag ?: 0,
                searchText.toQueryLikeParam(),view.checkedFilterOptionChip?.optionId ?: 0,
                systemTimeInMillis())
        if (view.addStudentVisible) {
            view.pendingStudentList = db.clazzEnrollmentDao.findByClazzUidAndRole(filterByClazzUid,
                    ClazzEnrollment.ROLE_STUDENT_PENDING, selectedSortOption?.flag ?: 0,
                    searchText.toQueryLikeParam(),view.checkedFilterOptionChip?.optionId ?: 0,
                    systemTimeInMillis())
        }
    }


    override fun handleClickEntry(entry: ClazzEnrollment) {
        //Just go to PersonDetail - this view is not used as a picker
        systemImpl.go(PersonDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to entry.clazzEnrollmentPersonUid.toString()), context)
    }

    fun handleClickPendingRequest(enrollment: ClazzEnrollment, approved: Boolean) {
        GlobalScope.launch(doorMainDispatcher()) {
            if (approved) {
                try {
                    repo.approvePendingClazzEnrollment(enrollment)
                }catch(e: IllegalStateException) {
                    //did not have all entities present yet (e.g. sync race condition)
                    view.showSnackBar(systemImpl.getString(MessageID.content_editor_save_error, context))
                }
            } else {
                repo.clazzEnrollmentDao.updateAsync(enrollment.also {
                    it.clazzEnrollmentActive = false
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
        searchText = text
        updateListOnView()
    }

    companion object {

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.first_name, ClazzEnrollmentDao.SORT_FIRST_NAME_ASC, true),
                SortOrderOption(MessageID.first_name, ClazzEnrollmentDao.SORT_FIRST_NAME_DESC, false),
                SortOrderOption(MessageID.last_name, ClazzEnrollmentDao.SORT_LAST_NAME_ASC, true),
                SortOrderOption(MessageID.last_name, ClazzEnrollmentDao.SORT_LAST_NAME_DESC, false),
                SortOrderOption(MessageID.attendance, ClazzEnrollmentDao.SORT_ATTENDANCE_ASC, true),
                SortOrderOption(MessageID.attendance, ClazzEnrollmentDao.SORT_ATTENDANCE_DESC, false),
                SortOrderOption(MessageID.date_enroll, ClazzEnrollmentDao.SORT_DATE_REGISTERED_ASC, true),
                SortOrderOption(MessageID.date_enroll, ClazzEnrollmentDao.SORT_DATE_REGISTERED_DESC, false),
                SortOrderOption(MessageID.date_left, ClazzEnrollmentDao.SORT_DATE_LEFT_ASC, true),
                SortOrderOption(MessageID.date_left, ClazzEnrollmentDao.SORT_DATE_LEFT_DESC, false)
        )

        val FILTER_OPTIONS = listOf(MessageID.active to ClazzEnrollmentDao.FILTER_ACTIVE_ONLY,
                MessageID.all to 0)
    }
}