package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.http.UmHttpRequest;
import com.ustadmobile.core.impl.http.UmHttpResponse;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Created by mike on 12/25/17.
 */
public class TestEmbeddedHTTPD {

    private EmbeddedHTTPD httpd;

    private Object context;

    static class EmbeddeHttpdResponder implements RouterNanoHTTPD.UriResponder {

        @Override
        public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            return NanoHTTPD.newFixedLengthResponse("Hello world");
        }

        @Override
        public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            return NanoHTTPD.newFixedLengthResponse("Hello world");
        }

        @Override
        public NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            return NanoHTTPD.newFixedLengthResponse("Hello world");
        }

        @Override
        public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            return NanoHTTPD.newFixedLengthResponse("Hello world");
        }

        @Override
        public NanoHTTPD.Response other(String method, RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
            return NanoHTTPD.newFixedLengthResponse("Hello world");
        }
    }

    @Before
    public void startServer() throws IOException {
        context = PlatformTestUtil.getTargetContext();
        httpd = new EmbeddedHTTPD(0, context);
        httpd.start();
    }

    @After
    public void stopServer() {
        httpd.stop();
        httpd = null;
    }

    @Test
    public void givenResponseListenerAdded_whenRequestMade_shouldReceiveResponseStartAndFinishedEvent()
            throws IOException{

        httpd.addRoute(".*", EmbeddeHttpdResponder.class);

        EmbeddedHTTPD.ResponseListener responseListener = mock(EmbeddedHTTPD.ResponseListener.class);
        httpd.addResponseListener(responseListener);

        UmHttpResponse response = UstadMobileSystemImpl.Companion.getInstance().makeRequestSync(
                new UmHttpRequest(context, httpd.getLocalHttpUrl() + "dir/filename.txt"));

        ArgumentCaptor<NanoHTTPD.IHTTPSession> sessionArgumentCaptor = ArgumentCaptor.forClass(
                NanoHTTPD.IHTTPSession.class);
        ArgumentCaptor<NanoHTTPD.Response> responseArgumentCaptor = ArgumentCaptor.forClass(
                NanoHTTPD.Response.class);
        verify(responseListener).responseStarted(sessionArgumentCaptor.capture(),
                responseArgumentCaptor.capture());
        Assert.assertEquals("Received expected request on response started",
                "/dir/filename.txt", sessionArgumentCaptor.getValue().getUri());

        verify(responseListener, timeout(10000)).responseFinished(sessionArgumentCaptor.capture(),
                responseArgumentCaptor.capture());
        Assert.assertEquals("Received expected request on response finished",
                "/dir/filename.txt", sessionArgumentCaptor.getValue().getUri());

        httpd.removeResponseListener(responseListener);
    }



}
