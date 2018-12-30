package com.ustadmobile.lib.db;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmAppDatabase_Jdbc;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.PersonAuthDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.UmAccount;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.Target;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TestClazzDao {

    public static final String TEST_URI = "http://localhost:8089/api/";

    private static final int SYNC_SEND_LIMIT = 100;

    private static final int SYNC_RECEIVE_LIMIT = 100;

    private UmAppDatabase serverDb;

    private UmAppDatabase clientDb;

    private UmAppDatabase clientRepo;

    private Person accountPerson;

    private static final String TEST_USERNAME = "testuser";

    private static final String TEST_PASSWORD = "secret";

    private String accessToken;

    private HttpServer server;

    @Before
    public void setup() throws IOException{
        UmAppDatabase.setInstance(new UmAppDatabase_Jdbc(null, "UmAppDatabase"));
        UmAppDatabase.setInstance(new UmAppDatabase_Jdbc(null, "db1"), "db1");

        serverDb = UmAppDatabase.getInstance(null);
        clientDb = UmAppDatabase.getInstance(null, "db1");
        serverDb.clearAllTables();
        clientDb.clearAllTables();

        startServer();

        accountPerson = new Person();
        accountPerson.setFirstNames("Test");
        accountPerson.setLastName("Account");
        accountPerson.setUsername(TEST_USERNAME);
        accountPerson.setPersonUid(serverDb.getRepository("http://localhost/dummy/", "")
                .getPersonDao().insert(accountPerson));
        PersonAuth personAuth = new PersonAuth(accountPerson.getPersonUid(),
                PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                        PersonAuthDao.encryptPassword(TEST_PASSWORD));
        serverDb.getRepository("http://localhost/dummy/", "").getPersonDao()
                .insertPersonAuth(personAuth);

        AtomicReference<UmAccount> accountRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        clientDb.getRepository(TEST_URI, "").getPersonDao()
                .login(TEST_USERNAME, TEST_PASSWORD, new UmCallback<UmAccount>() {
                    @Override
                    public void onSuccess(UmAccount result) {
                        accountRef.set(result);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        System.err.println("ERROR: TestClazz failed to login");
                        latch.countDown();
                    }
                });

        try { latch.await(10, TimeUnit.SECONDS); }
        catch(InterruptedException e) {
            //should not happen
        }

        accessToken = accountRef.get().getAuth();

        clientRepo = clientDb.getRepository(TEST_URI, accessToken);


    }

    private void startServer() throws IOException {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .packages("com.ustadmobile.core.db.dao");
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(TEST_URI), resourceConfig);
        server.start();
    }

    public void tearDown() {
        server.shutdownNow();
    }


    @Test
    public void givenAccountWithDirectClazzPermission_whenSynced_thenShouldBePresentOnLocalDb() {
        ClazzDao dao = UmAppDatabase.getInstance(null).getClazzDao();
        Assert.assertNotNull(dao);
    }

    public void givenAccountWithoutClazzPermission_whenSynced_thenShouldNotBePresentOnLocalDb() {

    }

    public void givenAccountWithLocationPermission_whenSynced_thenShouldBePresentInLocalDb() {

    }



}
