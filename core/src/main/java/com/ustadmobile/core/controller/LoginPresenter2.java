package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UmAccount;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.CreateAccountView;
import com.ustadmobile.core.view.LoginView2;

import java.util.Hashtable;

public class LoginPresenter2 extends UstadBaseController<LoginView2>{

    public LoginPresenter2(Object context, Hashtable arguments, LoginView2 view) {
        super(context, arguments, view);
    }

    @Override
    public void setUIStrings() {

    }

    public void handleClickLogin(String username, String password, String serverUrl) {
        view.setInProgress(true);
        view.setErrorMessage(null);
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
                        String nextDest = getArgumentString(ARG_NEXT) != null ?
                                getArgumentString(ARG_NEXT) :
                                impl.getAppConfigString(AppConfig.KEY_FIRST_DEST, null,
                                        context);
                        UstadMobileSystemImpl.getInstance().go(nextDest, context);
                    });
                }else {
                    view.runOnUiThread(() -> {
                        view.setPassword("");
                        view.setErrorMessage(UstadMobileSystemImpl.getInstance().getString(
                                        MessageID.invalid_username_or_password, context));
                    });
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
        Hashtable args = new Hashtable();
        if(getArgumentString(ARG_NEXT) != null)
            args.put(ARG_NEXT, getArgumentString(ARG_NEXT));
        if(getArgumentString(UstadMobileSystemImpl.ARG_NO_HISTORY) != null)
            args.put(UstadMobileSystemImpl.ARG_NO_HISTORY, getArgumentString(UstadMobileSystemImpl.ARG_NO_HISTORY));

        UstadMobileSystemImpl.getInstance().go(CreateAccountView.VIEW_NAME, args, context);
    }

}
