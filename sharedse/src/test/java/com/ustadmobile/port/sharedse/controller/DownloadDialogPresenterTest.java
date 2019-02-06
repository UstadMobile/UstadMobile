package com.ustadmobile.port.sharedse.controller;


import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.WaitForLiveData;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.port.sharedse.view.DownloadDialogView;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import static com.ustadmobile.port.sharedse.controller.DownloadDialogPresenter.ARG_CONTENT_ENTRY_UID;
import static org.mockito.Mockito.mock;

public class DownloadDialogPresenterTest {

    private DownloadDialogView mockedDialogView;

    private DownloadDialogPresenter presenter;

    private UmAppDatabase umAppDatabase;

    private Object context;

    private ContentEntry rootEntry;

    @Before
    public void setUp(){
        context = PlatformTestUtil.getTargetContext();
        mockedDialogView = mock(DownloadDialogView.class);

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
    }
    @Test
    public void givenNoExistingDownloadSet_whenOnCreateCalled_shouldCreateDownloadSetAndSetItems() {

        Hashtable args =  new Hashtable();
        args.put(ARG_CONTENT_ENTRY_UID, String.valueOf(rootEntry.getContentEntryUid()));

        presenter = new DownloadDialogPresenter(context,args, mockedDialogView);
        presenter.onCreate(new Hashtable());

        WaitForLiveData.observeUntil(umAppDatabase.getDownloadJobItemDao()
                .findAllLive(), 5, TimeUnit.SECONDS, allItems -> allItems.size() == 4);


        Assert.assertTrue(umAppDatabase.getDownloadSetDao()
                .findDownloadSetUidByRootContentEntryUid(rootEntry.getContentEntryUid()) > 0);

        Assert.assertEquals("Four DownloadJobItems were created ",
                umAppDatabase.getDownloadJobItemDao().findAll().size(),4);

    }

    @Test
    public void givenExistingDownloadSet_whenOnCreateCalled_shouldNotCreateDownloadSetAndSetItems(){

    }

}
