package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.DialogResultListener;
import com.ustadmobile.core.view.RegistrationView;

import java.awt.TextField;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;


/**
 * The controller/presenter for registration.
 * Created by varuna on 7/28/2017.
 */
public class RegistrationPresenter extends UstadBaseController {

    private RegistrationView view;

    public static final int RESULT_REGISTRATION_SUCCESS=2;

    public static final int TYPE_CLASS_TEXT = 1;
    public static final int TYPE_CLASS_DATETIME = 4;
    public static final int TYPE_CLASS_NUMBER = 2;
    public static final int TYPE_CLASS_PHONE = 3;
    public static final int TYPE_TEXT_VARIATION_EMAIL_ADDRESS = 32;
    public static final int TYPE_TEXT_VARIATION_PASSWORD = 96;

    public static final int TYPE_AUTOCOMPETE_TEXT_VIEW = 4070;

    //Hashed user authentication to cache in case they login next time when offline
    //TODO
    public static final String PREFKEY_AUTHCACHE_PREFIX = "um-authcache-";

    private DialogResultListener resultListener;

    Map<Integer, Integer> extraFieldsMap;
    Map<Integer, String[]> extraFieldsOptions;


    //TODO: Remove. Instead get it from build config
    public int[] extraFields = new int[]{MessageID.field_university,
            MessageID.field_fullname, MessageID.field_gender,
            MessageID.field_email, MessageID.field_phonenumber,
            MessageID.field_faculty};

    String[] universities = {"Kabul University",
            "Kabul Polytechnic University", "Kabul Education University"};
    String[] gender = {"Female", "Male"};
    String[] faculty = {};

    //TODO: Remove. Instead get it from properties
    public void setExtraFields(){
        extraFieldsMap = new HashMap<>();
        extraFieldsMap.put(MessageID.field_university, TYPE_AUTOCOMPETE_TEXT_VIEW);
        extraFieldsMap.put(MessageID.field_fullname, TYPE_CLASS_TEXT);
        extraFieldsMap.put(MessageID.field_gender, TYPE_AUTOCOMPETE_TEXT_VIEW);
        extraFieldsMap.put(MessageID.field_email, TYPE_CLASS_TEXT);
        extraFieldsMap.put(MessageID.field_phonenumber, TYPE_CLASS_TEXT);
        extraFieldsMap.put(MessageID.field_faculty, TYPE_AUTOCOMPETE_TEXT_VIEW);

        extraFieldsOptions = new HashMap<>();
        extraFieldsOptions.put(MessageID.field_university, universities);
        extraFieldsOptions.put(MessageID.field_gender, gender);
        extraFieldsOptions.put(MessageID.field_faculty, faculty);
    }


    public RegistrationPresenter(Object context, RegistrationView view) {
        super(context);
        setExtraFields();
        this.view = view;

        for (Map.Entry<Integer, Integer> entry : extraFieldsMap.entrySet()) {
            int name = entry.getKey();
            int type = entry.getValue();
            String[] options = null;
            if(extraFieldsOptions.containsKey(name)) {
                options = extraFieldsOptions.get(name);
            }
            view.addField(name, type, options);
        }

        /*
        //TODO: Replace with values from build config
        for(int i=0; i < extraFields.length; i++){
            view.addField(extraFields[i], 0);
        }
        */
    }
    public void setUIStrings() {
        //Doesn't do much
    }

    /**
     * Handle register link in Registration view
     */
    public void handleClickRegister(String username, String password, Hashtable fields) {
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
