package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.lib.db.entities.UmAccount;

import java.util.Hashtable;

import static com.ustadmobile.core.db.dao.PersonAuthDao.ENCRYPTED_PASS_PREFIX;
import static com.ustadmobile.core.db.dao.PersonAuthDao.encryptPassword;
import static com.ustadmobile.core.view.Login2View.ARG_LOGIN_USERNAME;

public class Login2Presenter extends UstadBaseController<Login2View> {

    static final String ARG_NEXT = "next";

    public static final String ARG_SERVER_URL = "apiUrl";

    private String mNextDest;

    public Login2Presenter(Object context, Hashtable arguments, Login2View view) {
        super(context, arguments, view);
        if(arguments != null && arguments.containsKey(ARG_NEXT)){
            mNextDest = arguments.get(ARG_NEXT).toString();
        }else {
            mNextDest = UstadMobileSystemImpl.getInstance().getAppConfigString(
                    AppConfig.KEY_FIRST_DEST, "BasePoint", context);
        }
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);


        if(getArguments() != null && getArguments().containsKey(ARG_SERVER_URL)){
            view.setServerUrl((String)getArguments().get(ARG_SERVER_URL));
        }else {
            view.setServerUrl(UstadMobileSystemImpl.getInstance().getAppConfigString(
                    AppConfig.KEY_API_URL, "http://localhost", getContext()));
        }

        if(getArguments().containsKey(ARG_LOGIN_USERNAME)){
            String username = getArguments().get(ARG_LOGIN_USERNAME).toString();
            if(username!=null && !username.isEmpty()){
                view.updateUsername(username);
            }
        }
    }

    public void handleClickLogin(String username, String password, String serverUrl, boolean saveToFingerprint) {
        view.setInProgress(true);
        view.setErrorMessage("");
        UmAppDatabase loginRepoDb = UmAppDatabase.getInstance(getContext()).getRepository(serverUrl,
                "");
        UstadMobileSystemImpl systemImpl = UstadMobileSystemImpl.getInstance();
        //Update password hash to impl
        String passwordHash = ENCRYPTED_PASS_PREFIX + encryptPassword(password);

        loginRepoDb.getPersonDao().login(username, password, new UmCallback<UmAccount>() {
            @Override
            public void onSuccess(UmAccount result) {
                if(result != null) {
                    if(saveToFingerprint){
                        UmAccountManager.setFingerprintPersonId(result.getPersonUid(), context,
                                systemImpl);
                        UmAccountManager.setFingerprintUsername(result.getUsername(), context,
                                systemImpl);
                        UmAccountManager.setFringerprintAuth(result.getAuth(), context,
                                systemImpl);
                    }

                    UmAccountManager.updateCredCache(username, result.getPersonUid(),
                            passwordHash, context, systemImpl);
                    loginOK(result, serverUrl);

                }else {
                    view.runOnUiThread(() -> {
                        view.setErrorMessage(systemImpl.getString(MessageID.wrong_user_pass_combo,
                                getContext()));
                        view.setPassword("");
                        view.setInProgress(false);
                    });
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                view.runOnUiThread(() -> {
                    //Try local login:
                    if(UmAccountManager.checkCredCache(username, passwordHash, context, systemImpl)){
                        loginOKFromOtherSource(serverUrl,
                                UmAccountManager.getCachedPersonUid(context, systemImpl),
                                username, passwordHash);
                    }else {
                        view.setErrorMessage(systemImpl.getString(
                                MessageID.login_network_error, getContext()));
                        view.setInProgress(false);
                    }
                });
            }
        });
    }


    private void loginOK(UmAccount result, String serverUrl){
        UstadMobileSystemImpl systemImpl = UstadMobileSystemImpl.getInstance();
        if(serverUrl != null && !serverUrl.isEmpty()){
            result.setEndpointUrl(serverUrl);
        }
        view.runOnUiThread(() -> view.setInProgress(false));
        UmAccountManager.setActiveAccount(result, getContext());
        view.forceSync();
        view.updateLastActive();
        systemImpl.go(mNextDest, getContext());
    }

    public void loginOKFromOtherSource(String serverUrl, String auth){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        UmAccount result = new UmAccount(
                Long.parseLong(UmAccountManager.getFingerprintPersonId(context, impl)),
                UmAccountManager.getFingerprintUsername(context, impl),
                auth, serverUrl);
        loginOK(result, serverUrl);
    }

    private void loginOKFromOtherSource(String serverUrl, Long personUid, String username,
                                        String auth){
        UmAccount result = new UmAccount(personUid, username,auth, serverUrl);
        loginOK(result, serverUrl);
    }

    public void handleClickFingerPrint() {

    }
}
