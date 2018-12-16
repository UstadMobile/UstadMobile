package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.Register2View;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.UmAccount;

import java.util.Hashtable;

public class Register2Presenter extends UstadBaseController<Register2View> {

    public static final String ARG_NEXT = "next";

    public static final String ARG_SERVER_URL = "apiUrl";

    private String mNextDest;

    public Register2Presenter(Object context, Hashtable arguments, Register2View view) {
        super(context, arguments, view);
        if(arguments.containsKey(ARG_NEXT)){
            mNextDest = arguments.get(ARG_NEXT).toString();
        }
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        if(getArguments().containsKey(ARG_SERVER_URL)){
            view.setServerUrl((String)getArguments().get(ARG_SERVER_URL));
        }else {
            view.setServerUrl(UstadMobileSystemImpl.getInstance().getAppConfigString(
                    AppConfig.KEY_API_URL, "http://localhost", getContext()));
        }
    }

    @Override
    public void setUIStrings() {

    }

    public void handleClickRegister(Person person, String password,String serverUrl) {
        view.setInProgress(true);
        UmAppDatabase loginRepoDb = UmAppDatabase.getInstance(getContext()).getRepository(serverUrl,
                "");
        UstadMobileSystemImpl systemImpl = UstadMobileSystemImpl.getInstance();
        loginRepoDb.getPersonDao().register(person, password, new UmCallback<UmAccount>() {
            @Override
            public void onSuccess(UmAccount result) {
                if(result != null) {
                    result.setEndpointUrl(serverUrl);
                    view.runOnUiThread(() -> view.setInProgress(false));
                    UmAccountManager.setActiveAccount(result, getContext());
                    systemImpl.go(mNextDest, getContext());
                }else {
                    view.runOnUiThread(() -> {
                        view.setErrorMessage(systemImpl.getString(MessageID.err_registering_new_user,
                                getContext()));
                        view.setInProgress(false);
                    });
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                view.runOnUiThread(() -> view.setErrorMessage(systemImpl.getString(
                        MessageID.login_network_error, getContext())));
            }
        });
    }

}
