package com.ustadmobile.port.android.view;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.Login2Presenter;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.port.android.sync.UmAppDatabaseSyncWorker;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class Login2Activity extends UstadBaseActivity implements Login2View {

    private Login2Presenter mPresenter;

    private String mServerUrl;

    private TextView mUsernameTextView;

    private TextView mPasswordTextView;

    private TextView mErrorTextView;

    private ProgressBar mProgressBar;

    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        setSupportActionBar(findViewById(R.id.um_toolbar));

        mErrorTextView = findViewById(R.id.activity_login_errormessage);

        mPresenter = new Login2Presenter(this, UMAndroidUtil.bundleToHashtable(
                getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));
        mUsernameTextView = findViewById(R.id.activity_login_username);
        mPasswordTextView = findViewById(R.id.activity_login_password);
        mLoginButton = findViewById(R.id.activity_login_button_login);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setScaleY(3f);
        findViewById(R.id.activity_login_button_login).setOnClickListener(
                (evt) -> mPresenter.handleClickLogin(mUsernameTextView.getText().toString(),
                        mPasswordTextView.getText().toString(), mServerUrl));


    }

    @Override
    public void updateVersionOnLogin(String version){
        mErrorTextView.setText(version);
    }

    @Override
    public void setInProgress(boolean inProgress) {
        mProgressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        mPasswordTextView.setEnabled(!inProgress);
        mUsernameTextView.setEnabled(!inProgress);
        mLoginButton.setEnabled(!inProgress);
        mLoginButton.getBackground().setAlpha(inProgress ? 128 : 255 );
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        mErrorTextView.setText(errorMessage);
    }

    @Override
    public void setServerUrl(String serverUrl) {
        this.mServerUrl = serverUrl;
    }

    @Override
    public void setUsername(String username) {
        mUsernameTextView.setText(username);
    }

    @Override
    public void setPassword(String password) {
        mPasswordTextView.setText(password);
    }

    @Override
    public void setFinishAfficinityOnView() {
        runOnUiThread(() -> finishAffinity());
    }

    @Override
    public void forceSync() {
        WorkManager.getInstance().cancelAllWorkByTag(UmAppDatabaseSyncWorker.TAG);
        UmAppDatabaseSyncWorker.queueSyncWorkerWithPolicy(100, TimeUnit.MILLISECONDS,
                ExistingWorkPolicy.APPEND);
        sendToast("Sync started");
        WorkManager.getInstance().getWorkInfosByTagLiveData(UmAppDatabaseSyncWorker.TAG).observe(
                this, workInfos -> {
                    for(WorkInfo wi:workInfos){
                        if(wi.getState().isFinished()){
                            sendToast("Sync finished");
                        }
                    }
                });
    }
}
