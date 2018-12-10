package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;

import com.ustadmobile.core.db.dao.WamdaPersonDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.CreateAccountView;
import com.ustadmobile.core.view.LoginView2;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.lib.db.entities.WamdaPerson;

import java.util.Hashtable;

public class LoginPresenter2 extends UstadBaseController<LoginView2> {

    public static final String ARG_NEXT = "next";

    private String mNextDest;

    public LoginPresenter2(Object context, Hashtable arguments, LoginView2 view) {
        super(context, arguments, view);
        if(arguments != null && arguments.containsKey(ARG_NEXT)){
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

    public void handleSocialNetworkSignUp(Person person){
        UmAppDatabase.getInstance(getContext()).getPersonDao().createNewAccount(person, new UmCallback<UmAccount>() {
            @Override
            public void onSuccess(UmAccount result) {
                UmAccountManager.setActiveAccount(result, context);
                UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                String nextDest = getArgumentString(ARG_NEXT) != null ?
                        getArgumentString(ARG_NEXT) :
                        impl.getAppConfigString(AppConfig.KEY_FIRST_DEST, null, context);

                WamdaPerson newWamdaPerson = WamdaPersonDao.makeWamdaPersonForNewUser(result.getPersonUid(),
                        impl.getString(MessageID.wamda_default_profile_status, getContext()),
                        getContext());
                UmAccountManager.getRepositoryForActiveAccount(context).getWamdaPersonDao()
                        .insertAsync(newWamdaPerson, null);

                impl.go(nextDest, context);
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
