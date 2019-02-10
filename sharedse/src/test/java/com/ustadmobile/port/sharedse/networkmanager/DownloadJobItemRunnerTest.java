package com.ustadmobile.port.sharedse.networkmanager;

import com.google.gson.Gson;
import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryFileStatus;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSetItem;
import com.ustadmobile.lib.db.entities.EntryStatusResponse;
import com.ustadmobile.lib.db.entities.NetworkNode;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class which tests {@link DownloadJobItemRunner} behaves as expected when downloading
 * content entries.
 *
 * @author Kileha3
 */
public class DownloadJobItemRunnerTest {

    private MockWebServer mockCloudWebServer;

    private MockWebServer mockPeerWebServer;

    private NetworkManagerBle mockedNetworkManager;

    private UmAppDatabase umAppDatabase;

    private String cloudEndPoint, localEndPoint;

    private ContentEntryFileDispatcher cloudServerDispatcher = null;

    private ContentEntryFileDispatcher mockPeerServerDispatcher = null;

    private static File webServerTmpDir;

    private static File webServerTmpContentEntryFile;

    private static final long TEST_CONTENT_ENTRY_FILE_UID = 1000L;

    private static final String ENDPOINT_FILE_POSTFIX = "/ContentEntryFileServer/";

    private static final String TEST_FILE_RESOURCE_PATH =
            "/com/ustadmobile/port/sharedse/networkmanager/thelittlechicks.epub";

    private  Object context;


    private EntryStatusResponse entryStatusResponse;

    private DownloadJobItemHistory history;

    private BleEntryStatusTask mockedEntryStatusTask;

    private NetworkNode networkNode;

    private BleMessage wifiDirectGroupInfoMessage;

    private WiFiDirectGroupBle groupBle;


