package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.LoginController;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AppView;
import com.ustadmobile.core.view.LoginView;

/**
 * Created by mike on 8/24/15.
 */
public class LoginFragment extends Fragment {

    public static final String ARG_POSITIONID = "POSITION";

    private int positionID = 0;

    private ViewGroup mRootViewGroup;

    /**
     * Mapping of positions to IDs of the layout resources.  Positions (tabs) as defined in
     * LoginView are 0 - Normal login, 1 - Register, 2 - Join Class
     */
    public static final int[] layoutIDs = new int[]{R.layout.fragment_login, R.layout.fragment_login_register, R.layout.fragment_login_joinclass};


    public LoginFragment() {

    }

    public static LoginFragment newInstance(int positionId) {
        LoginFragment retVal = new LoginFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITIONID, positionId);
        retVal.setArguments(args);
        return retVal;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            positionID = getArguments().getInt(ARG_POSITIONID);
        }
    }

    private void setTextViewHint(ViewGroup viewGroup, int id, String hint) {
        ((TextView)viewGroup.findViewById(id)).setHint(hint);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootViewGroup = (ViewGroup)inflater.inflate(layoutIDs[positionID], container, false);
        LoginActivity loginActivity = (LoginActivity)getActivity();


        switch(this.positionID) {
            case LoginView.SECTION_LOGIN:
                Button loginButton = (Button)mRootViewGroup.findViewById(R.id.login_button);
                loginButton.setOnClickListener(loginActivity);
                CheckBox advancedCheckbox =(CheckBox) mRootViewGroup.findViewById(R.id.login_advanced_checkbox);
                advancedCheckbox.setOnCheckedChangeListener(loginActivity);
                ((EditText)mRootViewGroup.findViewById(R.id.login_xapi_server)).setText(loginActivity.mXAPIServer);
                ((TextView) mRootViewGroup.findViewById(R.id.login_version_label)).setText(loginActivity.mVersionLabel);
                break;
            case LoginView.SECTION_REGISTER:
                Spinner countrySpinner = (Spinner)mRootViewGroup.findViewById(R.id.login_registercountry);
                ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(),
                    R.layout.login_register_countrytextview, UstadMobileConstants.COUNTRYNAMES);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                countrySpinner.setAdapter(adapter);
                Button registerButton = (Button)mRootViewGroup.findViewById(R.id.login_registerbutton);
                registerButton.setOnClickListener(loginActivity);
                lookupCountry(countrySpinner, getActivity());
                break;
        }

        setUIStrings();

        return mRootViewGroup;
    }

    public void setUIStrings() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        switch(this.positionID) {
            case LoginView.SECTION_LOGIN:
                Button loginButton = (Button)mRootViewGroup.findViewById(R.id.login_button);
                loginButton.setText(impl.getString(MessageID.login, getContext()));
                ((TextView)mRootViewGroup.findViewById(R.id.login_username)).setHint(
                        impl.getString(MessageID.username, getContext()));
                ((TextView)mRootViewGroup.findViewById(R.id.login_password)).setHint(
                        impl.getString(MessageID.password, getContext()));

                CheckBox advancedCheckbox =(CheckBox) mRootViewGroup.findViewById(R.id.login_advanced_checkbox);
                advancedCheckbox.setText(impl.getString(MessageID.advanced, getContext()));
                ((TextView)mRootViewGroup.findViewById(R.id.login_server_label)).setText(
                        impl.getString(MessageID.server, getContext()));
                break;
            case LoginView.SECTION_REGISTER:
                Button registerButton = (Button)mRootViewGroup.findViewById(R.id.login_registerbutton);
                registerButton.setText(impl.getString(MessageID.register, getContext()));

                setTextViewHint(mRootViewGroup, R.id.login_registername,
                        impl.getString(MessageID.name, getContext()));
                ((TextView)mRootViewGroup.findViewById(R.id.login_registerphonenum)).setHint(
                        impl.getString(MessageID.phone_number, getContext()));
                ((RadioButton)mRootViewGroup.findViewById(R.id.login_register_radio_male)).setText(
                        impl.getString(MessageID.male, getContext()));
                ((RadioButton)mRootViewGroup.findViewById(R.id.login_register_radio_female)).setText(
                        impl.getString(MessageID.female, getContext()));

                String optSffx = " (" + impl.getString(MessageID.optional, getContext()) + ")";
                setTextViewHint(mRootViewGroup, R.id.login_registerusername,
                        impl.getString(MessageID.username, getContext()) + optSffx);
                setTextViewHint(mRootViewGroup, R.id.login_registerpassword,
                        impl.getString(MessageID.password, getContext()) + optSffx);
                setTextViewHint(mRootViewGroup, R.id.login_registeremail,
                        impl.getString(MessageID.email, getContext()) + optSffx);
                setTextViewHint(mRootViewGroup, R.id.login_registerregcode,
                        impl.getString(MessageID.regcode, getContext()) + optSffx);


                break;
        }
    }


    public void lookupCountry(final Spinner countrySpinner, final Activity activity) {
        final Context ctx = getActivity();
        LoginController.getCountryCode(ctx, UstadMobileDefaults.DEFAULT_GEOIP_SERVER, new UmCallback() {
            @Override
            public void onSuccess(Object result) {
                String countryCode = (String)result;
                final int countryIndex = LoginController.getCountryIndexByCode(countryCode);
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        countrySpinner.setSelection(countryIndex);
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                UstadMobileSystemImpl.getInstance().getAppView(ctx).showNotification(
                        "Sorry - Could not detect country", AppView.LENGTH_LONG);
            }
        });
    }

}