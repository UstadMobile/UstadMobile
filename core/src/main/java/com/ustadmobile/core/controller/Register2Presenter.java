package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.AppConfig;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.Register2View;

import java.util.Hashtable;

public class Register2Presenter extends UstadBaseController<Register2View> {

    public static final String ARG_NEXT = "next";

    public static final String ARG_SERVER_URL = "apiUrl";

    public Register2Presenter(Object context, Hashtable arguments, Register2View view) {
        super(context, arguments, view);
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

    public void handleClickRegister() {
        //call register in a similar way to how this was done on Login2Presenter handleClickLogin
    }

}
