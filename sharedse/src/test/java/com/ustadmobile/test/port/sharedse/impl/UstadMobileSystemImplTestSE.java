package com.ustadmobile.test.port.sharedse.impl;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.sharedse.TestEntryStatusTask;
import com.ustadmobile.test.sharedse.impl.TestContext;
import com.ustadmobile.test.sharedse.impl.UstadMobileSystemImplTest;
import com.ustadmobile.test.sharedse.network.MockNetworkManager;
import com.ustadmobile.test.sharedse.network.MockWifiNetwork;
import com.ustadmobile.test.sharedse.network.MockWirelessArea;

/**
 * Created by kileha3 on 10/05/2017.
 */

public class UstadMobileSystemImplTestSE extends UstadMobileSystemImplTest {


    private MockNetworkManager networkManager;

    private MockNetworkManager testDriver;

    private MockWirelessArea wirelessArea;

    private TestContext testDriverContext;


    public UstadMobileSystemImplTestSE() {
        super();
        wirelessArea = new MockWirelessArea();
        networkManager = new MockNetworkManager(TestConstants.TEST_MOCK_LOCAL_BLUETOOTH_DEVICE,
                wirelessArea);
        networkManager.setMockDeviceIpAddress("127.0.0.1");
        testDriver = new MockNetworkManager(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE,
            wirelessArea);
        testDriverContext = new TestContext();
        testDriver.init(testDriverContext);
        testDriver.startTestControlServer();
    }

    /**
     * Init must be called by a @BeforeClass method in SharedSeTestSuiteRun to do system impl
     * initilization with a context
     *
     * @param context
     */
    @Override
    public void init(Object context) {
        if(!isInitialized()) {
            super.init(context);
            networkManager.init(context);

            CatalogEntryInfo testEntryInfo = new CatalogEntryInfo();
            testEntryInfo.acquisitionStatus = CatalogController.STATUS_ACQUIRED;
            testEntryInfo.srcURLs = new String[]{"http://foo.com/bar.epub"};
            testEntryInfo.fileURI = "/path/to/file";
            testEntryInfo.mimeType = "application/zip+epub";
            CatalogController.setEntryInfo(TestEntryStatusTask.ENTRY_ID, testEntryInfo,
                CatalogController.SHARED_RESOURCE, testDriver.getContext());

            //Setup the default mock wifi
            MockWifiNetwork defaultMockNetwork = new MockWifiNetwork(
                MockNetworkManager.MOCK_WIRELESS_DEFAULT_WIRELESS_SSID,
                MockNetworkManager.MOCK_WIRELESS_DEFAULT_WIRELESS_PASSPHRASE);
            wirelessArea.addWifiNetwork(defaultMockNetwork);

            String selfIpAddr = "127.0.0.2";//TODO: set from test constant

            networkManager.connectWifi(MockNetworkManager.MOCK_WIRELESS_DEFAULT_WIRELESS_SSID,
                MockNetworkManager.MOCK_WIRELESS_DEFAULT_WIRELESS_PASSPHRASE);
            defaultMockNetwork.setDeviceIpAddr(networkManager, selfIpAddr);
            networkManager.setMockDeviceIpAddress(selfIpAddr);

            testDriver.connectWifi(MockNetworkManager.MOCK_WIRELESS_DEFAULT_WIRELESS_SSID,
                MockNetworkManager.MOCK_WIRELESS_DEFAULT_WIRELESS_PASSPHRASE);
            defaultMockNetwork.setDeviceIpAddr(testDriver, TestConstants.TEST_REMOTE_SLAVE_SERVER);
            testDriver.setMockDeviceIpAddress(TestConstants.TEST_REMOTE_SLAVE_SERVER);
        }
    }

    public MockNetworkManager getMockTestDriver() {
        return testDriver;
    }


    @Override
    public NetworkManager getNetworkManager() {
        return networkManager;
    }




}
