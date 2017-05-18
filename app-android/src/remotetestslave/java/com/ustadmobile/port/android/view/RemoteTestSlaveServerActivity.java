package com.ustadmobile.port.android.view;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.sharedse.http.RemoteTestServerHttpd;

import java.io.IOException;

public class RemoteTestSlaveServerActivity extends UstadBaseActivity implements ServiceConnection {

    private RemoteTestServerHttpd serverHttpd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_test_slave_server_acitivity);
        try {
            CatalogController.makeDeviceFeed(
                    UstadMobileSystemImpl.getInstance().getStorageDirs(CatalogController.SHARED_RESOURCE, this),
                    CatalogController.SHARED_RESOURCE, this);
            Toast.makeText(this, "Device feed scanned.", Toast.LENGTH_LONG).show();
        }catch(IOException e) {
            UstadMobileSystemImpl.getInstance().getAppView(this).showAlertDialog("Scan error",
                    "Error calling makeDeviceFeed");
        }

    }

    /**
     * UstadBaseActivity and SystemImpl will automatically bind this to the network service.
     *
     * Any activity that implements ServiceConnection will get a onServiceConnected call when
     * the service is bound
     *
     * @param name
     * @param iBinder
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        super.onServiceConnected(name, iBinder);

        if(iBinder instanceof NetworkServiceAndroid.LocalServiceBinder) {
            //network service has been bound - OK to start Httpd
            serverHttpd = new RemoteTestServerHttpd(TestConstants.TEST_REMOTE_SLAVE_SERVER_PORT,
                    UstadMobileSystemImplSE.getInstanceSE().getNetworkManager());
            try {
                serverHttpd.start();
                Toast.makeText(this, "Test slave server started", Toast.LENGTH_LONG).show();
            }catch(IOException e) {
                //show a pop
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("NanoHTTPD Server");
                builder.setMessage("NanoHTTPD server failed to start");
                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }

        }

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        super.onServiceDisconnected(name);
    }
}
