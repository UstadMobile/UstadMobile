package com.ustadmobile.core.viewmodel.clazzenrolment.edit

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.MAX_VALID_DATE
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.test.viewmodeltest.ViewModelTestBuilder
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CoursePermission
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.SystemPermission
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class ClazzEnrolmentEditViewModelTest : AbstractMainDispatcherTest()  {

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
            extendDi {
                bind<EnrolIntoCourseUseCase>() with scoped(endpointScope).singleton {
                    EnrolIntoCourseUseCase(
                        db = instance(tag = DoorTag.TAG_DB),
                        repo = instance(tag = DoorTag.TAG_REPO),
                    )
                }
            }

            val activeUserPerson = setActiveUser(endpoint)

            val context = activeDb.withDoorTransactionAsync {
                val clazzUid = activeDb.doorPrimaryKeyManager.nextId(Clazz.TABLE_ID)
                val clazz = Clazz().apply {
                    this.clazzUid = clazzUid
                    clazzName = "Test Course"
                    clazzTimeZone = TimeZone.currentSystemDefault().id
                }

                CreateNewClazzUseCase(activeDb).invoke(clazz)


                if(canAddTeacher)
                    activeDb.coursePermissionDao().upsertAsync(
                        CoursePermission(
                            cpClazzUid = clazz.clazzUid,
                            cpToPersonUid = activeUserPerson.personUid,
                            cpPermissionsFlag = PermissionFlags.COURSE_MANAGE_TEACHER_ENROLMENT,
                        )
                    )

                if(canAddStudent)
                    activeDb.coursePermissionDao().upsertAsync(
                        CoursePermission(
                            cpClazzUid = clazz.clazzUid,
                            cpToPersonUid = activeUserPerson.personUid,
                            cpPermissionsFlag = PermissionFlags.COURSE_MANAGE_STUDENT_ENROLMENT,
                        )
                    )

                if(canAddTeacher || canAddStudent) {
                    activeDb.systemPermissionDao().upsertAsync(
                        SystemPermission(
                            spToPersonUid = activeUserPerson.personUid,
                            spPermissionsFlag = PermissionFlags.DIRECT_ENROL,
                        )
                    )
                }


                val addPersonUseCase: AddNewPersonUseCase = di.onActiveEndpoint().direct.instance()
                addPersonUseCase(personToEnrol)

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

            val readyAppUiState = withTimeout(5000){
                viewModel.appUiState.filter { it.actionBarButtonState.visible }.first()
            }

            viewModel.uiState.test(timeout = 5.seconds, name = "found readystate") {
                val readyState = awaitItemWhere { it.fieldsEnabled }
                assertTrue(ClazzEnrolment.ROLE_STUDENT in readyState.roleOptions)
                assertTrue(ClazzEnrolment.ROLE_TEACHER in readyState.roleOptions)
                cancelAndIgnoreRemainingEvents()
            }

            readyAppUiState.actionBarButtonState.onClick()

            activeDb.clazzEnrolmentDao().findAllByPersonUid(
                testContext.personToEnrol.personUid
            ).assertItemReceived(timeout = 5.seconds, name = "found person enrolled in course") {
                it.isNotEmpty() &&
                    it.first().clazzEnrolmentClazzUid == testContext.clazz.clazzUid &&
                    it.first().clazzEnrolmentPersonUid == testContext.personToEnrol.personUid
            }
        }
    }

    @Test
    fun givenExistingLeavingReason_whenOnCreateAndHandleClickSaveCalled_thenValuesShouldBeSetOnViewAndDatabaseShouldBeUpdated() {
        testClazzEnrolmentEditViewModel {testContext ->
            val enrolUseCase = EnrolIntoCourseUseCase(activeDb, null)
            val enrolment = ClazzEnrolment(
                personUid = testContext.personToEnrol.personUid,
                clazzUid = testContext.clazz.clazzUid
            ).also {
                it.clazzEnrolmentRole = ClazzEnrolment.ROLE_STUDENT
                it.clazzEnrolmentDateJoined = Clock.System.now().minus(1.days).toEpochMilliseconds()
            }
            enrolment.clazzEnrolmentUid = enrolUseCase(enrolment, timeZoneId = "UTC")


            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = enrolment.clazzEnrolmentUid.toString()
                ClazzEnrolmentEditViewModel(di, savedStateHandle)
            }

            val leaveTime = Clock.System.now().plus(10.days).toEpochMilliseconds()

            val appStateWithButton = withTimeout(5000) {
                viewModel.appUiState.filter { it.actionBarButtonState.visible }.first()
            }

            viewModel.uiState.test(timeout = 5.seconds) {
                val readyState = awaitItemWhere { it.fieldsEnabled }

                viewModel.onEntityChanged(readyState.clazzEnrolment?.shallowCopy {
                    clazzEnrolmentDateLeft = leaveTime
                })

                appStateWithButton.actionBarButtonState.onClick()
                cancelAndIgnoreRemainingEvents()
            }

            activeDb.clazzEnrolmentDao().findAllByPersonUid(
                testContext.personToEnrol.personUid
            ).assertItemReceived(name = "enrolment date left is updated",timeout = 5.seconds) { enrolments ->
                enrolments.any {
                    it.clazzEnrolmentDateLeft in leaveTime until MAX_VALID_DATE
                }
            }
        }
    }

}