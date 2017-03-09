package com.ustadmobile.port.android.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.sharedse.p2p.P2PNode;
import com.ustadmobile.port.sharedse.p2p.P2PTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import edu.rit.se.wifibuddy.DnsSdTxtRecord;
import edu.rit.se.wifibuddy.WifiDirectHandler;

/**
 * Created by kileha3 on 07/03/2017.
 */

public class P2PDownloadTaskAndroid extends P2PTask {

    private P2PManagerAndroid p2pManager;

    private int status;

    public static final String P2P_SUPERNODE_SERVERADDR = "http://192.168.49.1:8001/";

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);

            boolean result = intent.getBooleanExtra(WifiDirectHandler.EXTRA_NOPROMPT_NETWORK_SUCCEEDED,
                    false);
            if(!result) {
                //failed
                P2PDownloadTaskAndroid.this.status = -1;
                P2PDownloadTaskAndroid.this.fireTaskEnded();
            }else {
                P2PDownloadTaskAndroid.this.downloadInBackground();
            }


        }
    };


    public P2PDownloadTaskAndroid(P2PNode node, String downloadUri, P2PManagerAndroid p2pManager) {
        super(node, downloadUri);
        this.p2pManager = p2pManager;
    }

    @Override
    public void start() {

        if(!p2pManager.isConnectedToSameNetwork()){
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiDirectHandler.Action.NOPROMPT_NETWORK_CONNECTIVITY_ACTION);
            LocalBroadcastManager.getInstance(p2pManager.getP2pService()).registerReceiver(mBroadcastReceiver,
                    filter);
            WifiDirectHandler wifiDirectHandler = p2pManager.getP2pService().getWifiDirectHandlerAPI();
            DnsSdTxtRecord txtRecord = wifiDirectHandler.getDnsSdTxtRecordMap().get(getNode().getNodeAddress());
            wifiDirectHandler.connectToNoPromptService(txtRecord);


        }else{
            P2PDownloadTaskAndroid.this.downloadInBackground();
        }
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void disconnect(HashMap<String, String> prevConnectedNetwork) {
        if(prevConnectedNetwork.size()>0 && !getNode().getNodeAddress().equals(prevConnectedNetwork.get(P2PManagerAndroid.NETWORK_MACADDRESS))){
            p2pManager.setIsConnectedToSameNetwork(false);
            WifiManager wifiManager = (WifiManager)p2pManager.getP2pService().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.enableNetwork(Integer.parseInt(prevConnectedNetwork.get(P2PManagerAndroid.NETWORK_ID)),true);
        }else{
            p2pManager.setIsConnectedToSameNetwork(true);
        }
    }


    private void downloadInBackground() {
        final AsyncTask<String,Integer,Boolean> task = new AsyncTask<String,Integer,Boolean>() {


            @Override
            protected Boolean doInBackground(String... params) {
                InputStream in = null;
                HttpURLConnection con = null;
                OutputStream out = null;
                File file = null;
                try {
                    String downloadUri = UMFileUtil.joinPaths(new String[]{P2P_SUPERNODE_SERVERADDR,
                            getDownloadUri()});
                    URL url = new URL(downloadUri);

                    con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("GET");
                    con.connect();
                    int fileLength = con.getContentLength();
                    in = con.getInputStream();
                    file = new File(P2PDownloadTaskAndroid.this.getDestinationPath());
                    out = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int bufferLength;
                    int downloadProgress;
                    while ((bufferLength = in.read(buffer)) > 0) {
                        downloadProgress = +bufferLength;
                        publishProgress((downloadProgress * 100) / fileLength);
                        out.write(buffer, 0, bufferLength);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    UMIOUtils.closeOutputStream(out);
                    UMIOUtils.closeInputStream(in);
                    if (con != null)
                        con.disconnect();
                }

                return file != null && file.exists();
            }

            @Override
            protected void onPostExecute(Boolean isFileAvailable) {
                disconnect(p2pManager.getPreviousConnectedNetwork());
                P2PDownloadTaskAndroid.this.fireTaskEnded();
            }
        };
        task.execute();
    }


}