    //Uid of the
    private long testDownloadJobItemUid;

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
    }


    @Before
    public void setup() throws IOException {
        context = PlatformTestUtil.getTargetContext();
        mockedNetworkManager = spy(NetworkManagerBle.class);

        mockedEntryStatusTask = mock(BleEntryStatusTask.class);
        mockedNetworkManager.setContext(context);

        groupBle =  new WiFiDirectGroupBle("networkSSID","networkPass123");


        File downloadTmpDir = File.createTempFile("DownloadJobItemRunnerTest", "dldir");
        if(!(downloadTmpDir.delete() && downloadTmpDir.mkdirs())) {
            throw new IOException("Failed to create tmp directory" + downloadTmpDir);
        }

        UmAppDatabase.getInstance(context).clearAllTables();
        umAppDatabase = UmAppDatabase.getInstance(context);
        networkNode = new NetworkNode();
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4F");
        networkNode.setLastUpdateTimeStamp(System.currentTimeMillis());
        networkNode.setNodeId(umAppDatabase.getNetworkNodeDao().insert(networkNode));

        ContentEntry contentEntry = new ContentEntry();
        contentEntry.setTitle("Test entry");
        contentEntry.setContentEntryUid(umAppDatabase.getContentEntryDao().insert(contentEntry));

        ContentEntryFile contentEntryFile = new ContentEntryFile();
        contentEntryFile.setContentEntryFileUid(TEST_CONTENT_ENTRY_FILE_UID);
        contentEntryFile.setFileSize(webServerTmpContentEntryFile.length());
        contentEntryFile.setMimeType("application/epub+zip");
        umAppDatabase.getContentEntryFileDao().insert(contentEntryFile);

        umAppDatabase.getContentEntryContentEntryFileJoinDao().insert(
                new ContentEntryContentEntryFileJoin(contentEntry, contentEntryFile));

        DownloadSet downloadSet = new DownloadSet();
        downloadSet.setDestinationDir(downloadTmpDir.getAbsolutePath());
        downloadSet.setDsRootContentEntryUid(0L);
        downloadSet.setDsUid((int)umAppDatabase.getDownloadSetDao().insert(downloadSet));

        DownloadSetItem downloadSetItem = new DownloadSetItem(downloadSet, contentEntry);
        downloadSetItem.setDsiUid((int)umAppDatabase.getDownloadSetItemDao().insert(downloadSetItem));

        DownloadJob downloadJob = new DownloadJob(downloadSet);
        downloadJob.setTimeCreated(System.currentTimeMillis());
        downloadJob.setTimeRequested(System.currentTimeMillis());
        downloadJob.setDjStatus(JobStatus.QUEUED);
        downloadJob.setDjUid((int)umAppDatabase.getDownloadJobDao().insert(downloadJob));

        DownloadJobItem downloadJobItem = new DownloadJobItem(downloadJob, downloadSetItem,
                contentEntryFile);
        downloadJobItem.setDjiStatus(JobStatus.QUEUED);
        downloadJobItem.setDownloadedSoFar(0);
        downloadJobItem.setDestinationFile(new File(downloadTmpDir,
                String.valueOf(TEST_CONTENT_ENTRY_FILE_UID)).getAbsolutePath());
        downloadJobItem.setDjiContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
        testDownloadJobItemUid = umAppDatabase.getDownloadJobItemDao().insert(downloadJobItem);


        ConnectivityStatus connectivityStatus = new ConnectivityStatus();
        connectivityStatus.setConnectedOrConnecting(true);
        connectivityStatus.setConnectivityState(ConnectivityStatus.STATE_UNMETERED);
        umAppDatabase.getConnectivityStatusDao().insert(connectivityStatus);

        entryStatusResponse = new EntryStatusResponse();
        entryStatusResponse.setErContentEntryFileUid(downloadJobItem.getDjiContentEntryFileUid());
        entryStatusResponse.setErNodeId(networkNode.getNodeId());
        entryStatusResponse.setAvailable(true);
        entryStatusResponse.setResponseTime(System.currentTimeMillis());


        history = new DownloadJobItemHistory();
        history.setNetworkNode(networkNode.getNodeId());
        history.setStartTime(System.currentTimeMillis());
        history.setSuccessful(true);
        umAppDatabase.getDownloadJobItemHistoryDao().insert(history);

        cloudServerDispatcher =  new ContentEntryFileDispatcher();
        mockCloudWebServer = new MockWebServer();
        mockCloudWebServer.setDispatcher(cloudServerDispatcher);
        mockCloudWebServer.start();
        cloudEndPoint = mockCloudWebServer.url("/").toString();


        when(mockedNetworkManager.makeEntryStatusTask(any(Object.class),
                any(),any(NetworkNode.class))).thenReturn(mockedEntryStatusTask);


        doAnswer(invocation -> {
            BleMessageResponseListener bleResponseListener = invocation.getArgument(3);
            startPeerWebServer();
            Thread.sleep(1);
            int mockPort = mockPeerWebServer.getPort();
            groupBle.setEndpoint(localEndPoint);
            wifiDirectGroupInfoMessage = new BleMessage(WIFI_GROUP_CREATION_RESPONSE,
                    new Gson().toJson(groupBle).getBytes());

            bleResponseListener.onResponseReceived(networkNode.getBluetoothMacAddress(), wifiDirectGroupInfoMessage);

            return null;
        }).when(mockedNetworkManager).sendMessage(any(Object.class), any(BleMessage.class),
                any(NetworkNode.class), any(BleMessageResponseListener.class));

        doAnswer((invocation -> {
            umAppDatabase.getConnectivityStatusDao().update(ConnectivityStatus.STATE_CONNECTING_LOCAL);
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            umAppDatabase.getConnectivityStatusDao().update(ConnectivityStatus.STATE_CONNECTED_LOCAL, groupBle.getSsid());
            return null;
        })).when(mockedNetworkManager).connectToWiFi(eq(groupBle.getSsid()), eq(groupBle.getPassphrase()));

    }

    @After
    public void tearDown() throws IOException{
        if(mockCloudWebServer != null){
            mockCloudWebServer.shutdown();
        }
        if(mockPeerWebServer != null){
            mockPeerWebServer.shutdown();
        }
    }

    private void startPeerWebServer() throws IOException {
        if(mockPeerWebServer == null){
            mockPeerServerDispatcher = new ContentEntryFileDispatcher();
            mockPeerWebServer = new MockWebServer();
            mockPeerWebServer.setDispatcher(mockPeerServerDispatcher);
            mockPeerWebServer.start();
            localEndPoint = mockPeerWebServer.url("/").toString();
        }
    }


    @Test
    public void givenDownload_whenRun_shouldDownloadAndComplete() {

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, cloudEndPoint);

        jobItemRunner.run();

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);

        assertEquals("File download task completed successfully",
                item.getDjiStatus(), JobStatus.COMPLETE);

        assertEquals("Same file size", webServerTmpContentEntryFile.length(),
                new File(item.getDestinationFile()).length());

        ContentEntryFileStatus status = umAppDatabase.getContentEntryFileStatusDao()
                .findByContentEntryFileUid(item.getDjiContentEntryFileUid());

        assertNotNull("File status were updated successfully",status);

        assertEquals("Local file path is the same",
               item.getDestinationFile(),  status.getFilePath());
    }

    @Test
    public void givenDownloadStarted_whenFailsOnce_shouldRetryAndComplete() {

        cloudServerDispatcher.setNumTimesToFail(1);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, cloudEndPoint);

        jobItemRunner.run();

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                (int)testDownloadJobItemUid);

        assertEquals("File download task retried and completed successfully",
                item.getDjiStatus(),JobStatus.COMPLETE);

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
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, cloudEndPoint);

        jobItemRunner.run();

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                (int)testDownloadJobItemUid);

        assertEquals("File download task retried and completed with failure status",
                item.getDjiStatus(),JobStatus.FAILED);
    }

    @Test
    public void givenDownloadUnmeteredConnectivityOnly_whenConnectivitySwitchesToMetered_shouldStopAndSetStatusToWaiting()
            throws InterruptedException {

        cloudServerDispatcher.setThrottle(512, 1,TimeUnit.SECONDS);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, cloudEndPoint);

        new Thread(jobItemRunner).start();

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        umAppDatabase.getConnectivityStatusDao().update(ConnectivityStatus.STATE_METERED);

        CountDownLatch latch = new CountDownLatch(1);
        UmObserver<Integer> itemStatusObserver = (newStatus) -> {
            if(newStatus == JobStatus.WAITING_FOR_CONNECTION)
                latch.countDown();
        };
        UmLiveData<Integer> statusLiveData = umAppDatabase.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid());
        statusLiveData.observeForever(itemStatusObserver);

        try { latch.await(3, TimeUnit.SECONDS); }
        catch(InterruptedException e) {
            //should not happen
        }

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                (int)testDownloadJobItemUid);

        assertEquals("File download task stopped after network status " +
                        "change and set status to waiting", item.getDjiStatus(),
                JobStatus.WAITING_FOR_CONNECTION);

    }

    @Test
    public void givenDownloadStarted_whenJobIsStopped_shouldStopAndSetStatus()
            throws InterruptedException {

        cloudServerDispatcher.setThrottle(512, 1,TimeUnit.SECONDS);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, cloudEndPoint);

        new Thread(jobItemRunner).start();

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        umAppDatabase.getDownloadJobItemDao().updateStatus(item.getDjiUid(),JobStatus.STOPPING);

        CountDownLatch latch = new CountDownLatch(1);
        UmObserver<Integer> itemStatusObserver = (newStatus) -> {
            if(newStatus == JobStatus.STOPPED)
                latch.countDown();
        };
        UmLiveData<Integer> statusLiveData = umAppDatabase.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid());
        statusLiveData.observeForever(itemStatusObserver);

        try { latch.await(3, TimeUnit.SECONDS); }
        catch(InterruptedException e) {
            //should not happen
        }

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                (int)testDownloadJobItemUid);

        assertEquals("File download job was stopped and status was updated",
                item.getDjiStatus(), JobStatus.STOPPED);
    }

    @Test
    public void givenDownloadStartsOnMeteredConnection_whenJobSetChangedToDisableMeteredConnection_shouldStopAndSetStatus()
            throws InterruptedException {

        cloudServerDispatcher.setThrottle(512, 1,TimeUnit.SECONDS);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, cloudEndPoint);

        umAppDatabase.getConnectivityStatusDao().update(ConnectivityStatus.STATE_METERED);
        umAppDatabase.getDownloadSetDao().setMeteredConnectionBySetUid(
                item.getDownloadSetItem().getDsiDsUid(),true);

        new Thread(jobItemRunner).start();

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        CountDownLatch latch = new CountDownLatch(1);
        UmObserver<Integer> statusObserver = (status) -> {
            if(status == JobStatus.WAITING_FOR_CONNECTION)
                latch.countDown();
        };


        UmLiveData<Integer> statusLiveData = umAppDatabase.getDownloadJobItemDao()
                .getLiveStatus(item.getDjiUid());

        statusLiveData.observeForever(statusObserver);

        umAppDatabase.getDownloadSetDao().setMeteredConnectionBySetUid(
                item.getDownloadSetItem().getDsiDsUid(),false);

        try { latch.await(3, TimeUnit.SECONDS); }
        catch(InterruptedException e) {
            //should not happen
        }

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                (int)testDownloadJobItemUid);

        assertEquals("File download job is waiting for network after changing download" +
                        " set setting to use unmetered connection only",
                item.getDjiStatus(), JobStatus.WAITING_FOR_CONNECTION);

    }

    @Test
    public void givenDownloadStarted_whenConnectionGoesOff_shouldStopAndSetStatusToWaiting()
            throws InterruptedException {

        cloudServerDispatcher.setThrottle(512, 1,TimeUnit.SECONDS);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, cloudEndPoint);

        new Thread(jobItemRunner).start();

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        umAppDatabase.getConnectivityStatusDao().update(ConnectivityStatus.STATE_DISCONNECTED);

        CountDownLatch latch = new CountDownLatch(1);
        UmObserver<Integer> itemStatusObserver = (newStatus) -> {
            if(newStatus == JobStatus.WAITING_FOR_CONNECTION)
                latch.countDown();
        };
        UmLiveData<Integer> statusLiveData = umAppDatabase.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid());
        statusLiveData.observeForever(itemStatusObserver);

        try { latch.await(3, TimeUnit.SECONDS); }
        catch(InterruptedException e) {
            //should not happen
        }

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                (int)testDownloadJobItemUid);

        assertEquals("File download job is waiting for network after the network goes off",
                item.getDjiStatus(), JobStatus.WAITING_FOR_CONNECTION);
    }


    @Test
    public void givenDownloadLocallyAvailable_whenDownloadStarted_shouldDownloadFromLocalNode()
            throws InterruptedException {

        entryStatusResponse.setAvailable(true);
        umAppDatabase.getEntryStatusResponseDao().insert(entryStatusResponse);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, cloudEndPoint);

        new Thread(jobItemRunner).start();


        CountDownLatch latch = new CountDownLatch(1);
        UmObserver<Integer> itemStatusObserver = (newStatus) -> {
            if(newStatus == JobStatus.COMPLETE)
                latch.countDown();
        };

        UmLiveData<Integer> statusLiveData = umAppDatabase.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid());
        statusLiveData.observeForever(itemStatusObserver);

        latch.await(6, TimeUnit.SECONDS);

        assertTrue("File downloaded from peer web server",
                mockPeerWebServer.getRequestCount() >=1);
        assertEquals("Cloud mock server received no requests", 0 ,
                mockCloudWebServer.getRequestCount());

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        assertEquals("File downloaded successfully",item.getDjiStatus(),
                JobStatus.COMPLETE);


    }

    @Test
    public void givenDownloadStartedWithoutFileAvailable_whenDownloadBecomesLocallyAvailable_shouldSwitchToDownloadLocally()
            throws InterruptedException {

        cloudServerDispatcher.setThrottle(512, 1,TimeUnit.SECONDS);

        entryStatusResponse.setAvailable(false);
        entryStatusResponse.setErId((int)
                umAppDatabase.getEntryStatusResponseDao().insert(entryStatusResponse));

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, cloudEndPoint);

        new Thread(jobItemRunner).start();

        CountDownLatch latch = new CountDownLatch(1);
        UmObserver<Integer> statusObserver = (status) -> {
            if(status == JobStatus.COMPLETE)
                latch.countDown();
        };
        UmLiveData<Integer> statusLiveData = umAppDatabase.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid());
        statusLiveData.observeForever(statusObserver);

        Thread.sleep(TimeUnit.SECONDS.toMillis(3));

        entryStatusResponse.setAvailable(true);
        umAppDatabase.getEntryStatusResponseDao().insert(entryStatusResponse);

        latch.await(100, TimeUnit.SECONDS);

        assertTrue("File downloaded from peer web server",
                mockPeerWebServer.getRequestCount() >= 1
                        && mockCloudWebServer.getRequestCount() >= 1);


        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                (int)testDownloadJobItemUid);
        assertEquals("File downloaded successfully",item.getDjiStatus(),
                JobStatus.COMPLETE);

        List<DownloadJobItemHistory> histories = umAppDatabase.getDownloadJobItemHistoryDao().
                findHistoryItemsByDownloadJobItem(item.getDjiUid());

        assertTrue("First download request was sent to the cloud web server and " +
                        "last one was to peer web server",
                histories.get(0).getMode() == DownloadJobItemHistory.MODE_CLOUD
                        && histories.get(1).getMode() == DownloadJobItemHistory.MODE_LOCAL);


    }

    @Test
    public void givenDownloadLocallyAvailableFromBadNode_whenDownloadStarted_shouldDownloadFromCloud()
            throws InterruptedException {

        when(mockedNetworkManager.makeEntryStatusTask(any(Object.class),
                any(BleMessage.class),any(NetworkNode.class),
                any(BleMessageResponseListener.class))).thenReturn(mockedEntryStatusTask);

        entryStatusResponse.setAvailable(true);
        umAppDatabase.getEntryStatusResponseDao().insert(entryStatusResponse);

        //add failure history (bad not)
        for(int i = 0; i < 4; i++){
            history.setSuccessful(false);
            umAppDatabase.getDownloadJobItemHistoryDao().insert(history);
        }


        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, cloudEndPoint);

        new Thread(jobItemRunner).start();


        CountDownLatch latch = new CountDownLatch(1);
        UmObserver<Integer> statusObserver = (status) -> {
            if(status == JobStatus.COMPLETE)
                latch.countDown();
        };
        UmLiveData<Integer> statusLiveData = umAppDatabase.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid());
        statusLiveData.observeForever(statusObserver);

        latch.await(10, TimeUnit.SECONDS);

        assertTrue("File downloaded from cloud web server",
                mockCloudWebServer.getRequestCount() >=1 && mockPeerWebServer == null);

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                (int)testDownloadJobItemUid);
        assertEquals("File downloaded successfully from cloud",item.getDjiStatus(),
                JobStatus.COMPLETE);
    }


    @Test
    public void givenDownloadLocallyAvailableWhenConnected_whenPeerFailsRepeatedly_shouldDownloadFromCloud()
            throws Exception {

        startPeerWebServer();

        mockPeerServerDispatcher.setNumTimesToFail(4);

        entryStatusResponse.setAvailable(true);
        umAppDatabase.getEntryStatusResponseDao().insert(entryStatusResponse);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);

        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, cloudEndPoint);


        new Thread(jobItemRunner).start();

        CountDownLatch latch = new CountDownLatch(1);

        UmObserver<Integer> statusObserver = (status) -> {
            if(status == JobStatus.COMPLETE)
                latch.countDown();
        };

        UmLiveData<Integer> statusLiveData = umAppDatabase.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid());
        statusLiveData.observeForever(statusObserver);

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        entryStatusResponse.setAvailable(true);
        umAppDatabase.getEntryStatusResponseDao().insert(entryStatusResponse);

        latch.await(10, TimeUnit.SECONDS);


        assertTrue("File downloaded from cloud web server after failure when try " +
                        "to download from peer",
                mockPeerWebServer.getRequestCount() >=1 && mockPeerWebServer.getRequestCount()>=1);

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                (int)testDownloadJobItemUid);

        assertEquals("File downloaded successfully from cloud",
                item.getDjiStatus(), JobStatus.COMPLETE);

    }

    @Test
    public void givenDownloadLocallyAvailableWhenDisconnected_whenPeerFailsRepeatedly_shouldStopAndSetStatus() throws InterruptedException {

        entryStatusResponse.setAvailable(true);
        umAppDatabase.getEntryStatusResponseDao().insert(entryStatusResponse);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, cloudEndPoint);

        doAnswer((invocation -> {
            umAppDatabase.getConnectivityStatusDao().update(ConnectivityStatus.STATE_CONNECTING_LOCAL);
            return null;
        })).when(mockedNetworkManager).connectToWiFi(eq(groupBle.getSsid()), eq(groupBle.getPassphrase()));



        new Thread(jobItemRunner).start();

        CountDownLatch latch = new CountDownLatch(1);

        UmObserver<Integer> statusObserver = (status) -> {
            if(status == JobStatus.WAITING_FOR_CONNECTION)
                latch.countDown();
        };
        UmLiveData<Integer> statusLiveData = umAppDatabase.getDownloadJobItemDao().getLiveStatus(
                item.getDjiUid());
        statusLiveData.observeForever(statusObserver);

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        entryStatusResponse.setAvailable(true);
        umAppDatabase.getEntryStatusResponseDao().insert(entryStatusResponse);

        latch.await(15, TimeUnit.SECONDS);


        //Verify that connection failed at least once when trying to connect to a peer
        verify(mockedNetworkManager, atLeast(1))
                .connectToWiFi(eq(groupBle.getSsid()),eq(groupBle.getPassphrase()));

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                (int)testDownloadJobItemUid);

        assertEquals("File failed to be downloaded from peer and status was updated",
                item.getDjiStatus(), JobStatus.WAITING_FOR_CONNECTION);
    }

}
