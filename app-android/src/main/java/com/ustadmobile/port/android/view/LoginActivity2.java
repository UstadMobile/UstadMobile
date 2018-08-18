package com.ustadmobile.port.android.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.LoginPresenter2;
import com.ustadmobile.core.view.LoginView2;
import com.ustadmobile.port.android.util.UMAndroidUtil;

public class LoginActivity2 extends UstadBaseActivity implements LoginView2{

    private LoginPresenter2 mPresenter;

    private EditText usernameEditText;

    private EditText passwordEditText;

    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        setSupportActionBar(findViewById(R.id.activity_login2_toolbar));

        usernameEditText = findViewById(R.id.activity_login2_username_edit_text);
        passwordEditText = findViewById(R.id.activity_login2_password_edit_text);
        errorTextView = findViewById(R.id.activity_login2_error_text);

        mPresenter = new LoginPresenter2(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        findViewById(R.id.activity_login2_login_button).setOnClickListener((view) -> {
            mPresenter.handleClickLogin(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString(), "");
        });

        findViewById(R.id.activity_login2_create_account).setOnClickListener(
                (view) -> mPresenter.handleClickCreateAccount());
    }

    @Override
    public void setFacebookLoginVisible(boolean facebookLoginVisible) {
        findViewById(R.id.activity_login2_facebook_button).setVisibility(
                facebookLoginVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setTwitterLoginVisible(boolean twitterLoginVisible) {
        findViewById(R.id.activity_login2_twitter_button).setVisibility(
                twitterLoginVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setUsername(String username) {
        usernameEditText.setText(username);
    }

    @Override
    public void setPassword(String password) {
        passwordEditText.setText(password);
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        if(errorMessage != null) {
            errorTextView.setText(errorMessage);
        }
        errorTextView.setVisibility(errorMessage != null ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setInProgress(boolean inProgress) {

    }
}
