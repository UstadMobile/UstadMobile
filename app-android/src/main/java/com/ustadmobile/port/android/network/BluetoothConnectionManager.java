package com.ustadmobile.port.android.network;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
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

import edu.rit.se.wifibuddy.DnsSdTxtRecord;
import edu.rit.se.wifibuddy.WifiDirectHandler;

import static com.ustadmobile.port.android.network.DownloadManagerAndroid.ACTION_DOWNLOAD_STARTING;
import static com.ustadmobile.port.android.network.DownloadManagerAndroid.EXTRA_DOWNLOAD_SOURCE_ADDRESS;
import static com.ustadmobile.port.android.network.NetworkManagerAndroid.NO_PROMPT_NETWORK_NAME;
import static com.ustadmobile.port.android.network.NetworkManagerAndroid.NO_PROMPT_NETWORK_PASS;
import static edu.rit.se.wifibuddy.WifiDirectHandler.NOPROMPT_STATUS_ACTIVE;


/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothConnectionManager {

    public static final String TAG = "NetworkManager";

    /**
     * Name for the SDP record when creating server socket
     */
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    /**
     * Unique UUID for this application for both secure and insecure communication
     */
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("c22707f1-0500-4a34-a292-532d47795ee7");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("ad9e3a05-7d80-4a12-b50b-91c72d442683");

    /**
     * Message types sent from the BluetoothConnectionManager Handler
     */
    //Sent when bluetooth state has changed
    public static final int MESSAGE_STATE_CHANGE = 1;

    //sent when the device has received a message from the other side
    public static final int MESSAGE_READ = 2;

    //sent when device sent a message to another device
    public static final int MESSAGE_WRITE = 3;

    //Sent to when we have the connected device name
    public static final int MESSAGE_DEVICE_NAME = 4;

    //Sent to notify the UI using toast
    public static final int MESSAGE_TOAST = 5;

    /**
     * Command sent from the client device to check
     * if files can be downloaded locally
     */
    public static final String COMMAND_FILE_AVAILABLE_CHECK_TAG = "file_check";

    /**
     * Command send by server device acknowledging requested file availability
     */
    public static final String COMMAND_FILE_AVAILABLE_FEEDBACK_TAG = "file_check_feedback";

    /**
     * Command sent by client device to request file acquisition to the server device:-
     * This will trigger the creation of No-Prompt network if is not created
     */
    public static final String COMMAND_TAG_FILE_ACQUIRE_REQUEST = "file_acquisition_request";

    /**
     * Command sent by server device to start connection upon acquisition
     * request from the client device:-
     */
    public static final String COMMAND_TAG_FILE_ACQUIRE_FEEDBACK = "file_acquisition_feedback";

    /**
     * Separators used to separate file IDS which was sent as a message string
     */
    public static final String FILE_IDS_SEPARATOR = "@";

    /**
     * Separator used to separate commands in a message, if we have multiple commands
     */
    public static final String FILE_AVAILABLE_COMMAND_SEPARATOR = ":";


    public static final String FILE_AVAILABILITY_RESPONSE = "available";
    /**
     * Broadcast sent after the file checking process is completed
     */
    public static final String ACTION_FILE_CHECKING_COMPLETED = "action_file_checking_completed";

    /**
     * Broadcast sent when bluetooth connection state changes
     */
    public static final String ACTION_DEVICE_BLUETOOTH_CONNECTIVITY_CHANGE = "action_device_bluetooth_connectivity_change";

    /**
     * Extra data tag passed on broadcast intent which is a Bluetooth Address
     */
    public static final String EXTRA_FILE_CHECKING_TASK_ADDRESS = "extra_file_checking";
    /**
     * This indicate whether is an acquisition or file checking task
     */
    public static final String EXTRA_BLUETOOTH_TASK_TYPE = "extra_bluetooth_task_type";

    /**
     * Extra data tag passed on broadcast intent which is the actual connection state
     */
    public static final String EXTRA_DEVICE_BLUETOOTH_CONNECTIVITY_FLAG = "extra_device_connectivity";

    /**
     * Bluetooth Address tag
     */
    public static final String BLUETOOTH_ADDRESS = "bluetooth_address";

    /**
     * Flag to return if the checked file is available and can be downloaded locally
     */
    public static final boolean STATUS_AVAILABLE=true;
    /**
     * Flag to return if file is not available
     */
    public static final boolean STATUS_UNAVAILABLE=false;

    /**
     * Default bluetooth address which will be return in
     * android 6.0 and higher version.
     */
    public static final String DEFAULT_BLUETOOTH_ADDRESS="02:00:00:00:00:00";

    /**
     * Used when the task is only checking the file if is available locally
     */
    public static final String BLUETOOTH_TASK_TYPE_STATUS_CHECK="checking";

    /**
     * Used when the task is acquiring the file from server device
     */
    public static final String BLUETOOTH_TASK_TYPE_ACQUIRE ="acquiring";

    /**
     * Key names received from the BluetoothConnectionManager Handler
     */
    public static final String DEVICE_NAME = "device_name";

    public static final String TOAST = "toast";


    private final BluetoothAdapter mAdapter;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private NetworkManagerAndroid managerAndroid;
    private DnsSdTxtRecord txtRecord=null;
    private int mState;
    private String bluetoothTaskType;
    private String deviceMacAddress=null;

    public static String NETWORK_NAME_TO_CONNECT_TO=null;


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
     * Index to get the task type from the message
     */
    public static final int MESSAGE_TASK_TYPE_INDEX=0;

    /**
     * Index to get the command from the message
     */
    public static final int MESSAGE_ACTUAL_COMMAND_INDEX=1;
    /**
     * Index to get actual data from the message
     */
    public static final int MESSAGE_DATA_INDEX=2;

    /**
     * Index to get extra data from the message (Network Passphrase)
     */
    public static final int MESSAGE_EXTRA_DATA_INDEX=3;



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
                    //read all message sent from another device
                    byte[] readBuf = (byte[]) msg.obj;
                    String receivedCommand[]  = new String(readBuf, 0, msg.arg1).split(FILE_AVAILABLE_COMMAND_SEPARATOR);
                    Log.d(BluetoothConnectionManager.TAG,"Received Command: "+new String(readBuf, 0, msg.arg1));

                    //If the current task is checking file availability (Actual checking and its feedback)
                    if(BLUETOOTH_TASK_TYPE_STATUS_CHECK.equals(bluetoothTaskType) ||
                            BLUETOOTH_TASK_TYPE_STATUS_CHECK.equals(receivedCommand[MESSAGE_TASK_TYPE_INDEX])){

                        switch (receivedCommand[MESSAGE_ACTUAL_COMMAND_INDEX]){

                            //Server side processing the files - checking their availability status
                            case COMMAND_FILE_AVAILABLE_CHECK_TAG:
                                List<String> request=new ArrayList<>();
                                Collections.addAll(request, TextUtils.split(receivedCommand[MESSAGE_DATA_INDEX],FILE_IDS_SEPARATOR));
                                List<String> fileAvailabilityCheckResult= checkAvailability(request);
                                String feedbackCommand= BLUETOOTH_TASK_TYPE_STATUS_CHECK +
                                        FILE_AVAILABLE_COMMAND_SEPARATOR + COMMAND_FILE_AVAILABLE_FEEDBACK_TAG +
                                        FILE_AVAILABLE_COMMAND_SEPARATOR +idsToString(fileAvailabilityCheckResult);
                                sendCommandMessage(feedbackCommand);

                                break;

                            //Client side processing of the feedback received from the server device
                            case COMMAND_FILE_AVAILABLE_FEEDBACK_TAG:

                                if(managerAndroid.getCurrentBluetoothTask()!=null){

                                    String serverBluetoothAddress=managerAndroid.getCurrentBluetoothTask()
                                            .getNode().getNodeBluetoothAddress();
                                    //Make sure this is being processed on the client side and not on the server side
                                    if(!getBluetoothMacAddress().equals(serverBluetoothAddress)){
                                        try{

                                            List<String> requestResponse=stringsToIds(receivedCommand[MESSAGE_DATA_INDEX]);

                                            for (int position=0;position<requestResponse.size();position++){
                                                HashMap<String,String> response=new HashMap<>();
                                                response.put(BLUETOOTH_ADDRESS,serverBluetoothAddress);
                                                response.put(FILE_AVAILABILITY_RESPONSE,requestResponse.get(position).replaceAll("\\s+",""));

                                                if(managerAndroid.knownNodes.size()>1 && managerAndroid.getCurrentTaskIndex()
                                                        < managerAndroid.getBluetoothTaskQueue().size()){

                                                    if(!managerAndroid.getAvailableFiles().containsValue(response)){

                                                        if(Boolean.parseBoolean(requestResponse.get(position))){
                                                            managerAndroid.getAvailableFiles().put(NetworkManagerAndroid.FILE_IDS_TO_PROCESS.get(position),
                                                                    response);
                                                            NetworkManagerAndroid.FILE_IDS_TO_PROCESS.remove(position);
                                                        }
                                                    }else if(managerAndroid.getCurrentTaskIndex()
                                                            == (managerAndroid.getBluetoothTaskQueue().size()-1)){

                                                        managerAndroid.getAvailableFiles().put(NetworkManagerAndroid.FILE_IDS_TO_PROCESS.get(position),
                                                                response);

                                                    }

                                                }else{
                                                    managerAndroid.getAvailableFiles().put(NetworkManagerAndroid.FILE_IDS_TO_PROCESS.get(position),
                                                            response);

                                                }

                                            }


                                        }catch (ArrayIndexOutOfBoundsException e){
                                            e.printStackTrace();
                                        }

                                        //If the que is processed, update the UI
                                        if(managerAndroid.getCurrentTaskIndex()==(managerAndroid.getBluetoothTaskQueue().size()-1)){
                                            managerAndroid.setCurrentTaskIndex(0);
                                            stop();
                                            Intent taskCompleted=new Intent(ACTION_FILE_CHECKING_COMPLETED);
                                            taskCompleted.putExtra(EXTRA_FILE_CHECKING_TASK_ADDRESS,serverBluetoothAddress);
                                            LocalBroadcastManager.getInstance(managerAndroid.getP2pService()).sendBroadcast(taskCompleted);
                                            setBluetoothTaskType(null);

                                        }else{
                                            start();
                                        }
                                        int index=managerAndroid.getCurrentTaskIndex()+1;
                                        managerAndroid.setCurrentTaskIndex(index);
                                        managerAndroid.getCurrentBluetoothTask().fireTaskEnded();

                                    }

                                }
                                break;

                        }
                    }else if(BLUETOOTH_TASK_TYPE_ACQUIRE.equals(receivedCommand[MESSAGE_TASK_TYPE_INDEX])){

                        WifiDirectHandler wifiDirectHandler=managerAndroid.getP2pService().getWifiDirectHandlerAPI();
                        switch (receivedCommand[MESSAGE_ACTUAL_COMMAND_INDEX]){

                            case COMMAND_TAG_FILE_ACQUIRE_REQUEST:

                                /**
                                 * Using normal WiFi-direct technique
                                 */
                                /* deviceMacAddress=receivedCommand[MESSAGE_DATA_INDEX].replace(FILE_IDS_SEPARATOR,
                                        FILE_AVAILABLE_COMMAND_SEPARATOR).replaceAll("\\s+","");

                                Log.d(TAG,"Wifi Connection to "+deviceMacAddress);
                                if(wifiDirectHandler.getThisDevice().status!= WifiP2pDevice.CONNECTED){
                                    wifiDirectHandler.connectToNormalWifiDirect(deviceMacAddress);
                                }else{
                                    String ipAddress=getClientIpAddress();
                                    Log.d(BluetoothConnectionManager.TAG,"Device IP address "+ipAddress);

                                    String checkCommand= BLUETOOTH_TASK_TYPE_ACQUIRE +
                                            FILE_AVAILABLE_COMMAND_SEPARATOR+COMMAND_TAG_FILE_ACQUIRE_FEEDBACK +
                                            FILE_AVAILABLE_COMMAND_SEPARATOR +ipAddress;
                                    sendCommandMessage(checkCommand);
                                }*/

                                /**
                                 * using WiFi-direct group technique
                                 */

                                //If the group is already created, send the group info otherwise create it
                                if(wifiDirectHandler.getNoPromptServiceStatus()==NOPROMPT_STATUS_ACTIVE){
                                    String ssid=wifiDirectHandler.getNoPromptServiceData().getRecord().get(NO_PROMPT_NETWORK_NAME);
                                    String passPhrase=wifiDirectHandler.getNoPromptServiceData().getRecord().get(NO_PROMPT_NETWORK_PASS);
                                    String sendGroupInfo= BLUETOOTH_TASK_TYPE_ACQUIRE +
                                            FILE_AVAILABLE_COMMAND_SEPARATOR+COMMAND_TAG_FILE_ACQUIRE_FEEDBACK +
                                            FILE_AVAILABLE_COMMAND_SEPARATOR +ssid+FILE_AVAILABLE_COMMAND_SEPARATOR+passPhrase;
                                    sendCommandMessage(sendGroupInfo);
                                    start();

                                }else{
                                    wifiDirectHandler.setAddLocalServiceAfterGroupCreation(false);
                                    wifiDirectHandler.startAddingNoPromptService(NetworkManagerAndroid.serviceData());
                                }


                                break;
                            case COMMAND_TAG_FILE_ACQUIRE_FEEDBACK:

                                /**
                                 * Using normal WiFi-Direct technique
                                 */
                                /*String ipAddress=receivedCommand[MESSAGE_DATA_INDEX];
                                Intent intent=new Intent(ACTION_DOWNLOAD_STARTING);
                                intent.putExtra(EXTRA_DOWNLOAD_SOURCE_ADDRESS,ipAddress);
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);*/


                                /**
                                 * Using WiFi-Direct group technique
                                 */

                                //if the this devices is connected to the group, start downloading the file otherwise connect
                                WifiInfo wifiInfo=managerAndroid.getP2pService().getWifiDirectHandlerAPI().getCurrentConnectedWifiInfo();
                                if(wifiInfo.getSSID().replace("\"","").equalsIgnoreCase(receivedCommand[MESSAGE_DATA_INDEX].replace("\"",""))){
                                    Intent intent=new Intent(ACTION_DOWNLOAD_STARTING);
                                    intent.putExtra(EXTRA_DOWNLOAD_SOURCE_ADDRESS,"");
                                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                                }else{
                                    NETWORK_NAME_TO_CONNECT_TO=receivedCommand[MESSAGE_DATA_INDEX];
                                    HashMap<String,String> record=new HashMap<>();
                                    record.put(NO_PROMPT_NETWORK_NAME,receivedCommand[MESSAGE_DATA_INDEX]);
                                    record.put(NO_PROMPT_NETWORK_PASS,receivedCommand[MESSAGE_EXTRA_DATA_INDEX]);
                                    txtRecord=new DnsSdTxtRecord(null,record,null);
                                    wifiDirectHandler.connectToNoPromptService(txtRecord);
                                }

                                break;


                        }



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
     * Start message exchange between client and server device. Specifically start AcceptThread to begin a
     * session in listening (server) mode.
     */
    public synchronized void start() {
        Log.d(TAG, "start");
        //If there was a connection or any part listening cancel them
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }

        updateConnectionState();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
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

        //Notify the UI on newly device connection by sending its name
        Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        updateConnectionState();
    }

    /**
     * Stop all threads currently running
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
     * Indicate that the connection attempt failed and notify the UI/listening part.
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
     * Indicate that the connection was lost and notify the UI/Listening part
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

            //for future use just let secure connection be, we might need it sometimes
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
            Log.d(TAG, "Socket Type: " + mSocketType + "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket;

            //So long as the device is not connected to another device,
            // accept incoming connection
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
                //for future use just let secure connection be, we might need it sometimes
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
                    connectionLost();
                }
            }
        }

        /**
         * Write to the connected OutStream.
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
     * Sends a message to another device.
     * @param message A string of text to send - which will be a combination
     *                of commands and actual data to process.
     */
    public void sendCommandMessage(String message) {
        Log.d(TAG,"Message to send "+message);
        //make sure the bluetooth is connected before sending a message
        if (getState() != BluetoothConnectionManager.STATE_CONNECTED) {
            return;
        }
        //make sure the message is not blank message
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            write(send);
        }
    }

    /**
     * Method to return an instance of bluetoothAdapter
     * @return
     */
    public BluetoothAdapter getBluetoothAdapter(){
        return mAdapter;
    }

    /**
     * Method to check files if are available locally
     * (It is not in NetworkManagerAndroid since it is much likely to be bluetooth work)
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

    /**
     * Get device bluetooth Address
     * @return
     */

    String getBluetoothMacAddress() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(WifiDirectHandler.TAG, "device does not support bluetooth");
            return null;
        }
        String address = mBluetoothAdapter.getAddress();
        //if is android 6+, resolve the bluetooth address since it will return a default address
        if (address.equals(DEFAULT_BLUETOOTH_ADDRESS)) {
            try {
                ContentResolver mContentResolver = managerAndroid.getP2pService().
                        getApplicationContext().getContentResolver();
                address = Settings.Secure.getString(mContentResolver, BLUETOOTH_ADDRESS);
                Log.d(WifiDirectHandler.TAG,"Bluetooth Address - Resolved: " + address);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.d(WifiDirectHandler.TAG,"Bluetooth Address-No resolution: " + address);
        }
        return address;
    }


    public DnsSdTxtRecord getTxtRecord(){
        return this.txtRecord;
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
     * Convert string of file ID's responses as received from peer
     * device and change them to array
     * @param fileIds
     * @return
     */
    private List<String> stringsToIds(String fileIds){
        List<String> fileIdsList=new ArrayList<>();
        Collections.addAll(fileIdsList, TextUtils.split(fileIds,FILE_IDS_SEPARATOR));
        return fileIdsList;
    }


    /**
     * Broadcast bluetooth current device status to the listening parts
     * @param state - actual state of the bluetooth
     *              connection as described above (Connection states)
     */
    private void sendBroadcasts(int state){
        Intent connectivity=new Intent(ACTION_DEVICE_BLUETOOTH_CONNECTIVITY_CHANGE);
        connectivity.putExtra(EXTRA_DEVICE_BLUETOOTH_CONNECTIVITY_FLAG,state);
        if(state==STATE_CONNECTED){
            connectivity.putExtra(EXTRA_BLUETOOTH_TASK_TYPE,bluetoothTaskType);
        }
        LocalBroadcastManager.getInstance(managerAndroid.getP2pService()).sendBroadcast(connectivity);
    }

    /**
     * Get current connected device bluetooth address
     * @return
     */
    public String getConnectedDeviceAddress(){
        return this.deviceMacAddress;
    }

    /**
     * Get current bluetooth task type
     * @param bluetoothTaskType
     * BLUETOOTH_TASK_TYPE_STATUS_CHECK : checking file availability status
     * BLUETOOTH_TASK_TYPE_ACQUIRE: acquiring a file from the device
     */
    public void setBluetoothTaskType(String bluetoothTaskType){
        this.bluetoothTaskType=bluetoothTaskType;
    }

    /**
     * Using normal WiFi-direct technique,
     * Obtain local client device IP address since the server IP address is known
     *NOTE:
     * When connected on the same network (any network) the device will tend to get IP
     * address based on currently connected network otherwise it will use WiFi-direct client IP address
     */
/*
    public String getClientIpAddress(){

        return getIPFromBytes(getLocalIPAddress());
    }


    private byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> enumInterface = NetworkInterface.getNetworkInterfaces();
                 enumInterface.hasMoreElements();) {

                NetworkInterface intf = enumInterface.nextElement();
                for (Enumeration<InetAddress> enumIPAddress = intf.getInetAddresses(); enumIPAddress.hasMoreElements();) {
                    InetAddress inetAddress = enumIPAddress.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) {
                            return inetAddress.getAddress();
                        }
                    }
                }
            }
        } catch (SocketException | NullPointerException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    *//**
     * Convert to bytes to real IP address
     * @param ipAddr - IP address obtained from getLocalIPAddress method as bytes
     * @return
     *//*
    private String getIPFromBytes(byte[] ipAddr) {
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }*/

}
