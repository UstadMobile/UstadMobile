package com.ustadmobile.test.port.sharedse.impl;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.sharedse.TestEntryStatusTask;
import com.ustadmobile.test.sharedse.impl.TestContext;
import com.ustadmobile.test.sharedse.impl.UstadMobileSystemImplTest;
import com.ustadmobile.test.sharedse.network.MockNetworkManager;
import com.ustadmobile.test.sharedse.network.MockRemoteDevice;
import com.ustadmobile.test.sharedse.network.MockWirelessArea;

/**
 * Created by kileha3 on 10/05/2017.
 */

public class UstadMobileSystemImplTestSE extends UstadMobileSystemImplTest {


    private MockNetworkManager networkManager;

    private MockRemoteDevice testDriver;

    private MockWirelessArea wirelessArea;


    public UstadMobileSystemImplTestSE() {
        super();
        wirelessArea = new MockWirelessArea();
        networkManager = new MockNetworkManager(TestConstants.TEST_MOCK_LOCAL_BLUETOOTH_DEVICE,
                wirelessArea);
        networkManager.setMockDeviceIpAddress("127.0.0.1");
        testDriver = networkManager.addMockTestDriver(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE);
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
            new MockRemoteDevice(TestConstants.TEST_MOCK_LOCAL_BLUETOOTH_DEVICE, wirelessArea,
                networkManager, context);//add the "main" device to the wireless area
            CatalogEntryInfo testEntryInfo = new CatalogEntryInfo();
            testEntryInfo.acquisitionStatus = CatalogController.STATUS_ACQUIRED;
            testEntryInfo.srcURLs = new String[]{"http://foo.com/bar.epub"};
            testEntryInfo.fileURI = "/path/to/file";
            testEntryInfo.mimeType = "application/zip+epub";
            CatalogController.setEntryInfo(TestEntryStatusTask.ENTRY_ID, testEntryInfo,
                CatalogController.SHARED_RESOURCE, testDriver.getContext());
        }
    }

    public MockRemoteDevice getMockTestDriver() {
        return testDriver;
    }


    @Override
    public NetworkManager getNetworkManager() {
        return networkManager;
    }




}
