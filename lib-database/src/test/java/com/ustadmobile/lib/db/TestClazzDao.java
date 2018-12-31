package com.ustadmobile.lib.db;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmAppDatabase_Jdbc;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.PersonAuthDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.LocationAncestorJoin;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.PersonGroup;
import com.ustadmobile.lib.db.entities.PersonGroupMember;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.entities.UmAccount;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
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

    private Clazz myClazz;

    private Clazz otherClazz;

    private Location myClazzLocation;

    private Location otherClazzLocation;

    private static final String TEST_USERNAME = "testuser";

    private static final String TEST_PASSWORD = "secret";

    private String accessToken;

    private static HttpServer server;

    private static final String TEST_CLAZZ_NAME = "Test Clazz";

    @Before
    public void setup() {
        UmAppDatabase.setInstance(new UmAppDatabase_Jdbc(null, "UmAppDatabase"));
        UmAppDatabase.setInstance(new UmAppDatabase_Jdbc(null, "db1"), "db1");

        serverDb = UmAppDatabase.getInstance(null);
        clientDb = UmAppDatabase.getInstance(null, "db1");
        serverDb.clearAllTables();
        clientDb.clearAllTables();


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

        myClazzLocation = new Location("Test location", "test location desc");
        myClazzLocation.setLocationUid(serverDummyRepo.getLocationDao().insert(myClazzLocation));

        LocationAncestorJoin locationJoin = new LocationAncestorJoin(myClazzLocation.getLocationUid(),
                myClazzLocation.getLocationUid());
        serverDb.getLocationAncestorJoinDao().insert(locationJoin);

        otherClazzLocation = new Location("Other location", "test other location");
        otherClazzLocation.setLocationUid(serverDummyRepo.getLocationDao()
                .insert(otherClazzLocation));

        LocationAncestorJoin otherLocationJoin = new LocationAncestorJoin(
                otherClazzLocation.getLocationUid(), otherClazzLocation.getLocationUid());
        serverDb.getLocationAncestorJoinDao().insert(otherLocationJoin);


        myClazz = new Clazz(TEST_CLAZZ_NAME, myClazzLocation.getLocationUid());
        myClazz.setClazzUid(serverDummyRepo.getClazzDao().insert(myClazz));

        otherClazz = new Clazz("Other Clazz");
        otherClazz.setClazzUid(serverDummyRepo.getClazzDao().insert(otherClazz));

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


    @BeforeClass
    public static void startServer() throws IOException {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .packages("com.ustadmobile.core.db.dao");
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(TEST_URI), resourceConfig);
        server.start();
    }

    @AfterClass
    public static void tearDown() {
        server.shutdownNow();
    }


    @Test
    public void givenAccountWithDirectClazzPermission_whenSynced_thenEntitiesWithPermissionShouldBeOnClientDb() {
        ClazzDao dao = UmAppDatabase.getInstance(null).getClazzDao();
        Assert.assertNotNull(dao);

        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/",
                "");

        Role teacherRole = new Role("teacher",
                Role.PERMISSION_CLAZZ_RECORD_ACTIVITY
                | Role.PERMISSION_CLAZZ_SELECT);
        teacherRole.setRoleUid(serverDummyRepo.getRoleDao().insert(teacherRole));

        EntityRole entityRole = new EntityRole(Clazz.TABLE_ID, myClazz.getClazzUid(),
                accountPersonGroup.getGroupUid(), teacherRole.getRoleUid());
        serverDummyRepo.getEntityRoleDao().insert(entityRole);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(),
                100, 100);

        Assert.assertNotNull("Clazz synced to client when permission present",
                clientDb.getClazzDao().findByUid(myClazz.getClazzUid()));
        Assert.assertNull("Other clazz that account does not have permission to view is not present",
                clientDb.getClazzDao().findByUid(otherClazz.getClazzUid()));
    }


    @Test
    public void givenClazzUpdatedLocallyByAccountWithPermission_whenSynced_thenShouldBeUpdatedOnServer() {
        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/",
                "");
        Role teacherRole = new Role("teacher",
                Role.PERMISSION_CLAZZ_RECORD_ACTIVITY
                        | Role.PERMISSION_CLAZZ_SELECT | Role.PERMISSION_CLAZZ_UPDATE);
        teacherRole.setRoleUid(serverDummyRepo.getRoleDao().insert(teacherRole));

        EntityRole entityRole = new EntityRole(Clazz.TABLE_ID, myClazz.getClazzUid(),
                accountPersonGroup.getGroupUid(), teacherRole.getRoleUid());
        serverDummyRepo.getEntityRoleDao().insert(entityRole);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);
        boolean clazzPresentOnClientAfterSync =
                clientDb.getClazzDao().findByUid(myClazz.getClazzUid()) != null;

        String newClazzName = myClazz.getClazzName() + System.currentTimeMillis();
        myClazz.setClazzName(newClazzName);
        clientRepo.getClazzDao().update(myClazz);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        Assert.assertTrue("Clazz was synced to client after first sync ",
                clazzPresentOnClientAfterSync);
        Assert.assertEquals("After sync, clazz entity updated on server", newClazzName,
                serverDb.getClazzDao().findByUid(myClazz.getClazzUid()).getClazzName());
    }

    @Test
    public void givenClazzUpdatedLocallyByAccountWithoutPermission_whenSynced_thenShouldNotBeUpdatedOnServer() {
        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/",
                "");
        Role readOnlyRole = new Role("teacher", Role.PERMISSION_CLAZZ_SELECT );
        readOnlyRole.setRoleUid(serverDummyRepo.getRoleDao().insert(readOnlyRole));

        EntityRole entityRole = new EntityRole(Clazz.TABLE_ID, myClazz.getClazzUid(),
                accountPersonGroup.getGroupUid(), readOnlyRole.getRoleUid());
        serverDummyRepo.getEntityRoleDao().insert(entityRole);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        String newClazzName = myClazz.getClazzName() + System.currentTimeMillis();
        myClazz.setClazzName(newClazzName);
        clientRepo.getClazzDao().update(myClazz);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        Assert.assertEquals("After sync, given account has only select permission, clazz " +
                        "entity is not updated on server (retains old name)", TEST_CLAZZ_NAME,
                serverDb.getClazzDao().findByUid(myClazz.getClazzUid()).getClazzName());
    }

    @Test
    public void givenAccountHasSelectPermissionOverLocation_whenSynced_thenShouldBePresentInClientDb() {
        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/",
                "");
        Role readOnlyRole = new Role("teacher", Role.PERMISSION_CLAZZ_SELECT );
        readOnlyRole.setRoleUid(serverDummyRepo.getRoleDao().insert(readOnlyRole));

        EntityRole locationRole = new EntityRole(Location.TABLE_ID,
                myClazzLocation.getLocationUid(), accountPersonGroup.getGroupUid(),
                readOnlyRole.getRoleUid());
        serverDummyRepo.getEntityRoleDao().insert(locationRole);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        Assert.assertNotNull("After account has permission over location, clazz entity is " +
                "present on client db", clientDb.getClazzDao().findByUid(myClazz.getClazzUid()));
        Assert.assertNull("After sync, clazz in a different location is not synced to client",
                clientDb.getClazzDao().findByUid(otherClazz.getClazzUid()));
    }

    @Test
    public void givenAccountWithInsertPermission_whenSynced_thenShouldCreateNewClazz() {
        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/",
                "");
        Role insertRole = new Role("insert role", Role.PERMISSION_CLAZZ_INSERT);
        insertRole.setRoleUid(serverDummyRepo.getRoleDao().insert(insertRole));

        EntityRole insertEntityRole = new EntityRole(Clazz.TABLE_ID,
                0, accountPersonGroup.getGroupUid(), insertRole.getRoleUid());
        serverDummyRepo.getEntityRoleDao().insert(insertEntityRole);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        Clazz newClazz = new Clazz("New Clazz", 0);
        newClazz.setClazzUid(clientRepo.getClazzDao().insert(newClazz));

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        Assert.assertNotNull("After sync when account has insert permission, entity is on server",
                serverDb.getClazzDao().findByUid(newClazz.getClazzUid()));
    }

    @Test
    public void givenAccountWithoutInsertPermission_whenSynced_thenShouldNotCreateNewClazz() {
        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/",
                "");
        Role insertRole = new Role("insert role", Role.PERMISSION_CLAZZ_UPDATE);
        insertRole.setRoleUid(serverDummyRepo.getRoleDao().insert(insertRole));

        EntityRole insertEntityRole = new EntityRole(Clazz.TABLE_ID,
                0, accountPersonGroup.getGroupUid(), insertRole.getRoleUid());
        serverDummyRepo.getEntityRoleDao().insert(insertEntityRole);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        Clazz newClazz = new Clazz("New Clazz", 0);
        newClazz.setClazzUid(clientRepo.getClazzDao().insert(newClazz));

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        Assert.assertNull("After sync when account does not have insert permission, entity " +
                        "is not on server", serverDb.getClazzDao().findByUid(newClazz.getClazzUid()));
    }



}
