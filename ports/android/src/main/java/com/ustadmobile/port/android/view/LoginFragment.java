package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.toughra.ustadmobile.R;
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


        if(this.positionID == 0) {
            Button loginButton = (Button)viewGroup.findViewById(R.id.login_button);
            loginButton.setOnClickListener(LoginViewAndroid.getViewById(loginActivity.getViewID()));
        }

        return viewGroup;
    }
}