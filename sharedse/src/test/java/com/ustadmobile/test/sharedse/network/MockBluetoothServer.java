package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.port.sharedse.networkmanager.BluetoothServer;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by kileha3 on 10/05/2017.
 */

public class MockBluetoothServer extends BluetoothServer {

    private boolean running = false;

    private HashMap<String, String> availableEntries;

    public MockBluetoothServer(NetworkManager manager) {
        super(manager);
        availableEntries = new HashMap<>();
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    public MockBluetoothServer setEntryAvailable(String entryId, String uri) {
        availableEntries.put(entryId, uri);
        return this;
    }

    @Override
    public void handleNodeConnected(String deviceAddress, InputStream inputStream, OutputStream outputStream) throws IOException {
        if(availableEntries.size() > 0) {
            //handle this in the mock server
        }else {
            super.handleNodeConnected(deviceAddress, inputStream, outputStream);
        }

    }
}
