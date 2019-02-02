package com.ustadmobile.port.sharedse.networkmanager;

import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSetItem;
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

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

import static org.mockito.Mockito.spy;

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

    private Object context;

    private int defaultId = 1;

    private Dispatcher dispatcher = null;

    private static File webServerTmpDir;

    private static File webServerTmpContentEntryFile;

    private static final long TEST_CONTENT_ENTRY_UID = 1L;

    private static final String ENDPOINT_FILE_POSTFIX = "/ContentEntryFileServer/";

    private static final String TEST_FILE_RESOURCE_PATH =
            "/com/ustadmobile/port/sharedse/networkmanager/thelittlechicks.epub";

    private File downloadTmpDir;

    //Uid of the
    private long testDownloadJobItemUid;

    private class ContentEntryFileDispatcher extends Dispatcher {

        private int numTimesToFail;

        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

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
                            .setResponseCode(200);

                    return response;
                }catch(IOException e) {
                    //should not happen
                }


            }
            return new MockResponse().setResponseCode(404);
        }

        private void setNumTimesToFail(int numTimesToFail) {
            this.numTimesToFail = numTimesToFail;
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
                ""+TEST_CONTENT_ENTRY_UID);
        FileOutputStream fout = new FileOutputStream(webServerTmpContentEntryFile);
        UMIOUtils.readFully(resIn, fout);
        resIn.close();
        fout.close();
    }


    @Before
    public void setup() throws IOException {
        context =  PlatformTestUtil.getTargetContext();
        mockedNetworkManager = spy(NetworkManagerBle.class);

        downloadTmpDir = File.createTempFile("DownloadJobItemRunnerTest", "dldir");
        if(!(downloadTmpDir.delete() && downloadTmpDir.mkdirs())) {
            throw new IOException("Failed to create tmp directory" + downloadTmpDir);
        }

        UmAppDatabase.getInstance(context).clearAllTables();
        umAppDatabase = UmAppDatabase.getInstance(context);

        ContentEntry contentEntry = new ContentEntry();
        contentEntry.setTitle("Test entry");
        contentEntry.setContentEntryUid(umAppDatabase.getContentEntryDao().insert(contentEntry));

        ContentEntryFile contentEntryFile = new ContentEntryFile();
        contentEntryFile.setFileSize(webServerTmpContentEntryFile.length());
        contentEntryFile.setMimeType("application/epub+zip");
        umAppDatabase.getContentEntryFileDao().insert(contentEntryFile);

        umAppDatabase.getContentEntryContentEntryFileJoinDao().insert(
                new ContentEntryContentEntryFileJoin(contentEntry, contentEntryFile));

        DownloadSet downloadSet = new DownloadSet();
        downloadSet.setDestinationDir(downloadTmpDir.getAbsolutePath());
        downloadSet.setDsRootContentEntryUid(defaultId);
        downloadSet.setMeteredNetworkAllowed(false);
        downloadSet.setDsUid((int)umAppDatabase.getDownloadSetDao().insert(downloadSet));

        DownloadSetItem downloadSetItem = new DownloadSetItem(downloadSet, contentEntry);
        downloadSetItem.setDsiUid((int)umAppDatabase.getDownloadSetItemDao().insert(downloadSetItem));

        DownloadJob downloadJob = new DownloadJob(downloadSet);
        downloadJob.setTimeCreated(System.currentTimeMillis());
        downloadJob.setTimeRequested(System.currentTimeMillis());
        downloadJob.setDjStatus(JobStatus.QUEUED);
        downloadJob.setDjUid((int)umAppDatabase.getDownloadJobDao().insert(downloadJob));

        DownloadJobItem downloadJobItem = new DownloadJobItem(downloadJob, downloadSetItem);
        downloadJobItem.setDjiStatus(JobStatus.QUEUED);
        downloadJobItem.setDownloadedSoFar(0);
        downloadJobItem.setDestinationFile(new File(downloadTmpDir,
                String.valueOf(TEST_CONTENT_ENTRY_UID)).getAbsolutePath());
        downloadJobItem.setDjiContentEntryFileUid(contentEntryFile.getContentEntryFileUid());
        testDownloadJobItemUid = umAppDatabase.getDownloadJobItemDao().insert(downloadJobItem);

        dispatcher =  new ContentEntryFileDispatcher();

        mockWebServer = new MockWebServer();
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
                new DownloadJobItemRunner(item, mockedNetworkManager, umAppDatabase, endPoint);

        jobItemRunner.run();

        Assert.assertEquals("Same file size", webServerTmpContentEntryFile.length(),
                new File(item.getDestinationFile()).length());
    }

    @Test
    public void givenDownloadStarted_whenFailsOnce_shouldRetryAndComplete() {

    }

    @Test
    public void givenDownloadStarted_whenFailsExceedMaxAttempts_shouldStopAndSetStatusToFailed() {

    }

    @Test
    public void givenDownloadUnmeteredConnectivityOnly_whenConnectivitySwitchesToMetered_shouldStopAndSetStatusToWaiting() {

    }

    @Test
    public void givenDownloadStarted_whenJobIsStopped_shouldStopAndSetStatus() {

    }


}
