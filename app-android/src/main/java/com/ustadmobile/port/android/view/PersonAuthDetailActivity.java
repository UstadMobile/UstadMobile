package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.PersonAuthDetailPresenter;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.PersonAuthDetailView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

;

public class PersonAuthDetailActivity extends UstadBaseActivity implements PersonAuthDetailView {

    private Toolbar toolbar;
    private PersonAuthDetailPresenter mPresenter;

    private EditText usernameET, passwordET, updatePasswordET;


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
        setContentView(R.layout.activity_personauth_detail);

        //Toolbar:
        toolbar = findViewById(R.id.activity_person_auth_detail_toolbar);
        toolbar.setTitle(getText(R.string.update_username_password));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        usernameET = findViewById(R.id.activity_personauth_detail_username);
        passwordET = findViewById(R.id.activity_personauth_detail_password);
        updatePasswordET = findViewById(R.id.activity_personauth_detail_confirm_password);


        //Call the Presenter
        mPresenter = new PersonAuthDetailPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

        usernameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.setUsernameSet(s.toString());
            }
        });

        passwordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.setPasswordSet(s.toString());
            }
        });

        updatePasswordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPresenter.setConfirmPasswordSet(s.toString());
            }
        });

        //FAB and its listener
        FloatingTextButton fab = findViewById(R.id.activity_person_auth_detail_fab);

        fab.setOnClickListener(v -> mPresenter.handleClickDone());


    }


    @Override
    public void updateUsername(String username) {
        usernameET.setText(username);
    }

    @Override
    public void sendMessage(int messageId) {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String toast = impl.getString(messageId, this);
        runOnUiThread(() -> Toast.makeText(
                this,
                toast,
                Toast.LENGTH_SHORT
        ).show());
    }
}
