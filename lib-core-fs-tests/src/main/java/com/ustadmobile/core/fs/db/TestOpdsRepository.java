package com.ustadmobile.core.fs.db;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.OpdsFeedWithRelationsDao;
import com.ustadmobile.lib.db.entities.OpdsFeedWithRelations;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.test.core.ResourcesHttpdTestServer;
import com.ustadmobile.test.core.TestCaseCallbackHelper;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;


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
    public void testOpdsRepository() {
//        OpdsFeedWithRelationsDao repository = DbManager.getInstance(PlatformTestUtil.getTargetContext())
//                .getOpdsFeedWithRelationsRepository();
//
//        String opdsUrl = UMFileUtil.joinPaths(new String[] {
//                ResourcesHttpdTestServer.getHttpRoot(), "com/ustadmobile/test/core/acquire-multi.opds"});
//
//        UmLiveData<OpdsFeedWithRelations> feed = repository.getFeedByUrl(opdsUrl);
//        TestCaseCallbackHelper helper = new TestCaseCallbackHelper(this);
//
//        OpdsFeedWithRelations[] returnedVal = new OpdsFeedWithRelations[1];
//        helper.add(20000, () -> {
//            feed.observeForever((t) -> {
//                if(t != null) {
//                    returnedVal[0] = t;
//                    helper.onSuccess(t);
//                }
//            });
//        });
//        helper.start();
//        Assert.assertNotNull(returnedVal[0]);
//        try { Thread.sleep(240000); }
//        catch(InterruptedException e) {}
    }

}
