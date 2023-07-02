package com.ustadmobile.core.viewmodel.scopedgrant.detail

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import kotlin.time.Duration.Companion.seconds
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import org.junit.Test
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.enrolPersonIntoClazzAtLocalTimezone
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.Role.Companion.PERMISSION_CLAZZ_CONTENT_UPDATE
import com.ustadmobile.lib.db.entities.Role.Companion.PERMISSION_CLAZZ_UPDATE
import com.ustadmobile.lib.db.entities.ScopedGrant
import kotlinx.coroutines.delay
import org.mockito.kotlin.*

class ScopedGrantDetailViewModelTest {


    @Test
    fun givenViewModelNotYetCreated_whenInitialized_thenShouldQueryDatabase(){

        testViewModel<ScopedGrantDetailViewModel> {

            //Create Test clazz:
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

            val testScopedGrant = ScopedGrant().apply {
                sgTableId = Clazz.TABLE_ID
                sgEntityUid = clazz.clazzUid
                sgGroupUid = enroledPerson.personGroupUid
                sgPermissions = Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT or
                        Role.PERMISSION_CLAZZ_CONTENT_UPDATE
                sgUid = activeDb.scopedGrantDao.insertAsync(this)
            }

            savedStateHandle[UstadView.ARG_ENTITY_UID] = testScopedGrant.sgUid.toString()


            viewModelFactory {
                ScopedGrantDetailViewModel(di, savedStateHandle)
            }

            val scopedGrantDaoRepo = spy(activeRepo.scopedGrantDao)
            activeRepo.stub{
                on {scopedGrantDao}.thenReturn(scopedGrantDaoRepo)
            }

            val accountPersonUid = accountManager.activeAccount.personUid


            viewModel.uiState
                .test{
                    delay(250)
                    verify(
                        scopedGrantDaoRepo,
                        timeout(5000)
                    ).findByUidFlow(testScopedGrant.sgUid)

                    cancelAndIgnoreRemainingEvents()
                }

        }

    }

    @Test
    fun givenViewModelNotYetCreated_whenInitialized_thenShouldSetEntity(){

        testViewModel<ScopedGrantDetailViewModel> {

            //Create Test clazz:
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

            val testScopedGrant = ScopedGrant().apply {
                sgTableId = Clazz.TABLE_ID
                sgEntityUid = clazz.clazzUid
                sgGroupUid = enroledPerson.personGroupUid
                sgPermissions = Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT or
                        Role.PERMISSION_CLAZZ_CONTENT_UPDATE
                sgUid = activeDb.scopedGrantDao.insertAsync(this)
            }

            savedStateHandle[UstadView.ARG_ENTITY_UID] = testScopedGrant.sgUid.toString()


            viewModelFactory {
                ScopedGrantDetailViewModel(di, savedStateHandle)
            }

            val scopedGrantDaoRepo = spy(activeRepo.scopedGrantDao)
            activeRepo.stub{
                on {scopedGrantDao}.thenReturn(scopedGrantDaoRepo)
            }



            viewModel.uiState
                .assertItemReceived(timeout=5.seconds) {
                    it.scopedGrant?.sgUid == testScopedGrant.sgUid
                }

        }

    }

    @Test
    fun givenViewModelInitialised_whenEditClicked_thenShouldGoToScopedGrantEdit(){

        testViewModel<ScopedGrantDetailViewModel> {

            //Create Test clazz:
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

            val testScopedGrant = ScopedGrant().apply {
                sgTableId = Clazz.TABLE_ID
                sgEntityUid = clazz.clazzUid
                sgGroupUid = enroledPerson.personGroupUid
                sgPermissions = Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT or
                        Role.PERMISSION_CLAZZ_CONTENT_UPDATE
                sgUid = activeDb.scopedGrantDao.insertAsync(this)
            }

            savedStateHandle[UstadView.ARG_ENTITY_UID] = testScopedGrant.sgUid.toString()


            viewModelFactory {
                ScopedGrantDetailViewModel(di, savedStateHandle)
            }


            viewModel.uiState.test(timeout = 30.seconds) {
                viewModel.appUiState.test(timeout = 5000.seconds) {
                    val editButtonClickableState = awaitItemWhere { it.fabState.visible }
                    editButtonClickableState.fabState.onClick()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.navCommandFlow.assertItemReceived(timeout = 5.seconds) {
                    val cmd = it as NavigateNavCommand
                    cmd.viewName == ScopedGrantEditView.VIEW_NAME &&
                            cmd.args[UstadView.ARG_ENTITY_UID] == testScopedGrant.sgUid.toString()
                }

                cancelAndIgnoreRemainingEvents()
            }


        }

    }




    @Test
    fun givenViewModelInitialised_whenDataLoaded_thenShouldSetCorrectBitMaskList(){

        testViewModel<ScopedGrantDetailViewModel> {

            //Create Test clazz:
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

            //Insert a custom scoped grant
            val customScopedGrant = ScopedGrant().apply {
                sgTableId = Clazz.TABLE_ID
                sgEntityUid = clazz.clazzUid
                sgGroupUid = enroledPerson.personGroupUid
                sgPermissions = Role.PERMISSION_CLAZZ_UPDATE or
                        Role.PERMISSION_CLAZZ_CONTENT_UPDATE
                sgUid = activeDb.scopedGrantDao.insertAsync(this)
            }

            savedStateHandle[UstadView.ARG_ENTITY_UID] = customScopedGrant.sgUid.toString()

            viewModelFactory {
                ScopedGrantDetailViewModel(di, savedStateHandle)
            }

            viewModel.uiState
                .assertItemReceived(timeout=5.seconds) {
                    it.bitmaskList.isNotEmpty() &&
                    it.bitmaskList.size == 2 &&
                    it.bitmaskList[0].flagVal == PERMISSION_CLAZZ_UPDATE &&
                    it.bitmaskList[1].flagVal == PERMISSION_CLAZZ_CONTENT_UPDATE
                }

        }
    }



}