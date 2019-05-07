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

import static com.ustadmobile.core.view.Login2View.ARG_STARTSYNCING;

public class Login2Presenter extends UstadBaseController<Login2View> {

    private static final String ARG_NEXT = "next";

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

        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String version = impl.getVersion(context);
        view.updateVersionOnLogin(version);
    }

    /**
     * Handles login. If successful, will go to the next main destination. If not it will show
     * an error message on the view.
     * @param username  Username in plain text
     * @param password  Password in plain text
     * @param serverUrl Server url in plain text
     */
    public void handleClickLogin(String username, String password, String serverUrl) {
        view.setInProgress(true);
        view.setErrorMessage("");
        UmAppDatabase loginRepoDb = UmAppDatabase.getInstance(getContext()).getRepository(serverUrl,
                "");
        UstadMobileSystemImpl systemImpl = UstadMobileSystemImpl.getInstance();
        loginRepoDb.getPersonDao().login(username, password, new UmCallback<UmAccount>() {
            @Override
            public void onSuccess(UmAccount result) {
                if(result != null) {
                    result.setEndpointUrl(serverUrl);
                    view.runOnUiThread(() -> view.setInProgress(false));
                    view.setFinishAfficinityOnView();
                    UmAccountManager.setActiveAccount(result, getContext());

                    view.forceSync();
                    Hashtable<String, String> args = new Hashtable<>();
                    args.put(ARG_STARTSYNCING, "true");
                    systemImpl.go(mNextDest, args, getContext());
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


}
