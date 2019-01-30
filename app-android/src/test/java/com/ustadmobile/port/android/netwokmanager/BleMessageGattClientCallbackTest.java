package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import com.ustadmobile.port.sharedse.networkmanager.BleMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageLongToBytes;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.DEFAULT_MTU_SIZE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.MAXIMUM_MTU_SIZE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.USTADMOBILE_BLE_SERVICE_UUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test class which tests {@link BleMessageGattClientCallback} to make sure all
 * {@link BluetoothGattCallback} callbacks it behaves as expected.
 *
 * @author kileha3
 */


@RunWith(RobolectricTestRunner.class)
public class BleMessageGattClientCallbackTest {

    private BluetoothGatt mockedGattClient;

    private BleMessage messageToSend;

    private BluetoothGattCharacteristic mockedCharacteristic;

    private BleMessageGattClientCallback gattClientCallback;


    @Before
    public void setUp(){
        mockedGattClient = mock(BluetoothGatt.class);
        List<Long> entryList = Arrays.asList(1056289670L,4590875612L,9076137860L,2912543894L);
        messageToSend = new BleMessage(ENTRY_STATUS_REQUEST, bleMessageLongToBytes(entryList));

        gattClientCallback = new BleMessageGattClientCallback(messageToSend);

        BluetoothGattService service = new BluetoothGattService(USTADMOBILE_BLE_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        mockedCharacteristic = mock(BluetoothGattCharacteristic.class);
        service.addCharacteristic(mockedCharacteristic);

        when(mockedGattClient.getServices()).thenReturn(Collections.singletonList(service));
        when(mockedGattClient.requestMtu(MAXIMUM_MTU_SIZE)).thenReturn(true);
        when(mockedCharacteristic.getUuid()).thenReturn(USTADMOBILE_BLE_SERVICE_UUID);

    }


    @Test
    public void givenOnConnectionStateChanged_whenConnectedWithFailureStatus_thenShouldDisconnect(){
        gattClientCallback.onConnectionStateChange(mockedGattClient,
                BluetoothGatt.GATT_FAILURE, BluetoothProfile.STATE_CONNECTED);

        //Verify that client was disconnected from the gatt server
        verify(mockedGattClient).disconnect();

    }

    @Test
    public void givenOnConnectionStateChanged_whenDisconnectedWithSuccessStatus_thenShouldDisconnect(){
       gattClientCallback.onConnectionStateChange(mockedGattClient,
                BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_DISCONNECTED);

        //Verify that client was disconnected from the gatt server
        verify(mockedGattClient).disconnect();
    }

    @Test
    public void givenOnConnectionStateChanged_whenConnectedWithSuccessStatus_thenShouldDiscoverServices(){
        gattClientCallback.onConnectionStateChange(mockedGattClient,
                BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED);

        //Verify that service discovery was started
        verify(mockedGattClient).discoverServices();
    }


    @Test
    public void givenOnMtuChangeRequested_whenReceivedChangeCallback_thenShouldDiscoverServices(){
        gattClientCallback.onConnectionStateChange(mockedGattClient,
                BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED);

        //Verify that MTU change was requested
        verify(mockedGattClient).requestMtu(MAXIMUM_MTU_SIZE);
        gattClientCallback.onMtuChanged(mockedGattClient,MAXIMUM_MTU_SIZE,BluetoothGatt.GATT_SUCCESS);
        //Verify that service discovery was started
        verify(mockedGattClient).discoverServices();
    }


    @Test
    public void givenServiceIsDiscovered_whenMatchingCharacteristicsFound_thenShouldRequestPermissionToWrite(){
        gattClientCallback.onServicesDiscovered(mockedGattClient, BluetoothGatt.GATT_SUCCESS);

        //verify that permission was set
        verify(mockedCharacteristic).setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        //Verify that characteristics permission was requested
        verify(mockedGattClient).setCharacteristicNotification(mockedCharacteristic,true);
    }

    @Test
    public void givenOnCharacteristicWrite_whenGrantedPermissionToWrite_thenShouldStartSendingPackets(){
        byte[][] packets = messageToSend.getPackets(DEFAULT_MTU_SIZE);

        for(int i = 0; i < packets.length; i++) {
            gattClientCallback.onCharacteristicWrite(mockedGattClient, mockedCharacteristic,
                    BluetoothGatt.GATT_SUCCESS);

            //verify that characteristics value was modified
            verify(mockedCharacteristic).setValue(packets[i]);
            //Verify that characteristics was modified

            //onCharacteristicWrite is called when permission is granted, and each time writing
            // finishes. Verifying that writeCharacteristic was called thus verifies
            // that the process will repeat.
            verify(mockedGattClient, times(i + 1)).writeCharacteristic(mockedCharacteristic);
        }
    }

    @Test
    public void givenOnConnectionStateChanged_whenMtuChangeRequested_thenShouldWaitAndDiscoverServices(){
        gattClientCallback.onConnectionStateChange(mockedGattClient,
                BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED);

        gattClientCallback.onMtuChanged(mockedGattClient,DEFAULT_MTU_SIZE, BluetoothGatt.GATT_SUCCESS);

        /*Verify that service discovery was started of which it might be dur to
        the request timeout or successful MTU change*/
        verify(mockedGattClient).discoverServices();
    }
}
