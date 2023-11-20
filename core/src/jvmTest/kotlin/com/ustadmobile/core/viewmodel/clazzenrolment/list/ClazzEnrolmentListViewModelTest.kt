package com.ustadmobile.core.viewmodel.clazzenrolment.list

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ClazzEnrolmentListViewModelTest : AbstractMainDispatcherTest()  {


    //Test should be enabled after rework of doorFlow
    //@Test
    fun givenExistingEnrolments_whenInitialized_thenWillSetListNameAndEditPermissions() {
        testViewModel<ClazzEnrolmentListViewModel> {
            val endpoint = Endpoint("https://test.com/")
            val activeUserPerson = setActiveUser(endpoint)
            val (enroledPerson, clazz) = activeDb.withDoorTransactionAsync {
                val person = activeDb.insertPersonAndGroup(Person().apply {
                    firstNames = "Test"
                    lastName = "User"
                })

                val clazz = Clazz().apply {
                    clazzName = "Test Clazz"
                    clazzTimeZone = "UTC"
                }
                activeDb.createNewClazzAndGroups(clazz, systemImpl, emptyMap())

                activeDb.grantScopedPermission(activeUserPerson,
                    Role.ALL_PERMISSIONS, Clazz.TABLE_ID, clazz.clazzUid)
                activeDb.enrolPersonIntoClazzAtLocalTimezone(person, clazz.clazzUid,
                    ClazzEnrolment.ROLE_STUDENT)

                Pair(person, clazz)
            }

            delay(1000)

            viewModelFactory {
                savedStateHandle[UstadView.ARG_PERSON_UID] = enroledPerson.personUid.toString()
                savedStateHandle[UstadView.ARG_CLAZZUID] = enroledPerson.personUid.toString()
                ClazzEnrolmentListViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test(timeout = 10.seconds) {
                //Should not be needed, related to doorflow
                delay(3000)

                val readyState = awaitItemWhere {
                    it.enrolmentList.isNotEmpty() &&
                        it.canEditTeacherEnrolments &&
                        it.canEditStudentEnrolments &&
                        it.personName != null
                }

                assertEquals(clazz.clazzUid, readyState.enrolmentList.first().clazzEnrolmentClazzUid)
                assertTrue(readyState.canEditTeacherEnrolments)
                assertTrue(readyState.canEditStudentEnrolments)
                assertEquals(clazz.clazzName, readyState.courseName)
                assertEquals("Test User", readyState.personName)
                cancelAndIgnoreRemainingEvents()
            }

        }
    }

}