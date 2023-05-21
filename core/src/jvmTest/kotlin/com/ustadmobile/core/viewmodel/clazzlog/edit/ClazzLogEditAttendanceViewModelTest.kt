package com.ustadmobile.core.viewmodel.clazzlog.edit

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.schedule.generateUid
import com.ustadmobile.core.test.viewmodeltest.ViewModelTestBuilder
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceViewModel
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.flow.doorFlow
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
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
        val clazz: Clazz,
        val enroledPersons: List<Person>,
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

                val enroledPersons = namesToEnrol.map {name ->
                    val studentPerson = activeDb.insertPersonAndGroup(Person().apply {
                        firstNames = name.split(" ").first()
                        lastName = name.split(" ").last()
                    })
                    activeDb.enrolPersonIntoClazzAtLocalTimezone(studentPerson, clazz.clazzUid,
                        ClazzEnrolment.ROLE_STUDENT)

                    studentPerson
                }

                ClazzLogEditAttendanceViewModelTestContext(person, clazz, enroledPersons)
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
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenNewClazzLogSpecifiedAndPreviousClazzLogExists_whenGoPreviousSelected_thenShouldShowPreviouslyRecordedLogs() {
        testClazzLogEditAttendanceView { testContext ->
            activeDb.withDoorTransactionAsync {
                val clazzLog = ClazzLog().apply {
                    logDate = systemTimeInMillis() - 1000
                    clazzLogClazzUid = testContext.clazz.clazzUid
                    clazzLogNumPresent = testContext.enroledPersons.size
                    clazzLogUid = generateUid()
                }
                activeDb.clazzLogDao.insertAsync(clazzLog)
                activeDb.clazzLogAttendanceRecordDao.insertListAsync(
                    testContext.enroledPersons.map {
                        ClazzLogAttendanceRecord().apply {
                            clazzLogAttendanceRecordClazzLogUid = clazzLog.clazzLogUid
                            clazzLogAttendanceRecordPersonUid = it.personUid
                            attendanceStatus = ClazzLogAttendanceRecord.STATUS_ATTENDED
                        }
                    }
                )

                clazzLog
            }

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

                viewModel.onChangeClazzLog(readyState.clazzLogsList.first())

                val previousLogState = awaitItemWhere { state ->
                    state.clazzLogAttendanceRecordList.all {
                        it.attendanceRecord?.attendanceStatus == ClazzLogAttendanceRecord.STATUS_ATTENDED
                    }
                }

                assertTrue(readyState.clazzLogAttendanceRecordList.all {
                    it.attendanceRecord?.attendanceStatus == 0
                })

                assertEquals(2, readyState.clazzLogsList.size)
                defaultEnroledNames.forEach { name ->
                    assertTrue(previousLogState.clazzLogAttendanceRecordList.any {
                        it.person?.firstNames == name.split(" ").first() &&
                            it.attendanceRecord?.attendanceStatus == ClazzLogAttendanceRecord.STATUS_ATTENDED
                    })
                }

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenEntityArgUidSpecified_whenCreated_thenShouldShowClazzLogAndEnrolledStudents() {
        testClazzLogEditAttendanceView { testContext ->
            val existingClazzLog = activeDb.withDoorTransactionAsync {
                val clazzLog = ClazzLog().apply {
                    logDate = systemTimeInMillis() - 1000
                    clazzLogClazzUid = testContext.clazz.clazzUid
                    clazzLogNumPresent = testContext.enroledPersons.size
                    clazzLogUid = generateUid()
                }

                activeDb.clazzLogDao.insertAsync(clazzLog)
                clazzLog
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = existingClazzLog.clazzLogUid.toString()
                ClazzLogEditAttendanceViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.clazzLogsList.isNotEmpty() && it.clazzLogAttendanceRecordList.isNotEmpty()
                }

                assertEquals(existingClazzLog.clazzLogUid, readyState.clazzLogsList.first().clazzLogUid)
                defaultEnroledNames.forEach { name ->
                    assertTrue(readyState.clazzLogAttendanceRecordList.any {
                        it.person?.firstNames == name.split(" ").first()
                    })
                }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenNewClazzLogSpecified_whenStatusUpdatedAndSaveClicked_thenShouldSaveIntoDatabase() {
        testClazzLogEditAttendanceView { testContext ->
            val existingClazzLog = activeDb.withDoorTransactionAsync {
                val clazzLog = ClazzLog().apply {
                    logDate = systemTimeInMillis() - 1000
                    clazzLogClazzUid = testContext.clazz.clazzUid
                    clazzLogNumPresent = testContext.enroledPersons.size
                    clazzLogUid = generateUid()
                }

                activeDb.clazzLogDao.insertAsync(clazzLog)
                clazzLog
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = existingClazzLog.clazzLogUid.toString()
                ClazzLogEditAttendanceViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                awaitItemWhere {
                    it.clazzLogsList.isNotEmpty() && it.clazzLogAttendanceRecordList.isNotEmpty()
                }

                viewModel.onClickMarkAll(ClazzLogAttendanceRecord.STATUS_ATTENDED)

                viewModel.onClickSave()
                cancelAndIgnoreRemainingEvents()
            }

            activeDb.doorFlow(arrayOf("ClazzLogAttendanceRecord")) {
                activeDb.clazzLogAttendanceRecordDao.findByClazzLogUid(existingClazzLog.clazzLogUid)
            }.assertItemReceived(timeout = 5.seconds) { attendanceList ->
                attendanceList.size == testContext.enroledPersons.size &&
                    attendanceList.all { it.attendanceStatus == ClazzLogAttendanceRecord.STATUS_ATTENDED }
            }
        }
    }

}