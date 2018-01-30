package com.ustadmobile.lib.androidtestserver.view;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.port.android.view.UstadBaseActivity;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.sharedse.http.RemoteTestServerHttpd;
import com.ustadmobile.umtestserver.R;

import java.io.IOException;

import static com.ustadmobile.port.android.view.SplashScreenActivity.REQUIRED_PERMISSIONS;

public class RemoteTestSlaveServerActivity extends UstadBaseActivity implements ServiceConnection, ActivityCompat.OnRequestPermissionsResultCallback {

    private RemoteTestServerHttpd serverHttpd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_test_slave_server);
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 1);
    }


    protected void checkPermissions() {
        boolean hasRequiredPermissions = true;
        for(int i = 0; i < REQUIRED_PERMISSIONS.length; i++) {
            hasRequiredPermissions &= ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSIONS[i]) == PackageManager.PERMISSION_GRANTED;
        }
        if(hasRequiredPermissions) {
            scanFeed();
        }else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        checkPermissions();
    }

    protected void scanFeed() {
        String contentDir = UstadMobileSystemImpl.getInstance().getStorageDirs(CatalogPresenter.SHARED_RESOURCE,
                this)[0].getDirURI();

        DbManager.getInstance(this).getOpdsEntryWithRelationsRepository()
                .findEntriesByContainerFileDirectoryAsList(contentDir);
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        serverHttpd.stop();
    }
}
