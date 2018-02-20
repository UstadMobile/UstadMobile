package com.ustadmobile.core.fs.db;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.db.dao.OpdsEntryWithRelationsDao;
import com.ustadmobile.core.fs.db.repository.OpdsEntryRepository;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;
import com.ustadmobile.test.core.ResourcesHttpdTestServer;
import com.ustadmobile.test.core.TestCaseCallbackHelper;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by mike on 1/14/18.
 */

public class TestOpdsRepository extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ResourcesHttpdTestServer.startServer();
    }

    @Override
    protected void tearDown() throws Exception {
        ResourcesHttpdTestServer.stopServer();
        super.tearDown();
    }

    @Test
    public void testOpdsRepositoryLoadFromFeed() {
        OpdsEntryWithRelationsDao repository = DbManager.getInstance(PlatformTestUtil.getTargetContext())
                .getOpdsEntryWithRelationsRepository();

        String opdsUrl = UMFileUtil.joinPaths(new String[] {
                ResourcesHttpdTestServer.getHttpRoot(), "com/ustadmobile/test/core/acquire-multi.opds"});


        TestCaseCallbackHelper helper = new TestCaseCallbackHelper(this);

        helper.add(180000, () -> {
            UmLiveData<OpdsEntryWithRelations> feed = repository.getEntryByUrl(opdsUrl, null,
                    new OpdsEntry.OpdsItemLoadCallback() {
                        @Override
                        public void onDone(OpdsEntry item) {
                            helper.onSuccess(item);
                        }

                        @Override
                        public void onEntryAdded(OpdsEntryWithRelations childEntry, OpdsEntry parentFeed, int position) {

                        }

                        @Override
                        public void onLinkAdded(OpdsLink link, OpdsEntry parentItem, int position) {

                        }

                        @Override
                        public void onError(OpdsEntry item, Throwable cause) {

                        }
                    });
        }).add(180000, () -> {
            //now find the child entries
            OpdsEntryWithRelations parent = (OpdsEntryWithRelations)helper.getResult();
            helper.clear();
            Assert.assertEquals("Loaded expected title from feed", "Hills and Such",
                    parent.getTitle());
            Assert.assertEquals("Loaded expected ID from feed",
                    "http://umcloud1.ustadmobile.com/opds/courseid/6CM",
                    parent.getEntryId());

            UmLiveData<List<OpdsEntryWithRelations>> childEntryListLiveData = DbManager
                    .getInstance(PlatformTestUtil.getTargetContext()).getOpdsEntryWithRelationsDao()
                    .getEntriesByParentAsList(parent.getUuid());
            UmObserver<List<OpdsEntryWithRelations>> observer = new UmObserver<List<OpdsEntryWithRelations>>() {
                @Override
                public void onChanged(List<OpdsEntryWithRelations> entryList) {
                    if(entryList != null && !entryList.isEmpty()) {
                        childEntryListLiveData.removeObserver(this);
                        helper.onSuccess(entryList);
                    }

                }
            };
            childEntryListLiveData.observeForever(observer);

        }).add(180000, () -> {
            List<OpdsEntryWithRelations> childEntriesList = (List<OpdsEntryWithRelations>)helper.getResult();
            Assert.assertTrue("Loaded entries", childEntriesList.size() > 2);
            helper.clear();
        });
        helper.start();
    }

    public void testDirScan() throws IOException{
        File tmpDir = File.createTempFile("testopdsrepository", "");
        tmpDir.delete();
        tmpDir.mkdir();

        File epubOutFile = new File(tmpDir, "thelittlechicks.epub");
        InputStream epubResIn = getClass().getResourceAsStream("/com/ustadmobile/test/core/thelittlechicks.epub");
        FileOutputStream fout = new FileOutputStream(epubOutFile);
        UMIOUtils.readFully(epubResIn, fout);
        fout.flush();
        fout.close();

        OpdsEntryRepository repository = (OpdsEntryRepository)DbManager
                .getInstance(PlatformTestUtil.getTargetContext()).getOpdsEntryWithRelationsRepository();

        ArrayList<OpdsEntryWithRelations> entriesInDir = new ArrayList<>();
        UmLiveData<List<OpdsEntryWithRelations>> entriesInDirLiveData = repository
                .findEntriesByContainerFileDirectoryAsList(Arrays.asList(tmpDir.getAbsolutePath()),
                        null);
        UmObserver<List<OpdsEntryWithRelations>> observer = (entriesInDirList) -> {
            if(entriesInDirList != null && !entriesInDirList.isEmpty()) {
                synchronized (TestOpdsRepository.this) {
                    entriesInDir.clear();
                    entriesInDir.addAll(entriesInDirList);
                    notifyAll();
                }
            }
        };

        entriesInDirLiveData.observeForever(observer);
        if(entriesInDir.isEmpty()) {
            synchronized (this){
                try { wait(); }
                catch(InterruptedException e) {}
            }
        }

        Assert.assertTrue(entriesInDir.size() > 0);
        entriesInDirLiveData.removeObserver(observer);
    }

}
