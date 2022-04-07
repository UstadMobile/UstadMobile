package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.ClazzEnrolmentDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.approvePendingClazzEnrolment
import com.ustadmobile.core.util.ext.declinePendingClazzEnrolment
import com.ustadmobile.core.util.ext.toListFilterOptions
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.ClazzEnrolmentListView
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_FILTER_BY_ENROLMENT_ROLE
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.PersonWithClazzEnrolmentDetails
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance

class ClazzMemberListPresenter(context: Any, arguments: Map<String, String>, view: ClazzMemberListView,
                               di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ClazzMemberListView, PersonWithClazzEnrolmentDetails>(context, arguments, view, di, lifecycleOwner), OnSortOptionSelected, OnSearchSubmitted {


    private var filterByClazzUid: Long = -1

    override val sortOptions: List<SortOrderOption>
        get() = SORT_OPTIONS

    val json: Json by di.instance()


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
        val termMap = terminology?.ctTerminology?.let {
            json.decodeFromString(MapSerializer(String.serializer(), String.serializer()),
                it
            )
        } ?: mapOf()
        view.termMap = termMap

        selectedSortOption = SORT_OPTIONS[0]
        view.listFilterOptionChips = FILTER_OPTIONS.toListFilterOptions(context, di)
        updateListOnView()

        view.addTeacherVisible = db.clazzDao.personHasPermissionWithClazz(mLoggedInPersonUid,
                filterByClazzUid, Role.PERMISSION_CLAZZ_ADD_TEACHER)
    }

    private fun updateListOnView() {
        view.list = repo.clazzEnrolmentDao.findByClazzUidAndRole(filterByClazzUid,
                ClazzEnrolment.ROLE_TEACHER, selectedSortOption?.flag ?: 0,
                searchText.toQueryLikeParam(), view.checkedFilterOptionChip?.optionId ?: 0,
                mLoggedInPersonUid, systemTimeInMillis())
        view.studentList = repo.clazzEnrolmentDao.findByClazzUidAndRole(filterByClazzUid,
                ClazzEnrolment.ROLE_STUDENT, selectedSortOption?.flag ?: 0,
                searchText.toQueryLikeParam(), view.checkedFilterOptionChip?.optionId ?: 0,
                mLoggedInPersonUid, systemTimeInMillis())
        if (view.addStudentVisible) {
            view.pendingStudentList = db.clazzEnrolmentDao.findByClazzUidAndRole(filterByClazzUid,
                    ClazzEnrolment.ROLE_STUDENT_PENDING, selectedSortOption?.flag ?: 0,
                    searchText.toQueryLikeParam(), view.checkedFilterOptionChip?.optionId ?: 0,
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
                if (approved) {
                    repo.approvePendingClazzEnrolment(enrolmentDetails, filterByClazzUid)

                } else {
                    repo.declinePendingClazzEnrolment(enrolmentDetails, filterByClazzUid)
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

    fun handlePickNewMemberClicked(args: Map<String,String>){
        navigateForResult(
            NavigateForResultOptions(
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
                SortOrderOption(MessageID.first_name, ClazzEnrolmentDao.SORT_FIRST_NAME_ASC, true),
                SortOrderOption(MessageID.first_name, ClazzEnrolmentDao.SORT_FIRST_NAME_DESC, false),
                SortOrderOption(MessageID.last_name, ClazzEnrolmentDao.SORT_LAST_NAME_ASC, true),
                SortOrderOption(MessageID.last_name, ClazzEnrolmentDao.SORT_LAST_NAME_DESC, false),
                SortOrderOption(MessageID.attendance, ClazzEnrolmentDao.SORT_ATTENDANCE_ASC, true),
                SortOrderOption(MessageID.attendance, ClazzEnrolmentDao.SORT_ATTENDANCE_DESC, false),
                SortOrderOption(MessageID.date_enroll, ClazzEnrolmentDao.SORT_DATE_REGISTERED_ASC, true),
                SortOrderOption(MessageID.date_enroll, ClazzEnrolmentDao.SORT_DATE_REGISTERED_DESC, false),
                SortOrderOption(MessageID.date_left, ClazzEnrolmentDao.SORT_DATE_LEFT_ASC, true),
                SortOrderOption(MessageID.date_left, ClazzEnrolmentDao.SORT_DATE_LEFT_DESC, false)
        )

        val FILTER_OPTIONS = listOf(MessageID.active to ClazzEnrolmentDao.FILTER_ACTIVE_ONLY,
                MessageID.all to 0)

        const val RESULT_PERSON_KEY = "person"
    }
}