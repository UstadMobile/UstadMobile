package com.ustadmobile.core.viewmodel.coursegroupset.edit

import app.cash.turbine.test
import com.ustadmobile.core.test.viewmodeltest.ViewModelTestBuilder
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.flow.doorFlow
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class CourseGroupSetEditViewModelTest : AbstractMainDispatcherTest()  {


    private data class TestContext(
        val clazz: Clazz,
        val enrolledPeople: List<Person>,
    )

    private val defaultNamesToEnrol = listOf(
        "Bart Simpson",
        "Nelzon Muntz",
        "Sherry Mackleberry",
        "Terry Mackleberry",
        "Milhouse VanHouten"
    )

    private fun testCourseGroupSetEditViewModel(
        namesToEnrol: List<String> = defaultNamesToEnrol,
        block: suspend ViewModelTestBuilder<CourseGroupSetEditViewModel>.(TestContext) -> Unit
    ) {
        testViewModel {
            val clazz = Clazz().apply {
                clazzName = "Test"
            }

            val testContext = activeDb.withDoorTransactionAsync {
                activeDb.createNewClazzAndGroups(clazz, systemImpl, emptyMap())
                val enrolledPeople = namesToEnrol.map { name ->
                    val enrolledPerson = activeDb.insertPersonAndGroup(Person().apply {
                        firstNames = name.substringBefore(" ")
                        lastName = name.substringAfter(" ")
                    })

                    activeDb.enrolPersonIntoClazzAtLocalTimezone(
                        personToEnrol = enrolledPerson,
                        clazzUid = clazz.clazzUid,
                        role = ClazzEnrolment.ROLE_STUDENT
                    )

                    enrolledPerson

                }
                TestContext(clazz, enrolledPeople)
            }

            block(testContext)
        }
    }

    @Test
    fun givenNewGroupSetBeingCreated_whenInitiatedAssignedAndSaveClicked_thenShouldShowEnrolledStudentNamesAndDefaultGroupNumAndSaveToDatabase() {
        testCourseGroupSetEditViewModel { testContext ->
            viewModelFactory {
                savedStateHandle[UstadView.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()
                CourseGroupSetEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere {
                    it.fieldsEnabled
                }

                viewModel.onEntityChanged(readyState.courseGroupSet?.shallowCopy {
                    cgsName = "Assignment groups"
                    cgsTotalGroups = 2
                })

                testContext.enrolledPeople.forEachIndexed { index, person ->
                    viewModel.onChangeGroupAssignment(person.personUid, index % 2)
                }

                viewModel.onClickSave()

                assertEquals(defaultNamesToEnrol.size, readyState.membersList.size)
                testContext.enrolledPeople.forEach {person ->
                    assertTrue(readyState.membersList.any {
                        it.name == person.fullName() &&
                            it.personUid == person.personUid &&
                            it.cgm?.cgmPersonUid == person.personUid
                    })
                }

                var courseGroupSetUid = 0L
                activeDb.doorFlow(arrayOf("CourseGroupSet")) {
                    activeDb.courseGroupSetDao.findAllCourseGroupSetForClazzListAsync(
                        testContext.clazz.clazzUid
                    )
                }.test(timeout = 5.seconds) {
                    val groupSetReady = awaitItemWhere { it.isNotEmpty() }
                    courseGroupSetUid = groupSetReady.first().cgsUid
                    cancelAndIgnoreRemainingEvents()
                }

                val members = activeDb.courseGroupMemberDao.findByCourseGroupSetAndClazz(
                    cgsUid = courseGroupSetUid,
                    clazzUid = testContext.clazz.clazzUid,
                    time = systemTimeInMillis(),
                    activeFilter = 0,
                )

                testContext.enrolledPeople.forEachIndexed { index, person ->
                    val courseGroupMember = members.first { it.personUid == person.personUid }
                    assertEquals(index % 2, courseGroupMember.cgm?.cgmGroupNumber)
                    assertEquals(courseGroupSetUid, courseGroupMember.cgm?.cgmSetUid)
                }

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun givenExistingGroupSet_whenInitiatedUpdatedAndSaved_thenWillShowExistingAssignmentsAndUpdateToDatabase(){

    }

}