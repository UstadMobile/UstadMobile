package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.LoginView2;
import com.ustadmobile.lib.db.entities.UmAccount;

import java.util.Hashtable;

public class LoginPresenter2 extends UstadBaseController<LoginView2> {

    public static final String ARG_NEXT = "next";

    private String mNextDest;

    public LoginPresenter2(Object context, Hashtable arguments, LoginView2 view) {
        super(context, arguments, view);
        if(arguments.containsKey(ARG_NEXT)){
            mNextDest = arguments.get(ARG_NEXT).toString();
        }
    }

    public void handleClickLogin(String username, String password, String serverUrl) {
        UmAppDatabase loginRepoDb = UmAppDatabase.getInstance(getContext()).getRepository(serverUrl,
                "");
        loginRepoDb.getPersonDao().login(username, password, new UmCallback<UmAccount>() {
            @Override
            public void onSuccess(UmAccount result) {
                if(result != null) {
                    result.setEndpointUrl(serverUrl);
                    UmAccountManager.setActiveAccount(result, getContext());
                    UstadMobileSystemImpl.getInstance().go(mNextDest, getContext());
                }
            }

            @Override
            public void onFailure(Throwable exception) {

            }
        });
    }


    @Override
    public void setUIStrings() {

    }
}
