package com.ustadmobile.port.android.p2p;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import edu.rit.se.wifibuddy.DnsSdTxtRecord;
import edu.rit.se.wifibuddy.FailureReason;
import edu.rit.se.wifibuddy.ServiceType;
import edu.rit.se.wifibuddy.WifiDirectHandler;

import static com.ustadmobile.port.android.p2p.P2PManagerAndroid.SERVICE_NAME;


public class P2PServiceAndroid extends Service{


    private final static int NOTIFICATION_CODE =12;
    private ConnectionChangeListener connectionChangeListener =new ConnectionChangeListener();
    private WifiDirectHandler wifiDirectHandler;
    private final IBinder mBinder = new LocalServiceBinder();
    private static final String NO_PROMPT_NETWORK_PASS="passphrase",
            NO_PROMPT_NETWORK_NAME="networkName",
            DEVICE_MAC_ADDRESS="deviceAddress",
            DEVICE_STATUS="deviceStatus";


    //download a file from p2p
    private NotificationCompat.Builder mBuilder;

    private NotificationManager mNotifyManager;

    private int DOWNLOAD_NOTIFICATION_ID = 8;

    private int fileDownloadingTracker = 0;

    private ArrayList<AsyncTask<String, String, Void>> downloadQueue;

    private ArrayList<String> deviceToDownloadFrom=new ArrayList<>();

    private JSONObject nodeListObject=new JSONObject();
    private String UstadFullDomain=SERVICE_NAME+"."+ServiceType.PRESENCE_TCP+".local.";


    /**
     * Fixed length of time to wait if a no prompt network fails
     */
    public static final int NOPROMPT_RETRY_WAIT_INTERVAL = 10000;

    public static final int NOPROMPT_RETRY_MAX_RETRIES = 6;
    private BroadcastReceiver mNetworkReceiver;


