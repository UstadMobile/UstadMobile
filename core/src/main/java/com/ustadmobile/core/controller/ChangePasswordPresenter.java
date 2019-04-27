package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.dao.PersonAuthDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;

import com.ustadmobile.core.view.ChangePasswordView;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.UmAccount;


/**
 * Presenter for ChangePassword view
 **/
public class ChangePasswordPresenter extends UstadBaseController<ChangePasswordView> {

    UmAppDatabase repository;
    private long currentPersonUid = 0;
    private PersonDao personDao;
    private PersonAuthDao personAuthDao;
    private String passwordSet;
    private String confirmPasswordSet;
    private Person currentPerson;
    private PersonAuth currentPersonAuth;
    private String usernameSet;
    private long loggedInPersonUid = 0L;


    public ChangePasswordPresenter(Object context, Hashtable arguments, ChangePasswordView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        personDao = repository.getPersonDao();
        personAuthDao = repository.getPersonAuthDao();
        UmAccount cp = UmAccountManager.getActiveAccount(context);
        if(cp!=null){
            currentPersonUid = cp.getPersonUid();
        }


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if (currentPersonUid != 0) {
            personDao.findByUidAsync(currentPersonUid, new UmCallback<Person>() {
                @Override
                public void onSuccess(Person result) {
                    currentPerson = result;
                    usernameSet = currentPerson.getUsername();
                    if (usernameSet != null) {
                        view.updateUsername(usernameSet);
                    }

                    personAuthDao.findByUidAsync(currentPersonUid, new UmCallback<PersonAuth>() {
                        @Override
                        public void onSuccess(PersonAuth result) {
                            currentPersonAuth = result;
                            if (result == null) {
                                currentPersonAuth = new PersonAuth();
                                currentPersonAuth.setPersonAuthUid(currentPersonUid);
                            }
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                        }
                    });
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });

        }
    }

    public void handleClickSave(){

    }

    public void handleClickDone() {
        if(passwordSet != null && !passwordSet.isEmpty() && usernameSet != null
                && !usernameSet.isEmpty() && currentPersonAuth != null && currentPerson != null ){
            if(!passwordSet.equals(confirmPasswordSet)){
                view.sendMessage(MessageID.passwords_dont_match);
                return;
            }
            currentPerson.setUsername(usernameSet);
            currentPersonAuth.setPasswordHash(PersonAuthDao.encryptPassword(passwordSet));
            personDao.updateAsync(currentPerson, null);

            personAuthDao.updateAsync(currentPersonAuth, new UmCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    personAuthDao.resetPassword(currentPersonUid, passwordSet,
                            loggedInPersonUid, new UmCallback<Integer>() {
                                @Override
                                public void onSuccess(Integer result) {
                                    personAuthDao.updateAsync(currentPersonAuth, new UmCallback<Integer>() {
                                        @Override
                                        public void onSuccess(Integer result) {view.finish();}

                                        @Override
                                        public void onFailure(Throwable exception) {exception.printStackTrace();
                                            view.sendMessage(MessageID.unable_to_update_password);}
                                    });
                                }

                                @Override
                                public void onFailure(Throwable exception) {
                                    view.sendMessage(MessageID.unable_to_update_password);
                                }
                            });
                }

                @Override
                public void onFailure(Throwable exception) {exception.printStackTrace();
                    view.sendMessage(MessageID.unable_to_update_password);}
            });
        }

    }

    public String getPasswordSet() {
        return passwordSet;
    }

    public void setPasswordSet(String passwordSet) {
        this.passwordSet = passwordSet;
    }

    public String getUsernameSet() {
        return usernameSet;
    }

    public void setUsernameSet(String usernameSet) {
        this.usernameSet = usernameSet;
    }

    public String getConfirmPasswordSet() {
        return confirmPasswordSet;
    }

    public void setConfirmPasswordSet(String confirmPasswordSet) {
        this.confirmPasswordSet = confirmPasswordSet;
    }

}
