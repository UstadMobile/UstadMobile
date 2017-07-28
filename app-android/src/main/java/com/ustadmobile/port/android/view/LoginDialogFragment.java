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
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.DialogResultListener;
import com.ustadmobile.core.view.DismissableDialog;
import com.ustadmobile.core.view.LoginView;
import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

        Button forgotPassword= (Button) mView.findViewById(R.id.fragment_login_forgot_password_button);
        forgotPassword.setText(Html.fromHtml("<u>"
                + impl.getString(MessageID.forgot_password, getContext()) + "</u>"));
        forgotPassword.setTransformationMethod(null);

        Button loginButton = (Button)mView.findViewById(R.id.fragment_login_dialog_login_button);
        loginButton.setOnClickListener(this);

        Button registerButton = (Button)mView.findViewById(R.id.fragment_login_dialog_register_button);
        registerButton.setOnClickListener(this);

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

            case R.id.fragment_login_dialog_register_button:
                //TODO: Go to Registration fragment
                //Make something, persist and set active user in impl.
                setUserTemp();
                break;
        }
    }

    /**
     * TODO: Delete this and its call after Registration fragment is done.
     */
    public void setUserTemp(){
        //TODO: Remove this after logged in user is set
        Object context = getContext();
        UserManager userManager =
                PersistenceManager.getInstance().getManager(UserManager.class);
        UserCustomFieldsManager userCustomFieldsManager =
                PersistenceManager.getInstance().getManager(UserCustomFieldsManager.class);
        String loggedInUsername = null;
        User loggedInUser = null;

        String usertempUsername = "usertemp";
        loggedInUsername = UstadMobileSystemImpl.getInstance().getActiveUser(context);
        //ignore loggedInUsername cause if we're clicking register, we want this user
        //to log in..
        List<User> users = userManager.findByUsername(context, usertempUsername);
        if(users!= null && !users.isEmpty()){
            loggedInUser = users.get(0);
        }else{
            //create a test user
            try {
                loggedInUser = (User)userManager.makeNew();
                loggedInUser.setUsername(usertempUsername);
                loggedInUser.setUuid(UUID.randomUUID().toString());
                loggedInUser.setPassword("secret");
                loggedInUser.setNotes("test user");
                loggedInUser.setDateCreated(System.currentTimeMillis());
                userManager.persist(context, loggedInUser);

                String universityName = "Web University";
                String name = "Bob Burger";
                String gender = "M";
                String email = "bob@bobsburgers.com";
                String phoneNumber = "+0123456789";
                String faculty = "A faculty";
                String username = "autocustomreguser";
                String password = "secret";

                Map<Integer, String> map = new HashMap<>();
                map.put(70, universityName);
                map.put(71, name);
                map.put(72, gender);
                map.put(73, email);
                map.put(74, phoneNumber);
                map.put(75, faculty);
                map.put(76, username);
                map.put(77, password);

                userCustomFieldsManager.createUserCustom(map,loggedInUser, context);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        UstadMobileSystemImpl.getInstance().setActiveUser(loggedInUser.getUsername(), context);
        System.out.println("!!!!!!Made a test user. PLEASE DELETE THIS FOR PROD!!!!!!");

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
