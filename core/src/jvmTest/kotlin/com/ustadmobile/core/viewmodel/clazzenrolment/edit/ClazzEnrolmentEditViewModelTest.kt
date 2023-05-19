package com.ustadmobile.core.viewmodel.clazzenrolment.edit

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.MAX_VALID_DATE
import com.ustadmobile.core.test.viewmodeltest.ViewModelTestBuilder
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class ClazzEnrolmentEditViewModelTest {

    val endpoint = Endpoint("https://app.test.com/")

    @Suppress("unused")
    class ClazzEnrolmentEditTestContext(
        val clazz: Clazz,
        val activeUserPerson: Person,
        val personToEnrol: Person,
    )

    private fun testClazzEnrolmentEditViewModel(
        canAddTeacher: Boolean = true,
        canAddStudent: Boolean = true,
        personToEnrol: Person = Person().apply {
            firstNames = "Person"
            lastName = "ToAdd"
        },
        block: suspend ViewModelTestBuilder<ClazzEnrolmentEditViewModel>.(ClazzEnrolmentEditTestContext) -> Unit
    ) {
        testViewModel {
            val activeUserPerson = setActiveUser(endpoint)

            val context = activeDb.withDoorTransactionAsync {
                val clazzUid = activeDb.doorPrimaryKeyManager.nextId(Clazz.TABLE_ID)
                val clazz = Clazz().apply {
                    this.clazzUid = clazzUid
                    clazzName = "Test Course"
                    clazzTimeZone = TimeZone.currentSystemDefault().id
                }
                activeDb.createNewClazzAndGroups(clazz, systemImpl, emptyMap())

                if(canAddTeacher)
                    activeDb.grantScopedPermission(activeUserPerson, Role.PERMISSION_CLAZZ_ADD_TEACHER,
                        Clazz.TABLE_ID, clazz.clazzUid)

                if(canAddStudent)
                    activeDb.grantScopedPermission(activeUserPerson, Role.PERMISSION_CLAZZ_ADD_STUDENT,
                        Clazz.TABLE_ID, clazz.clazzUid)

                activeDb.insertPersonAndGroup(personToEnrol)

                ClazzEnrolmentEditTestContext(clazz, activeUserPerson, personToEnrol)
            }

            block(context)
        }
    }

    @Test
    fun givenNoExistingEntity_whenInitializedAndOnClickSaveCalled_thenShouldSaveToDatabase() {
        testClazzEnrolmentEditViewModel { testContext ->
            viewModelFactory {
                savedStateHandle[UstadView.ARG_PERSON_UID] = testContext.personToEnrol.personUid.toString()
                savedStateHandle[UstadView.ARG_CLAZZUID] = testContext.clazz.clazzUid.toString()


                ClazzEnrolmentEditViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere { it.fieldsEnabled }
                assertTrue(ClazzEnrolment.ROLE_STUDENT in readyState.roleOptions)
                assertTrue(ClazzEnrolment.ROLE_TEACHER in readyState.roleOptions)
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.onClickSave()

            activeDb.clazzEnrolmentDao.findAllClazzesByPersonWithClazz(
                testContext.personToEnrol.personUid
            ).assertItemReceived {
                it.isNotEmpty() &&
                    it.first().clazzEnrolmentClazzUid == testContext.clazz.clazzUid &&
                    it.first().clazzEnrolmentPersonUid == testContext.personToEnrol.personUid
            }
        }
    }

    @Test
    fun givenExistingLeavingReason_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        testClazzEnrolmentEditViewModel {testContext ->
            val enrolment = activeDb.enrolPersonIntoClazzAtLocalTimezone(
                testContext.personToEnrol, testContext.clazz.clazzUid, ClazzEnrolment.ROLE_STUDENT
            )

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = enrolment.clazzEnrolmentUid.toString()
                ClazzEnrolmentEditViewModel(di, savedStateHandle)
            }

            val leaveTime = Clock.System.now().plus(10.days).toEpochMilliseconds()

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere { it.fieldsEnabled }

                viewModel.onEntityChanged(readyState.clazzEnrolment?.shallowCopy {
                    clazzEnrolmentDateLeft = leaveTime
                })
                viewModel.onClickSave()
                cancelAndIgnoreRemainingEvents()
            }

            activeDb.clazzEnrolmentDao.findAllClazzesByPersonWithClazz(
                testContext.personToEnrol.personUid
            ).assertItemReceived { enrolments ->
                enrolments.any {
                    it.clazzEnrolmentDateLeft in leaveTime until MAX_VALID_DATE
                }
            }
        }
    }

}