package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.ParcelUuid;

import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.BleEntryStatusTask;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;

import java.util.List;
import java.util.UUID;

import static android.content.Context.BLUETOOTH_SERVICE;

public class NetworkManagerAndroidBle extends NetworkManagerBle{

    private WifiManager wifiManager;

    private BluetoothManager bluetoothManager;

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;


    private BleGattServerAndroid gattServerAndroid;

    private BluetoothLeScanner bleServiceScanner;

    private Context context;


    private BluetoothAdapter.LeScanCallback leScanCallback = (device, rssi, scanRecord) -> {

    };

    private ScanCallback bleScannCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };


    @Override
    public void init(Object mContext) {
        this.context = ((Context)mContext);
        wifiManager= (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        bluetoothManager = (BluetoothManager)context.getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bleServiceScanner = bluetoothAdapter.getBluetoothLeScanner();
        gattServerAndroid = new BleGattServerAndroid(((Context)mContext),this);
    }

    @Override
    public boolean isWiFiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    @Override
    public boolean isBleCapable() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    @Override
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    @Override
    public boolean canDeviceAdvertise() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && bluetoothAdapter.isMultipleAdvertisementSupported();
    }

    @Override
    public void startAdvertising() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

            BluetoothGattService service = new BluetoothGattService(USTADMOBILE_BLE_SERVICE_UUID,
                    BluetoothGattService.SERVICE_TYPE_PRIMARY);

            BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
                    USTADMOBILE_BLE_SERVICE_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);

            service.addCharacteristic(writeCharacteristic);

            gattServerAndroid.getGattServer().addService(service);

            if (mBluetoothLeAdvertiser == null) {
                return;
            }

            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setConnectable(true)
                    .setTimeout(0)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                    .build();

            ParcelUuid parcelUuid = new ParcelUuid(USTADMOBILE_BLE_SERVICE_UUID);
            AdvertiseData data = new AdvertiseData.Builder()
                    .addServiceUuid(parcelUuid)
                    .build();

            mBluetoothLeAdvertiser.startAdvertising(settings, data, new AdvertiseCallback() {
                @Override public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                }

                @Override public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                }
            });
        }
    }

    @Override
    public void stopAdvertising() {
        if(mBluetoothLeAdvertiser == null) return;
        if (gattServerAndroid.getGattServer() != null) {
            gattServerAndroid.getGattServer().clearServices();
            gattServerAndroid.getGattServer().close();
        }
    }

    @Override
    public void startScanning() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            bleServiceScanner.startScan(bleScannCallback);
        }else{
            if(!bluetoothAdapter.isDiscovering()){
                bluetoothAdapter.startLeScan(new UUID[] { USTADMOBILE_BLE_SERVICE_UUID},leScanCallback);
            }
        }

    }

    @Override
    public void stopScanning() {
        if(bluetoothAdapter == null) return;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            bleServiceScanner.stopScan(bleScannCallback);
        }else{
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    @Override
    public boolean setWifiEnabled(boolean enabled) {
        return wifiManager.setWifiEnabled(enabled);
    }

    @Override
    public void createWifiDirectGroup() {

    }

    @Override
    protected BleEntryStatusTask makeEntryStatusTask(Object context, List<Long> entryUidsToCheck,
                                                     NetworkNode peerToCheck) {
        return new BleEntryStatusTaskAndroid((Context)context,entryUidsToCheck,peerToCheck);
    }

    public BluetoothManager getBluetoothManager(){
        return bluetoothManager;
    }
}
