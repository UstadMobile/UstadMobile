package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.ChangePasswordView;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.core.view.SelectLanguageDialogView;
import com.ustadmobile.core.view.UserProfileView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;

import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.lib.db.entities.UmAccount;

/**
 * Presenter for UserProfile view
 **/
public class UserProfilePresenter extends UstadBaseController<UserProfileView> {

    private UmProvider<Person> umProvider;
    UmAppDatabase repository;
    private PersonDao personDao;

    private long loggedInPersonUid = 0L;


    public UserProfilePresenter(Object context, Hashtable arguments, UserProfileView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        personDao = repository.getPersonDao();


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        UmAccount activeAccount = UmAccountManager.getActiveAccount(context);

        if(activeAccount != null){
            loggedInPersonUid = activeAccount.getPersonUid();
            personDao.findByUidAsync(loggedInPersonUid, new UmCallback<Person>() {
                @Override
                public void onSuccess(Person result) {
                    if(result!=null) {
                        String personName = result.getFirstNames() + " " + result.getLastName();
                        view.updateToolbarTitle(personName);
                    }
                }

                @Override
                public void onFailure(Throwable exception) {exception.printStackTrace();}
            });
        }


    }

    public void handleClickChangePassword(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        impl.go(ChangePasswordView.VIEW_NAME, args, context);
    }

    public void handleClickChangeLanguage(){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        impl.go(SelectLanguageDialogView.VIEW_NAME, args, context);

    }

    public void handleClickLogout(){
        UmAccountManager.setActiveAccount(null, context);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable<String, String> args = new Hashtable<>();
        impl.go(Login2View.VIEW_NAME, args, context);
    }

}
