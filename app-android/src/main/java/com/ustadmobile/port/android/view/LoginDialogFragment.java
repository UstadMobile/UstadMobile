package com.ustadmobile.port.android.view;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.MessageIDConstants;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.DialogResultListener;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.LoginView;

import java.util.Iterator;
import java.util.Vector;

/**
 * An Android Dialog Fragment that implements the LoginView.
 */
public class LoginDialogFragment extends UstadDialogFragment implements LoginView, View.OnClickListener, DismissableDialog {

    private LoginController mLoginController;

    private View mView;

    private String mXapiServer;

    public LoginDialogFragment() {
        //Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mView= inflater.inflate(R.layout.fragment_login_dialog, container, false);
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        ((TextView)mView.findViewById(R.id.fragment_login_title_text)).setText(
                impl.getString(MessageIDConstants.login));
        ((EditText)mView.findViewById(R.id.fragment_login_dialog_username_text)).setHint(
                impl.getString(MessageIDConstants.username));
        ((EditText)mView.findViewById(R.id.fragment_login_dialog_password)).setHint(
                impl.getString(MessageIDConstants.password));

        Button forgotPassword= (Button) mView.findViewById(R.id.fragment_login_forgot_password_button);
        forgotPassword.setText(Html.fromHtml("<u>" + impl.getString(MessageIDConstants.forgot_password) +
                "</u>"));
        forgotPassword.setTransformationMethod(null);

        Button loginButton = (Button)mView.findViewById(R.id.fragment_login_dialog_login_button);
        loginButton.setOnClickListener(this);
        loginButton.setText(impl.getString(MessageIDConstants.login));

        Button registerButton = (Button)mView.findViewById(R.id.fragment_login_dialog_register_button);
        registerButton.setText(impl.getString(MessageIDConstants.register));

        mLoginController = LoginController.makeControllerForView(this);
        mLoginController.setUIStrings();
        if(mResultListener != null)
            mLoginController.setResultListener(mResultListener);

        return mView;
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);


        super.onResume();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.fragment_login_dialog_login_button:
            case R.id.fragment_login_dialog_register_button:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    view.setElevation(8);
                }
                break;
        }


        switch(viewId){
            case R.id.fragment_login_dialog_login_button:
                String username = ((EditText)mView.findViewById(R.id.fragment_login_dialog_username_text)).getText().toString();
                String password = ((EditText)mView.findViewById(R.id.fragment_login_dialog_password)).getText().toString();
                mLoginController.handleClickLogin(username, password, mXapiServer);
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(mLoginController != null && mResultListener != null)
            mLoginController.setResultListener(mResultListener);
    }

    @Override
    public void setController(LoginController controller) {
        this.mLoginController = controller;
    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public void setXAPIServerURL(String xAPIServerURL) {
        this.mXapiServer = xAPIServerURL;
    }

    @Override
    public void setAdvancedSettingsVisible(boolean visible) {

    }

    @Override
    public void setVersionLabel(String versionLabel) {

    }

    @Override
    public int getDirection() {
        return 0;
    }

    @Override
    public void setDirection(int dir) {

    }

    @Override
    public void setAppMenuCommands(String[] labels, int[] ids) {

    }

    @Override
    public void setUIStrings() {

    }


}
