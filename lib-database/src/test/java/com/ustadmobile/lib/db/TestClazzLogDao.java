package com.ustadmobile.lib.db;

import com.ustadmobile.lib.db.entities.ClazzLog;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestClazzLogDao extends AbstractDaoTest{

    @Before
    public void setUp() {
        initDb();
    }



    @Test
    public void givenClazzLogInsertedByAccountWithPermission_whenSynced_thenShouldBeOnServer() {
        grantTeacherRoleOnMyClazzToAccountPerson();

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(),
                100, 100);

        //Make a ClazzLog
        ClazzLog clazzLog = new ClazzLog();
        clazzLog.setClazzLogClazzUid(myClazz.getClazzUid());
        clazzLog.setClazzLogUid(clientRepo.getClazzLogDao().insert(clazzLog));

        clientDb.getClazzLogDao().syncWith(clientRepo.getClazzLogDao(), accountPerson.getPersonUid(),
                100, 100);

        Assert.assertNotNull(serverDb.getClazzLogDao().findByUid(clazzLog.getClazzLogUid()));
    }



}
