package com.ustadmobile.lib.db;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmAppDatabase_Jdbc;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.PersonAuthDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.PersonGroup;
import com.ustadmobile.lib.db.entities.PersonGroupMember;
import com.ustadmobile.lib.db.entities.Role;
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

    private PersonGroup accountPersonGroup;

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


        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/",
                "");
        accountPerson = new Person();
        accountPerson.setFirstNames("Test");
        accountPerson.setLastName("Account");
        accountPerson.setUsername(TEST_USERNAME);
        accountPerson.setPersonUid(serverDummyRepo.getPersonDao().insert(accountPerson));
        PersonAuth personAuth = new PersonAuth(accountPerson.getPersonUid(),
                PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                        PersonAuthDao.encryptPassword(TEST_PASSWORD));
        serverDb.getRepository("http://localhost/dummy/", "").getPersonDao()
                .insertPersonAuth(personAuth);

        accountPersonGroup = new PersonGroup();
        accountPersonGroup.setGroupName("Test account group");
        accountPersonGroup.setGroupUid(serverDummyRepo.getPersonGroupDao()
                .insert(accountPersonGroup));

        PersonGroupMember accountGroupMember = new PersonGroupMember();
        accountGroupMember.setGroupMemberGroupUid(accountPersonGroup.getGroupUid());
        accountGroupMember.setGroupMemberPersonUid(accountPerson.getPersonUid());
        serverDummyRepo.getPersonGroupMemberDao().insert(accountGroupMember);

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
    public void givenAccountWithDirectClazzPermission_whenSynced_thenEntitiesWithPermissionShouldBeOnClientDb() {
        ClazzDao dao = UmAppDatabase.getInstance(null).getClazzDao();
        Assert.assertNotNull(dao);

        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/",
                "");


        Role teacherRole = new Role();
        teacherRole.setRoleName("teacher");
        teacherRole.setRolePermissions(Role.PERMISSION_CLAZZ_RECORD_ACTIVITY
                | Role.PERMISSION_SELECT);
        teacherRole.setRoleUid(serverDummyRepo.getRoleDao().insert(teacherRole));

        Clazz myClazz = new Clazz();
        myClazz.setClazzName("Test Clazz");
        myClazz.setClazzUid(serverDummyRepo.getClazzDao().insert(myClazz));

        Clazz otherClazz = new Clazz();
        otherClazz.setClazzName("Other clazz");
        otherClazz.setClazzUid(serverDummyRepo.getClazzDao().insert(otherClazz));


        EntityRole entityRole = new EntityRole();
        entityRole.setErEntityUid(myClazz.getClazzUid());
        entityRole.setErTableId(Clazz.TABLE_ID);
        entityRole.setErGroupUid(accountPersonGroup.getGroupUid());
        entityRole.setErRoleUid(teacherRole.getRoleUid());
        serverDummyRepo.getEntityRoleDao().insert(entityRole);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(),
                100, 100);

        Assert.assertNotNull("Clazz synced to client when permission present",
                clientDb.getClazzDao().findByUid(myClazz.getClazzUid()));
        Assert.assertNull("Other clazz that account does not have permission to view is not present",
                clientDb.getClazzDao().findByUid(otherClazz.getClazzUid()));
    }


    public void givenClazzUpdatedLocallyByAccountWithPermission_whenSynced_thenShouldBeUpdatedOnServer() {

    }



}
