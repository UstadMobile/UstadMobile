package com.ustadmobile.test.http;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import fi.iki.elonen.NanoHTTPD;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;


public class UmTestServer extends Dispatcher{

    private MockWebServer mockWebServer;

    private int port;

    private File baseDir;

    private String bindAddress;

    private long throttleBytesPerPeriod;

    private long throttlePeriodDuration;

    private TimeUnit throttleTimeUnit;


    public UmTestServer(int port, File baseDir, String bindAddress) {
        mockWebServer = new MockWebServer();
        mockWebServer.setBodyLimit(100*1024);
        mockWebServer.setDispatcher(this);

        this.port = port;
        this.baseDir = baseDir;
        this.bindAddress = bindAddress;
    }

    public void start() throws IOException {
        mockWebServer.start(InetAddress.getByName(bindAddress), port);
    }

    public void stop() {
        try {
            mockWebServer.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the port as given by the mock web server
     *
     * @return port number being listened on (-1 if not started)
     */
    public int getPort() {
        return mockWebServer.getPort();
    }

    @Override
    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        String requestPath = request.getPath();
        MockResponse mockResponse = new MockResponse();
        if(requestPath.contains("containerUid")){
            String containerUid = requestPath.split("=")[1];
            JSONObject mObject = new JSONObject();
            mObject.put("cefMd5", new String(Base64.getEncoder().encode(containerUid.getBytes())));
            mObject.put("ceUid","9090");
            mObject.put("ceContainerUid",containerUid);
            mObject.put("cePath",containerUid);
            mObject.put("ceCefUid",containerUid);
            JSONArray mEntries = new JSONArray();
            mEntries.put(mObject);
            mockResponse.addHeader("Content-Type", "application/json; charset=UTF-8");
            mockResponse.setResponseCode(200)
                    .setBody(mEntries.toString());
            return mockResponse;
        }else{
            File responseFile = new File(baseDir, requestPath);

            if(!responseFile.exists()) {
                return new MockResponse().setResponseCode(404);
            }

            try {
                BufferedSource fileBuffer = Okio.buffer(Okio.source(responseFile));
                Buffer outBuffer = new Buffer();
                fileBuffer.readFully(outBuffer, responseFile.length());
                mockResponse.setBody(outBuffer);
                mockResponse.setResponseCode(200);
                if(throttleBytesPerPeriod > 0) {
                    mockResponse.throttleBody(throttleBytesPerPeriod, throttlePeriodDuration,
                            throttleTimeUnit);
                }

                return mockResponse;
            }catch(IOException e) {
                e.printStackTrace();
                return mockResponse.setResponseCode(500).setBody(e.toString());
            }
        }

    }

    public void throttle(long bytePerPeriod, long periodDuration, TimeUnit timeUnit){
        this.throttleBytesPerPeriod = bytePerPeriod;
        this.throttlePeriodDuration = periodDuration;
        this.throttleTimeUnit = timeUnit;
    }

    public long getThrottleBytesPerPeriod() {
        return throttleBytesPerPeriod;
    }

    public long getThrottlePeriodDuration() {
        return throttlePeriodDuration;
    }

    public TimeUnit getThrottleTimeUnit() {
        return throttleTimeUnit;
    }
}
