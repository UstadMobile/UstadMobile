package com.ustadmobile.core.controller

import com.ustadmobile.core.CoreTestConfig
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonAuthDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.lib.database.jdbc.DriverConnectionPoolInitializer
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonAuth
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.test.core.impl.PlatformTestUtil

import org.glassfish.grizzly.http.server.HttpServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import java.util.Hashtable

import com.ustadmobile.test.core.util.CoreTestUtil.TEST_URI
import com.ustadmobile.test.core.util.CoreTestUtil.startServer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify

class TestLogin2Presenter {

    internal var mainImpl: UstadMobileSystemImpl? = null

    internal var systemImplSpy: UstadMobileSystemImpl? = null

    private var server: HttpServer? = null

    private var db: UmAppDatabase? = null

    private var repo: UmAppDatabase? = null

    private var mockView: Login2View? = null


    @Before
    fun setUp() {
        DriverConnectionPoolInitializer.bindDataSource("UmAppDatabase",
                CoreTestConfig.TESTDB_JDBCURL_UMMAPPDATABASE, true)

        mainImpl = UstadMobileSystemImpl.instance
        systemImplSpy = Mockito.spy(mainImpl)
        UstadMobileSystemImpl.setMainInstance(systemImplSpy)
        server = startServer()

        db = UmAppDatabase.getInstance(PlatformTestUtil.targetContext)
        repo = db!!.getRepository(TEST_URI, "")

        db!!.clearAllTables()
        val testPerson = Person()
        testPerson.username = VALID_USER
        val personUid = repo!!.personDao.insert(testPerson)

        val testPersonAuth = PersonAuth(personUid,
                PersonAuthDao.ENCRYPTED_PASS_PREFIX + PersonAuthDao.encryptPassword(VALID_PASS))
        repo!!.personAuthDao.insert(testPersonAuth)

        mockView = Mockito.mock(Login2View::class.java)
        doAnswer {
            Thread(it.getArgument<Any>(0) as Runnable).start()
            null
        }.`when`<Login2View>(mockView).runOnUiThread(any())
    }

    @After
    fun tearDown() {
        UstadMobileSystemImpl.setMainInstance(mainImpl)
        systemImplSpy = null
        server!!.shutdownNow()
    }

    @Test
    fun givenValidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSystemImplGo() {
        val args = Hashtable<String,String>()
        args.put(Login2Presenter.ARG_NEXT, "somewhere")

        val presenter = Login2Presenter(PlatformTestUtil.targetContext,
                args, mockView!!)
        presenter.handleClickLogin(VALID_USER, VALID_PASS, TEST_URI)


        verify<UstadMobileSystemImpl>(systemImplSpy, timeout(5000)).go("somewhere",
                PlatformTestUtil.targetContext)

        val activeAccount = UmAccountManager.getActiveAccount(
                PlatformTestUtil.targetContext)
        Assert.assertNotNull(activeAccount)
    }

    @Test
    fun givenInvalidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        val args = Hashtable<String , String>()

        val presenter = Login2Presenter(PlatformTestUtil.targetContext,
                args, mockView!!)
        presenter.handleClickLogin(VALID_USER, "wrongpassword", TEST_URI)

        val expectedErrorMsg = UstadMobileSystemImpl.instance.getString(
                MessageID.wrong_user_pass_combo, PlatformTestUtil.targetContext)

        verify<Login2View>(mockView, timeout(5000)).setErrorMessage(expectedErrorMsg)
        verify<Login2View>(mockView, timeout(5000)).setPassword("")
    }


    @Test
    fun givenServerOffline_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        val args = Hashtable<String , String>()
        server!!.shutdownNow()

        val presenter = Login2Presenter(PlatformTestUtil.targetContext,
                args, mockView!!)
        presenter.handleClickLogin(VALID_USER, VALID_PASS, TEST_URI)
        val expectedErrorMsg = UstadMobileSystemImpl.instance.getString(
                MessageID.login_network_error, PlatformTestUtil.targetContext)
        verify<Login2View>(mockView, timeout(5000)).setErrorMessage(expectedErrorMsg)
    }

    companion object {

        private val VALID_USER = "testuser"

        private val VALID_PASS = "secret"
    }


}
