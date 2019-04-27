package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.UserProfilePresenter;
import com.ustadmobile.core.view.UserProfileView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

public class UserProfileActivity extends UstadBaseActivity implements UserProfileView {

    private Toolbar toolbar;
    private UserProfilePresenter mPresenter;

    private LinearLayout changePasswordLL, languageLL, logoutLL;

    private TextView languageSet;

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_user_profile);

        //Toolbar:
        toolbar = findViewById(R.id.activity_user_profile_toolbar);
        toolbar.setTitle(getText(R.string.app_name));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        changePasswordLL = findViewById(R.id.activity_user_profile_change_password_ll);
        languageLL = findViewById(R.id.activity_user_profile_language_ll);
        logoutLL = findViewById(R.id.activity_user_profile_logout_ll);
        languageSet = findViewById(R.id.activity_user_profile_language_selection);

        //Call the Presenter
        mPresenter = new UserProfilePresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        changePasswordLL.setOnClickListener(v -> mPresenter.handleClickChangePassword());
        languageLL.setOnClickListener(v -> mPresenter.handleClickChangeLanguage());
        logoutLL.setOnClickListener(v -> handleClickLogout());

    }


    private void handleClickLogout(){
        finishAffinity();
        mPresenter.handleClickLogout();
    }

    @Override
    public void updateToolbarTitle(String personName) {
        toolbar.setTitle(personName);
    }

    @Override
    public void setLanguageSet(String lang) {
        languageSet.setText(lang);
    }
}
