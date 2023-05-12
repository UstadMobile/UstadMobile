package com.ustadmobile.core.viewmodel.clazzassignment.detailoverview

import com.ustadmobile.core.test.viewmodeltest.ViewModelTestBuilder
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import kotlin.test.Test

class ClazzAssignmentDetailOverviewViewModelTest {

    class AssignmentDetailOverviewTestContext(
        val clazz: Clazz,
    )

    fun testClazzAssignmentDetailOverviewViewModel(
        activeUserRole: Int,
        block: suspend ViewModelTestBuilder<ClazzAssignmentDetailOverviewViewModel>.(AssignmentDetailOverviewTestContext) -> Unit
    ) {
        testViewModel {
            val context = AssignmentDetailOverviewTestContext(Clazz())
            block(context)
        }
    }

    @Test
    fun givenStudentWithNoSubmissionGivenYet_whenShown_thenShowNoSubmissionStatusAndAddFileTextWithComments() {
        testClazzAssignmentDetailOverviewViewModel(ClazzEnrolment.ROLE_STUDENT) {testContext ->
            viewModelFactory {
                ClazzAssignmentDetailOverviewViewModel(di, savedStateHandle)
            }
        }
    }
}