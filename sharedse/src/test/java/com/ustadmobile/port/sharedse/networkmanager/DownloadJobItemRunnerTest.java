package com.ustadmobile.port.sharedse.networkmanager;

import com.google.gson.Gson;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.WaitForLiveData;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.database.jdbc.DriverConnectionPoolInitializer;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContainerEntry;
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSetItem;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.port.sharedse.container.ContainerManager;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPDTestServer;
import com.ustadmobile.port.sharedse.util.SharedSeTestUtil;
import com.ustadmobile.sharedse.SharedSeTestConfig;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipFile;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

import static com.ustadmobile.port.sharedse.networkmanager.DownloadJobItemRunner.CONTENT_ENTRY_FILE_PATH;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class which tests {@link DownloadJobItemRunner} behaves as expected when downloading
 * content entries.
 *
 * @author Kileha3
 */
public class DownloadJobItemRunnerTest {

    @Deprecated
    private MockWebServer mockCloudWebServer;

    @Deprecated
    private MockWebServer mockPeerWebServer;

    private EmbeddedHTTPDTestServer cloudServer;

    private EmbeddedHTTPDTestServer peerServer;

    private NetworkManagerBle mockedNetworkManager;

    private AtomicBoolean mockedNetworkManagerBleWorking = new AtomicBoolean();

    private AtomicBoolean mockedNetworkManagerWifiConnectWorking = new AtomicBoolean();

    private UmAppDatabase clientDb;

    private UmAppDatabase clientRepo;

    private UmAppDatabase serverDb;

    private UmAppDatabase serverRepo;

    private String cloudEndPoint, localEndPoint;

    @Deprecated
    private ContentEntryFileDispatcher cloudServerDispatcher = null;

    @Deprecated
    private ContentEntryFileDispatcher mockPeerServerDispatcher = null;

    private static File webServerTmpDir;

    private static File containerTmpDir;

    private static File webServerTmpContentEntryFile;

    private static final long TEST_CONTENT_ENTRY_FILE_UID = 1000L;

    private static final String ENDPOINT_FILE_POSTFIX = "/" + CONTENT_ENTRY_FILE_PATH;

    private static final String TEST_FILE_RESOURCE_PATH =
            "/com/ustadmobile/port/sharedse/networkmanager/thelittlechicks.epub";

    private  Object context;


    private EntryStatusResponse entryStatusResponse;

    //private DownloadJobItemHistory history;

    private BleEntryStatusTask mockedEntryStatusTask;

    private NetworkNode networkNode;

    private ConnectivityStatus connectivityStatus;

    private BleMessage wifiDirectGroupInfoMessage;

    private WiFiDirectGroupBle groupBle;

    private DownloadJobItem downloadJobItem;

    private Container container;

    private ContainerManager containerManager;

    private static final int MAX_LATCH_WAITING_TIME = 15;

    private static final int MAX_THREAD_SLEEP_TIME = 2;

    private class ContentEntryFileDispatcher extends Dispatcher {

        private int numTimesToFail;

        private long throttleBytesPerPeriod = 0;

        private long throttlePeriod = 0L;

        private TimeUnit throttleTimeUnit;

        private AtomicInteger numFileGetRequests = new AtomicInteger();

        @Override
        public MockResponse dispatch(RecordedRequest request) {
            if(request.getMethod().equals("GET")) {
                numFileGetRequests.incrementAndGet();
            }

            if(numTimesToFail > 0) {
                numTimesToFail--;
                return new MockResponse().setResponseCode(500);
            }else if (request.getPath().startsWith(ENDPOINT_FILE_POSTFIX)){
                String contentEntryFilename = request.getPath().substring(
                        ENDPOINT_FILE_POSTFIX.length());
                File responseFile = new File(webServerTmpDir, contentEntryFilename);
                try {
                    BufferedSource fileBuffer = Okio.buffer(Okio.source(responseFile));
                    Buffer outBuffer = new Buffer();
                    fileBuffer.readFully(outBuffer, responseFile.length());

                    MockResponse response = new MockResponse()
                            .setBody(outBuffer)
                            .setResponseCode(200)
                            .addHeader("Content-Length", responseFile.length());

                    if(throttleBytesPerPeriod > 0) {
                        response.throttleBody(throttleBytesPerPeriod, throttlePeriod,
                                throttleTimeUnit);
                    }

                    return response;
                }catch(IOException e) {
                    //should not happen
                }


            }
            return new MockResponse().setResponseCode(404);
        }

