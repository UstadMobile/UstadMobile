package com.ustadmobile.port.android.netwokmanager;

import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.networkmanager.BleMessage;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * <h1>BleEntryStatusTaskAndroidTest</h1>
 *
 * Test class which tests {@link BleEntryStatusTaskAndroid} to make sure it behaves as expected
 * under different circumstances.
 *
 * @author kileha3
 */
public class BleEntryStatusTaskAndroidTest {

    private List<Long> entries = Arrays.asList(64L,64L);

    @Test
    public void givenEntryStatusIsCreated_whenStarted_thenShouldCreateBleClientCallback(){
        NetworkNode networkNode = new NetworkNode();
        BleEntryStatusTaskAndroid statusTask = new BleEntryStatusTaskAndroid(entries,networkNode);

        statusTask.run();

        assertNotNull("BleClientCallback should not be null ", statusTask.getGattClientCallback());
    }

    @Test
    public void givenEntryStatusIsCreated_whenStartedAndBleClientCallbackIsCreated_thenItShouldHaveRightMessage(){
        NetworkNode networkNode = new NetworkNode();
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(entries.get(0));
        buffer.putLong(entries.get(1));

        BleEntryStatusTaskAndroid statusTask = new BleEntryStatusTaskAndroid(entries,networkNode);

        statusTask.run();

        assertNotNull("BleClientCallback should not be null ", statusTask.getGattClientCallback());

        BleMessage message = statusTask.getMessage();

        assertEquals("Should have the same message", message.getPayload(),buffer.array());

    }

    @Test
    public void givenEntryStatusIsStarted_whenFinishes_thenShouldReportBackTaskResults(){

        BleEntryStatusTaskAndroid statusTask = mock(BleEntryStatusTaskAndroid.class);

        statusTask.run();

        //Verify that onResponseReceived was called
        verify(statusTask).onResponseReceived(any());
    }
}
