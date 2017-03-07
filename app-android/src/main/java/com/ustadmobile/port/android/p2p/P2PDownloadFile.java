package com.ustadmobile.port.android.p2p;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ustadmobile.port.sharedse.p2p.P2PTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import edu.rit.se.wifibuddy.WifiDirectHandler;

/**
 * Created by kileha3 on 05/03/2017.
 */

public class P2PDownloadFile extends AsyncTask<Void,Integer,Boolean>{

    private P2PTask task;
    private String reason;
    private NotificationCompat.Builder mBuilder;
    P2PDownloadFile(P2PTask task, NotificationCompat.Builder mBuilder){
        this.task=task;
        this.mBuilder=mBuilder;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mBuilder.setContentTitle("Initializing...").setContentText("Download in progress");
        Log.d(WifiDirectHandler.TAG,"P2PDownloadFile: preparing download task");
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        FileOutputStream fos;
        InputStream inputStream;
        File file=null;

        try {
            URL url = new URL(task.getDownloadUri());
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

            file = new File(myDir, "newFile.opds");

            URLConnection ucon = url.openConnection();

            HttpURLConnection httpConn = (HttpURLConnection) ucon;
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            int fileLength = httpConn.getContentLength();
            inputStream = httpConn.getInputStream();

            fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bufferLength;
            int downloadProgress;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                downloadProgress=+bufferLength;
                publishProgress((downloadProgress*100)/fileLength);
                fos.write(buffer, 0, bufferLength);
            }
            inputStream.close();
            fos.close();

        } catch (Exception e) {
            reason=stackTraceToString(e);
        }

        return file.exists();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mBuilder.setContentText("Downloading "+values[0]+"%");
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Boolean isCompletedSuccessfully) {
        super.onPostExecute(isCompletedSuccessfully);
        Log.d(WifiDirectHandler.TAG,"fileDownloadStatus: "+isCompletedSuccessfully);

    }

    private String stackTraceToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
