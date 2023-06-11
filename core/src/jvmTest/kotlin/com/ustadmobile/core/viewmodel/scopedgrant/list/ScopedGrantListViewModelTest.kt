package com.ustadmobile.core.viewmodel.scopedgrant.list

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
import com.ustadmobile.core.util.ext.loadFirstList
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.ScopedGrantDetailView
import com.ustadmobile.core.view.ScopedGrantEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.person.PersonViewModelConstants
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.composites.ScopedGrantEntityAndName
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.ScopedGrant
import kotlinx.coroutines.flow.filter
import org.mockito.kotlin.*
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ScopedGrantListViewModelTest {


    @Test
    fun givenViewModelNotYetCreated_whenInitialized_thenShouldQueryDatabase(){

        testViewModel<ScopedGrantListViewModel> {

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

            savedStateHandle[UstadView.ARG_CODE_TABLE] = Clazz.TABLE_ID.toString()
            savedStateHandle[UstadView.ARG_ENTITY_UID] = clazz.clazzUid.toString()


            viewModelFactory {
                ScopedGrantListViewModel(di, savedStateHandle)
            }

            val scopedGrantDaoRepo = spy(activeRepo.scopedGrantDao)
            activeRepo.stub{
                on {scopedGrantDao}.thenReturn(scopedGrantDaoRepo)
            }

            val accountPersonUid = accountManager.activeAccount.personUid

            viewModel.uiState
                .filter{ it.scopeGrantList() !is EmptyPagingSource }
                .test{
                    awaitItem()
                    verify(
                        scopedGrantDaoRepo,
                        timeout(5000)
                    ).findByTableIdAndEntityUidWithNameAsPagingSource(
                        eq(Clazz.TABLE_ID),
                        eq(clazz.clazzUid)

                    )

                    cancelAndIgnoreRemainingEvents()

                }
        }

    }

    @Test
    fun givenViewModelInitialised_whenAddClicked_thenShouldGoToPersonPicker(){

        testViewModel<ScopedGrantListViewModel> {

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

            savedStateHandle[UstadView.ARG_CODE_TABLE] = Clazz.TABLE_ID.toString()
            savedStateHandle[UstadView.ARG_ENTITY_UID] = clazz.clazzUid.toString()


            viewModelFactory {
                ScopedGrantListViewModel(di, savedStateHandle)
            }

            val scopedGrantDaoRepo = spy(activeRepo.scopedGrantDao)
            activeRepo.stub{
                on {scopedGrantDao}.thenReturn(scopedGrantDaoRepo)
            }

            viewModel.uiState
                .filter {it.scopeGrantList() !is EmptyPagingSource }
                .test {
                    viewModel.onClickAdd()
                    cancelAndIgnoreRemainingEvents()
                }

            viewModel.navCommandFlow.test{
                val navCommand = awaitItem() as NavigateNavCommand
                assertEquals( PersonListViewModel.DEST_NAME, navCommand.viewName)
                assertEquals(ListViewMode.PICKER.mode, navCommand.args[UstadView.ARG_LISTMODE])
                assertContains(
                    navCommand.args[PersonViewModelConstants.ARG_GO_TO_ON_PERSON_SELECTED]!!,
                    ScopedGrantEditView.VIEW_NAME
                )

            }

        }


    }

    @Test
    fun givenViewModelInitialised_whenClickedEntry_thenShouldGoToScopedGrantDetail(){

        testViewModel<ScopedGrantListViewModel> {

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

            val testEntity = ScopedGrantEntityAndName().apply {
                scopedGrant = testScopedGrant
                name = "Bob Dylan"
            }

            savedStateHandle[UstadView.ARG_CODE_TABLE] = Clazz.TABLE_ID.toString()
            savedStateHandle[UstadView.ARG_ENTITY_UID] = clazz.clazzUid.toString()

            viewModelFactory {
                ScopedGrantListViewModel(di, savedStateHandle)
            }

            val scopedGrantDaoRepo = spy(activeRepo.scopedGrantDao)
            activeRepo.stub{
                on {scopedGrantDao}.thenReturn(scopedGrantDaoRepo)
            }

            viewModel.uiState
                .filter {it.scopeGrantList() !is EmptyPagingSource }
                .test {
                    viewModel.onClickEntry(testEntity)
                    cancelAndIgnoreRemainingEvents()
                }

            viewModel.navCommandFlow
                .test{
                    val navCommand = awaitItem() as NavigateNavCommand
                    assertEquals(ScopedGrantDetailView.VIEW_NAME, navCommand.viewName)
                    assertEquals(testEntity.scopedGrant!!.sgUid.toString(),
                        navCommand.args[UstadView.ARG_ENTITY_UID])

                    cancelAndIgnoreRemainingEvents()
                }

        }
    }

    @Test
    fun givenViewModelInitialised_whenDataLoadedWithPerson_thenShouldSetEditPermission(){

        testViewModel<ScopedGrantListViewModel> {

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
            ScopedGrant().apply {
                sgTableId = Clazz.TABLE_ID
                sgEntityUid = clazz.clazzUid
                sgGroupUid = enroledPerson.personGroupUid
                sgPermissions = Role.PERMISSION_CLAZZ_UPDATE or
                        Role.PERMISSION_CLAZZ_CONTENT_UPDATE
                sgUid = activeDb.scopedGrantDao.insertAsync(this)
            }

            savedStateHandle[UstadView.ARG_CODE_TABLE] = Clazz.TABLE_ID.toString()
            savedStateHandle[UstadView.ARG_ENTITY_UID] = clazz.clazzUid.toString()

            viewModelFactory {
                ScopedGrantListViewModel(di, savedStateHandle)
            }

            val scopedGrantDaoRepo = spy(activeRepo.scopedGrantDao)
            activeRepo.stub{
                on {scopedGrantDao}.thenReturn(scopedGrantDaoRepo)
            }

            //Test that edit was set
            viewModel.uiState
                .test(timeout = 30.seconds){
                    val readyState = awaitItemWhere{
                        it.scopeGrantList() !is EmptyPagingSource
                    }
                    val grants = readyState.scopeGrantList().loadFirstList()
                    val name = clazz.clazzName + " - Parent"
                    assertEquals(name, grants.first().name)

                    viewModel.appUiState.assertItemReceived(timeout = 500.seconds) {
                        it.fabState.visible
                    }
                }

        }
    }


    @Test
    fun givenViewModelInitialised_whenDataLoadedWithPerson_thenShouldGetEntryFromDatabase(){
        testViewModel<ScopedGrantListViewModel> {

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
            ScopedGrant().apply {
                sgTableId = Clazz.TABLE_ID
                sgEntityUid = clazz.clazzUid
                sgGroupUid = enroledPerson.personGroupUid
                sgPermissions = Role.PERMISSION_CLAZZ_UPDATE or
                        Role.PERMISSION_CLAZZ_CONTENT_UPDATE
                sgUid = activeDb.scopedGrantDao.insertAsync(this)
            }

            savedStateHandle[UstadView.ARG_CODE_TABLE] = Clazz.TABLE_ID.toString()
            savedStateHandle[UstadView.ARG_ENTITY_UID] = clazz.clazzUid.toString()

            viewModelFactory {
                ScopedGrantListViewModel(di, savedStateHandle)
            }

            val scopedGrantDaoRepo = spy(activeRepo.scopedGrantDao)
            activeRepo.stub{
                on {scopedGrantDao}.thenReturn(scopedGrantDaoRepo)
            }

            //Test that edit was set
            viewModel.uiState
                .test(timeout = 30.seconds){
                    val readyState = awaitItemWhere{
                        it.scopeGrantList() !is EmptyPagingSource
                    }
                    val grants = readyState.scopeGrantList().loadFirstList()
                    assertEquals(enroledPerson.firstNames, grants.last().name)


                }

        }
    }

}