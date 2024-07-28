package com.ustadmobile.core.viewmodel.clazzassignment.peerreviewerallocationedit

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.test.viewmodeltest.ViewModelTestBuilder
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.flow.filter
import kotlinx.datetime.TimeZone
import kotlinx.serialization.builtins.ListSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class PeerReviewerAllocationEditViewModelTest : AbstractMainDispatcherTest() {

    val endpoint = Endpoint("https://app.test.com/")

    class PeerReviewerAllocationEditViewModelTestContext(
        val clazz: Clazz,
        val activePerson: Person,
        val studentPersons: List<Person>,
    )

    private fun testPeerReviewerAllocationEditViewModel(
        numStudentsToAdd: Int = 10,
        block: suspend ViewModelTestBuilder<PeerReviewerAllocationEditViewModel>.(PeerReviewerAllocationEditViewModelTestContext) -> Unit
    ) {
        testViewModel<PeerReviewerAllocationEditViewModel> {
            val activeUserPerson = setActiveUser(endpoint)

            val context = activeDb.withDoorTransactionAsync {
                val clazzUid = activeDb.doorPrimaryKeyManager.nextId(Clazz.TABLE_ID)
                val clazz = Clazz().apply {
                    this.clazzUid = clazzUid
                    clazzName = "Test Course"
                    clazzTimeZone = TimeZone.currentSystemDefault().id
                }

                CreateNewClazzUseCase(activeDb).invoke(clazz)
                activeDb.coursePermissionDao().upsertAsync(
                    CoursePermission(
                        cpToPersonUid = activeUserPerson.personUid,
                        cpClazzUid = clazzUid,
                        cpPermissionsFlag = CoursePermission.TEACHER_DEFAULT_PERMISSIONS
                    )
                )

                val enrolUseCase = EnrolIntoCourseUseCase(db = activeDb, repo = null)
                val addNewPersonUseCase = AddNewPersonUseCase(activeDb, null)
                val studentPersons = (0 until numStudentsToAdd).map { index ->
                    val studentPerson = Person().apply {
                        firstNames = "test"
                        lastName = "student${index}"
                    }
                    studentPerson.personUid = addNewPersonUseCase(studentPerson)

                    enrolUseCase(
                        enrolment = ClazzEnrolment(
                            clazzUid = clazzUid,
                            personUid = studentPerson.personUid,
                            role = ClazzEnrolment.ROLE_STUDENT
                        ),
                        timeZoneId = TimeZone.currentSystemDefault().id,
                    )
                    studentPerson
                }

                PeerReviewerAllocationEditViewModelTestContext(
                    clazz, activeUserPerson, studentPersons
                )
            }

            block(context)
        }
    }

    @Test
    fun givenCourseWithSubmittersAndNoExistingAllocations_whenInitialized_thenShouldLoadSubmittersAndSetUid() {
        val assignmentUid = 42L
        val numStudents = 12
        val numReviewers = 2
        testPeerReviewerAllocationEditViewModel(numStudentsToAdd = numStudents) { testContext ->
            viewModelFactory {
                savedStateHandle[ARG_CLAZZUID] =  testContext.clazz.clazzUid.toString()
                savedStateHandle[PeerReviewerAllocationEditViewModel.ARG_NUM_REVIEWERS_PER_SUBMITTER] =
                    numReviewers.toString()
                savedStateHandle[ARG_CLAZZ_ASSIGNMENT_UID] = assignmentUid.toString()
                PeerReviewerAllocationEditViewModel(di, savedStateHandle)
            }


            viewModel.uiState
                .filter { it.submitterListWithAllocations.size == numStudents }
                .test(timeout = 5.seconds) {
                    val uiState = awaitItem()
                    assertTrue(uiState.submitterListWithAllocations.all { submitterAndAllocations ->
                        submitterAndAllocations.allocations.size == numReviewers &&
                                submitterAndAllocations.allocations.all {
                                    it.praToMarkerSubmitterUid == submitterAndAllocations.submitter.submitterUid &&
                                            it.praAssignmentUid == assignmentUid &&
                                            it.praUid != 0L
                                }
                    })
                    cancelAndIgnoreRemainingEvents()
                }
        }
    }

    @Test
    fun givenCourseWithSubmittersAndExistingAllocations_whenInitialized_thenShouldLoadSubmittersAndSetAllocations() {
        val assignmentUid = 42L
        val numStudents = 10
        val numReviewers = 2

        testPeerReviewerAllocationEditViewModel(numStudentsToAdd = numStudents) { testContext ->
            val existingAllocations = listOf(
                PeerReviewerAllocation(
                    praUid = 1L,
                    praAssignmentUid = assignmentUid,
                    praToMarkerSubmitterUid = testContext.studentPersons.first().personUid,
                    praMarkerSubmitterUid = testContext.studentPersons[1].personUid,
                ),
                PeerReviewerAllocation(
                    praUid = 2L,
                    praAssignmentUid = assignmentUid,
                    praToMarkerSubmitterUid = testContext.studentPersons.first().personUid,
                    praMarkerSubmitterUid = testContext.studentPersons[2].personUid,
                ),
            )

            viewModelFactory {
                savedStateHandle[ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                savedStateHandle[PeerReviewerAllocationEditViewModel.ARG_NUM_REVIEWERS_PER_SUBMITTER] =
                    numReviewers.toString()
                savedStateHandle[ARG_CLAZZ_ASSIGNMENT_UID] = assignmentUid.toString()
                savedStateHandle[PeerReviewerAllocationEditViewModel.ARG_ALLOCATIONS] = json.encodeToString(
                    serializer = ListSerializer(PeerReviewerAllocation.serializer()),
                    value = existingAllocations
                )
                PeerReviewerAllocationEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState
                .filter { it.submitterListWithAllocations.size == numStudents }
                .test(timeout = 5.seconds) {
                    val uiState = awaitItem()
                    assertTrue(uiState.submitterListWithAllocations.all { submitterAndAllocations ->
                        submitterAndAllocations.allocations.size == numReviewers &&
                                submitterAndAllocations.allocations.all {
                                    it.praToMarkerSubmitterUid == submitterAndAllocations.submitter.submitterUid &&
                                            it.praAssignmentUid == assignmentUid &&
                                            it.praUid != 0L
                                }
                    })

                    val firstStudentAllocations = uiState.submitterListWithAllocations.first()
                    assertEquals(testContext.studentPersons[1].personUid,
                        firstStudentAllocations.allocations[0].praMarkerSubmitterUid)
                    assertEquals(testContext.studentPersons[2].personUid,
                        firstStudentAllocations.allocations[1].praMarkerSubmitterUid)
                    cancelAndIgnoreRemainingEvents()
                }
        }
    }


}