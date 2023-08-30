//package com.ustadmobile.core.controller
//
//import com.sun.net.httpserver.HttpServer
//import com.ustadmobile.core.CoreTestConfig
//import com.ustadmobile.core.db.UmAppDatabase
//import com.ustadmobile.core.MR
//import com.ustadmobile.core.impl.UmAccountManager
//import com.ustadmobile.core.impl.UstadMobileSystemImpl
//import com.ustadmobile.core.view.Register2View
//import com.ustadmobile.lib.umDatabase.jdbc.DriverConnectionPoolInitializer
//import com.ustadmobile.lib.db.entities.Person
//import com.ustadmobile.lib.db.entities.UmAccount
//import com.ustadmobile.test.core.impl.PlatformTestUtil
//
//import org.glassfish.grizzly.http.server.HttpServer
//import org.junit.After
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import org.mockito.Mockito
//
//import java.util.Hashtable
//
//import com.ustadmobile.test.core.util.CoreTestUtil.TEST_URI
//import com.ustadmobile.test.core.util.CoreTestUtil.startServer
//import com.ustadmobile.util.test.checkJndiSetup
//import org.mockito.ArgumentMatchers.any
//import org.mockito.Mockito.doAnswer
//import org.mockito.Mockito.timeout
//import org.mockito.Mockito.verify
//
//class TestRegister2Presenter {
//
//    private var systemImplSpy: UstadMobileSystemImpl? = null
//
//    private var server: HttpServer? = null
//
//    private var repo: UmAppDatabase? = null
//
//    private var clientDb: UmAppDatabase? = null
//
//    private var mockView: Register2View? = null
//
//    private var testPerson: Person? = null
//
//    private var args: MutableMap<String , String>? = null
//
//    @Before
//    fun setUp() {
//        checkJndiSetup()
//        val mainImpl = UstadMobileSystemImpl.instance
//        systemImplSpy = Mockito.spy(mainImpl)
//        UstadMobileSystemImpl.setMainInstance(systemImplSpy)
//        server = startServer()
//
//        val db = UmAppDatabase.getInstance(Any())
//        clientDb = UmAppDatabase.getInstance(Any(), "db1")
//        repo = db //db.getUmRepository(TEST_URI, "")
//
//        db.clearAllTables()
//        clientDb!!.clearAllTables()
//
//        testPerson = Person()
//        testPerson!!.emailAddr = "johndoe@example.com"
//        testPerson!!.username = "John"
//        testPerson!!.lastName = "Doe"
//
//
//
//        args = mutableMapOf<String,String>()
//        args!![LoginPresenter.ARG_NEXT] = DESTINATION
//
//        mockView = Mockito.mock(Register2View::class.java)
//        doAnswer {
//            Thread(it.getArgument<Any>(0) as Runnable).start()
//            null
//        }.`when`<Register2View>(mockView).runOnUiThread(any())
//    }
//
//    @After
//    fun tearDown() {
//        server!!.shutdownNow()
//    }
//
//
//    //TODO: Re-enable this test after the DAO checks for this
//    ////@Test
//    fun givenExistingPersonDetails_whenHandleRegisterCalled_thenShouldNotCreateAccount() {
//        repo!!.personDao.insert(testPerson!!)
//        val presenter = Register2Presenter(Any(), args!!, mockView!!)
//        presenter.handleClickRegister(testPerson!!, VALID_PASS, TEST_URI)
//
//        val expectedErrorMsg = UstadMobileSystemImpl.instance.getString(
//                MR.strings.err_registering_new_user, Any())
//        verify<Register2View>(mockView, timeout(5000)).setErrorMessageView(expectedErrorMsg)
//    }
//
//    //@Test
//    fun givenNewPersonDetails_whenHandleRegisterCalled_thenShouldCreateAnAccountAndGenerateAuthToken() {
//        val presenter = Register2Presenter(Any(), args!!, mockView!!)
//        presenter.setClientDb(clientDb!!)
//        presenter.setRepo(repo!!)
//        presenter.handleClickRegister(testPerson!!, VALID_PASS, TEST_URI)
//
//        verify<UstadMobileSystemImpl>(systemImplSpy, timeout((5000 * 100).toLong())).go(DESTINATION,
//                Any())
//
//        val activeAccount = UmAccountManager.getActiveAccount(
//                Any())
//        Assert.assertNotNull(activeAccount)
//        Assert.assertNotEquals("Active account uid is set ( != 0 )",
//                activeAccount!!.personUid, 0)
//
//        Assert.assertNotNull("Person object created on client",
//                clientDb!!.personDao.findByUid(activeAccount.personUid))
//        Assert.assertNotNull("Person object created on server",
//                repo!!.personDao.findByUid(activeAccount.personUid))
//    }
//
//    //@Test
//    fun givenServerOffline_whenHandleRegisterCalled_thenShouldCallSetErrorMessage() {
//        val args = Hashtable<String,String>()
//        server!!.shutdownNow()
//
//        val presenter = Register2Presenter(Any(),
//                args, mockView!!)
//        presenter.handleClickRegister(testPerson!!, VALID_PASS, TEST_URI)
//
//        val expectedErrorMsg = UstadMobileSystemImpl.instance.getString(
//                MR.strings.login_network_error, Any())
//        verify<Register2View>(mockView, timeout(5000)).setErrorMessageView(expectedErrorMsg)
//    }
//
//    companion object {
//
//        private const val VALID_PASS = "secret"
//
//        private const val DESTINATION = "somewhere"
//    }
//}
