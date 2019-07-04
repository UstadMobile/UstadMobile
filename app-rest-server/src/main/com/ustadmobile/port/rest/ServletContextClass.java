package com.ustadmobile.port.rest;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.EntityRoleDao;
import com.ustadmobile.core.db.dao.PersonAuthDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonGroupMemberDao;
import com.ustadmobile.core.db.dao.RoleDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServletContextClass implements ServletContextListener {
    private String dummyBaseUrl = "http://localhost/dummy/address/";
    private String dummyAuth = "dummy";
    private UmAppDatabase appDb;

    private PersonCustomFieldDao personCustomFieldDao;
    private PersonDao personDao;
    private RoleDao roleDao;
    private EntityRoleDao entityRoleDao;
    private PersonAuthDao personAuthDao;
    private PersonGroupMemberDao personGroupMemberDao;


    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        System.out.println("ServletContextListener destroyed");
    }

    //Run this before web application is started
    @Override
    public void contextInitialized(ServletContextEvent evt) {
        System.out.println("\nServletContextListener started");

        appDb = UmAppDatabase.getInstance(evt.getServletContext());
        appDb.setAttachmentsDir(evt.getServletContext().getRealPath("/WEB-INF/attachments/"));

        UmAppDatabase appDbRepository = appDb.getRepository(dummyBaseUrl, dummyAuth);


        personDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getPersonDao();
        roleDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getRoleDao();
        entityRoleDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getEntityRoleDao();
        personAuthDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getPersonAuthDao();
        personGroupMemberDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getPersonGroupMemberDao();


        //Load initial data
        loadInitialData();

    }

    private void loadInitialData(){
        //Any data goes here.

        //Create Admin
        Person adminPerson = personDao.findByUsername("admin");
        if(adminPerson == null) {
            adminPerson = new Person();
            adminPerson.setAdmin(true);
            adminPerson.setUsername("admin");
            adminPerson.setFirstNames("Admin");
            adminPerson.setLastName("Admin");
            adminPerson.setActive(true);

            adminPerson.setPersonUid(personDao.insert(adminPerson));

            PersonAuth adminPersonAuth = new PersonAuth(adminPerson.getPersonUid(),
                    PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                            PersonAuthDao.encryptPassword("golDoz1"));
            personAuthDao.insertAsync(adminPersonAuth, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    //Admin created.
                    System.out.println("ServletContextClass: Admin created. Continuing..");
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });

        }else {
            System.out.println("ServletContextClass: Admin Already created. Continuing..");
        }
    }


}
