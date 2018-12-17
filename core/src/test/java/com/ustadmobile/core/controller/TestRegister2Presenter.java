package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.Register2View;
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

import static com.ustadmobile.core.controller.TestLogin2Presenter.TEST_URI;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TestRegister2Presenter {

    private UstadMobileSystemImpl systemImplSpy;

    private HttpServer server;

    private UmAppDatabase repo;

    private UmAppDatabase clientDb;

    private static final String VALID_PASS = "secret";

    private static final String DESTINATION = "somewhere";

    private Register2View mockView;

    private Person testPerson;

    private Hashtable args;



    public static HttpServer startServer() {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .packages("com.ustadmobile.core.db.dao");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(TEST_URI), resourceConfig);
    }

    @Before
    public void setUp() {
        UstadMobileSystemImpl mainImpl = UstadMobileSystemImpl.getInstance();
        systemImplSpy = Mockito.spy(mainImpl);
        UstadMobileSystemImpl.setMainInstance(systemImplSpy);
        server = startServer();

        UmAppDatabase db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        clientDb = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext(), "db1");
        repo = db.getRepository(TEST_URI, "");

        db.clearAllTables();
        clientDb.clearAllTables();

        testPerson = new Person();
        testPerson.setEmailAddr("johndoe@example.com");
        testPerson.setUsername("John");
        testPerson.setLastName("Doe");



        args = new Hashtable();
        args.put(Login2Presenter.ARG_NEXT, DESTINATION);

        mockView = Mockito.mock(Register2View.class);
        doAnswer((invocationOnMock) -> {
            new Thread(((Runnable)invocationOnMock.getArgument(0))).start();
            return null;
        }).when(mockView).runOnUiThread(any());
    }

    @After
    public void tearDown(){
        server.shutdownNow();
    }


    @Test
    public void givenExistingPersonDetails_whenHandleRegisterCalled_thenShouldNotCreateAccount() {
        repo.getPersonDao().insert(testPerson);
        Register2Presenter presenter =
                new Register2Presenter(PlatformTestUtil.getTargetContext(), args, mockView);
        presenter.handleClickRegister(testPerson,VALID_PASS,TEST_URI);

        String expectedErrorMsg = UstadMobileSystemImpl.getInstance().getString(
                MessageID.err_registering_new_user, PlatformTestUtil.getTargetContext());
        verify(mockView, timeout(5000)).setErrorMessageView(expectedErrorMsg);
    }

    @Test
    public void givenNewPersonDetails_whenHandleRegisterCalled_thenShouldCreateAnAccountAndGenerateAuthToken() {
        Register2Presenter presenter =
                new Register2Presenter(PlatformTestUtil.getTargetContext(), args, mockView);
        presenter.setClientDb(clientDb);
        presenter.setRepo(repo);
        presenter.handleClickRegister(testPerson,VALID_PASS,TEST_URI);

        verify(systemImplSpy, timeout(5000 * 100)).go(DESTINATION,
                PlatformTestUtil.getTargetContext());

        UmAccount activeAccount = UmAccountManager.getActiveAccount(
                PlatformTestUtil.getTargetContext());
        Assert.assertNotNull(activeAccount);
        Assert.assertNotEquals("Active account uid is set ( != 0 )",
                activeAccount.getPersonUid(), 0);

        Assert.assertNotNull("Person object created on client",
                clientDb.getPersonDao().findByUid(activeAccount.getPersonUid()));
        Assert.assertNotNull("Person object created on server",
                repo.getPersonDao().findByUid(activeAccount.getPersonUid()));
    }

    @Test
    public void givenServerOffline_whenHandleRegisterCalled_thenShouldCallSetErrorMessage(){
        Hashtable args = new Hashtable();
        server.shutdownNow();

        Register2Presenter presenter = new Register2Presenter(PlatformTestUtil.getTargetContext(),
                args, mockView);
        presenter.handleClickRegister(testPerson, VALID_PASS, TEST_URI);

        String expectedErrorMsg = UstadMobileSystemImpl.getInstance().getString(
                MessageID.login_network_error, PlatformTestUtil.getTargetContext());
        verify(mockView, timeout(5000)).setErrorMessageView(expectedErrorMsg);
    }
}
