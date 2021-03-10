package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzEnrolmentDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.view.JoinWithCodeView
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

class JoinWithCodePresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: JoinWithCodeView

    private lateinit var di: DI

    private lateinit var clazzToEnrolInto: Clazz

    private lateinit var clazzEnrolmentRepoDaoSpy: ClazzEnrolmentDao

    private lateinit var accountManager: UstadAccountManager

    private var context: Any = Any()

    private var apiUrl: String? =null

    @Before
    fun setup() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        mockView = mock { }

        accountManager = di.direct.instance()

        val currentEndpoint = accountManager.activeAccount.endpointUrl
        accountManager.activeAccount = UmAccount(42L, "testuser",
                endpointUrl = currentEndpoint)

        val repo: UmAppDatabase by di.activeRepoInstance()

        clazzEnrolmentRepoDaoSpy = spy(repo.clazzEnrolmentDao)
        whenever(repo.clazzEnrolmentDao).thenReturn(clazzEnrolmentRepoDaoSpy)

        repo.personDao.insert(Person().apply {
            firstNames = "Test"
            lastName = "User"
            username = "testuser"
            personUid = accountManager.activeAccount.personUid
        })

        val systemImpl: UstadMobileSystemImpl by di.instance()

        apiUrl = currentEndpoint

        runBlocking {
            clazzToEnrolInto = Clazz("Test Class")
            repo.createNewClazzAndGroups(clazzToEnrolInto, systemImpl, context)
        }
    }

    @Test
    fun givenValidCode_whenHandleClickDoneCalled_thenShouldEnrollAsPendingStudent() {

        val presenter = JoinWithCodePresenter(context,
                mapOf(UstadView.ARG_CODE_TABLE to Clazz.TABLE_ID.toString(),
                UstadView.ARG_CODE to clazzToEnrolInto.clazzCode!!), mockView, di)
        presenter.onCreate(null)
        presenter.handleClickDone(clazzToEnrolInto.clazzCode!!)


        verifyBlocking(clazzEnrolmentRepoDaoSpy, timeout(5000 * 5000)) {
            insertAsync(argWhere {
                it.clazzEnrolmentPersonUid == accountManager.activeAccount.personUid &&
                        it.clazzEnrolmentRole == ClazzEnrolment.ROLE_STUDENT_PENDING
            })
        }

        verify(mockView, timeout(5000)).finish()
    }

    @Test
    fun givenInvalidCode_whenHandleClickDoenCalled_thenShouldShowError() {
        val presenter = JoinWithCodePresenter(context,
                mapOf(UstadView.ARG_CODE_TABLE to Clazz.TABLE_ID.toString()), mockView, di)
        presenter.handleClickDone("wrong")

        verify(mockView, timeout(5000)).errorText = any()
    }

    @Test
    fun givenLoadedWithIncorrectCode_whenLoading_thenShouldShowError(){
        val presenter = JoinWithCodePresenter(context, mapOf(
                UstadView.ARG_SERVER_URL to apiUrl.toString(),
                UstadView.ARG_CODE_TABLE to Clazz.TABLE_ID.toString(),
                UstadView.ARG_CODE to "wrong"
        ), mockView, di)
        presenter.onCreate(null)

        val runOnUiArgCaptor = argumentCaptor<Runnable>()
        verify(mockView, timeout(5000)).runOnUiThread(runOnUiArgCaptor.capture())
        runOnUiArgCaptor.firstValue.run()

        verify(mockView, timeout(10000)).errorText =
                UstadMobileSystemImpl.instance.getString(MessageID.invalid_register_code, context)
    }

    @Test
    fun givenLoadedWithNoTableCode_whenLoading_thenShouldShowError(){
        val presenter = JoinWithCodePresenter(context, mapOf(
                UstadView.ARG_SERVER_URL to apiUrl.toString(),
                UstadView.ARG_CODE to "any"
        ), mockView, di)
        presenter.onCreate(null)

        val runOnUiArgCaptor = argumentCaptor<Runnable>()
        verify(mockView, timeout(5000)).runOnUiThread(runOnUiArgCaptor.capture())
        runOnUiArgCaptor.firstValue.run()

        verify(mockView, timeout(5000)).errorText =
                UstadMobileSystemImpl.instance.getString(MessageID.invalid_register_code, context)
    }

    @Test
    fun givenLoadedWithNoApiUrl_whenLoading_thenShouldShowError(){
        val presenter = JoinWithCodePresenter(context, mapOf(
                UstadView.ARG_SERVER_URL to "",
                UstadView.ARG_CODE_TABLE to Clazz.TABLE_ID.toString(),
                UstadView.ARG_CODE to "any"
        ), mockView, di)
        presenter.onCreate(null)

        val runOnUiArgCaptor = argumentCaptor<Runnable>()
        verify(mockView, timeout(5000)).runOnUiThread(runOnUiArgCaptor.capture())
        runOnUiArgCaptor.firstValue.run()

        verify(mockView, timeout(5000)).errorText =
                UstadMobileSystemImpl.instance.getString(MessageID.invalid_register_code, context)
    }

    @Test
    fun givenLoadedWithValidParams_whenNotLoggedIn_thenShouldGoToLoginWithArgs(){
        val systemImpl: UstadMobileSystemImpl by di.instance()
        val accountManager = di.direct.instance<UstadAccountManager>()
        //Log out active account
        accountManager.activeAccount = UmAccount(0, null)


        val tableId = Clazz.TABLE_ID
        val presenter = JoinWithCodePresenter(context, mapOf(
                UstadView.ARG_SERVER_URL to apiUrl.toString(),
                UstadView.ARG_CODE_TABLE to tableId.toString(),
                UstadView.ARG_CODE to clazzToEnrolInto.clazzCode.toString()
        ), mockView, di)
        presenter.onCreate(null)

        val runOnUiArgCaptor = argumentCaptor<Runnable>()

        verify(mockView, timeout(5000)).runOnUiThread(runOnUiArgCaptor.capture())
        runOnUiArgCaptor.firstValue.run()

        verify(systemImpl, timeout(5000)).go(eq(Login2View.VIEW_NAME),
                eq(
                        mapOf(UstadView.ARG_SERVER_URL to apiUrl,
                                PersonEditView.REGISTER_VIA_LINK to "true",
                                UstadView.ARG_NEXT to
                                        JoinWithCodeView.VIEW_NAME +
                                        "?${UstadView.ARG_SERVER_URL}=${apiUrl}" +
                                        "&${UstadView.ARG_CODE_TABLE}=${tableId}"+
                                        "&${UstadView.ARG_CODE}=${clazzToEnrolInto.clazzCode.toString()}",
                        Login2View.ARG_NO_GUEST to "true")
                ), any(), any())

    }

    @Test
    fun givenLoadedWithValidParams_whenLoggedInWithDifferentApiUrl_thenShouldGoToLoginWithArgs(){
        val systemImpl: UstadMobileSystemImpl by di.instance()
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()
        val accountManager = di.direct.instance<UstadAccountManager>()
        val tableId = Clazz.TABLE_ID

        val endpointUrl = "http://www.someotherdomain.com/another/api/url/"

        val presenter = JoinWithCodePresenter(context, mapOf(
                UstadView.ARG_SERVER_URL to endpointUrl.toString(),
                UstadView.ARG_CODE_TABLE to tableId.toString(),
                UstadView.ARG_CODE to clazzToEnrolInto.clazzCode.toString()
        ), mockView, di)
        presenter.onCreate(null)

        val runOnUiArgCaptor = argumentCaptor<Runnable>()
        verify(mockView, timeout(5000)).runOnUiThread(runOnUiArgCaptor.capture())
        runOnUiArgCaptor.firstValue.run()

        verify(systemImpl, timeout(5000)).go(eq(Login2View.VIEW_NAME),
                eq(
                        mapOf(UstadView.ARG_SERVER_URL to endpointUrl,
                                PersonEditView.REGISTER_VIA_LINK to "true",
                                UstadView.ARG_NEXT to
                                        "${JoinWithCodeView.VIEW_NAME}" +
                                        "?${UstadView.ARG_SERVER_URL}=$endpointUrl" +
                                        "&${UstadView.ARG_CODE_TABLE}=${tableId}" +
                                        "&${UstadView.ARG_CODE}=${clazzToEnrolInto.clazzCode.toString()}",
                                Login2View.ARG_NO_GUEST to "true")
                ), any(), any())

    }

    @Test
    fun givenLoadedWithinValidParams_whenLoggedInWithCurrentApiUrl_thenShouldShowError(){
        val systemImpl: UstadMobileSystemImpl by di.instance()
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()
        val accountManager = di.direct.instance<UstadAccountManager>()
        val tableId = Clazz.TABLE_ID

        val activePerson = Person().apply {
            firstNames = "Test"
            lastName = "User"
            username = "testuser2"
            personUid = repo.personDao.insert(this)
        }


        accountManager.activeAccount = UmAccount(activePerson.personUid, activePerson.username,
                "", apiUrl ?:"", activePerson.firstNames, activePerson.lastName)

        val presenter = JoinWithCodePresenter(context, mapOf(
                UstadView.ARG_SERVER_URL to apiUrl.toString(),
                UstadView.ARG_CODE_TABLE to tableId.toString(),
                UstadView.ARG_CODE to "wrong"
        ), mockView, di)
        presenter.onCreate(null)

        val runOnUiArgCaptor = argumentCaptor<Runnable>()
        verify(mockView, timeout(5000)).runOnUiThread(runOnUiArgCaptor.capture())
        runOnUiArgCaptor.firstValue.run()

        verify(mockView, timeout(5000)).errorText =
                UstadMobileSystemImpl.instance.getString(MessageID.invalid_register_code, context)

    }

    @Test
    fun givenLoadedWithValidParams_whenLoggedInWithSameApiUrl_thenShouldContinue(){
        val systemImpl: UstadMobileSystemImpl by di.instance()
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()
        val accountManager = di.direct.instance<UstadAccountManager>()
        val tableId = Clazz.TABLE_ID

        val activePerson = Person().apply {
            firstNames = "Test"
            lastName = "User"
            username = "testuser2"
            personUid = repo.personDao.insert(this)
        }

        apiUrl = accountManager.activeAccount.endpointUrl

        accountManager.activeAccount = UmAccount(activePerson.personUid, activePerson.username,
                "", apiUrl ?: "", activePerson.firstNames, activePerson.lastName)

        val presenter = JoinWithCodePresenter(context, mapOf(
                UstadView.ARG_SERVER_URL to apiUrl.toString(),
                UstadView.ARG_CODE_TABLE to tableId.toString(),
                UstadView.ARG_CODE to clazzToEnrolInto.clazzCode.toString()
        ), mockView, di)
        presenter.onCreate(null)

        verifyZeroInteractions(mockView)

        //TODO: Check if we need to Veriy that fragment loaded / go was not called ?
    }

}