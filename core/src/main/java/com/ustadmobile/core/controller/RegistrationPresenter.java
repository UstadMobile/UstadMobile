package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.DialogResultListener;
import com.ustadmobile.core.view.RegistrationView;
import java.util.HashMap;


/**
 * Created by varuna on 7/28/2017.
 */

public class RegistrationPresenter extends UstadBaseController {

    private RegistrationView view;

    public static final int RESULT_REGISTRATION_SUCCESS=2;

    //Hashed user authentication to cache in case they login next time when offline
    public static final String PREFKEY_AUTHCACHE_PREFIX = "um-authcache-";

    private DialogResultListener resultListener;

    //TODO: Remove. Instead get it from build config
    public int[] extraFields = new int[]{MessageID.field_university,
            MessageID.field_fullname, MessageID.field_gender,
            MessageID.field_email, MessageID.field_phonenumber,
            MessageID.field_faculty};

    public RegistrationPresenter(Object context, RegistrationView view) {
        super(context);
        this.view = view;
        //TODO: Replace with values from build config
        for(int i=0; i < extraFields.length; i++){
            view.addField(extraFields[i], 0);
        }

    }

    @Override
    public void setUIStrings() {
        //Doens't do much
    }

    /**
     * Handle register link in Registration view
     */
    public void handleClickRegister(String username, String password, HashMap fields) {

        Object context = getContext();
        UstadMobileSystemImpl.getInstance().registerUser(username, password, fields, context);
        if(resultListener != null){
            resultListener.onDialogResult(RESULT_REGISTRATION_SUCCESS, view, null);
        }
    }

    public DialogResultListener getResultListener() {
        return resultListener;
    }

    public void setResultListener(DialogResultListener resultListener) {
        this.resultListener = resultListener;
    }
}
