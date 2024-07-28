package com.ustadmobile.core.viewmodel.clazzlog.attendancelist

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.schedule.generateUid
import com.ustadmobile.core.test.viewmodeltest.ViewModelTestBuilder
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.loadFirstList
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.lib.db.entities.Person
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ClazzLogListAttendanceViewModelTest  : AbstractMainDispatcherTest() {

    val endpoint = Endpoint("https://test.com/")

    private data class ClazzLogListAttendanceTestContext(
        val clazz: Clazz,
        val activePerson: Person,
    )

    private fun testClazzLogListAttendanceViewModel(
        addExistingLog: Boolean = false,
        grantAttendancePermission: Boolean = false,
        block: suspend ViewModelTestBuilder<ClazzLogListAttendanceViewModel>.(ClazzLogListAttendanceTestContext) -> Unit
    ) {
        testViewModel {
            val activePerson = setActiveUser(endpoint)
            val clazz = Clazz().apply {
                clazzTimeZone = "UTC"
            }

            val context = activeDb.withDoorTransactionAsync {
                clazz.clazzUid = CreateNewClazzUseCase(activeDb).invoke(clazz)


                activeDb.takeIf { grantAttendancePermission }?.coursePermissionDao()?.upsertAsync(
                    CoursePermission(
                        cpToPersonUid = activePerson.personUid,
                        cpClazzUid = clazz.clazzUid,
                        cpPermissionsFlag = CoursePermission.TEACHER_DEFAULT_PERMISSIONS
                    )
                )

                activeDb.takeIf { addExistingLog }?.clazzLogDao()?.insertAsync(
                    ClazzLog().apply {
                        clazzLogClazzUid = clazz.clazzUid
                        logDate = systemTimeInMillis()
                        clazzLogUid = generateUid()
                    }
                )

                ClazzLogListAttendanceTestContext(clazz, activePerson)
            }

            block(context)
        }
    }

    @Test
    fun givenExistingLogsAndUserHasRecordAttendancePermission_whenInitiated_thenOptionsIncludeCreateNewAndUpdate(){
        testClazzLogListAttendanceViewModel(
            addExistingLog = true,
            grantAttendancePermission = true
        ) { testContext ->
            viewModelFactory {
                savedStateHandle[UstadView.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzLogListAttendanceViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.recordAttendanceOptions.size == 2 && it.clazzLogsList() !is EmptyPagingSource
                }

                assertTrue(
                    ClazzLogListAttendanceViewModel.RecordAttendanceOption.RECORD_ATTENDANCE_NEW_SCHEDULE in
                    readyState.recordAttendanceOptions
                )
                assertTrue(
                    ClazzLogListAttendanceViewModel.RecordAttendanceOption.RECORD_ATTENDANCE_MOST_RECENT_SCHEDULE in
                        readyState.recordAttendanceOptions
                )
                assertEquals(2, readyState.recordAttendanceOptions.size)
                assertTrue(readyState.clazzLogsList().loadFirstList().isNotEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenNoExistingLogsAndUserHasRecordAttendancePermission_whenInitiated_thenOptionIsToCreateNew() {
        testClazzLogListAttendanceViewModel(
            addExistingLog = false,
            grantAttendancePermission = true
        ) { testContext ->
            viewModelFactory {
                savedStateHandle[UstadView.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzLogListAttendanceViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.recordAttendanceOptions.size == 1 && it.clazzLogsList() !is EmptyPagingSource
                }

                assertTrue(
                    ClazzLogListAttendanceViewModel.RecordAttendanceOption.RECORD_ATTENDANCE_NEW_SCHEDULE in
                        readyState.recordAttendanceOptions
                )

                assertEquals(1, readyState.recordAttendanceOptions.size)
                assertTrue(readyState.clazzLogsList().loadFirstList().isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenUserDoesNotHaveRecordAttendancePermission_whenInitiated_thenNoRecordAttendanceOptions() {
        testClazzLogListAttendanceViewModel(
            addExistingLog = true,
            grantAttendancePermission = false
        ) { testContext ->
            viewModelFactory {
                savedStateHandle[UstadView.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzLogListAttendanceViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.recordAttendanceOptions.isEmpty() && it.clazzLogsList() !is EmptyPagingSource
                }

                assertEquals(0, readyState.recordAttendanceOptions.size)
                assertTrue(readyState.clazzLogsList().loadFirstList().isNotEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

}