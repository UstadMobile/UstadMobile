package com.ustadmobile.port.android.netwokmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.BleMessage;
import com.ustadmobile.port.sharedse.networkmanager.BleMessageUtil;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
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

        NetworkNode networkNode = new NetworkNode();
        networkNode.setBluetoothMacAddress("");
        statusTask = new BleEntryStatusTaskAndroid(mock(Context.class), entries, networkNode);
        statusTask.setBluetoothManager(mockedBluetoothManager);

        when(mockedBluetoothManager.getAdapter())
                .thenReturn(mockedBluetoothAdapter);

        when(mockedBluetoothAdapter.getRemoteDevice("")).thenReturn(mockedDevice);

    }

    @Test
    public void givenEntryStatusIsCreated_whenStarted_thenShouldCreateBleClientCallback(){

        statusTask.run();

        assertNotNull("BleClientCallback should not be null ",
                statusTask.getGattClientCallback());
    }

    @Test
    public void givenEntryStatusIsCreated_whenStartedAndBleClientCallbackIsCreated_thenItShouldHaveRightMessage(){
        byte [] payload = BleMessageUtil.bleMessageLongToBytes(entries);

        statusTask.run();

        assertNotNull("BleClientCallback should not be null ", statusTask.getGattClientCallback());

        BleMessage message = statusTask.getMessage();

        assertTrue("Should have the same message", Arrays.equals(message.getPayload(),payload));
    }

}
