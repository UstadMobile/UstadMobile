package com.ustadmobile.test.port.android;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.sharedse.http.RemoteTestServerHttpd;

import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by kileha3 on 14/05/2017.
 */

public class StartTestSlaveServer {

    private static final Object serverLock=new Object();
    private static final int SERVER_RUNNING_DURATION=(15 * 60 * 1000);//15mins
    private boolean serverRunning=false;

    private final ServiceTestRule mTestRule=new ServiceTestRule();

    @Test
    public void runServer() throws Exception {
        mTestRule.bindService(new Intent(InstrumentationRegistry.getTargetContext(),
                NetworkServiceAndroid.class));
        //try { Thread.sleep(10000); }
        //catch(InterruptedException e ) { e.printStackTrace(); }
        NetworkManagerAndroid managerAndroid= (NetworkManagerAndroid) UstadMobileSystemImpl.getInstance().getNetworkManager();

        RemoteTestServerHttpd server=new RemoteTestServerHttpd(TestConstants.TEST_REMOTE_SLAVE_SERVER_PORT,managerAndroid);
        server.start();
        System.out.println("Started server on: " + TestConstants.TEST_REMOTE_SLAVE_SERVER_PORT);

        HttpURLConnection con = (HttpURLConnection)new URL("http://localhost:" + TestConstants.TEST_REMOTE_SLAVE_SERVER_PORT
                + "/?cmd=SUPERNODE&enabled=true" ).openConnection();
        con.connect();
        int responseCode = con.getResponseCode();
        con.disconnect();

        long startTime = new Date().getTime();
        long endTime = startTime + SERVER_RUNNING_DURATION;
        while(new Date().getTime() < endTime) {
            try { Thread.sleep(endTime - new Date().getTime()); }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Done");
        try { Thread.sleep(SERVER_RUNNING_DURATION); }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
        /*
        synchronized (serverLock){
            try{
                serverRunning=false;

                serverRunning=server.isAlive();
                assertThat("Was the server started?",serverRunning,is(true));

            } catch (IOException e) {
                e.printStackTrace();
            }
            serverLock.wait(SERVER_RUNNING_DURATION);
        }*/
    }
}
