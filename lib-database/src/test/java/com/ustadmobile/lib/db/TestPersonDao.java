package com.ustadmobile.lib.db;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.Role;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestPersonDao extends AbstractDaoTest{

    @Before
    public void setup() {
        initDb();
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

        Person studentPerson = new Person("somebody", "bob", "jones");
        studentPerson.setPersonUid(serverDummyRepo.getPersonDao().insert(studentPerson));

        Person otherPerson = new Person("someoneelse", "another", "stranger");
        otherPerson.setPersonUid(serverDummyRepo.getPersonDao().insert(otherPerson));

        ClazzMember studentClazzMember = new ClazzMember(myClazz.getClazzUid(),
                studentPerson.getPersonUid());
        serverDummyRepo.getClazzMemberDao().insert(studentClazzMember);

        Role teacherRole = new Role("Teacher", Role.PERMISSION_PERSON_SELECT |
            Role.PERMISSION_CLAZZ_SELECT | Role.PERMISSION_CLAZZ_UPDATE);
        teacherRole.setRoleUid(serverDummyRepo.getRoleDao().insert(teacherRole));

        EntityRole clazzTeacherRole = new EntityRole(Clazz.TABLE_ID, myClazz.getClazzUid(),
                accountPersonGroup.getGroupUid(), teacherRole.getRoleUid());
        serverDummyRepo.getEntityRoleDao().insert(clazzTeacherRole);

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(), 100, 100);

        Assert.assertNotNull("When user is granted PERSON_SELECT permission over class " +
                " the Person object for a person in that class is synced.",
                clientDb.getPersonDao().findByUid(studentPerson.getPersonUid()));
        Assert.assertNull("When a user is granted PERSON_SELECT permission over a class " +
                "a Person not in that class is not synced",
                clientDb.getPersonDao().findByUid(otherPerson.getPersonUid()));

    }



}
