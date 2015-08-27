package com.ustadmobile.port.android.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.LoginController;
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup)inflater.inflate(layoutIDs[positionID], container, false);
        LoginActivity loginActivity = (LoginActivity)getActivity();

        LoginViewAndroid loginView = LoginViewAndroid.getViewById(loginActivity.getViewID());

        switch(this.positionID) {
            case LoginView.SECTION_LOGIN:
                Button loginButton = (Button)viewGroup.findViewById(R.id.login_button);
                loginButton.setOnClickListener(loginView);
                break;
            case LoginView.SECTION_REGISTER:
                Spinner countrySpinner = (Spinner)viewGroup.findViewById(R.id.login_registercountry);
                ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity(),
                    R.layout.login_register_countrytextview, UstadMobileConstants.COUNTRYNAMES);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                countrySpinner.setAdapter(adapter);
                Button registerButton = (Button)viewGroup.findViewById(R.id.login_registerbutton);
                registerButton.setOnClickListener(loginView);
                lookupCountry(countrySpinner, getActivity());
                break;
        }

        return viewGroup;
    }

    public void lookupCountry(final Spinner countrySpinner, final Activity activity) {
        Thread countryLookupThread = new Thread() {
            public void run() {
                try {
                    String countryCode =
                            LoginController.getCountryCode(UstadMobileDefaults.DEFAULT_GEOIP_SERVER);
                    final int countryIndex = LoginController.getCountryIndexByCode(countryCode);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            countrySpinner.setSelection(countryIndex);
                        }
                    });
                }catch(Exception e) {
                    e.printStackTrace();
                    UstadMobileSystemImpl.getInstance().getAppView().showNotification(
                        "Sorry - Could not detect country", AppView.LENGTH_LONG);
                }
            }
        };
        countryLookupThread.start();
    }

}