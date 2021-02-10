package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzEnrollmentDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.view.EntityRoleEditView
import com.ustadmobile.door.DoorLifecycleObserver
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class EntityRoleEditPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: EntityRoleEditView

    private lateinit var di: DI

    private lateinit var clazzToEnrolInto: Clazz

    private lateinit var clazzEnrollmentRepoDaoSpy: ClazzEnrollmentDao

    private lateinit var accountManager: UstadAccountManager

    private var context: Any = Any()

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner


    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }


        mockView = mock { }

        mockLifecycleOwner = mock {
            on { currentState }.thenReturn(DoorLifecycleObserver.RESUMED)
        }

        accountManager = di.direct.instance()

        val currentEndpoint = accountManager.activeAccount.endpointUrl
        accountManager.activeAccount = UmAccount(42L, "testuser", endpointUrl = currentEndpoint)

        val repo: UmAppDatabase by di.activeRepoInstance()
        clazzEnrollmentRepoDaoSpy = spy(repo.clazzEnrollmentDao)
        whenever(repo.clazzEnrollmentDao).thenReturn(clazzEnrollmentRepoDaoSpy)

        repo.personDao.insert(Person().apply {
            firstNames = "Test"
            lastName = "User"
            username = "testuser"
            personUid = accountManager.activeAccount.personUid
        })

        val systemImpl: UstadMobileSystemImpl by di.instance()

        runBlocking {
            clazzToEnrolInto = Clazz("Test Class")
            repo.createNewClazzAndGroups(clazzToEnrolInto, systemImpl, context)
        }
    }

    @Test
    fun givenRoleNotFilled_whenSaved_shouldShowError() {

        val repo: UmAppDatabase by di.activeRepoInstance()

        val entityRoleWithNameAndRole = EntityRoleWithNameAndRole().apply {
            entityRoleScopeName = "Testing Blah"
            erTableId = Clazz.TABLE_ID
            erEntityUid = clazzToEnrolInto.clazzUid
            erGroupUid = 42
            erRoleUid = 42
            erActive = true
            erUid = repo.entityRoleDao.insert(this)

        }
        val presenter = EntityRoleEditPresenter(context, mapOf(), mockView, di,
                mockLifecycleOwner)
        presenter.handleClickSave(entityRoleWithNameAndRole)


        verify(mockView, timeout(5000)).errorText = any()
    }


}