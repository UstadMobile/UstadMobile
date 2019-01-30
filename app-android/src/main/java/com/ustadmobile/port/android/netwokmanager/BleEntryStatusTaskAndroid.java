package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.BleEntryStatusTask;
import com.ustadmobile.port.sharedse.networkmanager.BleMessage;
import com.ustadmobile.port.sharedse.networkmanager.BleMessageResponseListener;
import com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;

import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;

/**
 * This class handles all android specific entry status check from a peer device also,
 * it is responsible for creating BLE GATT client callback.
 *
 * <b>Note: Operation Flow</b>
 * <p>
 * - Once {@link BleEntryStatusTaskAndroid#run()} is called, it creates
 * {@link BleMessageGattClientCallback} and pass the list of entries to be checked
 * and peer device to be checked from. After entry status check
 * {@link BleEntryStatusTask#onResponseReceived} will be called to report back the results.
 *
 * <p>
 * Use {@link BleEntryStatusTaskAndroid#run()} to start executing the task itself,
 * this method will be called in {@link NetworkManagerBle#startMonitoringAvailability}
 * when pending task to be executed is found.
 *
 *
 * @see BleMessageGattClientCallback
 * @see BleEntryStatusTask
 * @see NetworkManagerBle
 *
 *  @author kileha3
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class    BleEntryStatusTaskAndroid extends BleEntryStatusTask {

    private BleMessageGattClientCallback mCallback;

    private BluetoothManager bluetoothManager;

    private NetworkNode peerToCheck;

    private Context context;

    /**
     * Constructor to be used when creating platform specific instance of BleEntryStatusTask
     * @param context Platform specific application context.
     * @param entryUidsToCheck List of Id's to be checked for availability from a peer device.
     * @param peerToCheck Peer device for those entries to be checked from.
     */
    public BleEntryStatusTaskAndroid(Context context, List<Long> entryUidsToCheck,
                              NetworkNode peerToCheck) {
        super(context,entryUidsToCheck,peerToCheck);
        this.context = context;
        this.peerToCheck = peerToCheck;
        byte [] messagePayload = BleMessageUtil.bleMessageLongToBytes(entryUidsToCheck);
        this.message = new BleMessage(ENTRY_STATUS_REQUEST,messagePayload);
    }

    /**
     * Constructor to be used when creating platform specific instance of BleEntryStatusTask
     * responsible for sending custom messages apart from entry status check.
     * @param context Platform specific application context.
     * @param message Message to be sent
     * @param peerToSendMessageTo peer to send message to
     * @param responseListener Message response listener object
     */
    public BleEntryStatusTaskAndroid(Context context , BleMessage message,
                                     NetworkNode peerToSendMessageTo,
                                     BleMessageResponseListener responseListener){
        super(context,message,peerToSendMessageTo, responseListener);
    }


    /**
     * Set bluetooth manager for BLE GATT communication
     * @param bluetoothManager BluetoothManager instance
     */
    void setBluetoothManager(BluetoothManager bluetoothManager){
        this.bluetoothManager = bluetoothManager;
    }

    /**
     * Start entry status check task
     */
    @Override
    public void run() {
       try{
           mCallback = new BleMessageGattClientCallback(message);
           mCallback.setOnResponseReceived(this);
           BluetoothDevice destinationPeer = bluetoothManager.getAdapter()
                   .getRemoteDevice(peerToCheck.getBluetoothMacAddress());
            BluetoothGatt gatt= destinationPeer.connectGatt(context,false,mCallback);
            if(gatt == null){
                UstadMobileSystemImpl.l(UMLog.ERROR,695,
                        "Failed to connect to "+destinationPeer.getAddress());
            }else{
                UstadMobileSystemImpl.l(UMLog.DEBUG,695,
                        "Connecting to "+destinationPeer.getAddress());
            }
       }catch (IllegalArgumentException e){
           UstadMobileSystemImpl.l(UMLog.ERROR,695,
                   "Wrong address format provided",e);
       }
    }

    /**
     * Get BleMessageGattClientCallback instance
     * @return Instance of a BleMessageGattClientCallback
     */
    BleMessageGattClientCallback getGattClientCallback() {
        return mCallback;
    }
}
