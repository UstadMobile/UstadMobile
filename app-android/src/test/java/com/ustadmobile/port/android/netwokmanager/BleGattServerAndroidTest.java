package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * <h1>BleGattServerAndroidTest</h1>
 *
 * Test class which tests {@link BleGattServerAndroid} to make sure it behaves as expected on all
 * {@link BluetoothGattServerCallback} callbacks
 *
 * @author kileha3
 */


@RunWith(RobolectricTestRunner.class)
public class BleGattServerAndroidTest {

    private BluetoothManager bluetoothManager;

    private BluetoothGattCharacteristic characteristic;

    private BluetoothGattServer gattServer;

    private BluetoothDevice bluetoothDevice;

    private Context context;

    @Before
    public void setUpMocks(){
        bluetoothManager = mock(BluetoothManager.class);
        characteristic = mock(BluetoothGattCharacteristic.class);
        bluetoothDevice = mock(BluetoothDevice.class);
        gattServer = mock(BluetoothGattServer.class);
        context = mock(Context.class);
    }

    @Test
    public void givenOnCharacteristicReadRequest_whenIsCharacteristicsWithSameUUID_thenShouldGrantPermission(){
        BleGattServerAndroid bleGattServer = new BleGattServerAndroid(context,bluetoothManager);
        BluetoothGattServerCallback serverCallback = bleGattServer.getGattServerCallback();


        serverCallback.onCharacteristicReadRequest(bluetoothDevice,0, 0,characteristic);

        //Verify that permission to write on the characteristics was granted
        verify(gattServer).sendResponse(bluetoothDevice,0,BluetoothGatt.GATT_SUCCESS,0,null);
    }


    @Test
    public void givenOnCharacteristicWriteRequest_whenIsCharacteristicsWithSameUUID_thenShouldGrantPermission(){
        Context context = mock(Context.class);
        BleGattServerAndroid bleGattServer = new BleGattServerAndroid(context,bluetoothManager);
        BluetoothGattServerCallback serverCallback = bleGattServer.getGattServerCallback();


        serverCallback.onCharacteristicWriteRequest(bluetoothDevice,0,
                characteristic,true,true,0,null);

        //Verify that permission to write on the characteristics was granted
        verify(gattServer).sendResponse(bluetoothDevice,0,BluetoothGatt.GATT_SUCCESS,0,null);
    }

    @Test
    public void givenOnCharacteristicWriteRequest_whenIsCharacteristicsWithSameUUID_thenShouldStartReceivingPackets(){

        //TODO: Make simulation possible for handling this test case

    }
}
