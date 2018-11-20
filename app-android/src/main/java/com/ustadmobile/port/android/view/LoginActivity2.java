package com.ustadmobile.port.android.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.toughra.ustadmobile.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.controller.LoginPresenter2;
import com.ustadmobile.core.view.LoginView2;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.util.UMAndroidUtil;



public class LoginActivity2 extends UstadBaseActivity implements LoginView2{

    private LoginPresenter2 mPresenter;

    private EditText usernameEditText;

    private EditText passwordEditText;

    private TextView errorTextView;

    private CallbackManager callbackManager;

    private FirebaseAuth mAuth;

    private boolean isFacebookLogin = false;

    private TwitterLoginButton twitterLoginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        FacebookSdk.setApplicationId(CoreBuildConfig.FACEBOOK_APPID);
        FacebookSdk.sdkInitialize(getApplicationContext());
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(CoreBuildConfig.TWITTER_KEY, CoreBuildConfig.TWITTER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);

        setSupportActionBar(findViewById(R.id.activity_login2_toolbar));
        usernameEditText = findViewById(R.id.activity_login2_username_edit_text);
        passwordEditText = findViewById(R.id.activity_login2_password_edit_text);
        errorTextView = findViewById(R.id.activity_login2_error_text);
        Button mFacebookButton = findViewById(R.id.activity_login2_facebook_button);
        Button mTwitterButton = findViewById(R.id.activity_login2_twitter_button);

        callbackManager = CallbackManager.Factory.create();
        mAuth = FirebaseAuth.getInstance();
        mPresenter = new LoginPresenter2(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        findViewById(R.id.activity_login2_login_button).setOnClickListener((view) -> {
            mPresenter.handleClickLogin(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString(), "");
        });

        findViewById(R.id.activity_login2_create_account).setOnClickListener(
                (view) -> mPresenter.handleClickCreateAccount());

        LoginButton facebookLoginButton = new LoginButton(this);
        twitterLoginButton = new TwitterLoginButton(this);

        mFacebookButton.setOnClickListener(v -> {
            isFacebookLogin = true;
            facebookLoginButton.performClick();
        });

        mTwitterButton.setOnClickListener(v -> {
            isFacebookLogin = false;
            twitterLoginButton.performClick();
        });

        facebookLoginButton.setReadPermissions("email", "public_profile");
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
            }
        });

        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                exception.printStackTrace();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(isFacebookLogin){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }else{
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        }
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

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateFirebaseUser(user);
                    }
                });
    }


    private void handleTwitterSession(TwitterSession session) {
        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateFirebaseUser(user);
                    }
                });
    }

    private void updateFirebaseUser(FirebaseUser user){
        String [] fullName = user.getDisplayName().split(" ");
        Person person = new Person();
        person.setEmailAddr(user.getEmail());
        person.setFirstNames(fullName[0]);
        person.setLastName(fullName.length > 1 ? fullName[1]:fullName[0]);
        person.setUsername(fullName[0]);
        person.setSocialAccount(true);
        mPresenter.handleSocialNetworkSignUp(person);
    }

}
