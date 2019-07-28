package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.LoginView
import org.junit.Before
import org.junit.Test


class LoginPresenterTest {

    private lateinit var impl: UstadMobileSystemImpl

    private lateinit var view: LoginView

    private lateinit var presenter:LoginPresenter

    private val context = Any()

    @Before
    fun setUp(){
        view = mock()
        impl = mock ()
        presenter = LoginPresenter(context, mapOf(),view, impl)
    }


    @Test
    fun givenAppPrefRegistrationVisibilityIsSetToTrue_whenLogin_shouldShowRegisterLabel(){
        doAnswer {
            "true"
        }.`when`(impl).getAppConfigString(any(), any(), any())

        presenter.onCreate(mapOf())

        verify(view).setRegistrationLinkVisible(eq(true))
    }


    @Test
    fun givenAppPrefRegistrationVisibilityIsSetToFalse_whenLogin_shouldNotShowRegisterLabel(){
        doAnswer {
            "false"
        }.`when`(impl).getAppConfigString(any(), any(), any())

        presenter.onCreate(mapOf())

        verify(view).setRegistrationLinkVisible(eq(false))
    }


    @Test
    fun givenRegisterLinkIsVisible_whenClicked_shouldOpenRegistrationSection(){
        doAnswer {
            "true"
        }.`when`(impl).getAppConfigString(any(), any(), any())

        presenter.onCreate(mapOf())

        presenter.handleCreateAccount()

        verify(impl, times(1)).go(any(), any(), any())

    }


//    private var server: HttpServer? = null
//
//    private var db: UmAppDatabase? = null
//
//    private var repo: UmAppDatabase? = null


    /*@Before
    fun setUp() {
        mainImpl = UstadMobileSystemImpl.instance
        impl = Mockito.spy(mainImpl)
        UstadMobileSystemImpl.setMainInstance(impl)
        server = startServer()

        db = UmAppDatabase.getInstance(Any())
        repo = db//db!!.getRepository(TEST_URI, "")

        db!!.clearAllTables()
        val testPerson = Person()
        testPerson.username = VALID_USER
        val personUid = repo!!.personDao.insert(testPerson)

        val testPersonAuth = PersonAuth(personUid,
                PersonAuthDao.ENCRYPTED_PASS_PREFIX + PersonAuthDao.encryptPassword(VALID_PASS))
        repo!!.personAuthDao.insert(testPersonAuth)

        view = Mockito.mock(LoginView::class.java)
        doAnswer {
            Thread(it.getArgument<Any>(0) as Runnable).start()
            null
        }.`when`<LoginView>(view).runOnUiThread(any())
    }

    @After
    fun tearDown() {
        UstadMobileSystemImpl.setMainInstance(mainImpl)
        impl = null
        server!!.shutdownNow()
    }

    @Test
    fun givenValidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSystemImplGo() {
        val args = Hashtable<String,String>()
        args.put(LoginPresenter.ARG_NEXT, "somewhere")

        val presenter = LoginPresenter(Any(),
                args, view!!)
        presenter.handleClickLogin(VALID_USER, VALID_PASS, TEST_URI)


        verify<UstadMobileSystemImpl>(impl, timeout(5000)).go("somewhere",
                Any())

        val activeAccount = UmAccountManager.getActiveAccount(
                Any())
        Assert.assertNotNull(activeAccount)
    }

    @Test
    fun givenInvalidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        val args = Hashtable<String , String>()

        val presenter = LoginPresenter(Any(),
                args, view!!)
        presenter.handleClickLogin(VALID_USER, "wrongpassword", TEST_URI)

        val expectedErrorMsg = UstadMobileSystemImpl.instance.getString(
                MessageID.wrong_user_pass_combo, Any())

        verify<LoginView>(view, timeout(5000)).setErrorMessage(expectedErrorMsg)
        verify<LoginView>(view, timeout(5000)).setPassword("")
    }


    @Test
    fun givenServerOffline_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        val args = Hashtable<String , String>()
        server!!.shutdownNow()

        val presenter = LoginPresenter(PlatformTestUtil.targetContext,
                args, view!!)
        presenter.handleClickLogin(VALID_USER, VALID_PASS, TEST_URI)
        val expectedErrorMsg = UstadMobileSystemImpl.instance.getString(
                MessageID.login_network_error, Any())
        verify<LoginView>(view, timeout(5000)).setErrorMessage(expectedErrorMsg)
    }

    companion object {

        private val VALID_USER = "testuser"

        private val VALID_PASS = "secret"
    }*/


}
