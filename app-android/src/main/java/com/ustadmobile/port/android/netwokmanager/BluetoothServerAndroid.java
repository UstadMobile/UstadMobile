package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothServer;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by kileha3 on 09/05/2017.
 */

public class BluetoothServerAndroid extends BluetoothServer {

    public static final String TAG = "BluetoothServer";
    private BluetoothAdapter mBluetoothAdapter;
    private static final String NAME_INSECURE = "BluetoothChatInsecure";
    private static final UUID UUID_INSECURE =
            UUID.fromString("ad9e3a05-7d80-4a12-b50b-91c72d442683");

    private BluetoothListeningThread mInsecureBluetoothListeningThread;


    BluetoothServerAndroid(NetworkManager networkManager) {
        super(networkManager);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void start() {
        if (mInsecureBluetoothListeningThread == null) {
            mInsecureBluetoothListeningThread = new BluetoothListeningThread();
            mInsecureBluetoothListeningThread.start();
        }
    }

    @Override
    public void stop() {

        if (mInsecureBluetoothListeningThread != null) {
            mInsecureBluetoothListeningThread.cancel();
            mInsecureBluetoothListeningThread = null;
        }

    }


    public BluetoothAdapter getBluetoothAdapter() {
        return this.mBluetoothAdapter;
    }

    public boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public UUID getInsecureUUID(){
        return UUID_INSECURE;
    }


    private class BluetoothListeningThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public BluetoothListeningThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        NAME_INSECURE, UUID_INSECURE);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmServerSocket = tmp;
        }

        public void run() {
            try {
                mmServerSocket.accept();
                boolean connected=mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET)
                        == BluetoothHeadset.STATE_CONNECTED;
                if(connected){
                    Log.d("Server Side ","Connected to "+mBluetoothAdapter.getName());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
