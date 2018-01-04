package com.ustadmobile.port.android.netwokmanager;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.nanolrs.android.service.UMSyncService;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.port.android.impl.UstadMobileSystemImplAndroid;
import com.ustadmobile.port.sharedse.impl.UstadMobileSystemImplSE;

import java.sql.SQLException;

import edu.rit.se.wifibuddy.WifiDirectHandler;
import listener.ActiveSyncListener;
import listener.ActiveUserListener;

import static com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid.PREF_KEY_SUPERNODE;


/**
 * <h1>NetworkServiceAndroid</h1>
 *
 * NetworkServiceAndroid is effectively a wrapper for NetworkManager. A service is required as this
 * encapsulates network discovery processes and the http server that should continue running
 * regardless of which activity is active.
 *
 * @see android.app.Service
 *
 * @author kileha3
 *
 */
public class NetworkServiceAndroid extends Service
        implements ActiveUserListener, ActiveSyncListener{

    private WifiDirectHandler wifiDirectHandler;
    private final IBinder mBinder = new LocalServiceBinder();
    private NetworkManagerAndroid networkManagerAndroid;

    private UMSyncService umSyncService;

    private boolean isSyncHappening = false;

    /**
     * Default time interval for Wi-Fi Direct service rebroadcasting.
     */
    public static final int SERVICE_REBROADCASTING_TIMER=120000;

    public NetworkServiceAndroid(){}

    @Override
    public void onCreate() {
        super.onCreate();
        networkManagerAndroid = (NetworkManagerAndroid)
                UstadMobileSystemImplAndroid.getInstanceAndroid().getNetworkManager();
        networkManagerAndroid.init(NetworkServiceAndroid.this);

        //Bind WifiService
        Intent wifiServiceIntent = new Intent(this, WifiDirectHandler.class);
        bindService(wifiServiceIntent, wifiP2PServiceConnection, BIND_AUTO_CREATE);

        //Sync: Bing SyncService TODO: Remove. Replaced by SyncJob
        Intent umSyncServiceIntent = new Intent(this, UMSyncService.class);
        UstadMobileSystemImplSE.getInstanceSE().addActiveUserListener(this);
        bindService(umSyncServiceIntent, umSyncServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        networkManagerAndroid.onDestroy();

        if(wifiDirectHandler!=null){
            wifiDirectHandler.removeGroup();
            wifiDirectHandler.stopServiceDiscovery();
            wifiDirectHandler.removeService();
            UstadMobileSystemImpl.getInstance().setAppPref("devices",
                    "",getApplicationContext());
        }
        unbindService(wifiP2PServiceConnection);

        //Sync: Remove active User as sync is over and unbind service.
        UstadMobileSystemImplSE.getInstanceSE().removeActiveUserListener(this);
        unbindService(umSyncServiceConnection);

        super.onDestroy();
    }

    /**
     *
     * @return NetworkManagerAndroid : NetworkManagerAndroid class reference
     */
    public NetworkManagerAndroid getNetworkManager() {
        return  networkManagerAndroid;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     *This is an interface for monitoring the state of an application service.
     * it defines callbacks for service binding, passed to bindService().
     * Either of the two methods will be invoked:
     * <p>
     *     <b>onServiceConnected</b>: Invoked when service successfully connected
     *     <b>onServiceDisconnected</b>: Invoked when service connection failed.
     * </p>
     */
    ServiceConnection wifiP2PServiceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            wifiDirectHandler = ((WifiDirectHandler.WifiTesterBinder) iBinder).getService();
            wifiDirectHandler.setStopDiscoveryAfterGroupFormed(false);
            wifiDirectHandler.setPeerDiscoveryInterval(SERVICE_REBROADCASTING_TIMER);
            wifiDirectHandler.setLocalServicePeerDiscoveryKickEnabled(false);

            boolean isSuperNodeEnabled = Boolean.parseBoolean(UstadMobileSystemImpl.getInstance().getAppPref(
                    PREF_KEY_SUPERNODE, "false", NetworkServiceAndroid.this.getApplicationContext()));
            networkManagerAndroid.setSuperNodeEnabled(NetworkServiceAndroid.this.getApplicationContext(),
                    isSuperNodeEnabled);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            wifiDirectHandler = null;
        }
    };

    /**
     *This is an interface for monitoring the state of an application service.
     * it defines callbacks for service binding, passed to bindService().
     * Either of the two methods will be invoked:
     * <p>
     *     <b>onServiceConnected</b>: Invoked when service successfully connected
     *     <b>onServiceDisconnected</b>: Invoked when service connection failed.
     * </p>
     */
    ServiceConnection umSyncServiceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            User loggedInUser = null;
            Node endNode = null;
            String mainNodeHostName = UMSyncService.DEFAULT_MAIN_SERVER_HOST_NAME;
            String loggedInUsername = null;
            Object context = getApplicationContext();

            UserManager userManager =
                    PersistenceManager.getInstance().getManager(UserManager.class);
            NodeManager nodeManager =
                    PersistenceManager.getInstance().getManager(NodeManager.class);

            umSyncService = ((UMSyncService.UMSyncBinder) iBinder).getService();

            loggedInUsername = UstadMobileSystemImpl.getInstance().getActiveUser(context);

            if(loggedInUsername != null && !loggedInUsername.isEmpty()) {
                loggedInUser = userManager.findByUsername(context, loggedInUsername);
            }

            if(loggedInUser ==null){
                //loggedInUser = null;
                //TODO: Remove on success of new way.
                System.out.println("No user logged in. Setting null (will not proceed)");
            }

            try {
                endNode = nodeManager.getMainNode(mainNodeHostName, context);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            umSyncService.setLoggedInUser(loggedInUser);
            umSyncService.setEndNode(endNode);

            //TODO: Test this new way:
            String loggedInUserCred = UstadMobileSystemImpl.getInstance().getActiveUserAuth(context);
            umSyncService.setPassword(loggedInUserCred);

            System.out.println("onServiceConnected ok.");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //sup?
            int x = 0;
        }

    };

    //ActiveUserListener:

    @Override
    public void credChanged(String cred, Object context){
        umSyncService.setPassword(cred);
        System.out.println("NetworkServiceandroid: Updated Active Cred..");
    }

    @Override
    public void userChanged(String username, Object context) {

        if(context == null){
            context = getApplicationContext();
        }

        UserManager userManager =
                PersistenceManager.getInstance().getManager(UserManager.class);
        User loggedInUser = null;

        loggedInUser = userManager.findByUsername(context, username);
        if(loggedInUser == null){
            System.out.println("No user logged in. Setting null (will not proceed)");
        }

        umSyncService.setLoggedInUser(loggedInUser);

        //TODO: test new way.
        String loggedInUserCred = UstadMobileSystemImpl.getInstance().getActiveUserAuth(context);
        //umSyncService.setPassword(loggedInUserCred);
        //The above didn't work since loggedInUserCred would be null always.
        if(loggedInUserCred == null){
            System.out.println("NetworkServiceandroid: Active Auth is null. Changing it..");
        }
        System.out.println("user changed.");

    }

    //ActiveSyncListener: TODO: remove . Not being used. Moved to the Activity

    @Override
    public boolean isSyncHappening(Object context) {
        return this.isSyncHappening;
    }

    @Override
    public void setSyncHappening(boolean happening, Object context) {
        this.isSyncHappening = happening;

    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we won't be dealing with IPC.
     */
    public class LocalServiceBinder extends Binder {
        public NetworkServiceAndroid getService(){
            return NetworkServiceAndroid.this;
        }

    }

    /**
     * @return WifiDirectHandler: Instance of the WifiDirectHandler from Wi-Fi buddy API
     */
    public WifiDirectHandler getWifiDirectHandlerAPI(){
        return wifiDirectHandler;
    }

}