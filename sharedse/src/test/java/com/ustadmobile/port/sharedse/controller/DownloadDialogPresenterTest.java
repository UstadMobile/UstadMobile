package com.ustadmobile.port.sharedse.controller;


import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.WaitForLiveData;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.networkmanager.DownloadJobItemManager;
import com.ustadmobile.lib.database.jdbc.DriverConnectionPoolInitializer;
import com.ustadmobile.lib.db.entities.Container;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.port.sharedse.networkmanager.DeleteJobTaskRunner;
import com.ustadmobile.port.sharedse.networkmanager.DownloadJobPreparer;
import com.ustadmobile.port.sharedse.networkmanager.NetworkManagerBle;
import com.ustadmobile.port.sharedse.view.DownloadDialogView;
import com.ustadmobile.sharedse.SharedSeTestConfig;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.verification.VerificationMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.ustadmobile.port.sharedse.controller.DownloadDialogPresenter.ARG_CONTENT_ENTRY_UID;
import static com.ustadmobile.port.sharedse.controller.DownloadDialogPresenter.STACKED_BUTTON_CANCEL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class which tests {@link DownloadDialogPresenter} to make sure it behaves as expected
 * when creating and managing download set, set items, jobs and job items.
 *
 * @author kileha3
 */

public class DownloadDialogPresenterTest {

    private DownloadDialogView mockedDialogView;

    private DownloadDialogPresenter presenter;

    private UmAppDatabase umAppDatabase;

    private UmAppDatabase umAppDatabaseRepo;

    private Object context;

    private ContentEntry rootEntry;

    private DownloadJob downloadJob;

    @Deprecated
    private DownloadSet downloadSet;

    private Container container;

    private long totalBytesToDownload = 0L;

    private static final int MAX_LATCH_WAITING_TIME = 15;

    private static final int MAX_THREAD_SLEEP_TIME = 2;

    private NetworkManagerBle mockedNetworkManager;

    private DeleteJobTaskRunner mockedDeleteTaskRunner = null;

    public static long TEST_ROOT_CONTENT_ENTRY_UID;

    @Before
    public void setUp() throws IOException {
        DriverConnectionPoolInitializer.bindDataSource("UmAppDatabase",
                SharedSeTestConfig.TESTDB_JDBCURL_UMMAPPDATABASE, true);
        context = PlatformTestUtil.getTargetContext();
        mockedDialogView = mock(DownloadDialogView.class);
        mockedDeleteTaskRunner = spy(DeleteJobTaskRunner.class);
        doAnswer((invocationOnMock) -> {
            new Thread(((Runnable)invocationOnMock.getArgument(0))).start();
            return null;
        }).when(mockedDialogView).runOnUiThread(any());


        umAppDatabase = UmAppDatabase.getInstance(context);
        umAppDatabase.clearAllTables();

        umAppDatabaseRepo = umAppDatabase.getRepository("http://localhost/dummy/",
                "");

        EmbeddedHTTPD httpd = new EmbeddedHTTPD(0,context);
        httpd.start();
        mockedNetworkManager = spy(NetworkManagerBle.class);
        mockedNetworkManager.setContext(context);
        mockedNetworkManager.onCreate();

        when(mockedNetworkManager.getHttpd()).thenReturn(httpd);

        when(mockedNetworkManager.makeDeleteJobTask(any(),any()))
                .thenReturn(mockedDeleteTaskRunner);


        rootEntry = new ContentEntry("Lorem ipsum title",
                "Lorem ipsum description",false,true);
        rootEntry.setContentEntryUid(umAppDatabase.getContentEntryDao().insert(rootEntry));
        TEST_ROOT_CONTENT_ENTRY_UID = rootEntry.getContentEntryUid();


        container = new Container();
        container.setContainerContentEntryUid(rootEntry.getContentEntryUid());
        container.setLastModified(System.currentTimeMillis());
        container.setFileSize(0);
        container.setContainerUid(umAppDatabase.getContainerDao().insert(container));

        ContentEntry entry2 = new ContentEntry("title 2", "title 2", true, true);
        ContentEntry entry3 = new ContentEntry("title 2", "title 2", false, true);
        ContentEntry entry4 = new ContentEntry("title 4", "title 4", true, false);

        entry2.setContentEntryUid(umAppDatabase.getContentEntryDao().insert(entry2));
        entry3.setContentEntryUid(umAppDatabase.getContentEntryDao().insert(entry3));
        entry4.setContentEntryUid(umAppDatabase.getContentEntryDao().insert(entry4));

        Container cEntry2 = new Container();
        cEntry2.setContainerContentEntryUid(entry2.getContentEntryUid());
        cEntry2.setLastModified(System.currentTimeMillis());
        cEntry2.setFileSize(500);
        cEntry2.setContainerUid(umAppDatabase.getContainerDao().insert(cEntry2));

        Container cEntry4 = new Container();
        cEntry4.setContainerContentEntryUid(entry4.getContentEntryUid());
        cEntry4.setLastModified(System.currentTimeMillis());
        cEntry4.setFileSize(500);
        cEntry4.setContainerUid(umAppDatabase.getContainerDao().insert(cEntry4));

        totalBytesToDownload = cEntry2.getFileSize() + cEntry4.getFileSize();

        umAppDatabase.getContentEntryParentChildJoinDao().insertList(Arrays.asList(
                new ContentEntryParentChildJoin(rootEntry, entry2, 0),
                new ContentEntryParentChildJoin(rootEntry, entry3, 0),
                new ContentEntryParentChildJoin(entry3, entry4, 0)
        ));

    }

