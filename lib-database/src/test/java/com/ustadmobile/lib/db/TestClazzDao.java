package com.ustadmobile.lib.db;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Role;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class TestClazzDao extends AbstractDaoTest{


    @Before
    public void setup() {
        initDb();
    }

    @Test
    public void givenAccountWithDirectClazzSelectPermission_whenSynced_thenEntitiesWithPermissionShouldBeOnClientDb() {
        ClazzDao dao = UmAppDatabase.getInstance(null).getClazzDao();
        Assert.assertNotNull(dao);

        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/",
                "");

        Role teacherRole = new Role("teacher",
                Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT
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

        AtomicReferenceCallback<Boolean> callbackRef = new AtomicReferenceCallback<>();
        clientDb.getClazzDao().personHasPermission(accountPerson.getPersonUid(),
                myClazz.getClazzUid(), Role.PERMISSION_CLAZZ_SELECT, callbackRef);
        Assert.assertTrue("DAO answers that user has permission to select their clazz",
                callbackRef.getResult(5, TimeUnit.SECONDS));


        callbackRef = new AtomicReferenceCallback<>();
        clientDb.getClazzDao().personHasPermission(accountPerson.getPersonUid(),
                otherClazz.getClazzUid(), Role.PERMISSION_CLAZZ_SELECT, callbackRef);
        Assert.assertFalse("DAO answers that user does not have permission to selec other clazz",
                callbackRef.getResult(5, TimeUnit.SECONDS));
    }


    @Test
    public void givenClazzUpdatedLocallyByAccountWithPermission_whenSynced_thenShouldBeUpdatedOnServer() {
        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/",
                "");
        Role teacherRole = new Role("teacher",
                Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT
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
