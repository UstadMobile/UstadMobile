package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;

import com.ustadmobile.port.sharedse.networkmanager.BleMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * <h1>BleMessageGattClientCallbackTest</h1>
 *
 * Test class which tests {@link BleMessageGattClientCallback} to make sure all
 * {@link BluetoothGattCallback} callbacks it behaves as expected.
 *
 * @author kileha3
 */


@RunWith(RobolectricTestRunner.class)
public class BleMessageGattClientCallbackTest {

    private String destinationAddress ="78:00:9E:DE:CB:21";

    private BluetoothGatt bluetoothGatt;

    private BluetoothManager bluetoothManager;

    private BleMessage bleMessage;

    private BluetoothGattCharacteristic characteristic;


    @Before
    public void setUpMocks(){
        bluetoothGatt = mock(BluetoothGatt.class);
        bluetoothManager = mock(BluetoothManager.class);
        bleMessage = mock(BleMessage.class);
        characteristic = mock(BluetoothGattCharacteristic.class);
    }


    @Test
    public void givenOnConnectionStateChanged_whenConnectedWithFailureStatus_thenShouldDisconnect(){
        BleMessageGattClientCallback clientCallback =
                new BleMessageGattClientCallback(bluetoothManager,bleMessage,destinationAddress);
        BluetoothGattCallback gattCallback = clientCallback.getGattClientCallback();


        gattCallback.onConnectionStateChange(bluetoothGatt,
                BluetoothGatt.GATT_FAILURE, BluetoothProfile.STATE_CONNECTED);

        //Verify that client was disconnected from the gatt server
        verify(bluetoothGatt).disconnect();

    }

    @Test
    public void givenOnConnectionStateChanged_whenDisconnectedWithSuccessStatus_thenShouldDisconnect(){
        BleMessageGattClientCallback clientCallback =
                new BleMessageGattClientCallback(bluetoothManager,bleMessage,destinationAddress);
        BluetoothGattCallback gattCallback = clientCallback.getGattClientCallback();


        gattCallback.onConnectionStateChange(bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_DISCONNECTED);

        //Verify that client was disconnected from the gatt server
        verify(bluetoothGatt).disconnect();
    }

    @Test
    public void givenOnConnectionStateChanged_whenConnectedWithSuccessStatus_thenShouldDiscoverServices(){
        BleMessageGattClientCallback clientCallback =
                new BleMessageGattClientCallback(bluetoothManager,bleMessage,destinationAddress);
        BluetoothGattCallback gattCallback = clientCallback.getGattClientCallback();


        gattCallback.onConnectionStateChange(bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED);

        //Verify that service discovery was started
        verify(bluetoothGatt).discoverServices();
    }

    @Test
    public void givenServiceIsDiscovered_whenMatchingCharacteristicsFound_thenShouldRequestPermissionToWrite(){
        BleMessageGattClientCallback clientCallback =
                new BleMessageGattClientCallback(bluetoothManager,bleMessage,destinationAddress);
        BluetoothGattCallback gattCallback = clientCallback.getGattClientCallback();


        gattCallback.onServicesDiscovered(bluetoothGatt, BluetoothGatt.GATT_SUCCESS);

        //Verify that characteristics permission was requested
        verify(bluetoothGatt).writeCharacteristic(characteristic);
    }

    @Test
    public void givenOnCharacteristicWrite_whenGrantedPermissionToWrite_thenShouldStartSendingPackets(){
        BluetoothGattCharacteristic characteristic = mock(BluetoothGattCharacteristic.class);
        BleMessageGattClientCallback clientCallback =
                new BleMessageGattClientCallback(bluetoothManager,bleMessage,destinationAddress);
        BluetoothGattCallback gattCallback = clientCallback.getGattClientCallback();


        gattCallback.onCharacteristicWrite(bluetoothGatt,characteristic,BluetoothGatt.GATT_SUCCESS);

        //verify that characteristics value was modified
        verify(characteristic).setValue(new byte[]{});
        //Verify that characteristics was modified
        verify(bluetoothGatt).writeCharacteristic(characteristic);

    }
}
