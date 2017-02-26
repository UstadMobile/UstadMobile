package com.ustadmobile.port.android.p2p;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import android.support.v4.app.NotificationCompat;

import edu.rit.se.wifibuddy.DnsSdTxtRecord;
import edu.rit.se.wifibuddy.ServiceData;
import edu.rit.se.wifibuddy.ServiceType;
import edu.rit.se.wifibuddy.WifiDirectHandler;

import static com.ustadmobile.core.controller.SettingsDataUsageController.PREFKEY_SUPERNODE;
import static com.ustadmobile.port.android.p2p.P2PManagerAndroid.SERVICE_NAME;


public class P2PServiceAndroid extends Service {


    private final static int NOTIFICATION_CODE =12;
    private boolean NOTIFICATION_STATUS =false;
    private WifiDirectHandler wifiDirectHandler;
    private final IBinder mBinder = new LocalServiceBinder();
    private static final String NO_PROMPT_NETWORK_PASS="passphrase",
            NO_PROMPT_NETWORK_NAME="networkName",
            DEVICE_MAC_ADDRESS="deviceAddress",
            DEVICE_STATUS="deviceStatus";


    private JSONArray nodeListArray=new JSONArray();

    private JSONObject nodeListObject=new JSONObject();

    String UstadFullDomain=SERVICE_NAME+"."+ServiceType.PRESENCE_TCP+".local.";


    public P2PServiceAndroid(){

    }



    @Override
    public void onCreate() {
        super.onCreate();

        UstadMobileSystemImpl.getInstance().setAppPref("devices","",getApplicationContext());
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiDirectHandler.Action.SERVICE_CONNECTED);
        filter.addAction(WifiDirectHandler.Action.MESSAGE_RECEIVED);
        filter.addAction(WifiDirectHandler.Action.DEVICE_CHANGED);
        filter.addAction(WifiDirectHandler.Action.WIFI_STATE_CHANGED);
        filter.addAction(WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE);



        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.d(WifiDirectHandler.TAG+"states:",intent.getAction());

             if(intent.getAction().equals(WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE)){

                    String deviceMac = intent.getStringExtra(WifiDirectHandler.TXT_MAP_KEY);
                    DnsSdTxtRecord txtRecord = wifiDirectHandler.getDnsSdTxtRecordMap().get(deviceMac);
                    String fullDomain = txtRecord.getFullDomain();



                    if(UstadFullDomain.equalsIgnoreCase(fullDomain)){

                        try{
                            JSONObject nodeObject=new JSONObject();
                            nodeObject.put(DEVICE_MAC_ADDRESS,deviceMac);
                            nodeObject.put(NO_PROMPT_NETWORK_NAME,txtRecord.getRecord().get(NO_PROMPT_NETWORK_NAME).toString());
                            nodeObject.put(NO_PROMPT_NETWORK_PASS,txtRecord.getRecord().get(NO_PROMPT_NETWORK_PASS).toString());
                            nodeObject.put(DEVICE_STATUS,String.valueOf(txtRecord.getDevice().status));

                            if(!isNodeExist(nodeListArray,deviceMac)){
                                nodeListArray.put(nodeObject);
                                nodeListObject.put("devices",nodeListArray);
                                UstadMobileSystemImpl.getInstance().setAppPref("devices",nodeListObject.toString(),getApplicationContext());
                            }

                            Log.i(WifiDirectHandler.TAG,"Available Node\n"
                            +nodeListObject.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        },filter);

        Intent wifiServiceIntent = new Intent(this, WifiDirectHandler.class);
        bindService(wifiServiceIntent, wifiP2PServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        if(wifiDirectHandler!=null){
            wifiDirectHandler.removeGroup();
            wifiDirectHandler.stopServiceDiscovery();
            wifiDirectHandler.removeService();
            UstadMobileSystemImpl.getInstance().setAppPref("devices","",getApplicationContext());
        }
        unbindService(wifiP2PServiceConnection);
        super.onDestroy();
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


    private boolean isNodeExist(JSONArray jsonArray, String deviceMacAddress){
        return jsonArray.toString().contains("\""+DEVICE_MAC_ADDRESS+"\":\""+deviceMacAddress+"\"");
    }


    ServiceConnection wifiP2PServiceConnection=new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            wifiDirectHandler = ((WifiDirectHandler.WifiTesterBinder) iBinder).getService();
            //wifiDirectHandler.registerP2p();
            if(Boolean.parseBoolean(UstadMobileSystemImpl.getInstance().getAppPref(PREFKEY_SUPERNODE, "false", getApplicationContext()))){
                HashMap<String,String> record=new HashMap<>();
                record.put("available","available");
                ServiceData serviceData=new ServiceData(SERVICE_NAME,8001,record, ServiceType.PRESENCE_TCP);
                wifiDirectHandler.startAddingNoPromptService(serviceData);
                showNotification();
            }else{
                if(NOTIFICATION_STATUS){
                    stopForeground(true);
                }
                if(wifiDirectHandler.isGroupFormed()){
                    wifiDirectHandler.removeGroup();

                }
                wifiDirectHandler.continuouslyDiscoverServices();
            }

        }


        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            wifiDirectHandler = null;

        }
    };


    public class LocalServiceBinder extends Binder {
        public P2PServiceAndroid getService(){
            return P2PServiceAndroid.this;
        }

    }


    public void dismissNotification(){
        stopForeground(true);
        NOTIFICATION_STATUS=false;
    }



    public void showNotification() {
        NOTIFICATION_STATUS=true;
        Bitmap bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.launcher_icon);
        final Notification notification = new NotificationCompat.Builder(this)
                .setCategory(Notification.CATEGORY_PROMO)
                .setContentTitle("Ustad Mobile")
                .setSmallIcon(R.drawable.launcher_icon)
                .setLargeIcon(bitmap)
                .setContentText(Html.fromHtml("Super node mode is running..."))
                .setColor(getResources().getColor(R.color.primary_dark))
                .setAutoCancel(true)
                .setVisibility(1)
                .setPriority(Notification.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(Html.fromHtml("Server node running...")))
                .build();

        startForeground(NOTIFICATION_CODE,notification);

    }

    public WifiDirectHandler getWifiDirectHandlerAPI(){
        return wifiDirectHandler;
    }

}