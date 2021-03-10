package com.ustadmobile.lib.db;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzEnrolment;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.LocationAncestorJoin;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonLocationJoin;
import com.ustadmobile.lib.db.entities.Role;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestPersonDao extends AbstractDaoTest{

    private Person myStudent;

    private Person otherStudent;

    private Location myLocation;

    @Before
    public void setup() {
        initDb();
    }

    private void initAddStudentToMyClazzAndGrantPermission(long permission) {
        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/", "");
        myStudent = new Person("somebody", "bob", "jones");
        myStudent.setPersonUid(serverDummyRepo.getPersonDao().insert(myStudent));

        otherStudent = new Person("someoneelse", "another", "stranger");
        otherStudent.setPersonUid(serverDummyRepo.getPersonDao().insert(otherStudent));

        ClazzEnrolment studentClazzEnrolment = new ClazzEnrolment(myClazz.getClazzUid(),
                myStudent.getPersonUid());
        serverDummyRepo.getClazzEnrolmentDao().insert(studentClazzEnrolment);

        Role teacherRole = new Role("Teacher", permission);
        teacherRole.setRoleUid(serverDummyRepo.getRoleDao().insert(teacherRole));

        EntityRole clazzTeacherRole = new EntityRole(Clazz.TABLE_ID, myClazz.getClazzUid(),
                accountPersonGroup.getGroupUid(), teacherRole.getRoleUid());
        serverDummyRepo.getEntityRoleDao().insert(clazzTeacherRole);
    }

    private void initAddPersonToLocationAndGrantPermission(long permission) {
        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/", "");

        myStudent = new Person("somebody", "bob", "jones");
        myStudent.setPersonUid(serverDummyRepo.getPersonDao().insert(myStudent));

        otherStudent = new Person("someoneelse", "another", "stranger");
        otherStudent.setPersonUid(serverDummyRepo.getPersonDao().insert(otherStudent));

        myLocation = new Location("No Mans Land", "Dont come here");
        myLocation.setLocationUid(serverDummyRepo.getLocationDao().insert(myLocation));

        LocationAncestorJoin locationAncestorJoin = new LocationAncestorJoin(
                myLocation.getLocationUid(), myLocation.getLocationUid());
        serverDb.getLocationAncestorJoinDao().insert(locationAncestorJoin);

        PersonLocationJoin personLocation = new PersonLocationJoin(myStudent, myLocation);
        serverDummyRepo.getPersonLocationJoinDao().insert(personLocation);

        Role testRole= new Role("Test", permission);
        testRole.setRoleUid(serverDummyRepo.getRoleDao().insert(testRole));

        EntityRole entityRole = new EntityRole(Location.TABLE_ID, myLocation.getLocationUid(),
                accountPersonGroup.getGroupUid(), testRole.getRoleUid());
        serverDummyRepo.getEntityRoleDao().insert(entityRole);
    }


    @Test
    public void givenAccountWithDirectPersonSelectPermission_whenSynced_thenEntitiesBeInClientDb() {
        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/", "");
        Person newPerson = new Person("somebody", "Bob", "Jones");
        newPerson.setPersonUid(serverDummyRepo.getPersonDao().insert(newPerson));
        Person otherPerson = new Person("someoneelse", "Another", "Stranger");
        otherPerson.setPersonUid(serverDummyRepo.getPersonDao().insert(otherPerson));

        Role personSelectRole = new Role("Person Select", Role.PERMISSION_PERSON_SELECT);
        personSelectRole.setRoleUid(serverDummyRepo.getRoleDao().insert(personSelectRole));

        EntityRole entityRole = new EntityRole(Person.TABLE_ID, newPerson.getPersonUid(),
                accountPersonGroup.getGroupUid(), personSelectRole.getRoleUid());
        serverDummyRepo.getEntityRoleDao().insert(entityRole);


        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);


        Assert.assertNotNull("After being granted direct permission on person, after sync " +
                "entity is in client db",
                clientRepo.getPersonDao().findByUid(newPerson.getPersonUid()));
        Assert.assertNull("For person entity where the account does not have select " +
                "permission, after sync, the person is not in client db",
                clientRepo.getPersonDao().findByUid(otherPerson.getPersonUid()));
    }

    @Test
    public void givenAccountWithPersonSelectPermissionOverClazz_whenSynced_thenPersonEntitiesAreInClientDb() {
        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/", "");

        initAddStudentToMyClazzAndGrantPermission(Role.PERMISSION_PERSON_SELECT |
            Role.PERMISSION_CLAZZ_SELECT | Role.PERMISSION_CLAZZ_UPDATE);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        Assert.assertNotNull("When user is granted PERSON_SELECT permission over class " +
                " the Person object for a person in that class is synced.",
                clientDb.getPersonDao().findByUid(myStudent.getPersonUid()));
        Assert.assertNull("When a user is granted PERSON_SELECT permission over a class " +
                "a Person not in that class is not synced",
                clientDb.getPersonDao().findByUid(otherStudent.getPersonUid()));

    }

    @Test
    public void givenPersonUpdatedByAccountWithUpdatePermissionOverClazz_whenSynced_thenShouldBeUpdatedOnServer() {
        initAddStudentToMyClazzAndGrantPermission(Role.PERMISSION_CLAZZ_SELECT |
                Role.PERMISSION_PERSON_SELECT | Role.PERMISSION_PERSON_UPDATE);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        String newFirstname = myStudent.getFirstNames() + System.currentTimeMillis();
        myStudent.setFirstNames(newFirstname);
        clientRepo.getPersonDao().update(myStudent);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        Assert.assertEquals("After local updateState by account with updateState permission granted" +
                        " by clazz, person name is updated on server", newFirstname,
                serverDb.getPersonDao().findByUid(myStudent.getPersonUid()).getFirstNames());
    }

    @Test
    public void givenPersonUpdatedByAccountWithoutUpdatePermissionOverClazz_whenSynced_thenShouldNotBeChangedOnServer(){
        initAddStudentToMyClazzAndGrantPermission(Role.PERMISSION_CLAZZ_SELECT |
                Role.PERMISSION_PERSON_SELECT);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        String oldFirstname = myStudent.getFirstNames();
        String newFirstname = myStudent.getFirstNames() + System.currentTimeMillis();
        myStudent.setFirstNames(newFirstname);
        clientRepo.getPersonDao().update(myStudent);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        Assert.assertEquals("After local updateState by account with updateState permission granted" +
                        " by clazz, person name is updated on server", oldFirstname,
                serverDb.getPersonDao().findByUid(myStudent.getPersonUid()).getFirstNames());
    }

    @Test
    public void givenAccountWithSelectionPermissionOverLocation_whenSynced_thenRelatedEntitiesShouldBeInClientDb() {
        initAddPersonToLocationAndGrantPermission(Role.PERMISSION_PERSON_SELECT);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        Assert.assertNotNull("When account is granted select permission over location, " +
                "person data is synced", clientDb.getPersonDao().findByUid(accountPerson.getPersonUid()));
        Assert.assertNull("When account is granted select permission over location, " +
                "person data not in that location is not synced to client",
                clientDb.getPersonDao().findByUid(otherStudent.getPersonUid()));
    }


}
