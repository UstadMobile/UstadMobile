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

    private UmAppDatabase umAppDatabase;

    private UmAppDatabase repo;

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

    //only for testing
    public void setClientDb(UmAppDatabase database){
        this.umAppDatabase = database;
    }

    public void setRepo(UmAppDatabase repo){
        this.repo = repo;
    }

    /**
     * Registering new user's account
     * @param person Person object to be registered
     * @param password Person password to be associated with the account.
     * @param serverUrl Server url where the account should be created
     */
    public void handleClickRegister(Person person, String password,String serverUrl) {
        view.runOnUiThread(() -> view.setInProgress(true));

        UstadMobileSystemImpl systemImpl = UstadMobileSystemImpl.getInstance();
        if(umAppDatabase == null){
            umAppDatabase = UmAppDatabase.getInstance(getContext()).getRepository(serverUrl,
                    "");
        }

        if(repo == null) {
            repo = UmAccountManager.getRepositoryForActiveAccount(getContext());
        }

        repo.getPersonDao()
                .register(person, password, new UmCallback<UmAccount>() {
            @Override
            public void onSuccess(UmAccount result) {
                if(result != null) {
                    person.setPersonUid(result.getPersonUid());
                    umAppDatabase.getPersonDao().insertAsync(person, new UmCallback<Long>() {
                        @Override
                        public void onSuccess(Long personUid) {
                            result.setEndpointUrl(serverUrl);
                            view.runOnUiThread(() -> view.setInProgress(false));
                            UmAccountManager.setActiveAccount(result, getContext());
                            systemImpl.go(mNextDest, getContext());
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            //simple insert - this should not happen
                            view.runOnUiThread(() -> view.setErrorMessageView(systemImpl.getString(
                                    MessageID.err_registering_new_user, getContext())));
                        }
                    });

                }else {
                    view.runOnUiThread(() -> {
                        view.setErrorMessageView(systemImpl.getString(MessageID.err_registering_new_user,
                                getContext()));
                        view.setInProgress(false);
                    });
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                view.runOnUiThread(() -> {
                    view.setInProgress(false);
                    view.setErrorMessageView(systemImpl.getString(
                            MessageID.login_network_error, getContext()));
                });
            }
        });
    }
}
