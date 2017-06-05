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
 * <h1>BluetoothServerAndroid</h1>
 *
 * This is a wrapper class which does all the work for setting up and managing Bluetooth
 * connections with other devices in android platform. It has a thread that listens for
 * incoming connections.
 *
 * @see com.ustadmobile.port.sharedse.networkmanager.BluetoothServer
 *
 *
 * @author kileha3
 */

public class BluetoothServerAndroid extends BluetoothServer implements Runnable{

    public static final String TAG = "BluetoothServer";
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothServerSocket mServerSocket;

    private Thread serverAcceptThread;

    private boolean running=false;

    BluetoothServerAndroid(NetworkManager networkManager) {
        super(networkManager);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void start() {
        if(serverAcceptThread == null) {
            running = true;
            serverAcceptThread = new Thread(this);
            serverAcceptThread.start();
        }
    }

    /**
     * Method which listen for the incoming connection
     * @exception IOException
     */
    public void run() {

        try {
            mServerSocket=mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                    BluetoothServer.SERVICE_NAME, BluetoothServer.SERVICE_UUID);
            while(running) {
                final BluetoothSocket clientSocket = mServerSocket.accept();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OutputStream out = null;
                        InputStream in = null;
                        try{
                            out = clientSocket.getOutputStream();
                            in = clientSocket.getInputStream();
                            handleNodeConnected(clientSocket.getRemoteDevice().getAddress(), in, out);
                            Log.d(TAG,"Connected to "+clientSocket.getRemoteDevice().getName());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally {
                            UMIOUtils.closeInputStream(in);
                            UMIOUtils.closeOutputStream(out);
                            if(clientSocket != null){
                                try { clientSocket.close(); }
                                catch(IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @exception IOException
     */
    @Override
    public void stop() {
        if(serverAcceptThread != null) {
            running = false;
            serverAcceptThread = null;
            if(mServerSocket != null) {
                try{
                    mServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mServerSocket = null;
            }
        }
    }

}
