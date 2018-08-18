package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UmAccount;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.CreateAccountView;
import com.ustadmobile.lib.db.entities.Person;

import java.util.Hashtable;

public class CreateAccountPresenter extends UstadBaseController<CreateAccountView>{

    public CreateAccountPresenter(Object context, Hashtable arguments, CreateAccountView view) {
        super(context, arguments, view);
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);
    }

    public void handleClickCreateAccount(){
        Person person = new Person();
        person.setFirstName(view.getFieldValue(CreateAccountView.FIELD_FIRSTNAME));
        person.setLastName(view.getFieldValue(CreateAccountView.FIELD_LASTNAME));
        person.setUsername(view.getFieldValue(CreateAccountView.FIELD_USERNAME));
        person.setEmailAddr(view.getFieldValue(CreateAccountView.FIELD_EMAIL));
        person.setPasswordHash(view.getFieldValue(CreateAccountView.FIELD_PASSWORD));

        UmAppDatabase.getInstance(context).getPersonDao().createNewAccount(person, new UmCallback<UmAccount>() {
            @Override
            public void onSuccess(UmAccount result) {
                UmAccount.setActiveAccount(result, context);
                UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
                String nextDest = getArgumentString(ARG_NEXT) != null ?
                        getArgumentString(ARG_NEXT) :
                        impl.getAppConfigString(AppConfig.KEY_FIRST_DEST, null, context);
                impl.go(nextDest, context);
            }

            @Override
            public void onFailure(Throwable exception) {
                view.setErrorMessage(exception  .getMessage());
            }
        });

    }

    @Override
    public void setUIStrings() {

    }
}
