package com.toughra.ustadmobile;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.ustadmobile.port.android.view.LoginActivity;
import com.ustadmobile.core.util.TestUtils;
import com.ustadmobile.test.port.android.UMAndroidTestUtil;

import java.util.Hashtable;

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
        final Hashtable loadedVals = new Hashtable();
        new Thread(new Runnable() {
            public void run() {
                try {   
                    System.out.println("Starting thread to check python service..");
                    boolean pythonServiceStatus = false;
                    pythonServiceStatus = UMAndroidTestUtil.waitForPythonService(getActivity().getApplicationContext());
                    loadedVals.put("pythonServiceRunning", pythonServiceStatus);
                }catch(Exception e) {
                    System.out.println("Exception in getting Python Service status.");
                }
            }
        }).start();

        System.out.println("Checking by waitForValueInTable");
        TestUtils.waitForValueInTable("pythonServiceRunning", loadedVals);
        boolean pythonServiceStatus = (boolean)loadedVals.get("pythonServiceRunning");

        assertTrue("Python Service Running", pythonServiceStatus);

        assertNotNull("Can launch activity with default intent", getActivity());
    }

}
