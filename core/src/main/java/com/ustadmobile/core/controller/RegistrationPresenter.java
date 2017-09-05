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
    public static final int TYPE_CLASS_YEAR = 6;
    public static final int TYPE_CLASS_PERCENTAGE = 5;
    public static final int TYPE_TEXT_VARIATION_EMAIL_ADDRESS = 32;
    public static final int TYPE_TEXT_VARIATION_PASSWORD = 96;

    public static final int TYPE_AUTOCOMPETE_TEXT_VIEW = 4070;
    public static final int TYPE_SPINNER = 555;

    //Hashed user authentication to cache in case they login next time when offline
    //TODO
    public static final String PREFKEY_AUTHCACHE_PREFIX = "um-authcache-";

    private DialogResultListener resultListener;

    public Map<Integer, Integer> extraFieldsMap;
    public Map<Integer, String[]> extraFieldsOptions;

    //TODO: Remove. Instead get it from properties
    String[] universities = {"Kabul University", "Kabul Polytechnic University",
            "Kabul Education University", "Other", "I don't know"};
    String[] gender = {"Female", "Male"};
    String[] faculty = {};
    String[] relationship={"Single", "Married"};
    String[] academic_year={};
    String[] english_proficiency = {"Fluent", "Good", "Fair", "Poor"};
    String[] yes_no_choices = {"Yes", "No"};
    String[] job_type = {"Short-term", "Long-term", "Part-time", "Full-time"};

    //TODO: Remove. Instead get it from properties
    public void setExtraFields(){
        extraFieldsMap = new HashMap<>();
        extraFieldsMap.put(MessageID.field_university, TYPE_AUTOCOMPETE_TEXT_VIEW);
        extraFieldsMap.put(MessageID.field_fullname, TYPE_CLASS_TEXT);
        extraFieldsMap.put(MessageID.field_gender, TYPE_AUTOCOMPETE_TEXT_VIEW);
        extraFieldsMap.put(MessageID.field_email, TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        extraFieldsMap.put(MessageID.field_phonenumber, TYPE_CLASS_PHONE);
        extraFieldsMap.put(MessageID.field_faculty, TYPE_CLASS_TEXT);

        extraFieldsMap.put(MessageID.field_father_name, TYPE_CLASS_TEXT);
        extraFieldsMap.put(MessageID.field_address, TYPE_CLASS_TEXT);
        extraFieldsMap.put(MessageID.field_tazkira_id, TYPE_CLASS_TEXT);
        extraFieldsMap.put(MessageID.field_relationship, TYPE_AUTOCOMPETE_TEXT_VIEW);
        extraFieldsMap.put(MessageID.field_department, TYPE_CLASS_TEXT);
        extraFieldsMap.put(MessageID.field_academic_year, TYPE_CLASS_PHONE);
        extraFieldsMap.put(MessageID.field_gpa, TYPE_CLASS_PERCENTAGE);

        extraFieldsMap.put(MessageID.field_would_work, TYPE_AUTOCOMPETE_TEXT_VIEW);
        extraFieldsMap.put(MessageID.field_would_work_elaborate, TYPE_CLASS_TEXT);

        extraFieldsMap.put(MessageID.field_have_work_experience, TYPE_AUTOCOMPETE_TEXT_VIEW);
        extraFieldsMap.put(MessageID.field_work_experience_elaborate, TYPE_CLASS_TEXT);

        extraFieldsMap.put(MessageID.field_type_job, TYPE_AUTOCOMPETE_TEXT_VIEW);
        extraFieldsMap.put(MessageID.field_english_proficiency, TYPE_AUTOCOMPETE_TEXT_VIEW);
        extraFieldsMap.put(MessageID.field_computer_application, TYPE_CLASS_TEXT);
        extraFieldsMap.put(MessageID.field_post_graduate, TYPE_CLASS_TEXT);
        extraFieldsMap.put(MessageID.field_comments, TYPE_CLASS_TEXT);


        extraFieldsOptions = new HashMap<>();
        extraFieldsOptions.put(MessageID.field_university, universities);
        extraFieldsOptions.put(MessageID.field_gender, gender);
        extraFieldsOptions.put(MessageID.field_faculty, faculty);
        extraFieldsOptions.put(MessageID.field_english_proficiency, english_proficiency);
        extraFieldsOptions.put(MessageID.field_type_job, job_type);
        extraFieldsOptions.put(MessageID.field_have_work_experience, yes_no_choices);
        extraFieldsOptions.put(MessageID.field_would_work, yes_no_choices);
        extraFieldsOptions.put(MessageID.field_relationship, relationship);
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
