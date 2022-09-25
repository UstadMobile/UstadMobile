package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzEnrolmentDao
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_ENROLMENT_ROLE
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance

class ClazzMemberListPresenter(context: Any, arguments: Map<String, String>, view: ClazzMemberListView,
                               di: DI, lifecycleOwner: LifecycleOwner)
    : UstadListPresenter<ClazzMemberListView, PersonWithClazzEnrolmentDetails>(context, arguments, view, di, lifecycleOwner), OnSortOptionSelected, OnSearchSubmitted {

    private var filterByClazzUid: Long = -1

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    var searchText: String? = null

    override fun onCreate(savedState: Map<String, String>?) {
        filterByClazzUid = arguments[ARG_CLAZZUID]?.toLong() ?: -1
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

        mLoggedInPersonUid = accountManager.activeAccount.personUid

        view.addStudentVisible = db.clazzDao.personHasPermissionWithClazz(mLoggedInPersonUid,
                filterByClazzUid, Role.PERMISSION_CLAZZ_ADD_STUDENT)

        val terminology = db.courseTerminologyDao.getTerminologyForClazz(filterByClazzUid)
        view.termMap = terminology.toTermMap(json, systemImpl, context)

        selectedSortOption = SORT_OPTIONS[0]
        view.listFilterOptionChips = FILTER_OPTIONS.toListFilterOptions(context, di)
        updateListOnView()

        view.addTeacherVisible = db.clazzDao.personHasPermissionWithClazz(mLoggedInPersonUid,
                filterByClazzUid, Role.PERMISSION_CLAZZ_ADD_TEACHER)
    }

    private fun updateListOnView() {
        view.list = repo.clazzEnrolmentDao.findByClazzUidAndRole(filterByClazzUid,
                ClazzEnrolment.ROLE_TEACHER, selectedSortOption?.flag ?: 0,
                searchText.toQueryLikeParam(), view.checkedFilterOptionChip?.optionId ?: ClazzEnrolmentDaoCommon.FILTER_ACTIVE_ONLY,
                mLoggedInPersonUid, systemTimeInMillis())
        view.studentList = repo.clazzEnrolmentDao.findByClazzUidAndRole(filterByClazzUid,
                ClazzEnrolment.ROLE_STUDENT, selectedSortOption?.flag ?: 0,
                searchText.toQueryLikeParam(), view.checkedFilterOptionChip?.optionId ?: ClazzEnrolmentDaoCommon.FILTER_ACTIVE_ONLY,
                mLoggedInPersonUid, systemTimeInMillis())
        if (view.addStudentVisible) {
            view.pendingStudentList = db.clazzEnrolmentDao.findByClazzUidAndRole(filterByClazzUid,
                    ClazzEnrolment.ROLE_STUDENT_PENDING, selectedSortOption?.flag ?: 0,
                    searchText.toQueryLikeParam(), view.checkedFilterOptionChip?.optionId ?: ClazzEnrolmentDaoCommon.FILTER_ACTIVE_ONLY,
                    mLoggedInPersonUid, systemTimeInMillis())
        }
    }

    override fun handleClickEntry(entry: PersonWithClazzEnrolmentDetails) {
        systemImpl.go(ClazzEnrolmentListView.VIEW_NAME,
                mapOf(UstadView.ARG_PERSON_UID to entry.personUid.toString(),
                        ARG_CLAZZUID to filterByClazzUid.toString(),
                        ARG_FILTER_BY_ENROLMENT_ROLE to entry.enrolmentRole.toString()), context)
    }

    fun handleClickPendingRequest(enrolmentDetails: PersonWithClazzEnrolmentDetails, approved: Boolean) {
        presenterScope.launch {
            try {
                repo.withDoorTransactionAsync { txRepo ->
                    if (approved) {
                        txRepo.approvePendingClazzEnrolment(enrolmentDetails, filterByClazzUid)

                    } else {
                        txRepo.declinePendingClazzEnrolment(enrolmentDetails, filterByClazzUid)
                    }
                }
            } catch (e: IllegalStateException) {
                //did not have all entities present yet (e.g. sync race condition)
                view.showSnackBar(systemImpl.getString(MessageID.content_editor_save_error, context) + e.message)
                Napier.e("Exception approving member", e)
            }
        }
    }


    override fun handleClickCreateNewFab() {
        //there really isn't a fab here. There are buttons for add teacher and add student in the list itself
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {}

    override fun onClickSort(sortOption: SortOrderOption) {
        super.onClickSort(sortOption)
        updateListOnView()
    }

    override fun onSearchSubmitted(text: String?) {
        searchText = text
        updateListOnView()
    }

    override fun onListFilterOptionSelected(filterOptionId: ListFilterIdOption) {
        super.onListFilterOptionSelected(filterOptionId)
        updateListOnView()
    }

    fun handlePickNewMemberClicked(role: Int) {

        val args = mutableMapOf(
            PersonListView.ARG_FILTER_EXCLUDE_MEMBERSOFCLAZZ to filterByClazzUid.toString(),
            ARG_FILTER_BY_ENROLMENT_ROLE to role.toString(),
            ARG_CLAZZUID to (arguments[ARG_CLAZZUID] ?: "-1"),
            UstadView.ARG_GO_TO_COMPLETE to ClazzEnrolmentEditView.VIEW_NAME,
            UstadView.ARG_POPUPTO_ON_FINISH to ClazzMemberListView.VIEW_NAME,
            ClazzMemberListView.ARG_HIDE_CLAZZES to true.toString(),
            UstadView.ARG_SAVE_TO_DB to true.toString()
        ).also {
            if(role == ClazzEnrolment.ROLE_STUDENT){
                it[UstadView.ARG_CODE_TABLE] = Clazz.TABLE_ID.toString()
            }
        }

        navigateForResult(NavigateForResultOptions(
            this, null,
                PersonListView.VIEW_NAME,
                ClazzEnrolment::class,
                ClazzEnrolment.serializer(),
                RESULT_PERSON_KEY,
                overwriteDestination = true,
                arguments = args.toMutableMap()
            )
        )
    }

    companion object {

        val SORT_OPTIONS = listOf(
                SortOrderOption(MessageID.first_name, ClazzEnrolmentDaoCommon.SORT_FIRST_NAME_ASC, true),
                SortOrderOption(MessageID.first_name, ClazzEnrolmentDaoCommon.SORT_FIRST_NAME_DESC, false),
                SortOrderOption(MessageID.last_name, ClazzEnrolmentDaoCommon.SORT_LAST_NAME_ASC, true),
                SortOrderOption(MessageID.last_name, ClazzEnrolmentDaoCommon.SORT_LAST_NAME_DESC, false),
                SortOrderOption(MessageID.attendance, ClazzEnrolmentDaoCommon.SORT_ATTENDANCE_ASC, true),
                SortOrderOption(MessageID.attendance, ClazzEnrolmentDaoCommon.SORT_ATTENDANCE_DESC, false),
                SortOrderOption(MessageID.date_enroll, ClazzEnrolmentDaoCommon.SORT_DATE_REGISTERED_ASC, true),
                SortOrderOption(MessageID.date_enroll, ClazzEnrolmentDaoCommon.SORT_DATE_REGISTERED_DESC, false),
                SortOrderOption(MessageID.date_left, ClazzEnrolmentDaoCommon.SORT_DATE_LEFT_ASC, true),
                SortOrderOption(MessageID.date_left, ClazzEnrolmentDaoCommon.SORT_DATE_LEFT_DESC, false)
        )

        val FILTER_OPTIONS = listOf(MessageID.active to ClazzEnrolmentDaoCommon.FILTER_ACTIVE_ONLY,
                MessageID.all to 0)

        const val RESULT_PERSON_KEY = "person"
    }
}