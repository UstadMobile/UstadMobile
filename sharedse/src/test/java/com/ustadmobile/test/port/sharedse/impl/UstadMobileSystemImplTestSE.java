package com.ustadmobile.test.port.sharedse.impl;

import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UMStorageDir;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManager;
import com.ustadmobile.test.core.buildconfig.TestConstants;
import com.ustadmobile.test.core.impl.TestContext;
import com.ustadmobile.test.sharedse.SharedSeTestSuite;
import com.ustadmobile.test.sharedse.impl.UstadMobileSystemImplTest;
import com.ustadmobile.test.sharedse.network.MockNetworkManager;
import com.ustadmobile.test.sharedse.network.MockWifiNetwork;
import com.ustadmobile.test.sharedse.network.MockWirelessArea;
import com.ustadmobile.test.sharedse.network.TestEntryStatusTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.ustadmobile.test.sharedse.network.MockNetworkManager.MOCK_WIFI_CONNECTION_DELAY;

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
        testDriverContext = new TestContext("testdriver");
        testDriver.setWifiDirectMacAddr(TestConstants.TEST_REMOTE_SLAVE_SERVER_WLAN_MAC);
        testDriver.setMockDeviceIpAddress(TestConstants.TEST_REMOTE_MOCK_SLAVE_SERVER);
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

            //make the context directory
            TestContext[] testContexts = new TestContext[]{(TestContext)context, testDriverContext};
            for(int i = 0; i < testContexts.length; i++) {
                UMStorageDir[] contextDir = getStorageDirs(CatalogPresenter.SHARED_RESOURCE, testContexts[i]);
                File currentDir;
                for(int j = 0; j < contextDir.length; j++) {
                    currentDir = new File(contextDir[j].getDirURI());
                    if(!currentDir.isDirectory())
                        currentDir.mkdirs();
                }
            }


            CatalogEntryInfo testEntryInfo = new CatalogEntryInfo();
            testEntryInfo.acquisitionStatus = CatalogPresenter.STATUS_ACQUIRED;
            testEntryInfo.srcURLs = new String[]{"http://foo.com/bar.epub"};

            File sharedStorageDir = new File(getStorageDirs(CatalogPresenter.SHARED_RESOURCE,
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
            CatalogPresenter.setEntryInfo(TestEntryStatusTask.ENTRY_ID, testEntryInfo,
                    CatalogPresenter.SHARED_RESOURCE, testDriver.getContext());

            //Setup the default mock wifi
            MockWifiNetwork defaultMockNetwork = new MockWifiNetwork(
                MockNetworkManager.MOCK_WIRELESS_DEFAULT_WIRELESS_SSID,
                MockNetworkManager.MOCK_WIRELESS_DEFAULT_WIRELESS_PASSPHRASE);
            wirelessArea.addWifiNetwork(defaultMockNetwork);

            String selfIpAddr = "127.0.0.2";//TODO: set from test constant

            networkManager.connectWifi(MockNetworkManager.MOCK_WIRELESS_DEFAULT_WIRELESS_SSID,
                MockNetworkManager.MOCK_WIRELESS_DEFAULT_WIRELESS_PASSPHRASE);



            testDriver.connectWifi(MockNetworkManager.MOCK_WIRELESS_DEFAULT_WIRELESS_SSID,
                MockNetworkManager.MOCK_WIRELESS_DEFAULT_WIRELESS_PASSPHRASE);

            try { Thread.sleep(MOCK_WIFI_CONNECTION_DELAY + 200); }
            catch(InterruptedException e) {}

            SharedSeTestSuite.REMOTE_SLAVE_SERVER = TestConstants.TEST_REMOTE_MOCK_SLAVE_SERVER;

            defaultMockNetwork.setDeviceIpAddr(testDriver, SharedSeTestSuite.REMOTE_SLAVE_SERVER);
            testDriver.setMockDeviceIpAddress(SharedSeTestSuite.REMOTE_SLAVE_SERVER);

            defaultMockNetwork.setDeviceIpAddr(networkManager, selfIpAddr);
            networkManager.setMockDeviceIpAddress(selfIpAddr);
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
