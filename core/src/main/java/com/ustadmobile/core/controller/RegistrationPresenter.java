package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.DialogResultListener;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.RegistrationView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
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
    public Map<Integer, int[]> extraFieldsOptions;

    UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

    int[] faculty, relationship, english_proficiency, yes_no_choices, job_type, gender, universities;

    public static List userFields;

    //TODO: Remove. Instead get it from properties
    public void setExtraFields(){

        universities = new int[]{
                MessageID.options_uni_none,
                MessageID.options_uni_kabul_uni,
                MessageID.options_uni_kabul_medical_science,
                MessageID.options_uni_kabul_polytechnic_uni,
                MessageID.options_uni_shaheed_rabani,
                MessageID.options_uni_jawzjan,
                MessageID.options_uni_herat,
                MessageID.options_uni_balkh,
                MessageID.options_uni_nagahar,
                MessageID.options_uni_khost,
                MessageID.options_uni_kandahar,
                MessageID.options_uni_kunduz,
                MessageID.options_uni_i_dont_know
        };
        gender = new int[]{MessageID.options_gender_female, MessageID.options_gender_male};
        faculty = new int[]{};
        relationship = new int[]{MessageID.options_relationship_single,
                MessageID.options_relationship_married};
        english_proficiency = new int[]{MessageID.options_english_fluent,
                MessageID.options_english_good, MessageID.options_english_fair,
                MessageID.options_english_poor};
        yes_no_choices = new int[]{MessageID.options_yes, MessageID.options_no};
        job_type = new int[]{MessageID.options_job_short, MessageID.options_job_long,
                MessageID.options_job_part, MessageID.options_job_full};

        userFields = new ArrayList();

        userFields.add(MessageID.field_fullname);
        userFields.add(MessageID.field_gender);
        userFields.add(MessageID.field_email);
        userFields.add(MessageID.field_phonenumber);
        userFields.add(MessageID.field_university);
        userFields.add(MessageID.field_address);


        extraFieldsMap = new LinkedHashMap<>();
        extraFieldsMap.put(MessageID.field_fullname, TYPE_CLASS_TEXT);
        extraFieldsMap.put(MessageID.field_gender, TYPE_AUTOCOMPETE_TEXT_VIEW);
        extraFieldsMap.put(MessageID.field_email, TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        extraFieldsMap.put(MessageID.field_phonenumber, TYPE_CLASS_PHONE);

        extraFieldsMap.put(MessageID.field_university, TYPE_AUTOCOMPETE_TEXT_VIEW);

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


    public RegistrationPresenter(Object context) {
        super(context);
    }

    public RegistrationPresenter(Object context, RegistrationView view) throws SQLException {
        super(context);
        setExtraFields();
        this.view = view;

        for (Map.Entry<Integer, Integer> entry : extraFieldsMap.entrySet()) {
            int name = entry.getKey();
            int type = entry.getValue();
            int[] options = null;
            String[] stringOptions = null;
            if(extraFieldsOptions.containsKey(name)) {
                options = extraFieldsOptions.get(name);
            }
            if(options != null) {
                stringOptions = new String[options.length];
                for (int i = 0; i < stringOptions.length; i++) {
                    stringOptions[i] = impl.getString(options[i], context);
                }
            }
            view.addField(name, type, stringOptions);
        }
    }

    /**
     * Get user detail
     * @param username  The username of the active user or any other user
     * @param field     The custom field field id/name
     * @return          value
     * @throws SQLException
     */
    public static String getUserDetail(String username, int field, Object dbContext){
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String value = impl.getUserDetail(username, field, dbContext);
        if(value == null){
            return "";
        }

        return value;

    }
    public void setUIStrings() {
        //Doesn't do much
    }

    /**
     * Handle register link in Registration view
     */
    public void handleClickRegister(String username, String password, Hashtable fields, boolean editMode) {
        Object context = getContext();
        if(editMode){
            UstadMobileSystemImpl.getInstance().updateUser(username, password, fields, context);
        }else {
            UstadMobileSystemImpl.getInstance().registerUser(username, password, fields, context);
        }
        if(resultListener != null){
            resultListener.onDialogResult(RESULT_REGISTRATION_SUCCESS, view, null);
        }

        if(view != null && view instanceof DismissableDialog){
            ((DismissableDialog)view).dismiss();
        }
    }

    public void handleClickUpdate(String username, Hashtable fields){

    }

    public DialogResultListener getResultListener() {
        return resultListener;
    }

    public void setResultListener(DialogResultListener resultListener) {
        this.resultListener = resultListener;
    }
}
