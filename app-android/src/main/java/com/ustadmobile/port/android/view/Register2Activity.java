package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
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


    private TextView errorMessageView;

    private UstadMobileSystemImpl systemImpl;

    private HashMap<Integer, Integer> fieldToViewIdMap = new HashMap<>();

    private Register2Presenter presenter;

    private String serverUrl;

    private Button registerUser;

    private ProgressBar progressDialog;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            errorMessageView.setVisibility(View.GONE);
            checkRegisterButtonStatus();
        }

        @Override
        public void afterTextChanged(Editable s) { }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        setUMToolbar(R.id.um_toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        fieldToViewIdMap.put(FIELD_FIRST_NAME, R.id.activity_create_account_firstname_text);
        fieldToViewIdMap.put(FIELD_LAST_NAME, R.id.activity_create_account_lastname_text);
        fieldToViewIdMap.put(FIELD_EMAIL, R.id.activity_create_account_email_text);
        fieldToViewIdMap.put(FIELD_USERNAME, R.id.activity_create_account_username_text);
        fieldToViewIdMap.put(FIELD_PASSWORD, R.id.activity_create_account_password_text);
        fieldToViewIdMap.put(FIELD_CONFIRM_PASSWORD, R.id.activity_create_account_password_confirmpassword_text);

        registerUser = findViewById(R.id.activity_create_account_create_account_button);
        errorMessageView = findViewById(R.id.activity_create_account_error_text);
        progressDialog = findViewById(R.id.progressBar);
        progressDialog.setIndeterminate(true);
        progressDialog.setScaleY(3f);

        systemImpl = UstadMobileSystemImpl.getInstance();

        presenter = new Register2Presenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()),this);
        presenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        for(int fieldId: fieldToViewIdMap.values()){
            ((TextInputEditText)findViewById(fieldId)).addTextChangedListener(textWatcher);
        }

        registerUser.setOnClickListener(v -> checkAccountFields());

    }


    private void checkAccountFields(){
        for(Integer fieldCode: fieldToViewIdMap.keySet()){
            if(getFieldValue(fieldCode).isEmpty()){
                setErrorMessageView(systemImpl.getString(MessageID.register_empty_fields,this));
                return;
            }
        }

        checkRegisterButtonStatus();

        if(getFieldValue(FIELD_PASSWORD).equals(getFieldValue(FIELD_CONFIRM_PASSWORD))){

            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

            if(getFieldValue(FIELD_EMAIL).matches(emailPattern)){
                if(getFieldValue(FIELD_PASSWORD).length() < 5){
                    disableButton(true);
                    setErrorMessageView(systemImpl.getString(MessageID.field_password_error_min,this));
                }else{
                    Person person = new Person();
                    person.setFirstNames(getFieldValue(FIELD_FIRST_NAME));
                    person.setLastName(getFieldValue(FIELD_LAST_NAME));
                    person.setEmailAddr(getFieldValue(FIELD_EMAIL));
                    person.setUsername(getFieldValue(FIELD_USERNAME));
                    new Thread(() -> presenter.handleClickRegister(person,
                            getFieldValue(FIELD_PASSWORD),serverUrl)).start();
                }
            }else{
                disableButton(true);
                setErrorMessageView(systemImpl.getString(MessageID.register_incorrect_email,this));
            }
        }else{
            disableButton(true);
            setErrorMessageView(systemImpl.getString(MessageID.filed_password_no_match,this));
        }
    }

    private void disableButton(boolean disable){
        registerUser.setBackgroundColor(ContextCompat.getColor(this,
                disable ? R.color.divider:R.color.accent));
        registerUser.setEnabled(!disable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return true;
    }

    @Override
    public void setErrorMessageView(String errorMessageView) {
        this.errorMessageView.setText(errorMessageView);
        this.errorMessageView.setVisibility(View.VISIBLE);
        disableButton(true);
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

    private void checkRegisterButtonStatus(){
        boolean isEnabled = true;
        for(Integer fieldCode: fieldToViewIdMap.keySet()){
            if(getFieldValue(fieldCode).isEmpty()){
               isEnabled = false;
                break;
            }
        }
        disableButton(!isEnabled);
    }


}
