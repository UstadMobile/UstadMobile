package com.ustadmobile.port.android.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Environment;
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
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiDirectHandler.Action.NOPROMPT_NETWORK_CONNECTIVITY_ACTION);
        LocalBroadcastManager.getInstance(p2pManager.getP2pService()).registerReceiver(mBroadcastReceiver,
                filter);
        WifiDirectHandler wifiDirectHandler = p2pManager.getP2pService().getWifiDirectHandlerAPI();
        DnsSdTxtRecord txtRecord = wifiDirectHandler.getDnsSdTxtRecordMap().get(getNode().getNodeAddress());
        wifiDirectHandler.connectToNoPromptService(txtRecord);
    }

    @Override
    public int getStatus() {
        return status;
    }

    private void downloadInBackground() {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                InputStream in = null;
                HttpURLConnection con = null;
                OutputStream out = null;
                try {
                    String downloadUri = UMFileUtil.joinPaths(new String[] { P2P_SUPERNODE_SERVERADDR,
                            getDownloadUri()});
                    URL url = new URL(downloadUri);
                    String sdCard = Environment.getExternalStorageDirectory().toString();

                    //point it to the right directory for the files to be saved
                    File myDir = new File(sdCard, "UstadDemoDownload");


                    /* if specified not exist create new */
                    if (!myDir.exists()) {
                        myDir.mkdir();
                    }else{
                        myDir.delete();
                        myDir.mkdir();
                    }

                    File file = new File(myDir, "newFile.opds");

                    con = (HttpURLConnection)url.openConnection();

                    con.setRequestMethod("GET");
                    con.connect();
                    int fileLength = con.getContentLength();
                    in = con.getInputStream();

                    out = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int bufferLength;
                    int downloadProgress;
                    while ((bufferLength = in.read(buffer)) > 0) {
                        downloadProgress=+bufferLength;
                        publishProgress((downloadProgress*100)/fileLength);
                        out.write(buffer, 0, bufferLength);
                    }
                }catch(IOException e) {
                    e.printStackTrace();
                }finally {
                    UMIOUtils.closeOutputStream(out);
                    UMIOUtils.closeInputStream(in);
                    if(con != null)
                        con.disconnect();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                P2PDownloadTaskAndroid.this.fireTaskEnded();
            }
        };
        task.execute();
    }




}
