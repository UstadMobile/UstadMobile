package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzMemberDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.view.JoinWithCodeView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class JoinWithCodePresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: JoinWithCodeView

    private lateinit var di: DI

    private lateinit var clazzToEnrolInto: Clazz

    private lateinit var clazzMemberRepoDaoSpy: ClazzMemberDao

    private lateinit var accountManager: UstadAccountManager

    private var context: Any = Any()

    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        mockView = mock { }

        accountManager = di.direct.instance()

        val currentEndpoint = accountManager.activeAccount.endpointUrl
        accountManager.activeAccount = UmAccount(42L, "testuser", endpointUrl = currentEndpoint)

        val repo: UmAppDatabase by di.activeRepoInstance()
        clazzMemberRepoDaoSpy = spy(repo.clazzMemberDao)
        whenever(repo.clazzMemberDao).thenReturn(clazzMemberRepoDaoSpy)

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
    fun givenValidCode_whenHandleClickDoneCalled_thenShouldEnrollAsPendingStudent() {

        val presenter = JoinWithCodePresenter(context, mapOf(), mockView, di)
        presenter.handleClickDone(clazzToEnrolInto.clazzCode!!)


        verifyBlocking(clazzMemberRepoDaoSpy, timeout(5000 * 5000)) {
            insertAsync(argWhere {
                it.clazzMemberPersonUid == accountManager.activeAccount.personUid &&
                        it.clazzMemberRole == ClazzMember.ROLE_STUDENT_PENDING
            })
        }

        verify(mockView, timeout(5000)).finish()
    }

    @Test
    fun givenInvalidCode_whenHandleClickDoenCalled_thenShouldShowError() {
        val presenter = JoinWithCodePresenter(context, mapOf(), mockView, di)
        presenter.handleClickDone("wrong")

        verify(mockView, timeout(5000)).errorText = any()
    }

    @Test
    fun givenValidCode_when

}