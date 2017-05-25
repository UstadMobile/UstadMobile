package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothServer;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by kileha3 on 09/05/2017.
 */

public class BluetoothServerAndroid extends BluetoothServer implements Runnable{

    public static final String TAG = "BluetoothServer";
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothServerSocket mServerSocket;

    private boolean running;

    BluetoothServerAndroid(NetworkManager networkManager) {
        super(networkManager);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void start() {
        running = true;
        new Thread(this).start();
    }

    public void run() {

        try {
            mServerSocket=mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                    BluetoothServer.SERVICE_NAME, BluetoothServer.SERVICE_UUID);
            while(running) {
                final BluetoothSocket clientSocket = mServerSocket.accept();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OutputStream out;
                        InputStream in;
                        try{
                            out = clientSocket.getOutputStream();
                            in = clientSocket.getInputStream();
                            handleNodeConnected(clientSocket.getRemoteDevice().getAddress(), in, out);
                            Log.d(TAG,"Connected to "+clientSocket.getRemoteDevice().getName());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void stop() {
       try{
           mServerSocket.close();
       } catch (IOException e) {
           e.printStackTrace();
       }

    }

}
