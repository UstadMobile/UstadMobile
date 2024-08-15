package com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist

import app.cash.turbine.test
import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.test.viewmodeltest.ViewModelTestBuilder
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.lib.db.entities.Person
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ClazzMemberListViewModelTest : AbstractMainDispatcherTest() {

    class ClazzMemberViewModelTestContext(
        val clazz: Clazz,
        val activeUserPerson: Person,
    )

    val learningSpace = LearningSpace("https://www.test.com/")

    private fun testClazzMemberViewModel(
        activeUserRole: Int,
        block: suspend ViewModelTestBuilder<ClazzMemberListViewModel>.(ClazzMemberViewModelTestContext) -> Unit,
    ) {
        testViewModel {
            val activePerson = setActiveUser(learningSpace)

            val context = activeDb.withDoorTransactionAsync {
                val clazzUid = activeDb.doorPrimaryKeyManager.nextId(Clazz.TABLE_ID)
                val clazz = Clazz().apply {
                    this.clazzUid = clazzUid
                    clazzName = "Test Course"
                }

                CreateNewClazzUseCase(activeDb).invoke(clazz)

                EnrolIntoCourseUseCase(activeDb, null).invoke(
                    ClazzEnrolment(
                        clazzUid = clazzUid,
                        personUid = activePerson.personUid,
                    ).apply {
                        clazzEnrolmentRole = activeUserRole
                    },
                    timeZoneId = "UTC"
                )

                ClazzMemberViewModelTestContext(clazz, activePerson)
            }

            block(context)
        }
    }

    @Test
    fun givenActiveUserDoesNotHaveAddPermissions_whenOnCreateCalled_thenShouldQueryDatabaseAndSetOnViewAndSetAddVisibleToFalse() {
        testClazzMemberViewModel(
            activeUserRole = ClazzEnrolment.ROLE_STUDENT
        ) { testContext ->
            viewModelFactory {
                savedStateHandle[UstadView.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzMemberListViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.studentList() !is EmptyPagingSource && it.teacherList() !is EmptyPagingSource
                }

                assertFalse(readyState.addStudentVisible)
                assertFalse(readyState.addTeacherVisible)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenActiveAccountHasAddPermissions_whenOnCreateCalled_thenShouldSetAddOptionsToBeVisible() {
        testClazzMemberViewModel(
            activeUserRole = 0
        ) { testContext ->
            activeDb.coursePermissionDao().upsertAsync(
                CoursePermission(
                    cpToPersonUid = testContext.activeUserPerson.personUid,
                    cpPermissionsFlag = CoursePermission.TEACHER_DEFAULT_PERMISSIONS,
                    cpClazzUid = testContext.clazz.clazzUid
                )
            )

            viewModelFactory {
                savedStateHandle[UstadView.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzMemberListViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.addStudentVisible && it.addTeacherVisible
                }

                assertTrue(readyState.addStudentVisible)
                assertTrue(readyState.addTeacherVisible)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

}