package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UmAccount;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.LoginView2;

import java.util.Hashtable;

public class LoginPresenter2 extends UstadBaseController<LoginView2>{

    public static final String ARG_NEXT_DEST = "next";

    public LoginPresenter2(Object context, Hashtable arguments, LoginView2 view) {
        super(context, arguments, view);
    }

    @Override
    public void setUIStrings() {

    }

    public void handleClickLogin(String username, String password, String serverUrl) {
        view.setInProgress(true);
        UmAppDatabase.getInstance(context).getPersonDao().authenticate(username, password,
                new UmCallback<UmAccount>() {
            @Override
            public void onSuccess(UmAccount result) {
                if(result != null) {
                    UmAccount.setActiveAccount(result, context);
                    view.runOnUiThread(() -> {
                        view.setInProgress(false);
                        view.setPassword("");
                        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                        String nextDest = getArguments().containsKey(ARG_NEXT_DEST) ?
                                (String)getArguments().get(ARG_NEXT_DEST) :
                                impl.getAppConfigString(AppConfig.KEY_FIRST_DEST, null,
                                        context);
                        UstadMobileSystemImpl.getInstance().go(nextDest, context);
                    });
                }else {
//                    view.setErrorMessage(UstadMobileSystemImpl.getInstance().getString());
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                view.setErrorMessage(UstadMobileSystemImpl.getInstance().getString(
                        MessageID.login_network_error, context));
            }
        });
    }

    public void handleClickCreateAccount(){

    }

}
