package com.ustadmobile.lib.db;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.PersonAuthDao;
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
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class AbstractDaoTest {

    protected UmAppDatabase serverDb;

    protected UmAppDatabase clientDb;

    protected UmAppDatabase clientRepo;

    public static final String TEST_URI = "http://localhost:8089/api/";

    private static final int SYNC_SEND_LIMIT = 100;

    private static final int SYNC_RECEIVE_LIMIT = 100;

    protected Person accountPerson;

    protected PersonGroup accountPersonGroup;

    protected Clazz myClazz;

    protected Clazz otherClazz;

    protected Location myClazzLocation;

    protected Location otherClazzLocation;

    protected static final String TEST_USERNAME = "testuser";

    protected static final String TEST_PASSWORD = "secret";

    protected String accessToken;

    protected static HttpServer server;

    protected static final String TEST_CLAZZ_NAME = "Test Clazz";

    public void initDb() {
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

        myClazzLocation = new Location("Test location", "test location desc",
                "Asia/Beirut");
        myClazzLocation.setLocationUid(serverDummyRepo.getLocationDao().insert(myClazzLocation));

        LocationAncestorJoin locationJoin = new LocationAncestorJoin(myClazzLocation.getLocationUid(),
                myClazzLocation.getLocationUid());
        serverDb.getLocationAncestorJoinDao().insert(locationJoin);

        otherClazzLocation = new Location("Other location", "test other location",
                "Asia/Beirut");
        otherClazzLocation.setLocationUid(serverDummyRepo.getLocationDao()
                .insert(otherClazzLocation));

        LocationAncestorJoin otherLocationJoin = new LocationAncestorJoin(
                otherClazzLocation.getLocationUid(), otherClazzLocation.getLocationUid());
        serverDb.getLocationAncestorJoinDao().insert(otherLocationJoin);


        myClazz = new Clazz(TEST_CLAZZ_NAME, myClazzLocation.getLocationUid());
        myClazz.setClazzUid(serverDummyRepo.getClazzDao().insert(myClazz));

        otherClazz = new Clazz("Other Clazz");
        otherClazz.setClazzUid(serverDummyRepo.getClazzDao().insert(otherClazz));

        AtomicReferenceCallback<UmAccount> callbackRef = new AtomicReferenceCallback<>();
        clientDb.getRepository(TEST_URI, "").getPersonDao()
                .login(TEST_USERNAME, TEST_PASSWORD, callbackRef);

        accessToken = callbackRef.getResult(5, TimeUnit.SECONDS).getAuth();
        clientRepo = clientDb.getRepository(TEST_URI, accessToken);

    }

    protected void grantTeacherRoleOnMyClazzToAccountPerson() {
        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/",
                "");
        long teacherPermissions =
                Role.PERMISSION_CLAZZ_ADD_STUDENT |
                        Role.PERMISSION_CLAZZ_SELECT |                  //See Clazzes
                        Role.PERMISSION_CLAZZ_UPDATE |                  //Update Clazz
                        Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT |     //See Clazz Activity
                        Role.PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE |     //Update Clazz Activity
                        Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT |     //Add/Take Clazz Activities
                        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT |   //See Attendance
                        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT |   //Take attendance
                        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE |   //Update attendance
                        Role.PERMISSION_PERSON_SELECT  |                //See People
                        Role.PERMISSION_PERSON_UPDATE |                 //Update people
                        Role.PERMISSION_PERSON_INSERT;
        Role teacherRole = new Role("teacher", teacherPermissions);
        teacherRole.setRoleUid(serverDummyRepo.getRoleDao().insert(teacherRole));

        EntityRole entityRole = new EntityRole(Clazz.TABLE_ID, myClazz.getClazzUid(),
                accountPersonGroup.getGroupUid(), teacherRole.getRoleUid());
        serverDummyRepo.getEntityRoleDao().insert(entityRole);

    }

    @BeforeClass
    public static void startServer() throws IOException {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .packages("com.ustadmobile.core.db.dao")
                .register(MultiPartFeature.class);
        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(TEST_URI), resourceConfig);
        server.start();
    }

    @AfterClass
    public static void stopServer() {
        server.shutdownNow();
    }



}
