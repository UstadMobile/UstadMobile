package com.ustadmobile.lib.db;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Role;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestClazzLogDao extends AbstractDaoTest{

    @Before
    public void setUp() {
        initDb();
    }

    protected void addScheduleToMyClazz() {
        //add scheduled occurences to myclazz

    }

    @Test
    public void givenClazzLogInsertedByAccountWithPermission_whenSynced_thenShouldBeOnServer() {
        ClazzDao dao = UmAppDatabase.getInstance(null).getClazzDao();
        Assert.assertNotNull(dao);

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

    public void givenScheduledClazzLogNotExisting_whenCreateClazzLogsCalled_thenClazzLogShouldExist() {

    }

}
