package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.dao.PersonPictureDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.io.File;
import java.util.Hashtable;

import com.ustadmobile.core.view.ChangePasswordView;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.core.view.SelectLanguageDialogView;
import com.ustadmobile.core.view.UserProfileView;

import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.lib.db.entities.Person;

import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.lib.db.entities.PersonPicture;
import com.ustadmobile.lib.db.entities.UmAccount;

/**
 * Presenter for UserProfile view
 **/
public class UserProfilePresenter extends UstadBaseController<UserProfileView> {

    private UmProvider<Person> umProvider;
    UmAppDatabase repository;
    private PersonDao personDao;
    private Person loggedInPerson;
    private PersonPictureDao personPictureDao;

    private long loggedInPersonUid = 0L;


    public UserProfilePresenter(Object context, Hashtable arguments, UserProfileView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        //Get provider Dao
        personDao = repository.getPersonDao();
        personPictureDao = repository.getPersonPictureDao();

    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        UmAccount activeAccount = UmAccountManager.getActiveAccount(context);

        if(activeAccount != null) {
            loggedInPersonUid = activeAccount.getPersonUid();
            personDao.findByUidAsync(loggedInPersonUid, new UmCallback<Person>() {
                @Override
                public void onSuccess(Person result) {
                    loggedInPerson = result;
                    if (loggedInPerson != null) {
                        String personName = result.getFirstNames() + " " + result.getLastName();
                        view.updateToolbarTitle(personName);

                        personPictureDao = repository.getPersonPictureDao();
                        personPictureDao.findByPersonUidAsync(loggedInPerson.getPersonUid(), new UmCallback<PersonPicture>() {
                            @Override
                            public void onSuccess(PersonPicture personPicture) {
                                if (personPicture != null)
                                    view.updateImageOnView(personPictureDao.getAttachmentPath(personPicture.getPersonPictureUid()));
                            }

                            @Override
                            public void onFailure(Throwable exception) {
                                exception.printStackTrace();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
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

    public void openPictureDialog(String imagePath){
        //Open Dialog
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        Hashtable args = new Hashtable();
        //TODO If needed:
//        args.put(ARG_PERSON_IMAGE_PATH, imagePath);
//        args.put(ARG_PERSON_UID, personUid);
//        impl.go(PersonPictureDialogView.VIEW_NAME, args, context);
    }

    public long getLoggedInPersonUid() {
        return loggedInPersonUid;
    }

    public void setLoggedInPersonUid(long loggedInPersonUid) {
        this.loggedInPersonUid = loggedInPersonUid;
    }

    public void handleCompressedImage(File imageFile){
        PersonPictureDao personPictureDao = repository.getPersonPictureDao();
        PersonPicture personPicture = new PersonPicture();
        personPicture.setPersonPicturePersonUid(loggedInPersonUid);
        personPicture.setPicTimestamp(System.currentTimeMillis());

        PersonDao personDao = repository.getPersonDao();

        personPictureDao.insertAsync(personPicture, new UmCallback<Long>() {
            @Override
            public void onSuccess(Long personPictureUid) {
                personPictureDao.setAttachmentFromTmpFile(personPictureUid, imageFile);

                //Update person and generate feeds for person
                personDao.updateAsync(loggedInPerson, new UmCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer result) {
                        //Do nothing
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
                view.updateImageOnView(personPictureDao.getAttachmentPath(personPictureUid));
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

    }
}
