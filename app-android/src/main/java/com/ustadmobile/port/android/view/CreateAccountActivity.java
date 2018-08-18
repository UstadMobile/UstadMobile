package com.ustadmobile.port.android.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CreateAccountPresenter;
import com.ustadmobile.core.view.CreateAccountView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.HashMap;

public class CreateAccountActivity extends UstadBaseActivity implements CreateAccountView{

    private HashMap<Integer, Integer> fieldToViewIdMap = new HashMap<>();

    private CreateAccountPresenter mPresenter;

    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        fieldToViewIdMap.put(FIELD_FIRSTNAME, R.id.activity_create_account_firstname_text);
        fieldToViewIdMap.put(FIELD_LASTNAME, R.id.activity_create_account_lastname_text);
        fieldToViewIdMap.put(FIELD_EMAIL, R.id.activity_create_account_email_text);
        fieldToViewIdMap.put(FIELD_USERNAME, R.id.activity_create_account_username_text);
        fieldToViewIdMap.put(FIELD_PASSWORD, R.id.activity_create_account_password_text);
        fieldToViewIdMap.put(FIELD_CONFIRM_PASSWORD, R.id.activity_create_account_password_confirmpassword_text);

        errorTextView = findViewById(R.id.activity_create_account_error_text);

        mPresenter = new CreateAccountPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        findViewById(R.id.activity_create_account_create_account_button)
                .setOnClickListener((view) -> mPresenter.handleClickCreateAccount());
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));
    }


    @Override
    public void setEnabled(boolean enabled) {

    }

    @Override
    public String getFieldValue(int fieldCode) {
        return ((EditText)findViewById(fieldToViewIdMap.get(fieldCode))).getText().toString();
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        errorTextView.setText(errorMessage);
    }
}
