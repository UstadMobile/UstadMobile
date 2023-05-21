package com.ustadmobile.core.viewmodel.clazzlog.edit

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.schedule.generateUid
import com.ustadmobile.core.test.viewmodeltest.ViewModelTestBuilder
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceViewModel
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.Person
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ClazzLogEditAttendanceViewModelTest {

    private val testEndpoint = Endpoint("https://test.com/")

    private val defaultEnroledNames = listOf("Bart Simpson", "Nelson Muntz", "Shelly Mackleberry",
        "Terri Mackleberry")

    data class ClazzLogEditAttendanceViewModelTestContext(
        val activeUser: Person,
        val clazz: Clazz
    )

    private fun testClazzLogEditAttendanceView(
        namesToEnrol: List<String> = defaultEnroledNames,
        block: suspend ViewModelTestBuilder<ClazzLogEditAttendanceViewModel>.(ClazzLogEditAttendanceViewModelTestContext) -> Unit
    ) {
        testViewModel {
            val person = setActiveUser(testEndpoint)
            val context = activeDb.withDoorTransactionAsync {
                val clazz = Clazz().apply {
                    clazzName = "Test"
                    clazzTimeZone = "UTC"
                }

                activeDb.createNewClazzAndGroups(clazz, systemImpl, emptyMap())

                namesToEnrol.forEach {name ->
                    val studentPerson = activeDb.insertPersonAndGroup(Person().apply {
                        firstNames = name.split(" ").first()
                        lastName = name.split(" ").last()
                    })
                    activeDb.enrolPersonIntoClazzAtLocalTimezone(studentPerson, clazz.clazzUid,
                        ClazzEnrolment.ROLE_STUDENT)
                }

                ClazzLogEditAttendanceViewModelTestContext(person, clazz)
            }

            block(context)
        }
    }

    @Test
    fun givenNewClazzLogSpecified_whenInitiated_thenShouldShowClazzLogAndEnrolledStudents() {
        testClazzLogEditAttendanceView {testContext ->
            val newClazzLog = ClazzLog().apply {
                clazzLogClazzUid = testContext.clazz.clazzUid
                logDate = systemTimeInMillis()
                clazzLogUid = generateUid()
            }

            viewModelFactory {
                savedStateHandle[ClazzLogEditAttendanceViewModel.ARG_NEW_CLAZZLOG] = json.encodeToString(
                    serializer = ClazzLog.serializer(),
                    value = newClazzLog
                )

                ClazzLogEditAttendanceViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 10.seconds) {
                val readyState = awaitItemWhere {
                    it.clazzLogsList.isNotEmpty() && it.clazzLogAttendanceRecordList.isNotEmpty()
                }

                assertEquals(newClazzLog.clazzLogUid, readyState.clazzLogsList.first().clazzLogUid)
                defaultEnroledNames.forEach { name ->
                    assertTrue(readyState.clazzLogAttendanceRecordList.any {
                        it.person?.firstNames == name.split(" ").first()
                    })
                }
            }
        }
    }

    fun givenNewClazzLogSpecifiedAndPreviousClazzLogExists_whenGoPreviousSelected_thenShouldShowPreviouslyRecordedLogs() {

    }

    fun givenEntityArgUidSpecified_whenCreated_thenShouldShowClazzLogAndEnrolledStudents() {

    }

    fun givenNewClazzLogSpecified_whenStatusUpdatedAndSaveClicked_thenShouldSaveIntoDatabase() {

    }

}