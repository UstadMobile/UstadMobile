package com.ustadmobile.test.port.android;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.sharedse.http.RemoteTestServerHttpd;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by kileha3 on 14/05/2017.
 */

public class StartServer{

    private static final Object serverLock=new Object();
    private static final int SERVER_RUNNING_DURATION=700000000;
    private boolean serverRunning=false;

    @Test
    public void runServer() throws InterruptedException {
        NetworkManagerAndroid managerAndroid= (NetworkManagerAndroid) UstadMobileSystemImpl.getInstance().getNetworkManager();
        RemoteTestServerHttpd server=new RemoteTestServerHttpd(TestConstants.TEST_DRIVER_COMMAND_PORT,managerAndroid);
        synchronized (serverLock){
            try{
                serverRunning=false;
                server.start();
                serverRunning=server.isAlive();
                assertThat("Was the server started?",serverRunning,is(true));

            } catch (IOException e) {
                e.printStackTrace();
            }
            serverLock.wait(SERVER_RUNNING_DURATION);
        }
    }
}
