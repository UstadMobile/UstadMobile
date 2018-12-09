package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.LoginView2;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Hashtable;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TestLoginPresenter2 {


    UstadMobileSystemImpl mainImpl;

    UstadMobileSystemImpl systemImplSpy;

    public static final String TEST_URI = "http://localhost:8089/api/";

    private HttpServer server;

    public static HttpServer startServer() {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .packages("com.ustadmobile.core.db.dao");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(TEST_URI), resourceConfig);
    }

    @Before
    public void setUp() {
        mainImpl = UstadMobileSystemImpl.getInstance();
        systemImplSpy = Mockito.spy(mainImpl);
        UstadMobileSystemImpl.setMainInstance(systemImplSpy);
        server = startServer();
    }

    @After
    public void tearDown() {
        UstadMobileSystemImpl.setMainInstance(mainImpl);
        systemImplSpy = null;
        server.shutdownNow();
    }

    @Test
    public void givenValidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSystemImplGo() {
        UmAppDatabase db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        UmAppDatabase repo = db.getRepository(TEST_URI, "");

        db.clearAllTables();
        Person testPerson = new Person();
        testPerson.setUsername("testuser");
        testPerson.setPasswordHash("secret");
        repo.getPersonDao().insert(testPerson);

        LoginView2 mockView = Mockito.mock(LoginView2.class);
        Hashtable args = new Hashtable();
        args.put(LoginPresenter2.ARG_NEXT, "somewhere");

        LoginPresenter2 presenter = new LoginPresenter2(PlatformTestUtil.getTargetContext(),
                args, mockView);
        presenter.handleClickLogin("testuser", "secret", TEST_URI);


        verify(systemImplSpy, timeout(5000)).go("somewhere",
                PlatformTestUtil.getTargetContext());

        UmAccount activeAccount = UmAccountManager.getActiveAccount(
                PlatformTestUtil.getTargetContext());
        Assert.assertNotNull(activeAccount);

    }



}
