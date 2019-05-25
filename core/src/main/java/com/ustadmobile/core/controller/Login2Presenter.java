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

public class Login2Presenter extends UstadBaseController<Login2View> {

    public static final String ARG_NEXT = "next";

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
    }

    public void handleClickLogin(String username, String password, String serverUrl, boolean saveToFingerprint) {
        view.setInProgress(true);
        view.setErrorMessage("");
        UmAppDatabase loginRepoDb = UmAppDatabase.getInstance(getContext()).getRepository(serverUrl,
                "");
        UstadMobileSystemImpl systemImpl = UstadMobileSystemImpl.getInstance();
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
                    view.setErrorMessage(systemImpl.getString(
                            MessageID.login_network_error, getContext()));
                    view.setInProgress(false);
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
        systemImpl.go(mNextDest, getContext());
    }

    public void fingerprintLogin(String serverUrl, String auth){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        UmAccount result = new UmAccount(
                Long.parseLong(UmAccountManager.getFingerprintPersonId(context, impl)),
                UmAccountManager.getFingerprintUsername(context, impl),
                auth, serverUrl);
        loginOK(result, serverUrl);
    }

    public void handleClickFingerPrint() {

    }
}