        void setNumTimesToFail(int numTimesToFail) {
            this.numTimesToFail = numTimesToFail;
        }

        void setThrottle(long throttleBytesPerPeriod, long throttlePeriod, TimeUnit timeUnit) {
            this.throttleBytesPerPeriod = throttleBytesPerPeriod;
            this.throttlePeriod = throttlePeriod;
            this.throttleTimeUnit = timeUnit;
        }

        int getNumFileGetRequests() {
            return numFileGetRequests.get();
        }

    }



    @BeforeClass
    public static void setupTmpDir() throws IOException{
        DriverConnectionPoolInitializer.bindDataSource("UmAppDatabase",
                SharedSeTestConfig.TESTDB_JDBCURL_UMMAPPDATABASE, true);
        DriverConnectionPoolInitializer.bindDataSource("clientdb",
                SharedSeTestConfig.TESTDB_JDBCURL_CLIENTUMAPPDATABASE, true);
        webServerTmpDir = File.createTempFile("downloadjobitemrunner", "tmpdir");
        if(!(webServerTmpDir.delete() && webServerTmpDir.mkdirs())) {
            throw new IOException("Could not make temporary directory");
        }

        InputStream resIn = DownloadJobItemRunnerTest.class.getResourceAsStream(
                TEST_FILE_RESOURCE_PATH);
        webServerTmpContentEntryFile = new File(webServerTmpDir,
                ""+ TEST_CONTENT_ENTRY_FILE_UID);
        FileOutputStream fout = new FileOutputStream(webServerTmpContentEntryFile);
        UMIOUtils.readFully(resIn, fout);
        resIn.close();
        fout.close();

        containerTmpDir = SharedSeTestUtil.makeTempDir("downloadjobitemrunnertest", "tmpdir");
    }