    /**
     * Action Listener that will watch for any failure of the no prompt network and recreate it
     * as required - sometimes group creation will failed with the "BUSY" status if the wifi is
     * doing other stuff.
     */
    protected WifiP2pManager.ActionListener mNoPromptActionListener = new WifiP2pManager.ActionListener() {

        private int retryCount;

        @Override
        public void onSuccess() {
            Log.i(WifiDirectHandler.TAG, "P2PServiceAndroid:noPromptActionListener:onSuccess");
            retryCount = 0;
        }

        @Override
        public void onFailure(int i) {
            Log.e(WifiDirectHandler.TAG, "P2PServiceAndroid:noPromptActionListener:onFailure: " +
                    FailureReason.fromInteger(i));
            retryCount++;
            if(retryCount < NOPROMPT_RETRY_MAX_RETRIES) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(WifiDirectHandler.TAG, "P2PServiceAndroid:noPromptActionListener: Retry #"
                                + retryCount);
                        getWifiDirectHandlerAPI().removeGroup(
                                new WifiP2pManager.ActionListener() {
                              @Override
                              public void onSuccess() {
                                  getWifiDirectHandlerAPI().startAddingNoPromptService(P2PManagerAndroid.makeServiceData(),
                                          mNoPromptActionListener);
                              }

                              @Override
                              public void onFailure(int i) {
                                  getWifiDirectHandlerAPI().startAddingNoPromptService(P2PManagerAndroid.makeServiceData(),
                                          mNoPromptActionListener);
                              }
                          });

                    }
                }, NOPROMPT_RETRY_WAIT_INTERVAL);
            }else {
                Log.e(WifiDirectHandler.TAG, "P2PServiceAndroid:noPromptActionListener: Exceeded retry counts!");

                retryCount = 0;
            }
        }
    };


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


                if(intent.getAction().equals(WifiDirectHandler.Action.DNS_SD_TXT_RECORD_AVAILABLE)){
                    String deviceMac = intent.getStringExtra(WifiDirectHandler.TXT_MAP_KEY);
                    DnsSdTxtRecord txtRecord = wifiDirectHandler.getDnsSdTxtRecordMap().get(deviceMac);
                    String fullDomain = txtRecord.getFullDomain();


                    //TODO Here: Validate the record received
                    if(UstadFullDomain.equalsIgnoreCase(fullDomain)){
                        try{
                            JSONObject nodeObject=new JSONObject();
                            nodeObject.put(DEVICE_MAC_ADDRESS,deviceMac);
                            nodeObject.put(NO_PROMPT_NETWORK_NAME,txtRecord.getRecord().get(NO_PROMPT_NETWORK_NAME).toString());
                            nodeObject.put(NO_PROMPT_NETWORK_PASS,txtRecord.getRecord().get(NO_PROMPT_NETWORK_PASS).toString());
                            nodeObject.put(DEVICE_STATUS,String.valueOf(txtRecord.getDevice().status));
                            nodeListObject.put(deviceMac, nodeObject);

                            Log.i(WifiDirectHandler.TAG,"Available Nodes:\n" +nodeListObject.toString());
                            UstadMobileSystemImpl.getInstance().setAppPref("devices",
                                    nodeListObject.toString(), getApplicationContext());

                            //update device list

                            if(!deviceToDownloadFrom.contains(deviceMac)){
                                deviceToDownloadFrom.add(deviceMac);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }else if(intent.getAction().equals(WifiDirectHandler.Action.SERVICE_CONNECTED)){
                    Toast.makeText(getApplicationContext(),"Device connected to the group",Toast.LENGTH_LONG).show();
                }


            }
        },filter);

        //Handle on connection change

        Intent wifiServiceIntent = new Intent(this, WifiDirectHandler.class);
        bindService(wifiServiceIntent, wifiP2PServiceConnection, BIND_AUTO_CREATE);

        mNetworkReceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                    NetworkInfo extraInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    String gateWaySSID=UstadMobileSystemImpl.getInstance().getAppPref("net_ssid", getApplicationContext());

                    if(extraInfo.getState()==NetworkInfo.State.CONNECTED && isCurrentWifiConnected(gateWaySSID)){

                        connectionChangeListener.onConnected(gateWaySSID);

                        //start file/Index exchange here
                        prepareFileDownload();

                    }else{
                        connectionChangeListener.onFailure(extraInfo.getReason());
                    }

                }

            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, intentFilter);
    }




    @Override
    public void onDestroy() {
        if(wifiDirectHandler!=null){
            wifiDirectHandler.removeGroup();
            wifiDirectHandler.stopServiceDiscovery();
            wifiDirectHandler.removeService();
            UstadMobileSystemImpl.getInstance().setAppPref("devices","",getApplicationContext());
        }
        unregisterReceiver(mNetworkReceiver);
        unbindService(wifiP2PServiceConnection);

        //kill download task
        if(downloadQueue.size()>0){
            killAllDownloadTasks();
        }
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



    ServiceConnection wifiP2PServiceConnection=new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            wifiDirectHandler = ((WifiDirectHandler.WifiTesterBinder) iBinder).getService();
            wifiDirectHandler.setStopDiscoveryAfterGroupFormed(false);
            P2PManagerAndroid manager = (P2PManagerAndroid)UstadMobileSystemImpl.getInstance().getP2PManager();
            manager.init(P2PServiceAndroid.this);

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

    }

    /**
     *
     * @param networkSSID SSID of the network which we are trying to connect with
     * @return return true if the current network matches the one we were trying to connect with
     */

    private boolean isCurrentWifiConnected(String networkSSID) {

        boolean isConnected=false;
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {

            final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !connectionInfo.getBSSID().isEmpty() && networkSSID!=null) {
                isConnected=connectionInfo.getSSID().replace("\"","").equals(networkSSID);
            }
        }
        return isConnected;
    }


    /**
     * show fore ground notification when super node mode is activated
     */
    public void showNotification() {
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


    /**
     *
     * @return instance of the WifiDirectHandler API
     */
    public WifiDirectHandler getWifiDirectHandlerAPI(){
        return wifiDirectHandler;
    }


    /* *************************  File handling starts here ****************** */

    /**
     * Cancel all downloads
     */
    private void killAllDownloadTasks() {
        if (null != downloadQueue & downloadQueue.size() > 0) {
            for (AsyncTask<String, String, Void> downloadInstance : downloadQueue) {
                if (downloadInstance != null) {
                    Log.i("NotificationReceiver", "Killing download thread");
                    downloadInstance.cancel(true);
                }
            }
            mNotifyManager.cancelAll();
        }
    }


    /**
     * Setup notification for file download process
     */
    private void prepareFileDownload(){

        if(deviceToDownloadFrom.size()>0){
            dismissNotification();
            downloadQueue=new ArrayList<>();
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setContentTitle("Downloading Files...").setContentText("Download in progress").setSmallIcon(R.drawable.ic_launcher);
            mBuilder.setProgress(0, 0, true);
            mNotifyManager.notify(DOWNLOAD_NOTIFICATION_ID, mBuilder.build());
            mBuilder.setAutoCancel(true);

            startDownloading();

        }else{
            Log.d(WifiDirectHandler.TAG,deviceToDownloadFrom.size()+" devices");
        }
        

    }

    /**
     * Start file downloading task, this will be called every time after completion of the task
     */
    void startDownloading(){
        FileDownloader fileDownloader=new FileDownloader();
        fileDownloader.execute("https://endlessfame.co.tz/cms-v1/files/images/poster/3X9A5308.jpg",deviceToDownloadFrom.get(fileDownloadingTracker));
        downloadQueue.add(fileDownloader);
    }


    /**
     * @param downloadUrl file link from p2p to be downloaded
     * @param fileName file name
     */
    private void downloadImagesToSdCard(String downloadUrl, String fileName) {
        FileOutputStream fos;
        InputStream inputStream;

        try {
            URL url = new URL(downloadUrl);
            String sdCard = Environment.getExternalStorageDirectory().toString();

            //point it to the right directory for the files to be saved
            File myDir = new File(sdCard, "UstadDemoDownload");

			/* if specified not exist create new */
            if (!myDir.exists()) {
                myDir.mkdir();
            }

            File file = new File(myDir, fileName);

            //check if file exits possibly resume download here


            URLConnection ucon = url.openConnection();

            HttpURLConnection httpConn = (HttpURLConnection) ucon;
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            inputStream = httpConn.getInputStream();

            fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, bufferLength);
            }
            inputStream.close();
            fos.close();
        } catch (Exception e) {
            Log.d(WifiDirectHandler.TAG,"Failed to Download file #"+(fileDownloadingTracker+1));
            e.printStackTrace();
        }

    }

    /**
     * Async class to handle the download out of the Main UI Thread
     */
    private class FileDownloader extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... param) {

            Log.d(WifiDirectHandler.TAG," Downloading file #"+(fileDownloadingTracker+1));
            //handle file name and extension to avoid overwriting them = with MAC ADDRESS
            downloadImagesToSdCard(param[0], "file_"+fileDownloadingTracker+"_" + param[1] + ".opds");
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.i(WifiDirectHandler.TAG, "onPreExecute Called");
            Log.d(WifiDirectHandler.TAG,"Preparing to download file #"+(fileDownloadingTracker+1));
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d(WifiDirectHandler.TAG," File #"+(fileDownloadingTracker+1)+" downloaded successfully");

            float len = deviceToDownloadFrom.size();

            // When the loop is finished, updates the notification
            if (fileDownloadingTracker >= len - 1) {
                mBuilder.setContentTitle("Download Completed");
                mBuilder.setContentText(fileDownloadingTracker +" files downloaded").setProgress(0, 0, false);
                mNotifyManager.notify(DOWNLOAD_NOTIFICATION_ID, mBuilder.build());
            } else {

                //add another download to the queue if any

                int per = (int) (((fileDownloadingTracker + 1) / len) * 100f);
                Log.i("Counter", "Counter : " + fileDownloadingTracker + ", per : " + per);
                mBuilder.setContentTitle("Downloading...");
                mBuilder.setContentText("Downloaded so far (" + fileDownloadingTracker + "/"+deviceToDownloadFrom.size()+")");
                mBuilder.setProgress(100, per, false);
                mNotifyManager.notify(DOWNLOAD_NOTIFICATION_ID, mBuilder.build());
            }

            fileDownloadingTracker++;

        }

    }
}