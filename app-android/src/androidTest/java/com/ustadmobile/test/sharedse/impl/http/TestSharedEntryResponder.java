package com.ustadmobile.test.sharedse.impl.http;

import com.google.gson.Gson;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.OpdsEntry;
import com.ustadmobile.port.sharedse.impl.http.SharedEntryInfo;
import com.ustadmobile.port.sharedse.impl.http.SharedEntryResponder;
import com.ustadmobile.test.core.ResourcesHttpdTestServer;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Random;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


public class TestSharedEntryResponder  {

    @BeforeClass
    public static void startHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.startServer();
    }

    @AfterClass
    public static void stopHttpResourcesServer() throws IOException {
        ResourcesHttpdTestServer.stopServer();
    }


    @Test
    public void givenExistingUuid_whenGetCalled_thenShouldReturnEntryInfoJson() throws IOException{
        OpdsEntry entry = UstadMobileSystemImpl.getInstance()
                .getOpdsAtomFeedRepository(PlatformTestUtil.getTargetContext())
                .getEntryByUrlStatic(UMFileUtil.joinPaths(
                        ResourcesHttpdTestServer.getHttpRoot(),
                        "com/ustadmobile/test/sharedse/crawlme-slow/index.opds"));
        int portNum = new Random().nextInt();
        RouterNanoHTTPD.UriResource mockUriResource = Mockito.mock(RouterNanoHTTPD.UriResource.class);
        when(mockUriResource.initParameter(eq(SharedEntryResponder.ARG_INDEX_FEED_UUID), any()))
                .thenReturn(entry.getUuid());
        when(mockUriResource.initParameter(eq(SharedEntryResponder.ARG_INDEX_CONTEXT), any()))
                .thenReturn(PlatformTestUtil.getTargetContext());
        when(mockUriResource.initParameter(eq(SharedEntryResponder.ARG_INDEX_PORTNUM), any()))
                .thenReturn(portNum);

        NanoHTTPD.Response response = new SharedEntryResponder().get(mockUriResource, null, null);
        String jsonStr = UMIOUtils.readStreamToString(response.getData());
        SharedEntryInfo returnInfo = new Gson().fromJson(jsonStr, SharedEntryInfo.class);


        Assert.assertEquals(returnInfo.getEntryUuid(), entry.getUuid());
        Assert.assertEquals(returnInfo.getMirrorServerPort(), portNum);
    }

}
