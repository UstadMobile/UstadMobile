package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzMemberDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.JoinWithCodeView
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.test.ext.insertPersonWithRole
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

    private var apiUrl: String? =null

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

        apiUrl = "www.ustadmobile.com/some/url"

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
    fun givenLoadedWithIncorrectCode_whenLoading_thenShouldShowError(){
        val presenter = JoinWithCodePresenter(context, mapOf(
                UstadView.ARG_SERVER_URL to "www.ustadmobile.com/some/url",
                UstadView.ARG_CODE_TABLE to Clazz.TABLE_ID.toString(),
                UstadView.ARG_CODE to "wrong"
        ), mockView, di)
        presenter.onCreate(null)

        verify(mockView, timeout(5000)).errorText =
                UstadMobileSystemImpl.instance.getString(MessageID.invalid_register_code, context)
    }

    @Test
    fun givenLoadedWithNoTableCode_whenLoading_thenShouldShowError(){
        val presenter = JoinWithCodePresenter(context, mapOf(
                UstadView.ARG_SERVER_URL to "www.ustadmobile.com/some/url",
                UstadView.ARG_CODE to "any"
        ), mockView, di)
        presenter.onCreate(null)

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

        verify(mockView, timeout(5000)).errorText =
                UstadMobileSystemImpl.instance.getString(MessageID.invalid_register_code, context)
    }

    @Test
    fun givenLoadedWithValidParams_whenNotLoggedIn_thenShouldGoToLoginWithArgs(){
        val systemImpl: UstadMobileSystemImpl by di.instance()
        val tableId = Clazz.TABLE_ID
        val presenter = JoinWithCodePresenter(context, mapOf(
                UstadView.ARG_SERVER_URL to apiUrl.toString(),
                UstadView.ARG_CODE_TABLE to tableId.toString(),
                UstadView.ARG_CODE to clazzToEnrolInto.clazzCode.toString()
        ), mockView, di)
        presenter.onCreate(null)

        verify(systemImpl, timeout(5000)).go(eq(Login2View.VIEW_NAME),
                eq(
                        mapOf(UstadView.ARG_SERVER_URL to apiUrl,
                                UstadView.ARG_NEXT to
                                        "${JoinWithCodeView.VIEW_NAME}?${UstadView.ARG_SERVER_URL}=$apiUrl" +
                                        "&${UstadView.ARG_CODE_TABLE}=$tableId")
                ), any())

    }

    @Test
    fun givenLoadedWithValidParams_whenLoggedInWithDifferentApiUrl_thenShouldGoToLoginWithArgs(){
        val systemImpl: UstadMobileSystemImpl by di.instance()
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()
        val accountManager = di.direct.instance<UstadAccountManager>()
        val tableId = Clazz.TABLE_ID

        val activePerson = Person().apply {
            firstNames = "Test"
            lastName = "User"
            username = "testuser2"
            personUid = db.personDao.insert(this)
        }


        val endpointUrl = "http://www.someotherdomain.com/another/api/url"
        accountManager.activeAccount = UmAccount(activePerson.personUid, activePerson.username,
                "", endpointUrl, activePerson.firstNames, activePerson.lastName)

        val presenter = JoinWithCodePresenter(context, mapOf(
                UstadView.ARG_SERVER_URL to apiUrl.toString(),
                UstadView.ARG_CODE_TABLE to tableId.toString(),
                UstadView.ARG_CODE to clazzToEnrolInto.clazzCode.toString()
        ), mockView, di)
        presenter.onCreate(null)

        verify(systemImpl, timeout(5000)).go(eq(Login2View.VIEW_NAME),
                eq(
                        mapOf(UstadView.ARG_SERVER_URL to apiUrl,
                                UstadView.ARG_NEXT to
                                        "${JoinWithCodeView.VIEW_NAME}?${UstadView.ARG_SERVER_URL}=$apiUrl" +
                                        "&${UstadView.ARG_CODE_TABLE}=$tableId")
                ), any())

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
            personUid = db.personDao.insert(this)
        }


        accountManager.activeAccount = UmAccount(activePerson.personUid, activePerson.username,
                "", apiUrl ?:"", activePerson.firstNames, activePerson.lastName)

        val presenter = JoinWithCodePresenter(context, mapOf(
                UstadView.ARG_SERVER_URL to apiUrl.toString(),
                UstadView.ARG_CODE_TABLE to tableId.toString(),
                UstadView.ARG_CODE to "wrong"
        ), mockView, di)
        presenter.onCreate(null)

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
            personUid = db.personDao.insert(this)
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