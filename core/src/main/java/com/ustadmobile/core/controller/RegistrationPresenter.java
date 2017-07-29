package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.RegistrationView;
import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by varuna on 7/28/2017.
 */

public class RegistrationPresenter extends UstadBaseController {

    private RegistrationView view;

    //TODO: Remove. Instead get it from build config
    public int[] extraFields = new int[]{MessageID.field_university,
            MessageID.field_fullname, MessageID.field_gender,
            MessageID.field_email, MessageID.field_phonenumber,
            MessageID.field_faculty};

    public RegistrationPresenter(Object context, RegistrationView view) {
        super(context);
        this.view = view;
        //TODO: Replace with values from build config
        for(int field:extraFields){
            view.addField(field, 0);
        }


    }

    @Override
    public void setUIStrings() {
        //Doens't do much
    }

    /**
     * Handle register link in Registration view
     */
    public void handleClickRegister(String username, String password, HashMap fields) {

        //TODO: Remove this after logged in user is set
        Object context = getContext();
        UserManager userManager =
                PersistenceManager.getInstance().getManager(UserManager.class);
        UserCustomFieldsManager userCustomFieldsManager =
                PersistenceManager.getInstance().getManager(UserCustomFieldsManager.class);

        String loggedInUsername = null;
        loggedInUsername = UstadMobileSystemImpl.getInstance().getActiveUser(context);
        //ignore loggedInUsername cause if we're clicking register, we want this user
        //to log in..

        User loggedInUser = null;
        List<User> users = userManager.findByUsername(context, username);
        if(users!= null && !users.isEmpty()){
            loggedInUser = users.get(0);
        }else{
            //create the user
            try {
                loggedInUser = (User)userManager.makeNew();
                loggedInUser.setUsername(username);
                loggedInUser.setUuid(UUID.randomUUID().toString());
                loggedInUser.setPassword(password);
                loggedInUser.setNotes("User Created via Registration Page");
                loggedInUser.setDateCreated(System.currentTimeMillis());
                userManager.persist(context, loggedInUser);

                userCustomFieldsManager.createUserCustom(fields,loggedInUser, context);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        UstadMobileSystemImpl.getInstance().setActiveUser(loggedInUser.getUsername(), context);
    }


}
