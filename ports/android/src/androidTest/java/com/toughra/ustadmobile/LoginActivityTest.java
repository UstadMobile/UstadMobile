package com.toughra.ustadmobile;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.ustadmobile.port.android.view.LoginActivity;
/**
 * Test the LoginActivity lifecycle
 *
 * Created by mike on 9/22/15.
 */
public class LoginActivityTest extends ActivityInstrumentationTestCase2<LoginActivity>{



    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception{
        super.setUp();
        Intent launchIntent = new Intent();
        setActivityIntent(launchIntent);
        getActivity();
    }

    public void testLoginActivity() {
        assertNotNull("Can launch activity with default intent", getActivity());
    }

}
