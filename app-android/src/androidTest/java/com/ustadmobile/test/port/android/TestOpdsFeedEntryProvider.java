package com.ustadmobile.test.port.android;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.db.dao.OpdsAtomFeedRepository;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.test.core.ResourcesHttpdTestServer;
import com.ustadmobile.test.core.TestCaseCallbackHelper;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import junit.framework.TestCase;

import org.junit.Test;


/**
 * Created by mike on 1/15/18.
 */

public class TestOpdsFeedEntryProvider extends TestCase{

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
    public void testOpdsFeedEntryProvider() {
        OpdsAtomFeedRepository repository = UstadMobileSystemImpl.getInstance()
                .getOpdsAtomFeedRepository(PlatformTestUtil.getTargetContext());
        String opdsUrl = UMFileUtil.joinPaths(new String[] {
                ResourcesHttpdTestServer.getHttpRoot(), "com/ustadmobile/test/core/acquire-multi.opds"});

        UmLiveData<OpdsEntryWithRelations> feed = repository.getEntryByUrl(opdsUrl);
        TestCaseCallbackHelper helper = new TestCaseCallbackHelper(this);

        OpdsEntryWithRelations[] returnedVal = new OpdsEntryWithRelations[1];
        helper.add(20000, () -> {
            feed.observeForever((t) -> {
                if(t != null) {
                    returnedVal[0] = t;
                    helper.onSuccess(t);
                }
            });
        }).add(20000, () -> {
            OpdsEntryWithRelations returnedFeed = (OpdsEntryWithRelations)helper.getResult();
            UmProvider<OpdsEntryWithRelations> entryProvider = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext())
                    .getOpdsEntryWithRelationsDao().getEntriesByParent(returnedFeed.getUuid());
            DataSource.Factory<Integer, OpdsEntryWithRelations> factory =
                    (DataSource.Factory<Integer, OpdsEntryWithRelations>)entryProvider.getProvider();
            LiveData<PagedList<OpdsEntryWithRelations>> entryList = new LivePagedListBuilder(factory, 20).build();
            entryList.observeForever((t) -> {
                if(t != null && entryList.getValue().size() > 0) {
                    OpdsEntryWithRelations entry = entryList.getValue().get(0);
                    assertNotNull(entry);
                    helper.onSuccess("OK");
                }
            });
        });
        helper.start();
    }

}