    @Before
    public void setup() throws IOException {
        context = PlatformTestUtil.getTargetContext();
        mockedNetworkManager = spy(NetworkManagerBle.class);

        mockedNetworkManagerBleWorking.set(true);

        mockedNetworkManagerWifiConnectWorking.set(true);

        mockedEntryStatusTask = mock(BleEntryStatusTask.class);
        mockedNetworkManager.setContext(context);

        EmbeddedHTTPD httpd = new EmbeddedHTTPD(0,context);
        httpd.start();

        when(mockedNetworkManager.getHttpd()).thenReturn(httpd);

        groupBle =  new WiFiDirectGroupBle("DIRECT-PeerNode","networkPass123");


        File downloadTmpDir = File.createTempFile("DownloadJobItemRunnerTest", "dldir");
        if(!(downloadTmpDir.delete() && downloadTmpDir.mkdirs())) {
            throw new IOException("Failed to create tmp directory" + downloadTmpDir);
        }

        UmAppDatabase.getInstance(context).clearAllTables();
        clientDb = UmAppDatabase.getInstance(context, "clientdb");
        clientRepo = clientDb.getRepository("http://localhost/dummy/", "");
        networkNode = new NetworkNode();
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4F");
        networkNode.setLastUpdateTimeStamp(System.currentTimeMillis());
        networkNode.setNodeId(clientDb.getNetworkNodeDao().insert(networkNode));

        serverDb = UmAppDatabase.getInstance(context);
        serverRepo = serverDb.getRepository("http://localhost/dummy/", "");




        //To be removed
        ContentEntry contentEntry = new ContentEntry();
        contentEntry.setTitle("Test entry");
        contentEntry.setContentEntryUid(clientDb.getContentEntryDao().insert(contentEntry));

        ContentEntryFile contentEntryFile = new ContentEntryFile();
        contentEntryFile.setContentEntryFileUid(0);
        contentEntryFile.setFileSize(webServerTmpContentEntryFile.length());
        contentEntryFile.setMimeType("application/epub+zip");
        clientDb.getContentEntryFileDao().insert(contentEntryFile);

        clientDb.getContentEntryContentEntryFileJoinDao().insert(
                new ContentEntryContentEntryFileJoin(contentEntry, contentEntryFile));
        //end to be removed

        container = new Container();
        container.setContainerUid(serverDb.getContainerDao().insert(container));
        containerManager = new ContainerManager(container, serverDb, serverRepo,
                webServerTmpDir.getAbsolutePath());
        ZipFile zipFile = new ZipFile(webServerTmpContentEntryFile);
        containerManager.addEntriesFromZip(zipFile);

        //add the container itself to the client database (would normally happen via sync/preload)
        clientRepo.getContainerDao().insert(container);


        DownloadSet downloadSet = new DownloadSet();
        downloadSet.setDestinationDir(downloadTmpDir.getAbsolutePath());
        downloadSet.setDsRootContentEntryUid(0L);
        downloadSet.setDsUid((int) clientDb.getDownloadSetDao().insert(downloadSet));

        DownloadSetItem downloadSetItem = new DownloadSetItem(downloadSet, contentEntry);
        downloadSetItem.setDsiUid((int) clientDb.getDownloadSetItemDao().insert(downloadSetItem));

        DownloadJob downloadJob = new DownloadJob(downloadSet);
        downloadJob.setTimeCreated(System.currentTimeMillis());
        downloadJob.setTimeRequested(System.currentTimeMillis());
        downloadJob.setDjStatus(JobStatus.QUEUED);
        downloadJob.setDjUid((int) clientDb.getDownloadJobDao().insert(downloadJob));

        downloadJobItem = new DownloadJobItem(downloadJob, downloadSetItem,
                container);
        downloadJobItem.setDjiStatus(JobStatus.QUEUED);
        downloadJobItem.setDownloadedSoFar(0);
        downloadJobItem.setDestinationFile(new File(downloadTmpDir,
                String.valueOf(TEST_CONTENT_ENTRY_FILE_UID)).getAbsolutePath());
        downloadJobItem.setDjiUid(clientDb.getDownloadJobItemDao().insert(downloadJobItem));



        connectivityStatus = new ConnectivityStatus();
        connectivityStatus.setConnectedOrConnecting(true);
        connectivityStatus.setConnectivityState(ConnectivityStatus.STATE_UNMETERED);
        connectivityStatus.setCsUid(1);
        clientDb.getConnectivityStatusDao().insert(connectivityStatus);

        entryStatusResponse = new EntryStatusResponse();
        entryStatusResponse.setErContainerUid(container.getContainerUid());
        entryStatusResponse.setErNodeId(networkNode.getNodeId());
        entryStatusResponse.setAvailable(true);
        entryStatusResponse.setResponseTime(System.currentTimeMillis());

        cloudServer = new EmbeddedHTTPDTestServer(0, PlatformTestUtil.getTargetContext(),
                serverDb);
        cloudServer.start();

        cloudServerDispatcher =  new ContentEntryFileDispatcher();
        mockCloudWebServer = new MockWebServer();
        mockCloudWebServer.setDispatcher(cloudServerDispatcher);
        mockCloudWebServer.start();
        //cloudEndPoint = mockCloudWebServer.url("/").toString();
        cloudEndPoint = cloudServer.getLocalURL();

        //Server itself will only start after request to connect to peer
        mockPeerServerDispatcher = new ContentEntryFileDispatcher();
        mockPeerWebServer = null;


        when(mockedNetworkManager.makeEntryStatusTask(any(Object.class),
                any(),any(NetworkNode.class))).thenReturn(mockedEntryStatusTask);

        when(mockedNetworkManager.makeEntryStatusTask(any(Object.class),
                any(BleMessage.class),any(NetworkNode.class),
                any(BleMessageResponseListener.class))).thenReturn(mockedEntryStatusTask);


        doAnswer(invocation -> {
            BleMessageResponseListener bleResponseListener = invocation.getArgument(3);
            startPeerWebServer();
            Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_THREAD_SLEEP_TIME));
            groupBle.setEndpoint(localEndPoint);
            wifiDirectGroupInfoMessage = new BleMessage(WIFI_GROUP_CREATION_RESPONSE,
                    new Gson().toJson(groupBle).getBytes());

            bleResponseListener.onResponseReceived(networkNode.getBluetoothMacAddress(),
                    (mockedNetworkManagerBleWorking.get() ? wifiDirectGroupInfoMessage : null),
                    (mockedNetworkManagerBleWorking.get() ? null : new IOException(
                            "BLE group details request failed")));

            return null;
        }).when(mockedNetworkManager).sendMessage(any(Object.class), any(BleMessage.class),
                any(NetworkNode.class), any(BleMessageResponseListener.class));

        doAnswer((invocation -> {
            clientDb.getConnectivityStatusDao()
                    .updateState(ConnectivityStatus.STATE_CONNECTING_LOCAL,groupBle.getSsid(), null);
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));

            int state = mockedNetworkManagerWifiConnectWorking.get()
                    ? ConnectivityStatus.STATE_CONNECTED_LOCAL : ConnectivityStatus.STATE_DISCONNECTED;
            clientDb.getConnectivityStatusDao().updateState(state,
                    state != ConnectivityStatus.STATE_DISCONNECTED ? groupBle.getSsid(): null , null);
            return null;
        })).when(mockedNetworkManager).connectToWiFi(eq(groupBle.getSsid()), eq(groupBle.getPassphrase()));


        doAnswer((invocation) -> {
            new Thread(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException e) { /*should not happen*/}
                clientDb.getConnectivityStatusDao().updateState(ConnectivityStatus.STATE_UNMETERED,
                        "normalwifi", null);
            }).start();
            return null;
        }).when(mockedNetworkManager).restoreWifi();

    }

    @After
    public void tearDown() throws IOException{
        if(mockCloudWebServer != null){
            mockCloudWebServer.shutdown();
        }
        if(mockPeerWebServer != null){
            mockPeerWebServer.shutdown();
        }

        if(cloudServer != null)
            cloudServer.stop();

        if(peerServer != null)
            peerServer.stop();
    }

    private void startPeerWebServer() throws IOException {
        if(mockPeerWebServer == null){
            mockPeerWebServer = new MockWebServer();
            mockPeerWebServer.setDispatcher(mockPeerServerDispatcher);
            mockPeerWebServer.start();
            localEndPoint = mockPeerWebServer.url("/").toString();
        }
    }

    public static void assertContainersHaveSameContent(long containerUid1, long containerUid2,
                                                       UmAppDatabase db1, UmAppDatabase repo1,
                                                       UmAppDatabase db2, UmAppDatabase repo2) throws IOException {

        Container container1 = repo1.getContainerDao().findByUid(containerUid1);
        ContainerManager manager1 = new ContainerManager(container1, db1, repo1);

        Container container2 = repo2.getContainerDao().findByUid(containerUid2);
        ContainerManager manager2 = new ContainerManager(container2, db2, repo2);

        Assert.assertEquals("Containers have same number of entries",
                container1.getCntNumEntries(),
                db2.getContainerEntryDao().findByContainer(containerUid2).size());

        for(ContainerEntryWithContainerEntryFile entry : manager1.getAllEntries()) {
            ContainerEntry entry2 = manager2.getEntry(entry.getCePath());
            Assert.assertNotNull("Client container also contains " + entry.getCePath(),
                    entry2);
            Assert.assertArrayEquals(
                    UMIOUtils.readStreamToByteArray(manager1.getInputStream(entry)),
                    UMIOUtils.readStreamToByteArray(manager2.getInputStream(entry2)));
        }
    }


    @Test
    public void givenDownload_whenRun_shouldDownloadAndComplete() throws IOException{
        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context, item, mockedNetworkManager, clientDb, clientRepo,
                        cloudEndPoint, connectivityStatus);

        jobItemRunner.run();

        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());

        assertEquals("File download task completed successfully",
                JobStatus.COMPLETE, item.getDjiStatus());

        Assert.assertEquals("Correct number of ContentEntry items available in client db",
                container.getCntNumEntries(),
                clientDb.getContainerEntryDao().findByContainer(item.getDjiContainerUid()).size());

        assertContainersHaveSameContent(item.getDjiContainerUid(), item.getDjiContainerUid(),
                serverDb, serverRepo, clientDb, clientRepo);

    }

    @Test
    public void givenDownloadStarted_whenFailsOnce_shouldRetryAndComplete() {

        cloudServerDispatcher.setNumTimesToFail(1);

        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, clientDb, clientRepo,
                        cloudEndPoint, connectivityStatus);

        jobItemRunner.run();

        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                downloadJobItem.getDjiUid());

        assertEquals("File download task retried and completed successfully",
                JobStatus.COMPLETE, item.getDjiStatus());

        assertEquals("Same file size", webServerTmpContentEntryFile.length(),
                new File(item.getDestinationFile()).length());

        assertEquals("Number of attempts = 2", 2, item.getNumAttempts());
        assertEquals("Number of file get requests = 2", 2,
                cloudServerDispatcher.getNumFileGetRequests());

    }

    @Test
    public void givenDownloadStarted_whenFailsExceedMaxAttempts_shouldStopAndSetStatusToFailed() {

        cloudServerDispatcher.setNumTimesToFail(4);

        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, clientDb, clientRepo,
                        cloudEndPoint, connectivityStatus);

        jobItemRunner.run();

        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                downloadJobItem.getDjiUid());

        assertEquals("File download task retried and completed with failure status",
                JobStatus.FAILED, item.getDjiStatus());
    }

    @Test
    public void givenDownloadUnmeteredConnectivityOnly_whenConnectivitySwitchesToMetered_shouldStopAndSetStatusToWaiting()
            throws InterruptedException {

        cloudServerDispatcher.setThrottle(512, 1,TimeUnit.SECONDS);

        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, clientDb, clientRepo,
                        cloudEndPoint, connectivityStatus);

        new Thread(jobItemRunner).start();

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_THREAD_SLEEP_TIME));

        clientDb.getConnectivityStatusDao().updateState(ConnectivityStatus.STATE_METERED, null);

        WaitForLiveData.observeUntil(clientDb.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid()), MAX_LATCH_WAITING_TIME,
                TimeUnit.SECONDS,status-> status == JobStatus.WAITING_FOR_CONNECTION);


        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                downloadJobItem.getDjiUid());

        assertEquals("File download task stopped after network status " +
                        "change and set status to waiting",
                JobStatus.WAITING_FOR_CONNECTION, item.getDjiStatus());

    }

    @Test
    public void givenDownloadStarted_whenJobIsStopped_shouldStopAndSetStatus()
            throws InterruptedException {

        cloudServerDispatcher.setThrottle(512, 1,TimeUnit.SECONDS);

        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, clientDb, clientRepo,
                        cloudEndPoint, connectivityStatus);

        new Thread(jobItemRunner).start();

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_THREAD_SLEEP_TIME));

        clientDb.getDownloadJobItemDao().updateStatus(item.getDjiUid(),JobStatus.STOPPING);

        WaitForLiveData.observeUntil(clientDb.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid()), MAX_LATCH_WAITING_TIME,
                TimeUnit.SECONDS, status -> status == JobStatus.STOPPED);

        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                downloadJobItem.getDjiUid());

        assertEquals("File download job was stopped and status was updated",
                JobStatus.STOPPED, item.getDjiStatus());
    }


    @Test
    public void givenDownloadStartsOnMeteredConnection_whenJobSetChangedToDisableMeteredConnection_shouldStopAndSetStatus()
            throws InterruptedException {

        cloudServerDispatcher.setThrottle(512, 3,TimeUnit.SECONDS);

        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());
        clientDb.getDownloadSetDao().setMeteredConnectionBySetUid(
                item.getDownloadSetItem().getDsiDsUid(),true);

        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, clientDb, clientRepo,
                        cloudEndPoint, connectivityStatus);

        clientDb.getConnectivityStatusDao().updateState(ConnectivityStatus.STATE_METERED, null);



        UstadMobileSystemImpl.l(UMLog.DEBUG, 699,
                " Running DownloadJobItemRunner for "+item.getDjiUid());

        new Thread(jobItemRunner).start();

        WaitForLiveData.observeUntil(clientDb.getDownloadJobItemDao()
                .getLiveStatus(item.getDjiUid()), MAX_LATCH_WAITING_TIME,
                TimeUnit.SECONDS, status -> status >= JobStatus.RUNNING_MIN);

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_THREAD_SLEEP_TIME));

        clientDb.getDownloadSetDao().setMeteredConnectionBySetUid(
                item.getDownloadSetItem().getDsiDsUid(),false);

        WaitForLiveData.observeUntil(clientDb.getDownloadJobItemDao()
                        .getLiveStatus(item.getDjiUid()), MAX_LATCH_WAITING_TIME,
                TimeUnit.SECONDS, status -> status == JobStatus.WAITING_FOR_CONNECTION);

        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                downloadJobItem.getDjiUid());


        assertEquals("File download job is waiting for network after changing download" +
                        " set setting to use unmetered connection only",
                JobStatus.WAITING_FOR_CONNECTION, item.getDjiStatus());

    }

    @Test
    public void givenDownloadStarted_whenConnectionGoesOff_shouldStopAndSetStatusToWaiting()
            throws InterruptedException {

        cloudServerDispatcher.setThrottle(512, 1,TimeUnit.SECONDS);

        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, clientDb, clientRepo,
                        cloudEndPoint, connectivityStatus);

        new Thread(jobItemRunner).start();

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_THREAD_SLEEP_TIME));

        clientDb.getConnectivityStatusDao().updateState(ConnectivityStatus.STATE_DISCONNECTED, null);


        WaitForLiveData.observeUntil(clientDb.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid()), MAX_LATCH_WAITING_TIME, TimeUnit.SECONDS,
                status->status == JobStatus.WAITING_FOR_CONNECTION);

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_THREAD_SLEEP_TIME));

        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                downloadJobItem.getDjiUid());

        assertEquals("File download job is waiting for network after the network goes off",
                JobStatus.WAITING_FOR_CONNECTION, item.getDjiStatus());
    }


    @Test
    public void givenDownloadLocallyAvailable_whenDownloadStarted_shouldDownloadFromLocalNode() {
        entryStatusResponse.setAvailable(true);
        clientDb.getEntryStatusResponseDao().insert(entryStatusResponse);

        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context, item, mockedNetworkManager, clientDb, clientRepo,
                        cloudEndPoint, connectivityStatus);

        jobItemRunner.run();

        assertTrue("File downloaded from peer web server",
                mockPeerWebServer.getRequestCount() >=1);
        assertEquals("Cloud mock server received no requests", 0 ,
                mockCloudWebServer.getRequestCount());

        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                downloadJobItem.getDjiUid());
        assertEquals("File downloaded successfully",JobStatus.COMPLETE,
                item.getDjiStatus());


    }

    //TODO: After basic cases are running
    public void givenDownloadStartedWithoutFileAvailable_whenDownloadBecomesLocallyAvailable_shouldSwitchToDownloadLocally()
            throws InterruptedException {

        cloudServerDispatcher.setThrottle(512, 1,TimeUnit.SECONDS);

        entryStatusResponse.setAvailable(false);
        entryStatusResponse.setErId((int)
                clientDb.getEntryStatusResponseDao().insert(entryStatusResponse));

        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, clientDb, clientRepo,
                        cloudEndPoint, connectivityStatus);

        new Thread(jobItemRunner).start();

        CountDownLatch latch = new CountDownLatch(1);
        UmObserver<Integer> statusObserver = (status) -> {
            if(status == JobStatus.COMPLETE)
                latch.countDown();
        };
        UmLiveData<Integer> statusLiveData = clientDb.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid());
        statusLiveData.observeForever(statusObserver);

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_THREAD_SLEEP_TIME));

        entryStatusResponse.setAvailable(true);
        clientDb.getEntryStatusResponseDao().insert(entryStatusResponse);

        latch.await(100, TimeUnit.SECONDS);

        assertTrue("File downloaded from peer web server",
                mockPeerWebServer.getRequestCount() >= 1
                        && mockCloudWebServer.getRequestCount() >= 1);


        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                downloadJobItem.getDjiUid());
        assertEquals("File downloaded successfully",item.getDjiStatus(),
                JobStatus.COMPLETE);

        List<DownloadJobItemHistory> histories = clientDb.getDownloadJobItemHistoryDao().
                findHistoryItemsByDownloadJobItem(item.getDjiUid());

        assertTrue("First download request was sent to the cloud web server and " +
                        "last one was to peer web server",
                histories.get(0).getMode() == DownloadJobItemHistory.MODE_CLOUD
                        && histories.get(1).getMode() == DownloadJobItemHistory.MODE_LOCAL);


    }

    @Test
    public void givenDownloadLocallyAvailableFromBadNode_whenDownloadStarted_shouldDownloadFromCloud() {

        entryStatusResponse.setAvailable(true);
        clientDb.getEntryStatusResponseDao().insert(entryStatusResponse);

        List<DownloadJobItemHistory> historyList = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            DownloadJobItemHistory history = new DownloadJobItemHistory();
            history.setNetworkNode(networkNode.getNodeId());
            history.setStartTime(System.currentTimeMillis());
            history.setSuccessful(false);
            historyList.add(history);
        }
        clientDb.getDownloadJobItemHistoryDao().insertList(historyList);


        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, clientDb, clientRepo,
                        cloudEndPoint, connectivityStatus);

        new Thread(jobItemRunner).start();

        WaitForLiveData.observeUntil(clientDb.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid()), MAX_LATCH_WAITING_TIME, TimeUnit.SECONDS,
                status -> status == JobStatus.COMPLETE);

        assertTrue("File downloaded from cloud web server",
                mockCloudWebServer.getRequestCount() >=1);
        assertNull("Mock webserver for peer has not been started", mockPeerWebServer);

        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                downloadJobItem.getDjiUid());
        assertEquals("File downloaded successfully from cloud",item.getDjiStatus(),
                JobStatus.COMPLETE);
    }


    @Test
    public void givenAlreadyConnectedToPeerWithFile_whenDownloadStarts_shouldDownloadFromSamePeerWithoutReconnecting()
            throws IOException{

        startPeerWebServer();

        entryStatusResponse.setAvailable(true);
        clientDb.getEntryStatusResponseDao().insert(entryStatusResponse);

        networkNode.setGroupSsid("DIRECT-group");
        networkNode.setEndpointUrl(mockPeerWebServer.url("/").toString());
        clientDb.getNetworkNodeDao().update(networkNode);

        connectivityStatus.setConnectivityState(ConnectivityStatus.STATE_CONNECTED_LOCAL);
        connectivityStatus.setWifiSsid("DIRECT-group");
        clientDb.getConnectivityStatusDao().updateStateSync(ConnectivityStatus.STATE_CONNECTED_LOCAL,
                "DIRECT-group");


        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context, item, mockedNetworkManager, clientDb, clientRepo,
                        cloudEndPoint, connectivityStatus);

        jobItemRunner.run();

        assertTrue("File downloaded from peer web server",
                mockPeerWebServer.getRequestCount() >=1);
        assertEquals("Cloud mock server received no requests", 0 ,
                mockCloudWebServer.getRequestCount());

        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                downloadJobItem.getDjiUid());
        assertEquals("File downloaded successfully",JobStatus.COMPLETE,
                item.getDjiStatus());

        //should not have attempted to change the connection
        verify(mockedNetworkManager, times(0)).connectToWiFi(any(), any());
        verify(mockedNetworkManager, times(0)).sendMessage(any(), any(),
                any(), any());
    }


    @Test
    public void givenDownloadLocallyAvailable_whenAnErrorOccursSendingConnectBleMessage_shouldRetryAndDownloadFromCloud(){

        entryStatusResponse.setAvailable(true);
        clientDb.getEntryStatusResponseDao().insert(entryStatusResponse);

        mockedNetworkManagerBleWorking.set(false);

        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, clientDb, clientRepo,
                        cloudEndPoint, connectivityStatus);

        jobItemRunner.run();

        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                downloadJobItem.getDjiUid());

        assertEquals("File download task completed successfully",
                JobStatus.COMPLETE, item.getDjiStatus());

        assertEquals("Same file size", webServerTmpContentEntryFile.length(),
                new File(item.getDestinationFile()).length());

        assertTrue("File downloaded from cloud web server",
                mockCloudWebServer.getRequestCount() == 1
                        && mockPeerWebServer.getRequestCount() == 0);

        //Verify that group request credentials were requested
        verify(mockedNetworkManager, atLeast(2))
                .sendMessage(any(),any(),any(),any());

    }

    @Test
    public void givenDownloadLocallyAvailable_whenBleConnectMessageIsReturnedButWifiDoesNotConnect_shouldRetryAndDownloadFromCloud() {
        entryStatusResponse.setAvailable(true);
        clientDb.getEntryStatusResponseDao().insert(entryStatusResponse);

        mockedNetworkManagerWifiConnectWorking.set(false);

        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, clientDb,clientRepo,
                        cloudEndPoint, connectivityStatus);
        jobItemRunner.setWiFiConnectionTimeout(3);

        jobItemRunner.run();

        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                downloadJobItem.getDjiUid());

        assertEquals("File download task completed successfully",
                JobStatus.COMPLETE, item.getDjiStatus());

        assertEquals("Same file size", webServerTmpContentEntryFile.length(),
                new File(item.getDestinationFile()).length());

        assertTrue("File downloaded from cloud web server",
                mockCloudWebServer.getRequestCount() == 1
                        && mockPeerWebServer.getRequestCount() == 0);

        //verify that peer tried to connect to the WiFi
        verify(mockedNetworkManager, atLeast(2)).connectToWiFi(any(),any());
    }

    @Test
    public void givenDownloadLocallyAvailable_whenConnectsButDownloadFails_shouldDownloadFromCloud() {

        mockPeerServerDispatcher.setNumTimesToFail(4);

        entryStatusResponse.setAvailable(true);
        clientDb.getEntryStatusResponseDao().insert(entryStatusResponse);

        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());

        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, clientDb, clientRepo,
                        cloudEndPoint, connectivityStatus);


        new Thread(jobItemRunner).start();

        WaitForLiveData.observeUntil(clientDb.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid()), 1000, TimeUnit.SECONDS,
                status -> status != null && status == JobStatus.COMPLETE);


        assertTrue("File downloaded from cloud web server after failure when try " +
                        "to download from peer",
                mockCloudWebServer.getRequestCount() >=1 && mockPeerWebServer.getRequestCount()>=1);
        verify(mockedNetworkManager).restoreWifi();

        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                downloadJobItem.getDjiUid());

        assertEquals("File downloaded successfully from cloud",
                JobStatus.COMPLETE, item.getDjiStatus());
    }


    public void givenDownloadLocallyAvailableWhenDisconnected_whenPeerFailsRepeatedly_shouldStopAndSetStatus() throws InterruptedException {

        entryStatusResponse.setAvailable(true);
        clientDb.getEntryStatusResponseDao().insert(entryStatusResponse);

        DownloadJobItemWithDownloadSetItem item =
                clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        downloadJobItem.getDjiUid());
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, clientDb, clientRepo,
                        cloudEndPoint, connectivityStatus);

        doAnswer((invocation -> {
            clientDb.getConnectivityStatusDao().updateState(ConnectivityStatus.STATE_CONNECTING_LOCAL,null);
            return null;
        })).when(mockedNetworkManager).connectToWiFi(eq(groupBle.getSsid()), eq(groupBle.getPassphrase()));



        new Thread(jobItemRunner).start();

        CountDownLatch latch = new CountDownLatch(1);

        UmObserver<Integer> statusObserver = (status) -> {
            if(status == JobStatus.WAITING_FOR_CONNECTION)
                latch.countDown();
        };
        UmLiveData<Integer> statusLiveData = clientDb.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid());
        statusLiveData.observeForever(statusObserver);

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_THREAD_SLEEP_TIME));

        entryStatusResponse.setAvailable(true);
        clientDb.getEntryStatusResponseDao().insert(entryStatusResponse);

        latch.await(15, TimeUnit.SECONDS);


        //Verify that connection failed at least once when trying to connect to a peer
        verify(mockedNetworkManager, atLeast(1))
                .connectToWiFi(eq(groupBle.getSsid()),eq(groupBle.getPassphrase()));

        item = clientDb.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                downloadJobItem.getDjiUid());

        assertEquals("File failed to be downloaded from peer and status was updated",
                item.getDjiStatus(), JobStatus.WAITING_FOR_CONNECTION);
    }

}
