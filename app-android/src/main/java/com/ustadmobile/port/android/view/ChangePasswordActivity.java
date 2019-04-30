package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ChangePasswordPresenter;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ChangePasswordView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.Objects;

;

public class ChangePasswordActivity extends UstadBaseActivity implements ChangePasswordView {

    private Toolbar toolbar;
    private ChangePasswordPresenter mPresenter;

    private EditText usernameET, passwordET,updatePasswordET;
    private Menu menu;


    /**
     * Creates the options on the toolbar - specifically the Done tick menu item
     * @param menu  The menu options
     * @return  true. always.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save, menu);

        menu.findItem(R.id.menu_save).setVisible(true);
        return true;
    }

    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
            return true;

        } else if (i == R.id.menu_save) {
            mPresenter.handleClickSave();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setting layout:
        setContentView(R.layout.activity_change_password);

        //Toolbar:
        toolbar = findViewById(R.id.activity_change_password_toolbar);
        toolbar.setTitle(getText(R.string.change_password));
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        usernameET = findViewById(R.id.activity_change_password_username);
        passwordET = findViewById(R.id.activity_change_password_password);
        updatePasswordET = findViewById(R.id.activity_change_password_password_confirm);


        //Call the Presenter
        mPresenter = new ChangePasswordPresenter(this,
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


    }

    @Override
    public void updateUsername(String username) {
        runOnUiThread(() -> usernameET.setText(username));

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
