package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.ustadmobile.port.sharedse.networkmanager.BluetoothConnectionHandler;
import com.ustadmobile.port.sharedse.networkmanager.BluetoothServer;

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
    private int bluetoothState;

    private BluetoothListeningThread mInsecureBluetoothListeningThread;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Context mContext;


    BluetoothServerAndroid(Context context) {
        this.mContext=context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothState = BLUETOOTH_STATE_NONE;
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

        bluetoothState=BLUETOOTH_STATE_NONE;
    }

    @Override
    public void handleNodeConnected(String deviceAddress, DataInputStream inputStream, DataOutputStream outputStream) {

    }

    @Override
    public void setBluetoothConnectionHandler(BluetoothConnectionHandler handler) {
        super.setBluetoothConnectionHandler(handler);
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return this.mBluetoothAdapter;
    }

    public boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public void setBluetoothState(int bluetoothState){
        this.bluetoothState=bluetoothState;
    }
    public int getBluetoothState(){
        return this.bluetoothState;
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
            BluetoothSocket socket;
            while (bluetoothState != BLUETOOTH_STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {

                    break;
                }

                if (socket != null) {
                    synchronized (this) {
                        switch (bluetoothState) {
                            case BLUETOOTH_STATE_CONNECTING:

                                if (mInsecureBluetoothListeningThread != null) {
                                    mInsecureBluetoothListeningThread.cancel();
                                    mInsecureBluetoothListeningThread = null;
                                }

                                new BluetoothConnectionThread(socket).start();

                                break;
                            case BLUETOOTH_STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
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



    private class BluetoothConnectionThread extends Thread {

        public BluetoothConnectionThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
            bluetoothState=BLUETOOTH_STATE_CONNECTED;


        }

        public void run() {

            while (bluetoothState == BLUETOOTH_STATE_CONNECTED) {
                bluetoothConnectionHandler.onConnected(inputStream,outputStream);
            }
        }
    }

}
