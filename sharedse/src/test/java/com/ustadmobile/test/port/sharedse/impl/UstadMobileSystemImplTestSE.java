package com.ustadmobile.test.port.sharedse.impl;

import com.ustadmobile.core.controller.CatalogController;
import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.sharedse.TestEntryStatusTask;
import com.ustadmobile.test.sharedse.impl.TestContext;
import com.ustadmobile.test.sharedse.impl.UstadMobileSystemImplTest;
import com.ustadmobile.test.sharedse.network.MockNetworkManager;
import com.ustadmobile.test.sharedse.network.MockWifiNetwork;
import com.ustadmobile.test.sharedse.network.MockWirelessArea;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
                wirelessArea, "MockMainTestDevice");
        networkManager.setMockDeviceIpAddress("127.0.0.1");
        testDriver = new MockNetworkManager(TestConstants.TEST_REMOTE_BLUETOOTH_DEVICE,
            wirelessArea, "MockTestDriver");
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

            File sharedStorageDir = new File(getStorageDirs(CatalogController.SHARED_RESOURCE,
                testDriver.getContext())[0].getDirURI());
            File testDriverEntryFile = new File(sharedStorageDir, "thelittlechicks.epub");
            InputStream entryIn = null;
            FileOutputStream fout = null;
            IOException ioe = null;
            try {
                entryIn = getClass().getResourceAsStream("/com/ustadmobile/test/sharedse/thelittlechicks.epub");
                fout = new FileOutputStream(testDriverEntryFile);
                UMIOUtils.readFully(entryIn, fout, 1024);
                fout.flush();
            }catch(IOException e) {
                ioe = e;
            }finally {
                UMIOUtils.closeInputStream(entryIn);
                UMIOUtils.closeOutputStream(fout);
                if(ioe != null) {
                    throw new RuntimeException(ioe);
                }
            }

            testEntryInfo.fileURI = testDriverEntryFile.getAbsolutePath();
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
