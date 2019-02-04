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

import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS;
import static com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle.WIFI_GROUP_CREATION_RESPONSE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

    private MockWebServer mockWebServer;

    private NetworkManagerBle mockedNetworkManager;

    private UmAppDatabase umAppDatabase;

    private String endPoint;

    private ContentEntryFileDispatcher dispatcher = null;

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

    private BleMessage message;

    private String ssId = "networkSSID", passphrase = "networkPass123";

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


        File downloadTmpDir = File.createTempFile("DownloadJobItemRunnerTest", "dldir");
        if(!(downloadTmpDir.delete() && downloadTmpDir.mkdirs())) {
            throw new IOException("Failed to create tmp directory" + downloadTmpDir);
        }

        UmAppDatabase.getInstance(context).clearAllTables();
        umAppDatabase = UmAppDatabase.getInstance(context);
        networkNode = new NetworkNode();
        networkNode.setBluetoothMacAddress("00:3F:2F:64:C6:4F");
        networkNode.setIpAddress("198.10.10.56");
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

        NetworkManagerBle.WiFiP2PGroupResponse response =
                new NetworkManagerBle.WiFiP2PGroupResponse();
        response.setPort(234);
        response.setGroupSsid(ssId);
        response.setGroupPassphrase(passphrase);
        message = new BleMessage(WIFI_GROUP_CREATION_RESPONSE,
                new Gson().toJson(response).getBytes());


        dispatcher =  new ContentEntryFileDispatcher();
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);
        mockWebServer.start();
        endPoint = mockWebServer.url("/").toString();
    }

    @After
    public void tearDown() throws IOException{
        mockWebServer.shutdown();
    }


    @Test
    public void givenDownload_whenRun_shouldDownloadAndComplete() {

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, endPoint);

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

        dispatcher.setNumTimesToFail(1);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, endPoint);

        jobItemRunner.run();

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                (int)testDownloadJobItemUid);

        assertEquals("File download task retried and completed successfully",
                item.getDjiStatus(),JobStatus.COMPLETE);

        assertEquals("Same file size", webServerTmpContentEntryFile.length(),
                new File(item.getDestinationFile()).length());

        assertEquals("Number of attempts = 2", 2, item.getNumAttempts());
        assertEquals("Number of file get requests = 2", 2,
                dispatcher.getNumFileGetRequests());

    }

    @Test
    public void givenDownloadStarted_whenFailsExceedMaxAttempts_shouldStopAndSetStatusToFailed() {
        dispatcher.setNumTimesToFail(4);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, endPoint);

        jobItemRunner.run();

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                (int)testDownloadJobItemUid);

        assertEquals("File download task retried and completed with failure status",
                item.getDjiStatus(),JobStatus.FAILED);
    }

    @Test
    public void givenDownloadUnmeteredConnectivityOnly_whenConnectivitySwitchesToMetered_shouldStopAndSetStatusToWaiting() throws InterruptedException {

        dispatcher.setThrottle(512, 1,TimeUnit.SECONDS);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, endPoint);

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
    public void givenDownloadStarted_whenJobIsStopped_shouldStopAndSetStatus() throws InterruptedException {
        dispatcher.setThrottle(512, 1,TimeUnit.SECONDS);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, endPoint);

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
    public void givenDownloadStartsOnMeteredConnection_whenJobSetChangedToDisableMeteredConnection_shouldStopAndSetStatus() throws InterruptedException {
        dispatcher.setThrottle(512, 1,TimeUnit.SECONDS);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, endPoint);

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
    public void givenDownloadStarted_whenConnectionGoesOff_shouldStopAndSetStatusToWaiting() throws InterruptedException {
        dispatcher.setThrottle(512, 1,TimeUnit.SECONDS);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, endPoint);

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
    public void givenDownloadLocallyAvailable_whenDownloadStarted_shouldDownloadFromLocalNode() {

        when(mockedNetworkManager.makeEntryStatusTask(any(Object.class),
                any(BleMessage.class),any(NetworkNode.class),
                any(BleMessageResponseListener.class))).thenReturn(mockedEntryStatusTask);

        entryStatusResponse.setAvailable(true);
        umAppDatabase.getEntryStatusResponseDao().insert(entryStatusResponse);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, endPoint);

        jobItemRunner.run();

        //Verify that network group creation request was sent
        verify(mockedNetworkManager, times(1))
                .sendMessage(any(Object.class), any(BleMessage.class),any(NetworkNode.class),
                        any(BleMessageResponseListener.class));
    }

    @Test
    public void givenDownloadStartedWithoutFileAvailable_whenDownloadBecomesLocallyAvailable_shouldSwitchToDownloadLocally() throws InterruptedException {
        when(mockedNetworkManager.makeEntryStatusTask(any(Object.class),
                any(BleMessage.class),any(NetworkNode.class),
                any(BleMessageResponseListener.class))).thenReturn(mockedEntryStatusTask);

        dispatcher.setThrottle(512, 1,TimeUnit.SECONDS);

        entryStatusResponse.setAvailable(false);
        entryStatusResponse.setErId((int)
                umAppDatabase.getEntryStatusResponseDao().insert(entryStatusResponse));

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, endPoint);

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

        latch.await(3, TimeUnit.SECONDS);

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                (int)testDownloadJobItemUid);

        assertEquals("File download job is waiting for network after file status change",
                item.getDjiStatus(), JobStatus.WAITING_FOR_CONNECTION);


        //Verify that network group creation request was sent
        verify(mockedNetworkManager, times(1))
                .sendMessage(any(Object.class), any(BleMessage.class),any(NetworkNode.class),
                        any(BleMessageResponseListener.class));


    }

    @Test
    public void givenDownloadLocallyAvailableFromBadNode_whenDownloadStarted_shouldDownloadFromCloud() {

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
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, endPoint);

        jobItemRunner.run();

        //Verify that file was downloaded from cloud
        verify(mockedNetworkManager, times(0))
                .sendMessage(any(Object.class), any(BleMessage.class),any(NetworkNode.class),
                        any(BleMessageResponseListener.class));
    }


    @Test
    public void givenDownloadLocallyAvailableWhenConnected_whenPeerFailsRepeatedly_shouldDownloadFromCloud()
            throws InterruptedException {

        entryStatusResponse.setAvailable(true);
        umAppDatabase.getEntryStatusResponseDao().insert(entryStatusResponse);

        DownloadJobItemWithDownloadSetItem item =
                umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                        (int)testDownloadJobItemUid);
        DownloadJobItemRunner jobItemRunner =
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, endPoint);

        //Handle group connection response
        doAnswer(invocation -> {
            Thread.sleep(3);
            jobItemRunner.onConnected(networkNode.getIpAddress(),ssId);
            return null;
        }).when(mockedNetworkManager).connectToWiFi(eq(ssId),eq(passphrase),
                any(WiFiDirectConnectionListener.class));


        //Handle group connection request
        doAnswer(invocation -> {
            if(mockedNetworkManager.getWifiDirectGroupChangeStatus() ==
                    WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS){
                jobItemRunner.onResponseReceived(networkNode.getBluetoothMacAddress(),message);
            }
            return null;
        }).when(mockedNetworkManager).sendMessage(any(Object.class)
                ,any(BleMessage.class),any(NetworkNode.class),any(BleMessageResponseListener.class));


        mockedNetworkManager.setWifiDirectGroupChangeStatus(WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS);

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

        latch.await(20, TimeUnit.SECONDS);


        //Verify that connection failed at least once when trying to connect to a peer
        verify(mockedNetworkManager, times(1))
                .sendMessage(any(Object.class), any(BleMessage.class),any(NetworkNode.class),
                        any(BleMessageResponseListener.class));

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
                new DownloadJobItemRunner(context,item, mockedNetworkManager, umAppDatabase, endPoint);

        //Handle group connection response
        doAnswer(invocation -> {
            Thread.sleep(3);
            jobItemRunner.onFailure(ssId);
            return null;
        }).when(mockedNetworkManager).connectToWiFi(eq(ssId),eq(passphrase),
                any(WiFiDirectConnectionListener.class));


        //Handle group connection request
        doAnswer(invocation -> {
            if(mockedNetworkManager.getWifiDirectGroupChangeStatus() ==
                    WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS){
                jobItemRunner.onResponseReceived(networkNode.getBluetoothMacAddress(),message);
            }
            return null;
        }).when(mockedNetworkManager).sendMessage(any(Object.class)
                ,any(BleMessage.class),any(NetworkNode.class),any(BleMessageResponseListener.class));


        mockedNetworkManager.setWifiDirectGroupChangeStatus(WIFI_DIRECT_GROUP_UNDER_CREATION_STATUS);

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

        latch.await(10, TimeUnit.SECONDS);


        //Verify that connection failed at least once when trying to connect to a peer
        verify(mockedNetworkManager, atLeast(2))
                .sendMessage(any(Object.class), any(BleMessage.class),any(NetworkNode.class),
                        any(BleMessageResponseListener.class));

        item = umAppDatabase.getDownloadJobItemDao().findWithDownloadSetItemByUid(
                (int)testDownloadJobItemUid);

        assertEquals("File failed to be downloaded from peer and ",
                item.getDjiStatus(), JobStatus.WAITING_FOR_CONNECTION);
    }

}
