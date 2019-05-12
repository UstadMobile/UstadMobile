package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.ustadmobile.lib.db.entities.NetworkNode;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil.bleMessageBytesToLong;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class which tests {@link BleEntryStatusTaskAndroid} to make sure it behaves as expected
 * under different circumstances.
 *
 * @author kileha3
 */
public class BleEntryStatusTaskAndroidTest {

    private List<Long> entries;


    private BleEntryStatusTaskAndroid statusTask;


    @Before
    public void setUp(){
        entries = Arrays.asList(1056289670L,4590875612L,9076137860L,2912543894L);
        BluetoothManager mockedBluetoothManager = mock(BluetoothManager.class);
        BluetoothAdapter mockedBluetoothAdapter = mock(BluetoothAdapter.class);
        BluetoothDevice mockedDevice = mock(BluetoothDevice.class);
        BluetoothGatt mockedGatt = mock(BluetoothGatt.class);
        Context context = mock(Context.class);


        NetworkManagerAndroidBle managerBle = mock(NetworkManagerAndroidBle.class);

        NetworkNode networkNode = new NetworkNode();
        networkNode.setBluetoothMacAddress("00:11:22:33:FF:EE");
        statusTask = new BleEntryStatusTaskAndroid(context, managerBle, entries, networkNode);
        statusTask.setBluetoothManager(mockedBluetoothManager);

        when(mockedBluetoothManager.getAdapter())
                .thenReturn(mockedBluetoothAdapter);

        when(mockedBluetoothAdapter.getRemoteDevice(networkNode.getBluetoothMacAddress()))
                .thenReturn(mockedDevice);

        when(mockedDevice.connectGatt(any(Context.class),
                eq(Boolean.FALSE),any(BluetoothGattCallback.class))).thenReturn(mockedGatt);

    }

    @Test
    public void givenEntryStatusIsCreated_whenStarted_thenShouldCreateBleClientCallback(){

        statusTask.run();

        assertNotNull("BleClientCallback should not be null ",
                statusTask.getGattClientCallback());
    }

    @Test
    public void givenEntryStatusIsCreated_whenStartedAndBleClientCallbackIsCreated_thenItShouldHaveRightMessage(){

        statusTask.run();

        assertNotNull("BleClientCallback should not be null ", statusTask.getGattClientCallback());

        List<Long> receivedEntries = bleMessageBytesToLong(statusTask.getMessage().getPayload());

        assertEquals("Should have the same message", receivedEntries,entries);
    }

}
