package com.ustadmobile.core.controller;

import com.ustadmobile.core.CoreTestConfig;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.PersonAuthDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.lib.database.jdbc.DriverConnectionPoolInitializer;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Hashtable;

import static com.ustadmobile.test.core.util.CoreTestUtil.TEST_URI;
import static com.ustadmobile.test.core.util.CoreTestUtil.startServer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TestLogin2Presenter {

    UstadMobileSystemImpl mainImpl;

    UstadMobileSystemImpl systemImplSpy;

    private HttpServer server;

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private static final String VALID_USER = "testuser";

    private static final String VALID_PASS = "secret";

    private Login2View mockView;


    @Before
    public void setUp() {
        DriverConnectionPoolInitializer.bindDataSource("UmAppDatabase",
                CoreTestConfig.TESTDB_JDBCURL_UMMAPPDATABASE, true);
        
        mainImpl = UstadMobileSystemImpl.getInstance();
        systemImplSpy = Mockito.spy(mainImpl);
        UstadMobileSystemImpl.setMainInstance(systemImplSpy);
        server = startServer();

        db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        repo = db.getRepository(TEST_URI, "");

        db.clearAllTables();
        Person testPerson = new Person();
        testPerson.setUsername(VALID_USER);
        long personUid = repo.getPersonDao().insert(testPerson);

        PersonAuth testPersonAuth = new PersonAuth(personUid,
                PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                        PersonAuthDao.encryptPassword(VALID_PASS));
        repo.getPersonAuthDao().insert(testPersonAuth);

        mockView = Mockito.mock(Login2View.class);
        doAnswer((invocationOnMock) -> {
            new Thread(((Runnable)invocationOnMock.getArgument(0))).start();
            return null;
        }).when(mockView).runOnUiThread(any());
    }

    @After
    public void tearDown() {
        UstadMobileSystemImpl.setMainInstance(mainImpl);
        systemImplSpy = null;
        server.shutdownNow();
    }

    @Test
    public void givenValidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSystemImplGo() {
        Hashtable args = new Hashtable();
        args.put(Login2Presenter.ARG_NEXT, "somewhere");

        Login2Presenter presenter = new Login2Presenter(PlatformTestUtil.getTargetContext(),
                args, mockView);
        presenter.handleClickLogin(VALID_USER, VALID_PASS, TEST_URI);


        verify(systemImplSpy, timeout(5000)).go("somewhere",
                PlatformTestUtil.getTargetContext());

        UmAccount activeAccount = UmAccountManager.getActiveAccount(
                PlatformTestUtil.getTargetContext());
        Assert.assertNotNull(activeAccount);
    }

    @Test
    public void givenInvalidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        Hashtable args = new Hashtable();

        Login2Presenter presenter = new Login2Presenter(PlatformTestUtil.getTargetContext(),
                args, mockView);
        presenter.handleClickLogin(VALID_USER, "wrongpassword", TEST_URI);

        String expectedErrorMsg = UstadMobileSystemImpl.getInstance().getString(
                MessageID.wrong_user_pass_combo, PlatformTestUtil.getTargetContext());

        verify(mockView, timeout(5000)).setErrorMessage(expectedErrorMsg);
        verify(mockView, timeout(5000)).setPassword("");
    }


    @Test
    public void givenServerOffline_whenHandleLoginCalled_thenShouldCallSetErrorMessage() {
        Hashtable args = new Hashtable();
        server.shutdownNow();

        Login2Presenter presenter = new Login2Presenter(PlatformTestUtil.getTargetContext(),
                args, mockView);
        presenter.handleClickLogin(VALID_USER, VALID_PASS, TEST_URI);
        String expectedErrorMsg = UstadMobileSystemImpl.getInstance().getString(
                MessageID.login_network_error, PlatformTestUtil.getTargetContext());
        verify(mockView, timeout(5000)).setErrorMessage(expectedErrorMsg);
    }




}
