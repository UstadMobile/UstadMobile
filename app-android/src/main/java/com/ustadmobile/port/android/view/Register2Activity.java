package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.Register2Presenter;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.Register2View;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.HashMap;


public class Register2Activity extends UstadBaseActivity implements Register2View {


    private TextView errorMessage;

    private UstadMobileSystemImpl systemImpl;

    private HashMap<Integer, Integer> fieldToViewIdMap = new HashMap<>();

    private Register2Presenter presenter;

    private String serverUrl;

    private ProgressBar progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        fieldToViewIdMap.put(FIELD_FIRST_NAME, R.id.activity_create_account_firstname_text);
        fieldToViewIdMap.put(FIELD_LAST_NAME, R.id.activity_create_account_lastname_text);
        fieldToViewIdMap.put(FIELD_EMAIL, R.id.activity_create_account_email_text);
        fieldToViewIdMap.put(FIELD_USERNAME, R.id.activity_create_account_username_text);
        fieldToViewIdMap.put(FIELD_PASSWORD, R.id.activity_create_account_password_text);
        fieldToViewIdMap.put(FIELD_CONFIRM_PASSWORD, R.id.activity_create_account_password_confirmpassword_text);

        Button registerUser = findViewById(R.id.activity_create_account_create_account_button);
        errorMessage = findViewById(R.id.activity_create_account_error_text);
        progressDialog = findViewById(R.id.progressBar);
        progressDialog.setIndeterminate(true);

        systemImpl = UstadMobileSystemImpl.getInstance();

        presenter = new Register2Presenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()),this);
        presenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        registerUser.setOnClickListener(v -> checkAccountFields());

    }


    private void checkAccountFields(){
        for(Integer fieldCode: fieldToViewIdMap.values()){
            if(getFieldValue(fieldCode).isEmpty()){
                setErrorMessage(systemImpl.getString(MessageID.register_empty_fields,this));
                return;
            }
        }

        if(getFieldValue(FIELD_PASSWORD).equals(getFieldValue(FIELD_CONFIRM_PASSWORD))){

            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

            if(getFieldValue(FIELD_EMAIL).matches(emailPattern)){
                if(getFieldValue(FIELD_PASSWORD).length() <= 5){
                    setErrorMessage(systemImpl.getString(MessageID.field_password_min,this));
                }else{
                    Person person = new Person();
                    person.setFirstNames(getFieldValue(FIELD_FIRST_NAME));
                    person.setLastName(getFieldValue(FIELD_LAST_NAME));
                    person.setEmailAddr(getFieldValue(FIELD_EMAIL));
                    person.setUsername(getFieldValue(FIELD_USERNAME));
                    presenter.handleClickRegister(person,getFieldValue(FIELD_PASSWORD),serverUrl);
                }
            }else{
                setErrorMessage(systemImpl.getString(MessageID.register_incorrect_email,this));
            }
        }else{
            setErrorMessage(systemImpl.getString(MessageID.filed_password_no_match,this));
        }
    }



    @Override
    public void setErrorMessage(String errorMessage) {
        this.errorMessage.setText(errorMessage);

    }

    @Override
    public void setServerUrl(String url) {
        this.serverUrl = url;
    }

    @Override
    public void setInProgress(boolean inProgress) {
        progressDialog.setVisibility(inProgress ? View.VISIBLE:View.GONE);
    }

    private String getFieldValue(int fieldCode) {
        return ((TextInputEditText)findViewById(fieldToViewIdMap.get(fieldCode))).getText().toString();
    }


}
