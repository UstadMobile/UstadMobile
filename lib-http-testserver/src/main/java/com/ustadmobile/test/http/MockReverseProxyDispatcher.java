package com.ustadmobile.test.http;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

//See: https://gist.github.com/paour/f1b72be812621819881f14ece951a59d

public class MockReverseProxyDispatcher extends Dispatcher {

    private OkHttpClient client;

    private HttpUrl serverUrl;

    public MockReverseProxyDispatcher(HttpUrl serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Override
    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        return null;
    }

    public void setLatency(int latency) {

    }

    public void setBandwidthThrottle() {

    }

}
