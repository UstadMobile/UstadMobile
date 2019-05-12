package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.dao.PersonAuthDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import java.util.Hashtable;
import java.util.List;

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
    private PersonDao personDao;
    private PersonAuthDao personAuthDao;

    private Person currentPerson;
    private PersonAuth currentPersonAuth;
    private long loggedInPersonUid = 0L;
    private String currentPassword;
    private String updatePassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getUpdatePassword() {
        return updatePassword;
    }

    public void setUpdatePassword(String updatePassword) {
        this.updatePassword = updatePassword;
    }

    public String getUpdatePasswordConfirm() {
        return updatePasswordConfirm;
    }

    public void setUpdatePasswordConfirm(String updatePasswordConfirm) {
        this.updatePasswordConfirm = updatePasswordConfirm;
    }

    private String updatePasswordConfirm;


    public ChangePasswordPresenter(Object context, Hashtable arguments, ChangePasswordView view) {
        super(context, arguments, view);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);

        repository = UmAccountManager.getRepositoryForActiveAccount(context);
        personDao = repository.getPersonDao();
        personAuthDao = repository.getPersonAuthDao();
        UmAccount cp = UmAccountManager.getActiveAccount(context);
        if(cp!=null){
            loggedInPersonUid = cp.getPersonUid();
        }


    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if (loggedInPersonUid != 0) {
            personDao.findByUidAsync(loggedInPersonUid, new UmCallback<Person>() {
                @Override
                public void onSuccess(Person result) {
                    currentPerson = result;
                    if(currentPerson != null){

                        personAuthDao.findByUidAsync(loggedInPersonUid, new UmCallback<PersonAuth>() {
                            @Override
                            public void onSuccess(PersonAuth result) {
                                currentPersonAuth = result;
                                if (result == null) {
                                    currentPersonAuth = new PersonAuth();
                                    currentPersonAuth.setPersonAuthUid(loggedInPersonUid);
                                }
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

    public void handleClickSave() {
        if(updatePassword != null && !updatePassword.isEmpty()  && updatePasswordConfirm != null &&
                !updatePasswordConfirm.isEmpty() && currentPersonAuth != null && currentPerson != null ){
            if(!updatePassword.equals(updatePasswordConfirm)){
                view.sendMessage(MessageID.passwords_dont_match);
                return;
            }

            currentPersonAuth.setPasswordHash(PersonAuthDao.encryptPassword(updatePassword));

            personAuthDao.updateAsync(currentPersonAuth, new UmCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {

                    personAuthDao.selfResetPassword(currentPerson.getUsername(), currentPassword, updatePassword, loggedInPersonUid,
                      new UmCallback<Integer>() {
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



}
