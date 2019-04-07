package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.networkmanager.BleMessage;
import com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.DEFAULT_MTU_SIZE;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.ENTRY_STATUS_REQUEST;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.USTADMOBILE_BLE_SERVICE_UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class which tests {@link BleGattServerAndroid} to make sure it behaves as expected on all
 * {@link BluetoothGattServerCallback} callbacks
 *
 * @author kileha3
 */


@RunWith(RobolectricTestRunner.class)
public class BleGattServerAndroidTest {

    private BluetoothGattCharacteristic mockedCharacteristics;

    private BluetoothGattServer mockedGattServer;

    private BleGattServerAndroid mGattServer;

    private BluetoothDevice mockedBluetoothDevice;

    private BleMessage bleMessage;

    @Before
    public void setUp(){

        Context context = mock(Context.class);

        mockedGattServer = mock(BluetoothGattServer.class);

        mockedBluetoothDevice = mock(BluetoothDevice.class);

        when(mockedBluetoothDevice.getAddress()).thenReturn("00:11:22:33:FF:EE");

        mockedCharacteristics = mock(BluetoothGattCharacteristic.class);

        BluetoothManager mockBluetoothManager = mock(BluetoothManager.class);

        EmbeddedHTTPD httpd = new EmbeddedHTTPD(0, context);
        try {
            httpd.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        NetworkManagerAndroidBle networkManager = new NetworkManagerAndroidBle(context, httpd);
        networkManager.setBluetoothManager(mockBluetoothManager);

        mGattServer = new BleGattServerAndroid(context,networkManager);
        mGattServer.setGattServer(mockedGattServer);

        when(mockBluetoothManager.openGattServer(any(Context.class),
                any(BluetoothGattServerCallback.class))).thenReturn(mockedGattServer);

        List<Long> entryList = Arrays.asList(1056289670L,4590875612L,9076137860L,2912543894L);
        bleMessage = new BleMessage(ENTRY_STATUS_REQUEST,(byte)42, BleMessageUtil.bleMessageLongToBytes(entryList));
        when(mockedCharacteristics.getUuid()).thenReturn(USTADMOBILE_BLE_SERVICE_UUID);
    }

    @Test
    public void givenOnCharacteristicReadRequestFromKnownSource_whenIsCharacteristicsWithSameUUID_thenShouldGrantPermission(){
        mGattServer.getGattServerCallback().onCharacteristicReadRequest(mockedBluetoothDevice,0, 0, mockedCharacteristics);
        //Verify that permission to read on the characteristics was granted
        verify(mockedGattServer).sendResponse(mockedBluetoothDevice,0,BluetoothGatt.GATT_SUCCESS,0,null);
    }

    @Test
    public void givenOnCharacteristicReadRequestFromUnknownSource_whenIsCharacteristicsWithSameUUID_thenShouldGrantPermission(){
        mockedCharacteristics.setWriteType(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);
        mGattServer.getGattServerCallback().onCharacteristicReadRequest(mockedBluetoothDevice,0, 0, mockedCharacteristics);

        //Verify that permission to read on the characteristics was granted
        verify(mockedGattServer).sendResponse(mockedBluetoothDevice,0,BluetoothGatt.GATT_SUCCESS,0,null);
    }


    @Test
    public void givenOnCharacteristicWriteRequest_whenIsCharacteristicsWithSameUUID_thenShouldGrantPermission(){
        mGattServer.getGattServerCallback().onCharacteristicWriteRequest(mockedBluetoothDevice,0, mockedCharacteristics,
                true,true,0,bleMessage.getPackets(DEFAULT_MTU_SIZE)[0]);

        //Verify that permission to write on the characteristics was granted
        verify(mockedGattServer).sendResponse(mockedBluetoothDevice,0,BluetoothGatt.GATT_SUCCESS,
                0,null);
    }

    @Test
    public void givenOnCharacteristicWriteRequest_whenIsCharacteristicsWithSameUUID_thenShouldStartReceivingPackets(){

        //TODO: Make simulation possible for handling this test case

    }
}
