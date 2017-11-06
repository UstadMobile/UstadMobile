package com.ustadmobile.test.port.sharedse;

import com.ustadmobile.core.controller.CatalogEntryInfo;
import com.ustadmobile.core.controller.CatalogPresenter;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.opds.UstadJSOPDSItem;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD;
import com.ustadmobile.test.core.impl.PlatformTestUtil;
import com.ustadmobile.test.sharedse.network.TestEntryStatusTask;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by mike on 8/28/17.
 */

public class TestCatalogUriResponder {

    @Before
    public void copyEntry() throws IOException{
        Object context = PlatformTestUtil.getTargetContext();
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();

        InputStream entryIn = null;
        OutputStream entryOut = null;
        IOException ioe = null;

        File sharedStorageDir = new File(impl.getStorageDirs(CatalogPresenter.SHARED_RESOURCE,
                context)[0].getDirURI());
        if(!sharedStorageDir.exists())
            sharedStorageDir.mkdirs();

        File entryTmpFile = new File(sharedStorageDir, "thelittlechicks.epub");

        try {
            entryIn = getClass().getResourceAsStream(
                    "/com/ustadmobile/test/sharedse/thelittlechicks.epub");
            entryOut = new FileOutputStream(entryTmpFile);
            UMIOUtils.readFully(entryIn, entryOut, 8*1024);
        }catch(IOException e) {
            ioe = e;
        }finally {
            UMIOUtils.closeOutputStream(entryOut);
            UMIOUtils.closeInputStream(entryIn);
            UMIOUtils.throwIfNotNullIO(ioe);
        }
        CatalogEntryInfo entryInfo = new CatalogEntryInfo();
        entryInfo.fileURI = entryTmpFile.getAbsolutePath();
        entryInfo.acquisitionStatus = CatalogPresenter.STATUS_ACQUIRED;
        entryInfo.mimeType = UstadJSOPDSItem.TYPE_EPUBCONTAINER;
        CatalogPresenter.setEntryInfo(TestEntryStatusTask.ENTRY_ID, entryInfo,
                CatalogPresenter.SHARED_RESOURCE, context);
    }

    @After
    public void removeEntry() {
        CatalogPresenter.removeEntry(TestEntryStatusTask.ENTRY_ID, CatalogPresenter.SHARED_RESOURCE,
                PlatformTestUtil.getTargetContext());
    }

    @Test
    public void testCatalogUriResponderEvents() throws IOException{
        Object context = PlatformTestUtil.getTargetContext();
        EmbeddedHTTPD httpd = new EmbeddedHTTPD(0, context);
        httpd.start();

        final int[] responseStartedEvtCount= new int[]{0};
        final int[] responseFinishedEvtCount = new int[]{0};

        EmbeddedHTTPD.ResponseListener listener = new EmbeddedHTTPD.ResponseListener() {
            @Override
            public void responseStarted(NanoHTTPD.Response response) {
                synchronized (responseStartedEvtCount) {
                    responseStartedEvtCount[0]++;
                }
            }

            @Override
            public void responseFinished(NanoHTTPD.Response response) {
                synchronized (responseFinishedEvtCount) {
                    responseFinishedEvtCount[0]++;
                }
            }
        };
        httpd.addResponseListener(listener);

        URL catalogEntryUrl = new URL("http://localhost:" + httpd.getListeningPort() +
                "/catalog/entry/" + TestEntryStatusTask.ENTRY_ID);

        IOException ioe = null;
        InputStream urlIn = null;

        try {
            urlIn = catalogEntryUrl.openStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            UMIOUtils.readFully(urlIn, bout, 8*1024);
        }catch(IOException e) {
            ioe = e;
        }finally {
            UMIOUtils.closeInputStream(urlIn);
        }

        httpd.removeResponseListener(listener);

        UMIOUtils.throwIfNotNullIO(ioe);

        Assert.assertEquals("response started called once", 1, responseStartedEvtCount[0]);
//        Assert.assertEquals("Response finished event called once", 1, responseFinishedEvtCount[0]);
    }

}