    private void insertDownloadJobAndJobItems(boolean meteredNetworkAllowed, int status){
        Assert.assertNotEquals(0, rootEntry.getContentEntryUid());
        UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "DownloadDialogPresenterTest " +
                "root entry uid = " + rootEntry.getContentEntryUid());
        downloadJob = new DownloadJob(rootEntry.getContentEntryUid(),
                System.currentTimeMillis());
        downloadJob.setMeteredNetworkAllowed(meteredNetworkAllowed);
        downloadJob.setDjStatus(status);
        DownloadJobItemManager itemManager = mockedNetworkManager
                .createNewDownloadJobItemManager(downloadJob);
        new DownloadJobPreparer(itemManager, umAppDatabase, umAppDatabaseRepo).run();
        System.out.println("job prepared");
    }

    private void insertDownloadJobAndJobItems() {
        insertDownloadJobAndJobItems(false, JobStatus.RUNNING);
    }

    @Test
    public void givenNoExistingDownloadJob_whenOnCreateCalled_shouldCreateDownloadJobAndJobItems() throws InterruptedException {
        CountDownLatch viewReadyLatch = new CountDownLatch(1);
        doAnswer((invocation) -> {
            viewReadyLatch.countDown();
            return null;
        }).when(mockedDialogView).setWifiOnlyOptionVisible(true);

        HashMap<String,String> args =  new HashMap<>();

        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));
        presenter = new DownloadDialogPresenter(context,mockedNetworkManager,args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo);
        presenter.onCreate(new HashMap<>());
        presenter.onStart();

        viewReadyLatch.await(MAX_LATCH_WAITING_TIME, TimeUnit.SECONDS);

        long downloadJobUid = umAppDatabase.getDownloadJobDao()
                .findDownloadJobUidByRootContentEntryUid(rootEntry.getContentEntryUid());
        assertTrue("Download job was with root content entry uid was created dby presenter",
                downloadJobUid > 0);

        WaitForLiveData.observeUntil(
                umAppDatabase.getDownloadJobItemDao().findByContentEntryUidLive(
                        rootEntry.getContentEntryUid()), 5, TimeUnit.SECONDS,
                (dji) -> dji != null && dji.getDownloadLength() ==  totalBytesToDownload);

        assertEquals("4 DownloadJobItem were created ",
                4, umAppDatabase.getDownloadJobItemDao().findAll().size());

        assertEquals("Total bytes to be downloaded was updated",
                totalBytesToDownload,
                umAppDatabase.getDownloadJobItemDao()
                        .findByContentEntryUid2(rootEntry.getContentEntryUid()).getDownloadLength());
    }


    @Test
    public void givenDownloadJobCreated_whenHandleClickCalled_shouldSetStatusToQueued() throws InterruptedException {
        CountDownLatch viewReadyLatch = new CountDownLatch(1);
        doAnswer((invocation) -> {
            viewReadyLatch.countDown();
            return null;
        }).when(mockedDialogView).setWifiOnlyOptionVisible(true);

        HashMap<String,String> args =  new HashMap<>();

        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));
        presenter = new DownloadDialogPresenter(context,mockedNetworkManager,args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo);
        presenter.onCreate(new HashMap<>());
        presenter.onStart();

        viewReadyLatch.await(MAX_LATCH_WAITING_TIME, TimeUnit.SECONDS);

        presenter.handleClickPositive();

        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobDao()
                        .getJobLive(presenter.getCurrentJobId()),MAX_LATCH_WAITING_TIME,TimeUnit.SECONDS,
                downloadJob -> downloadJob != null && downloadJob.getDjStatus() == JobStatus.QUEUED);

        DownloadJob queuedJob = umAppDatabase.getDownloadJobDao().findByUid(presenter.getCurrentJobId());
        assertEquals("Job status was changed to Queued after clicking continue",
                JobStatus.QUEUED, queuedJob.getDjStatus());
    }

    @Test
    public void givenDownloadRunning_whenCreated_shouldShowStackedOptions() {

        insertDownloadJobAndJobItems();

        HashMap<String,String> args =  new HashMap<>();
        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));
        presenter = new DownloadDialogPresenter(context,mockedNetworkManager,args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo);
        presenter.onCreate(new HashMap<>());
        presenter.onStart();

        verify(mockedDialogView, timeout(5000)).setStackedOptions(any(),any());
    }


    //TODO: Once this cooperates, the underlying method must return to being async
    @Test
    public void givenDownloadRunning_whenClickPause_shouldSetStatusToPaused() throws InterruptedException {
        insertDownloadJobAndJobItems();

        CountDownLatch viewReadyLatch = new CountDownLatch(1);
        doAnswer((invocation) -> {
            viewReadyLatch.countDown();
            return null;
        }).when(mockedDialogView).setWifiOnlyOptionVisible(true);

        HashMap<String,String> args =  new HashMap<>();

        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));
        presenter = new DownloadDialogPresenter(context,mockedNetworkManager,args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo);
        presenter.onCreate(new HashMap<>());
        presenter.onStart();

        viewReadyLatch.await(MAX_LATCH_WAITING_TIME, TimeUnit.SECONDS);

        presenter.handleClickStackedButton(DownloadDialogPresenter.STACKED_BUTTON_PAUSE);
        WaitForLiveData.observeUntil(
                umAppDatabase.getDownloadJobDao().getJobLive(downloadJob.getDjUid()),
                MAX_LATCH_WAITING_TIME, TimeUnit.SECONDS,
                job -> job != null && job.getDjStatus() == JobStatus.PAUSED);

        DownloadJob finishedJob = umAppDatabase.getDownloadJobDao().findByUid(downloadJob.getDjUid());
        assertEquals("Job status was changed to paused after clicking pause button",
                JobStatus.PAUSED, finishedJob.getDjStatus());
    }

    @Test
    public void givenDownloadRunning_whenClickCancel_shouldSetStatusToCancelling() throws InterruptedException {

        insertDownloadJobAndJobItems();

        CountDownLatch viewReadyLatch = new CountDownLatch(1);
        doAnswer((invocation) -> {
            viewReadyLatch.countDown();
            return null;
        }).when(mockedDialogView).setWifiOnlyOptionVisible(true);

        HashMap<String,String> args =  new HashMap<>();

        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));
        presenter = new DownloadDialogPresenter(context,mockedNetworkManager,args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo);
        presenter.onCreate(new HashMap<>());
        presenter.onStart();

        viewReadyLatch.await(MAX_LATCH_WAITING_TIME, TimeUnit.SECONDS);

        presenter.handleClickStackedButton(STACKED_BUTTON_CANCEL);

        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobDao()
                        .getJobLive(downloadJob.getDjUid()),MAX_LATCH_WAITING_TIME,TimeUnit.SECONDS,
                downloadJob -> downloadJob != null && downloadJob.getDjStatus() == JobStatus.CANCELLING);

        assertEquals("Job status was changed to cancelling after clicking cancel download",
                JobStatus.CANCELED,
                umAppDatabase.getDownloadJobDao().findByUid(downloadJob.getDjUid())
                        .getDjStatus());
    }

    //TODO: handle edge case of when the dialog is dismissed before it is ready
    @Test
    public void givenExistingDownloadSet_whenDialogDismissedWithoutSelection_shouldCleanUpUnQueuedJob() throws InterruptedException {

        insertDownloadJobAndJobItems(true, JobStatus.NOT_QUEUED);

        HashMap<String,String> args =  new HashMap<>();
        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));

        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobItemDao()
                        .findAllLive(), MAX_LATCH_WAITING_TIME , TimeUnit.SECONDS,
                allItems -> allItems.size() == 5);

        presenter = new DownloadDialogPresenter(context,mockedNetworkManager,args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo);
        presenter.onCreate(new HashMap<>());
        presenter.onStart();

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_THREAD_SLEEP_TIME));

        presenter.handleClickNegative();


        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobItemDao()
                .findAllLive(), MAX_LATCH_WAITING_TIME, TimeUnit.SECONDS,
                allItems -> allItems.size() == 0);

        assertEquals("All download items were deleted ",
                0, umAppDatabase.getDownloadJobItemDao().findAll().size());
    }


    @Test
    public void givenDownloadRunning_whenCompletedAndUserOptToDelete_shouldDeleteDownloadJobSetAndAssociatedFiles() throws InterruptedException {

        insertDownloadJobAndJobItems();

        CountDownLatch viewReadyLatch = new CountDownLatch(1);
        doAnswer((invocation) -> {
            viewReadyLatch.countDown();
            return null;
        }).when(mockedDialogView).setWifiOnlyOptionVisible(true);

        HashMap<String,String> args =  new HashMap<>();
        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));

        umAppDatabase.getDownloadJobDao().updateJobAndItems(downloadJob.getDjUid(),
                JobStatus.COMPLETE,JobStatus.RUNNING_MIN);

        presenter = new DownloadDialogPresenter(context,mockedNetworkManager,args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo);
        presenter.onCreate(new HashMap<>());
        presenter.onStart();

        Thread.sleep(TimeUnit.SECONDS.toMillis(MAX_THREAD_SLEEP_TIME));

        presenter.handleClickPositive();

        VerificationMode mode = timeout(TimeUnit.SECONDS.toMillis(MAX_LATCH_WAITING_TIME));

        //verify that DeleteJobTaskRunner was created
        verify(mockedNetworkManager,mode)
                .makeDeleteJobTask(any(),any());

        //verify that run method was called to start DeleteJobTaskWorker
        verify(mockedDeleteTaskRunner,mode).run();

    }

    @Test
    public void givenDownloadRunning_whenUserChangesWifiOnlyOption_shouldBeChangedInDb()
        throws InterruptedException{

        insertDownloadJobAndJobItems(true,JobStatus.RUNNING);

        HashMap<String,String> args =  new HashMap<>();

        CountDownLatch viewReadyLatch = new CountDownLatch(1);
        doAnswer((invocation) -> {
            viewReadyLatch.countDown();
            return null;
        }).when(mockedDialogView).setWifiOnlyOptionVisible(true);

        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));
        presenter = new DownloadDialogPresenter(context,mockedNetworkManager,args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo);
        presenter.onCreate(new HashMap<>());
        presenter.onStart();

        viewReadyLatch.await(MAX_LATCH_WAITING_TIME, TimeUnit.SECONDS);

        presenter.handleWiFiOnlyOption(true);

        WaitForLiveData.observeUntil(
                umAppDatabase.getDownloadJobDao().getLiveMeteredNetworkAllowed(
                        (int)downloadJob.getDjUid()),
                        MAX_LATCH_WAITING_TIME, TimeUnit.SECONDS,
                        allowed -> allowed != null && !allowed);

        assertFalse("Job is allowed to run on un metered connection only",
                umAppDatabase.getDownloadJobDao().findByUid((int)downloadJob.getDjUid())
                        .isMeteredNetworkAllowed());
    }

    @Test
    public void givenDownloadOptionsAreShown_whenStorageLocationChanged_shouldUpdateDestinationDirOnDownloadJob() throws InterruptedException {
        insertDownloadJobAndJobItems();

        CountDownLatch viewReadyLatch = new CountDownLatch(1);
        doAnswer((invocation) -> {
            viewReadyLatch.countDown();
            return null;
        }).when(mockedDialogView).setWifiOnlyOptionVisible(true);

        String destDir = "/sample/dir/public/ustadmobileContent/";

        HashMap<String,String> args =  new HashMap<>();
        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));
        presenter = new DownloadDialogPresenter(context,mockedNetworkManager,args, mockedDialogView,
                umAppDatabase, umAppDatabaseRepo);
        presenter.onCreate(new HashMap<>());
        presenter.onStart();

        viewReadyLatch.await(MAX_LATCH_WAITING_TIME, TimeUnit.SECONDS);

        presenter.handleStorageOptionSelection(destDir);

        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobDao()
                        .getJobLive((int)downloadJob.getDjUid()), MAX_LATCH_WAITING_TIME,
                TimeUnit.SECONDS, downloadJob -> downloadJob != null &&
                        downloadJob.getDjDestinationDir() != null &&
                        downloadJob.getDjDestinationDir().equals(destDir));

        assertEquals("DownloadSet destination directory changed successfully",
                destDir,
                umAppDatabase.getDownloadJobDao().findByUid((int)downloadJob.getDjUid()).getDjDestinationDir());
    }
}
