package com.ustadmobile.test.sharedse.network;

import com.ustadmobile.port.sharedse.networkmanager.BluetoothServer;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.buildconfig.TestConstants;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Created by kileha3 on 11/05/2017.
 */

public class MockRemoteDevice {

    private String bluetoothAddr;

    private Object context;

    private MockNetworkManager networkManager;

    protected MockBluetoothServer mockBluetoothServer;

    public MockRemoteDevice(String bluetoothAddr, Object context) {
        this.context = context;
        this.bluetoothAddr = bluetoothAddr;
        networkManager = new MockNetworkManager(bluetoothAddr);
        networkManager.init(context, TestConstants.TEST_NETWORK_SERVICE_NAME);
        mockBluetoothServer = new MockBluetoothServer(networkManager);
    }


    class MockBluetoothServer extends BluetoothServer implements Runnable {

        private PipedInputStream serverIn;

        private PipedOutputStream serverOut;

        private String remoteAddr;

        public MockBluetoothServer(NetworkManager manager) {
            super(manager);
        }

        protected void connectMockClient(String remoteAddr, PipedInputStream clientIn, PipedOutputStream clientOut) {
            try {
                serverIn = new PipedInputStream();
                serverIn.connect(clientOut);
                serverOut = new PipedOutputStream();
                serverOut.connect(clientIn);
                this.remoteAddr = remoteAddr;
                new Thread(this).start();
            }catch(IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void start() {

        }

        public void run() {
            try {
                handleNodeConnected(remoteAddr, serverIn, serverOut);
                serverIn.close();
                serverOut.close();
                remoteAddr = null;
            }catch(IOException e) {

            }
        }


        @Override
        public void stop() {

        }

    }

    public MockBluetoothServer getMockBluetoothServer() {
        return mockBluetoothServer;
    }


    public String getBluetoothAddr() {
        return bluetoothAddr;
    }

}
