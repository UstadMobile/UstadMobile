package com.ustadmobile.port.sharedse.controller;


import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.WaitForLiveData;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadSet;
import com.ustadmobile.lib.db.entities.DownloadSetItem;
import com.ustadmobile.port.sharedse.view.DownloadDialogView;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ustadmobile.port.sharedse.controller.DownloadDialogPresenter.ARG_CONTENT_ENTRY_UID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DownloadDialogPresenterTest {

    private DownloadDialogView mockedDialogView;

    private DownloadDialogPresenter presenter;

    private UmAppDatabase umAppDatabase;

    private Object context;

    private ContentEntry rootEntry;

    private DownloadJob downloadJob;

    private DownloadSet downloadSet;

    private long totalBytesToDownload = 0L;


    @Before
    public void setUp(){
        context = PlatformTestUtil.getTargetContext();
        mockedDialogView = mock(DownloadDialogView.class);
        doAnswer((invocationOnMock) -> {
            new Thread(((Runnable)invocationOnMock.getArgument(0))).start();
            return null;
        }).when(mockedDialogView).runOnUiThread(any());

        umAppDatabase = UmAppDatabase.getInstance(context);
        UmAppDatabase.getInstance(context).clearAllTables();


        rootEntry = new ContentEntry("Lorem ipsum title",
                "Lorem ipsum description",false,true);
        rootEntry.setContentEntryUid(umAppDatabase.getContentEntryDao().insert(rootEntry));

        ContentEntry entry2 = new ContentEntry("title 2", "title 2", true, true);
        ContentEntry entry3 = new ContentEntry("title 2", "title 2", false, true);
        ContentEntry entry4 = new ContentEntry("title 4", "title 4", true, false);

        entry2.setContentEntryUid(umAppDatabase.getContentEntryDao().insert(entry2));
        entry3.setContentEntryUid(umAppDatabase.getContentEntryDao().insert(entry3));
        entry4.setContentEntryUid(umAppDatabase.getContentEntryDao().insert(entry4));


        umAppDatabase.getContentEntryParentChildJoinDao().insertList(Arrays.asList(
                new ContentEntryParentChildJoin(rootEntry, entry2, 0),
                new ContentEntryParentChildJoin(rootEntry, entry3, 0),
                new ContentEntryParentChildJoin(entry3, entry4, 0)
        ));

        ContentEntryFile entry2File = new ContentEntryFile();
        entry2File.setLastModified(System.currentTimeMillis());
        entry2File.setFileSize(2000);
        entry2File.setContentEntryFileUid(umAppDatabase.getContentEntryFileDao().insert(entry2File));
        ContentEntryContentEntryFileJoin fileJoin =
                new ContentEntryContentEntryFileJoin(entry2, entry2File);
        fileJoin.setCecefjUid(umAppDatabase.getContentEntryContentEntryFileJoinDao().insert(fileJoin));

        ContentEntryFile entry4File = new ContentEntryFile();
        entry4File.setFileSize(3000);
        entry4File.setContentEntryFileUid(umAppDatabase.getContentEntryFileDao().insert(entry4File));

        umAppDatabase.getContentEntryContentEntryFileJoinDao().insert(
                new ContentEntryContentEntryFileJoin(entry4, entry4File));

        totalBytesToDownload = entry2File.getFileSize() + entry4File.getFileSize();

        when(mockedDialogView.getOptionIds()).thenReturn(new int[]{ 1,2,3});

    }

    private void insertDownloadSetAndSetItems(){
        downloadSet = new DownloadSet();
        downloadSet.setDsUid((int) umAppDatabase.getDownloadSetDao().insert(downloadSet));

        DownloadSetItem downloadSetItem = new DownloadSetItem(downloadSet,rootEntry);
        downloadSetItem.setDsiUid(umAppDatabase.getDownloadSetItemDao().insert(downloadSetItem));

        downloadJob = new DownloadJob(downloadSet);
        downloadJob.setDjStatus(JobStatus.RUNNING);
        downloadJob.setDjUid(umAppDatabase.getDownloadJobDao().insert(downloadJob));
    }

    @Test
    public void givenNoExistingDownloadSet_whenOnCreateCalled_shouldCreateDownloadSetAndSetItems() throws InterruptedException {

        Hashtable args =  new Hashtable();

        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));

        presenter = new DownloadDialogPresenter(context,args, mockedDialogView);
        presenter.onCreate(new Hashtable());

        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobItemDao()
                .findAllLive(), 5, TimeUnit.SECONDS, allItems -> allItems.size() == 4);


        assertTrue(umAppDatabase.getDownloadSetDao()
                .findDownloadSetUidByRootContentEntryUid(rootEntry.getContentEntryUid()) > 0);

        assertEquals("Four DownloadJobItems were created ",
                umAppDatabase.getDownloadJobItemDao().findAll().size(),4);

        Thread.sleep(2);

        assertEquals("Total bytes to be downloaded was updated",
                umAppDatabase.getDownloadJobDao().findById(
                        presenter.getCurrentJobId()).getTotalBytesToDownload(),
                totalBytesToDownload);


    }


    @Test
    public void givenDownloadSetCreated_whenHandleClickCalled_shouldSetStatusToQueued() throws InterruptedException {
        Hashtable args =  new Hashtable();
        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));

        CountDownLatch mLatch = new CountDownLatch(1);

        presenter = new DownloadDialogPresenter(context,args, mockedDialogView);
        presenter.onCreate(new Hashtable());

        //wait for calculating download to complete
        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobItemDao()
                .findAllLive(), 5, TimeUnit.SECONDS, allItems -> allItems.size() == 4);

        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobDao()
                        .getJobLive(presenter.getCurrentJobId()),5,TimeUnit.SECONDS,
                downloadJob -> downloadJob.getDjStatus() == JobStatus.PAUSED);

        presenter.handleClickPositive();

        mLatch.await(5,TimeUnit.SECONDS);

        assertEquals("Job status was changed to Queued after clicking continue",
                umAppDatabase.getDownloadJobDao().findById(
                        presenter.getCurrentJobId()).getDjStatus(),JobStatus.QUEUED);
    }

    @Test
    public void givenDownloadRunning_whenCreated_shouldShowStackedOptions() {

        insertDownloadSetAndSetItems();

        Hashtable args =  new Hashtable();
        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));

        presenter = new DownloadDialogPresenter(context,args, mockedDialogView);
        presenter.onCreate(new Hashtable());
        presenter.onStart();

        verify(mockedDialogView, timeout(2000)).setStackedOptions(any(),any());


    }

    @Test
    public void givenDownloadRunning_whenClickPause_shouldSetStatusToPaused() throws InterruptedException {

        insertDownloadSetAndSetItems();

        Hashtable args =  new Hashtable();
        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));

        CountDownLatch mLatch = new CountDownLatch(1);

        //wait for calculating download to complete
        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobItemDao()
                .findAllLive(), 3, TimeUnit.SECONDS, allItems -> allItems.size() == 4);


        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobDao()
                        .getJobLive(downloadJob.getDjUid()),5,TimeUnit.SECONDS,
                downloadJob -> downloadJob.getDjStatus() == JobStatus.PAUSED);

        presenter = new DownloadDialogPresenter(context,args, mockedDialogView);
        presenter.onCreate(new Hashtable());

        Thread.sleep(1);

        presenter.handleClickStackedButton(1);

        mLatch.await(3,TimeUnit.SECONDS);

        assertEquals("Job status was changed to paused after clicking pause button",
                umAppDatabase.getDownloadJobDao().findById(downloadJob.getDjUid())
                        .getDjStatus(),JobStatus.PAUSED);


    }

    @Test
    public void givenDownloadRunning_whenClickCancel_shouldSetStatusToCancelling() throws InterruptedException {

        insertDownloadSetAndSetItems();

        Hashtable args =  new Hashtable();
        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));

        CountDownLatch mLatch = new CountDownLatch(1);

        //wait for calculating download to complete
        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobItemDao()
                .findAllLive(), 5, TimeUnit.SECONDS, allItems -> allItems.size() == 4);


        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobDao()
                        .getJobLive(downloadJob.getDjUid()),5,TimeUnit.SECONDS,
                downloadJob -> downloadJob.getDjStatus() == JobStatus.CANCELLING);

        presenter = new DownloadDialogPresenter(context,args, mockedDialogView);
        presenter.onCreate(new Hashtable());

        Thread.sleep(1);

        presenter.handleClickStackedButton(2);

        mLatch.await(3,TimeUnit.SECONDS);

        assertEquals("Job status was changed to cancelling after clicking cancel download",
                umAppDatabase.getDownloadJobDao().findById(downloadJob.getDjUid())
                        .getDjStatus(),JobStatus.CANCELLING);
    }

    @Test
    public void givenDownloadRunning_whenUserChangesWifiOnlyOption_shouldBeChangedInDb() {

        insertDownloadSetAndSetItems();

        Hashtable args =  new Hashtable();
        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));

        //wait for calculating download to complete
        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobItemDao()
                .findAllLive(), 3, TimeUnit.SECONDS, allItems -> allItems.size() == 4);

        presenter = new DownloadDialogPresenter(context,args, mockedDialogView);
        presenter.onCreate(new Hashtable());
        presenter.handleWiFiOnlyOption(true);

        assertFalse("Job is allowed to run on un metered connection only",
                umAppDatabase.getDownloadSetDao().findByUid(downloadSet.getDsUid())
                        .isMeteredNetworkAllowed());
    }


}
