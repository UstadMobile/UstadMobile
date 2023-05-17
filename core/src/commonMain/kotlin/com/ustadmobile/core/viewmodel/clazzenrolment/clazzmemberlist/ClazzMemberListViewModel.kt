package com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist

import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.locale.CourseTerminologyStrings
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.PersonWithClazzEnrolmentDetails
import org.kodein.di.DI

data class ClazzMemberListUiState(

    val studentList: ListPagingSourceFactory<PersonWithClazzEnrolmentDetails> = { EmptyPagingSource() },

    val teacherList: ListPagingSourceFactory<PersonWithClazzEnrolmentDetails> = { EmptyPagingSource() },

    val pendingStudentList: ListPagingSourceFactory<PersonWithClazzEnrolmentDetails> = {
        EmptyPagingSource()
    },

    val addTeacherVisible: Boolean = false,

    val addStudentVisible: Boolean = false,

    val sortOptions: List<SortOrderOption> = listOf(
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
    ),

    val activeSortOrderOption: SortOrderOption = sortOptions.first(),

    val fieldsEnabled: Boolean = true,

    val selectedChipId: Int = ClazzEnrolmentDaoCommon.FILTER_ACTIVE_ONLY,

    val filterOptions: List<MessageIdOption2> = listOf(
        MessageIdOption2(MessageID.active, ClazzEnrolmentDaoCommon.FILTER_ACTIVE_ONLY),
        MessageIdOption2(MessageID.all, 0)
    ),

    val terminologyStrings: CourseTerminologyStrings? = null
)

class ClazzMemberListViewModel(
    di: DI, savedStateHandle: UstadSavedStateHandle
): UstadListViewModel<ClazzMemberListUiState>(
    di, savedStateHandle, ClazzMemberListUiState(), DEST_NAME
) {

    private val teacherListPagingSource: ListPagingSourceFactory<PersonWithClazzEnrolmentDetails> = {
        TODO()
    }


    init {

    }

    override fun onUpdateSearchResult(searchText: String) {
        TODO("Not yet implemented")
    }

    override fun onClickAdd() {
        TODO("Not yet implemented")
    }

    companion object {

        val DEST_NAME = "CourseMembers"

    }
}
