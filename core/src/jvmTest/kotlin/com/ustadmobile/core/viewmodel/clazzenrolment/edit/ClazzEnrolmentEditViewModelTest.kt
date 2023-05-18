package com.ustadmobile.core.viewmodel.clazzenrolment.edit

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.test.viewmodeltest.ViewModelTestBuilder
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.flow.doorFlow
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ClazzEnrolmentEditViewModelTest {

    val endpoint = Endpoint("https://app.test.com/")

    class ClazzEnrolmentEditTestContext(
        val clazz: Clazz,
        val activeUserPerson: Person,
        val personToEnrol: Person,
    )

    fun testClazzEnrolmentEditViewModel(
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
                    clazzTimeZone = "Asia/Dubai"
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
    fun givenNoExistingEntity_whenOnCreateAndHandleClickSaveCalled_thenShouldSaveToDatabase() {
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

}