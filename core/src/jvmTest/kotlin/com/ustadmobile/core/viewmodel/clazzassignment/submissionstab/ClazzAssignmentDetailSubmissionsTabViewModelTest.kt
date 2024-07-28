package com.ustadmobile.core.viewmodel.clazzassignment.submissionstab

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.test.viewmodeltest.ViewModelTestBuilder
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.loadFirstList
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import com.ustadmobile.lib.db.entities.Person
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class ClazzAssignmentDetailSubmissionsTabViewModelTest : AbstractMainDispatcherTest()  {

    private val studentNameList = listOf(
        "Bart Simpson",
        "Nelson Muntz",
        "Martin Prince",
        "Millhouse Van Houten",
        "Sherri Mackleberry",
        "Terri Mackleberry"
    )

    val endpoint = Endpoint("https://test.com/")

    private class TestContext(
        val activeUserPerson: Person,
        val clazz: Clazz,
        val students: List<Person>,
    )

    private fun testClazzAssignmentDetailSubmissionsTabViewModel(
        block: suspend ViewModelTestBuilder<ClazzAssignmentDetailSubmissionsTabViewModel>.(TestContext) -> Unit
    ) {
        testViewModel<ClazzAssignmentDetailSubmissionsTabViewModel> {
            val activeUser = setActiveUser(endpoint)
            val testContext = activeDb.withDoorTransactionAsync {
                val clazz = Clazz().apply {
                    clazzName = "Test clazz"
                    clazzTimeZone = "UTC"
                }

                clazz.clazzUid = CreateNewClazzUseCase(activeDb).invoke(clazz)

                val enrolUseCase = EnrolIntoCourseUseCase(activeDb, null)
                val addPersonuseCase = AddNewPersonUseCase(activeDb, null)

                val students = studentNameList.map {name ->
                    val studentPerson = Person().apply {
                        firstNames = name.substringBefore(" ")
                        lastName = name.substringAfter(" ")
                    }

                    studentPerson.personUid = addPersonuseCase(studentPerson)

                    enrolUseCase(
                        enrolment = ClazzEnrolment(
                            clazzUid = clazz.clazzUid,
                            personUid = studentPerson.personUid
                        ).apply {
                            clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT
                        },
                        timeZoneId = "UTC"
                    )

                    studentPerson
                }

                TestContext(activeUser, clazz, students)
            }

            block(testContext)
        }
    }

    @Test
    fun givenIndividualSubmissionAssignmentAndUserIsTeacher_whenInitiated_thenWillShowEnrolledStudentNamesAndSummaryTotals() {
        testClazzAssignmentDetailSubmissionsTabViewModel { testContext ->
            val assignment = ClazzAssignment().apply {
                caMarkingType = ClazzAssignment.MARKED_BY_COURSE_LEADER
                caGroupUid = 0
                caClazzUid = testContext.clazz.clazzUid
                caUid = activeDb.clazzAssignmentDao().insertAsync(this)
            }

            val enrolIntoCourseUseCase = EnrolIntoCourseUseCase(activeDb, null)

            activeDb.withDoorTransactionAsync {
                enrolIntoCourseUseCase(
                    enrolment = ClazzEnrolment(
                        clazzUid = testContext.clazz.clazzUid,
                        personUid = testContext.activeUserPerson.personUid,
                    ).apply {
                        clazzEnrolmentRole = ClazzEnrolment.ROLE_TEACHER
                    },
                    timeZoneId = "UTC",
                )


                testContext.students.forEachIndexed { index, student ->
                    //put in submission for 2/3 of students
                    if(index % 3 == 1  || index % 3 == 2) {
                        activeDb.courseAssignmentSubmissionDao().insertAsync(CourseAssignmentSubmission().apply {
                            casTimestamp = systemTimeInMillis()
                            casSubmitterUid = student.personUid
                            casSubmitterPersonUid = student.personUid
                            casAssignmentUid = assignment.caUid
                            casText = "My bad submission"
                        })
                    }

                    //mark 1/3 of students
                    if(index % 3 == 2) {
                        activeDb.courseAssignmentMarkDao().insertAsync(CourseAssignmentMark().apply {
                            camMark = 1.toFloat()
                            camSubmitterUid = student.personUid
                            camMarkerPersonUid = testContext.activeUserPerson.personUid
                            camAssignmentUid = assignment.caUid
                        })
                    }
                }
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = assignment.caUid.toString()
                savedStateHandle[UstadViewModel.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                ClazzAssignmentDetailSubmissionsTabViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 10.seconds) {
                val readyState = awaitItemWhere {
                    it.assignmentSubmitterList() !is EmptyPagingSource && it.progressSummary != null
                }
                assertEquals(studentNameList.size, readyState.progressSummary?.totalStudents)
                assertEquals(4, readyState.progressSummary?.submittedStudents)
                assertEquals(2, readyState.progressSummary?.markedStudents)

                val studentSubmissionSummaries = readyState.assignmentSubmitterList().loadFirstList()
                testContext.students.forEachIndexed { index, student ->
                    val studentSummary = studentSubmissionSummaries.single { it.name == student.fullName() }
                    val expectedStatus = when(index % 3) {
                        0 -> CourseAssignmentSubmission.NOT_SUBMITTED
                        1 -> CourseAssignmentSubmission.SUBMITTED
                        2 -> CourseAssignmentSubmission.MARKED
                        else -> throw IllegalStateException("Remainder cannot be anything else...")
                    }
                    assertEquals(expectedStatus, studentSummary.fileSubmissionStatus)
                }
            }
        }
    }
}