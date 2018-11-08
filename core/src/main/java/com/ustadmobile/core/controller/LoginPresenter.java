package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.lib.db.entities.Person;

import java.util.Hashtable;


/**
 * The Login Presenter - Responsible for the logic of authenticating the login details via the
 * database. It does that by comparing passwordHash to the hash on the database.
 */
public class LoginPresenter extends UstadBaseController<Login2View> {

    private PersonDao personDao;

    public static final int LOGIN_FAIL_REASON_USER_DOESNT_EXIST = 1;
    public static final int LOGIN_FAIL_REASON_AUTH_FAILED = 2;
    public static final int LOGIN_FAIL_REASON_INTERNAL_ERROR = 3;

    /**
     * Presenter initialisation - Gets the personDao instance from the database.
     *
     * @param context       The application context
     * @param arguments     The view arguments
     * @param view          The view
     */
    public LoginPresenter(Object context, Hashtable arguments, Login2View view) {
        super(context, arguments, view);
        personDao = UmAppDatabase.getInstance(context).getPersonDao();
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
    }

    /**
     * Updates the view since login was a success.
     * @param person    The new person who was authenticated succesfully.
     */
    private void loginSuccess(Person person){

    }

    /**
     * Updates the view that login failed.
     * TODO: finish
     *
     * @param username  The username of the authentication that failed.
     * @param reason    The reason flag as defined here LoginPresenter.
     */
    private void loginFail(String username, int reason){
        //1. Update the Login view with the reason

        //2. Maybe have a retry counter ?
    }

    /**
     * Authenticates the given plain text username and password against the database.
     *
     * @param username      The username of the person to be authenticated
     * @param password      The plain text password as is for the person to be authenticated
     */
    public void authenticatePerson(String username, String password) {
        //1. Hash the password TODO
        String passwordHash = password;

        //2. Query the database
        personDao.authenticateHashAsync(username, passwordHash, new UmCallback<Person>() {
            @Override
            public void onSuccess(Person result) {
                if (result != null){
                        loginSuccess(result);
                }else{
                    personDao.findByUsernameAsync(username, new UmCallback<Person>() {
                        @Override
                        public void onSuccess(Person result) {
                            if (result != null){
                                loginFail(username, LOGIN_FAIL_REASON_AUTH_FAILED);
                            }else{
                                loginFail(username, LOGIN_FAIL_REASON_USER_DOESNT_EXIST);
                            }
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                            loginFail(username, LOGIN_FAIL_REASON_INTERNAL_ERROR);
                        }
                    });

                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
                loginFail(username,LOGIN_FAIL_REASON_INTERNAL_ERROR);
            }
        });
    }

    /**
     * Overridden method. Does nothing.
     */
    @Override
    public void setUIStrings() { }

}
