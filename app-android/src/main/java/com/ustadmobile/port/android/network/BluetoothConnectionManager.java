package com.ustadmobile.port.android.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import edu.rit.se.wifibuddy.WifiDirectHandler;


/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothConnectionManager {

    public static final String TAG = "BluetoothManager";

    /**
     * Name for the SDP record when creating server socket
     */

    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    /**
     * Unique UUID for this application
     */
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("c22707f1-0500-4a34-a292-532d47795ee7");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("ad9e3a05-7d80-4a12-b50b-91c72d442683");

    /**
     * Message types sent from the BluetoothConnectionManager Handler
     */
    public static final int MESSAGE_STATE_CHANGE = 1;

    public static final int MESSAGE_READ = 2;

    public static final int MESSAGE_WRITE = 3;

    public static final int MESSAGE_DEVICE_NAME = 4;

    public static final int MESSAGE_TOAST = 5;

    /**
     * Command to exchange between connected devices to check if files are available locally or not
     */

    public static final String  COMMAND_TAG_FILE_AVAILABLE_CHECK = "file_status_check";
    public static final String COMMAND_TAG_FILE_AVAILABLE_FEEDBACK = "file_status_feedback";
    public static final String COMMAND_TAG_FILE_ACQUIRE = "acquire";

    /**
     * Separator used during transition to separate between commands and file ids
     */
    public static final String FILE_IDS_SEPARATOR = "@";
    public static final String FILE_AVAILABLE_COMMAND_SEPARATOR = ":";


    public static final String FILE_AVAILABILITY_RESPONSE = "available";
    public static final String ACTION_FILE_CHECKING_COMPLETED = "action_file_checking_completed";
    public static final String ACTION_DEVICE_BLUETOOTH_CONNECTIVITY_CHANGE = "action_device_bluetooth_connectivity_change";
    public static final String EXTRA_FILE_CHECKING_TASK = "extra_file_checking";
    public static final String EXTRA_DEVICE_BLUETOOTH_CONNECTIVITY_FLAG = "extra_device_connectivity";
    public static final String BLUETOOTH_ADDRESS = "bluetooth_address";

    /**
     * Code to return if the file is available
     */
    public static final boolean STATUS_AVAILABLE=true;
    /**
     * Code to return if file is not available
     */
    public static final boolean STATUS_UNAVAILABLE=false;


    /**
     * Key names received from the BluetoothConnectionManager Handler
     */
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Member fields
    private final BluetoothAdapter mAdapter;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private NetworkManagerAndroid managerAndroid;
    private int mState;


    /**
     *
     * Constants that indicate the current connection state

     *
     * bluetooth is doing nothing
     */
    public static final int STATE_NONE = 0;

    /**
     * Socket is now listening for incoming connections
     */
    public static final int STATE_LISTEN = 1;

    /**
     * Device now initiating an outgoing connection
     */
    public static final int STATE_CONNECTING = 2;

    /**
     * Device now connected to a remote device
     */
    public static final int STATE_CONNECTED = 3;



    /**
     * Handle all outgoing and incoming bluetooth connection message exchange
     */

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String sentCommand = new String(writeBuf);
                    Log.d(BluetoothConnectionManager.TAG,"Sent Command: "+sentCommand);

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String receivedCommand[]  = new String(readBuf, 0, msg.arg1).split(FILE_AVAILABLE_COMMAND_SEPARATOR);
                    Log.d(BluetoothConnectionManager.TAG,"Received Command: command "+receivedCommand[0]+" data: "+receivedCommand[1]);

                    switch (receivedCommand[0]){

                        case COMMAND_TAG_FILE_AVAILABLE_CHECK:
                            List<String> request=new ArrayList<>();
                            Collections.addAll(request, TextUtils.split(receivedCommand[1],FILE_IDS_SEPARATOR));
                            List<String> responses= checkAvailability(request);
                            String feedbackCommand= COMMAND_TAG_FILE_AVAILABLE_FEEDBACK +
                                    FILE_AVAILABLE_COMMAND_SEPARATOR +idsToString(responses);
                            sendCommandMessage(feedbackCommand);
                            break;

                        case COMMAND_TAG_FILE_AVAILABLE_FEEDBACK:

                            if(managerAndroid.getCurrentBluetoothTask()!=null){

                                String serverBluetoothAddress=managerAndroid.getCurrentBluetoothTask()
                                        .getNode().getNodeBluetoothAddress();

                                if(!getBluetoothMacAddress().equals(serverBluetoothAddress)){
                                    try{

                                        List<String> requestResponse=stringsToIds(receivedCommand[1]);

                                        for (int position=0;position<requestResponse.size();position++){
                                            HashMap<String,String> response=new HashMap<>();
                                            response.put(BLUETOOTH_ADDRESS,serverBluetoothAddress);
                                            response.put(FILE_AVAILABILITY_RESPONSE,requestResponse.get(position).replaceAll("\\s+",""));

                                            if(managerAndroid.knownNodes.size()>1 && managerAndroid.getCurrentTaskIndex()
                                                    < managerAndroid.getBluetoothTaskQueue().size()){

                                                if(!managerAndroid.getAvailableFiles().containsValue(response)){

                                                    if(Boolean.parseBoolean(requestResponse.get(position))){
                                                        managerAndroid.getAvailableFiles().put(NetworkManagerAndroid.FILE_IDSTO_PROCESS.get(position),response);
                                                        NetworkManagerAndroid.FILE_IDSTO_PROCESS.remove(position);
                                                    }
                                                }else if(managerAndroid.getCurrentTaskIndex()
                                                        == (managerAndroid.getBluetoothTaskQueue().size()-1)){
                                                    managerAndroid.getAvailableFiles().put(NetworkManagerAndroid.FILE_IDSTO_PROCESS.get(position),response);

                                                }

                                            }else{
                                                managerAndroid.getAvailableFiles().put(NetworkManagerAndroid.FILE_IDSTO_PROCESS.get(position),response);

                                            }

                                        }


                                    }catch (ArrayIndexOutOfBoundsException e){
                                        e.printStackTrace();
                                    }

                                    if(managerAndroid.getCurrentTaskIndex()==(managerAndroid.getBluetoothTaskQueue().size()-1)){
                                        managerAndroid.setCurrentTaskIndex(0);
                                        Intent taskCompleted=new Intent(ACTION_FILE_CHECKING_COMPLETED);
                                        taskCompleted.putExtra(EXTRA_FILE_CHECKING_TASK,serverBluetoothAddress);
                                        LocalBroadcastManager.getInstance(managerAndroid.getP2pService()).sendBroadcast(taskCompleted);
                                    }else{
                                        start();
                                    }
                                    int index=managerAndroid.getCurrentTaskIndex()+1;
                                    managerAndroid.setCurrentTaskIndex(index);
                                    managerAndroid.getCurrentBluetoothTask().fireTaskEnded();

                                }

                            }
                            break;

                        case COMMAND_TAG_FILE_ACQUIRE:
                            //command to
                            String deviceMacAddress=receivedCommand[1];
                            managerAndroid.getP2pService().getWifiDirectHandlerAPI().connectToNormalWifiDirect(deviceMacAddress);
                            break;

                    }

                    break;
                case MESSAGE_STATE_CHANGE:

                    switch (msg.arg1) {

                        case STATE_CONNECTED:
                            sendBroadcasts(STATE_CONNECTED);
                            Log.d(BluetoothConnectionManager.TAG,"Bluetooth State: Connected");
                            break;
                        case STATE_CONNECTING:
                            sendBroadcasts(STATE_CONNECTING);
                            Log.d(BluetoothConnectionManager.TAG,"Bluetooth State: Connecting");
                            break;
                        case STATE_LISTEN:
                            sendBroadcasts(STATE_LISTEN);
                            Log.d(BluetoothConnectionManager.TAG,"Bluetooth State: Listening");
                            break;

                        case STATE_NONE:
                            sendBroadcasts(STATE_NONE);
                            Log.d(BluetoothConnectionManager.TAG,"Bluetooth State: Not Connected");
                            break;
                    }

                    break;

            }
        }
    };


    private void sendBroadcasts(int state){
        Intent connectivity=new Intent(ACTION_DEVICE_BLUETOOTH_CONNECTIVITY_CHANGE);
        connectivity.putExtra(EXTRA_DEVICE_BLUETOOTH_CONNECTIVITY_FLAG,state);
        LocalBroadcastManager.getInstance(managerAndroid.getP2pService()).sendBroadcast(connectivity);
    }

    private Context mContext;
    public BluetoothConnectionManager(Context context,NetworkManagerAndroid managerAndroid) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        this.mContext=context;
        this.managerAndroid=managerAndroid;
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        /*if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }*/

        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }

        updateConnectionState();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connectToBluetoothDevice(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connecting to: " + device.getName());

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();

        updateConnectionState();

    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.d(TAG, "connected to "+device.getName()+" "+device.getAddress());

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        updateConnectionState();
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        mState = STATE_NONE;
        updateConnectionState();
    }

    /**
     * Write to the ConnectedThread in an un-synchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        ConnectedThread thread;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            thread = mConnectedThread;
        }
        thread.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        updateConnectionState();
        BluetoothConnectionManager.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        mState = STATE_NONE;
        updateConnectionState();
        BluetoothConnectionManager.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket;

            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {

                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothConnectionManager.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
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
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType="Insecure";

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            mAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException e) {

                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            synchronized (BluetoothConnectionManager.this) {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            while (mState == STATE_CONNECTED) {
                try {
                    bytes = mmInStream.read(buffer);
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }


    /**
     * Update current state of the bluetooth connection
     */
    private synchronized void updateConnectionState() {
        mState = getState();
        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, mState, -1).sendToTarget();
    }


    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    public void sendCommandMessage(String message) {
        if (getState() != BluetoothConnectionManager.STATE_CONNECTED) {

            return;
        }
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            write(send);
        }
    }


    public BluetoothAdapter getBluetoothAdapter(){
        return mAdapter;
    }

    /**
     * Check files if are available locally
     * @param fileIds- list of file ids to be checked if they are available locally
     * @return
     */

    public List<String> checkAvailability(List<String> fileIds){
        List<String> responses=new ArrayList<>();
        CatalogEntryInfo info;
        for(String fileId: fileIds){
            info=CatalogController.getEntryInfo(fileId, CatalogController.SHARED_RESOURCE,mContext);
            String isAvailable=info==null ? String.valueOf(STATUS_UNAVAILABLE):String.valueOf(STATUS_AVAILABLE);
            responses.add(isAvailable);

        }

        return responses;
    }




    String getBluetoothMacAddress() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(WifiDirectHandler.TAG, "device does not support bluetooth");
            return null;
        }
        String address = mBluetoothAdapter.getAddress();
        if (address.equals("02:00:00:00:00:00")) {
            try {
                ContentResolver mContentResolver = managerAndroid.getP2pService().getApplicationContext().getContentResolver();
                address = Settings.Secure.getString(mContentResolver, BLUETOOTH_ADDRESS);
                Log.d(WifiDirectHandler.TAG,"MAC Address - Resolved: " + address);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.d(WifiDirectHandler.TAG,"MAC Address-No resolution: " + address);
        }
        return address;
    }

    /**
     * Convert array of IDS to be processed to normal string form bluetooth exchange
     * @param fileIds - File ids as string
     * @return
     */
    public String idsToString(List<String> fileIds){
        return TextUtils.join(FILE_IDS_SEPARATOR, fileIds);
    }

    /**
     * Convert string of file id responses as received from peer
     * device and change them to array of ids
     * @param fileIds
     * @return
     */
    private List<String> stringsToIds(String fileIds){
        List<String> fileIdsList=new ArrayList<>();
        Collections.addAll(fileIdsList, TextUtils.split(fileIds,FILE_IDS_SEPARATOR));
        return fileIdsList;
    }
}
