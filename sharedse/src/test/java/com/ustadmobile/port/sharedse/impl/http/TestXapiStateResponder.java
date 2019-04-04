package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.StateEntity;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import fi.iki.elonen.router.RouterNanoHTTPD;

public class TestXapiStateResponder {

    private UmAppDatabase appRepo;
    private RouterNanoHTTPD httpd;

    @Before
    public void setup() throws IOException {

        UmAppDatabase appDatabase = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        appDatabase.clearAllTables();
        appRepo = appDatabase.getRepository("http://localhost/dummy/", "");

        httpd = new RouterNanoHTTPD(0);
        httpd.addRoute("/xAPI/activities/state(.*)+", XapiStateResponder.class, appRepo);
        httpd.start();
    }

    @Test
    public void testput() throws IOException {

        String urlString = "http://localhost:" + httpd.getListeningPort() + "/xAPI/activities/state";

        String content = UMIOUtils.readToString(
                getClass().getResourceAsStream("/com/ustadmobile/port/sharedse/state"), "UTF-8");

        urlString += "?activityId=" +
                URLEncoder.encode("http://www.example.com/activities/1", StandardCharsets.UTF_8.toString()) +
                "&agent=" +
                URLEncoder.encode("{\"objectType\": \"Agent\", \"name\": \"John Smith\", \"account\":{\"name\": \"123\", \"homePage\": \"http://www.example.com/users/\"}}",
                        StandardCharsets.UTF_8.toString()) +
                "&stateId=" +
                URLEncoder.encode("http://www.example.com/states/1", StandardCharsets.UTF_8.toString());
        HttpURLConnection httpCon = (HttpURLConnection) new URL(urlString).openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        OutputStreamWriter out = new OutputStreamWriter(
                httpCon.getOutputStream());
        out.write(content);
        out.close();
        httpCon.connect();

        int code = httpCon.getResponseCode();

        Assert.assertEquals(204, code);
        StateEntity stateEntity = appRepo.getStateDao().findByStateId("http://www.example.com/states/1");
        Assert.assertEquals("http://www.example.com/activities/1", stateEntity.getActivityId());
    }

    @Test
    public void testPost() throws IOException {

        String urlString = "http://localhost:" + httpd.getListeningPort() + "/xAPI/activities/state";

        String content = UMIOUtils.readToString(
                getClass().getResourceAsStream("/com/ustadmobile/port/sharedse/state"), "UTF-8");


        urlString += "?activityId=" +
                URLEncoder.encode("http://www.example.com/activities/1", StandardCharsets.UTF_8.toString()) +
                "&agent=" +
                URLEncoder.encode("{\"objectType\": \"Agent\", \"name\": \"John Smith\", \"account\":{\"name\": \"123\", \"homePage\": \"http://www.example.com/users/\"}}",
                        StandardCharsets.UTF_8.toString()) +
                "&stateId=" +
                URLEncoder.encode("http://www.example.com/states/1", StandardCharsets.UTF_8.toString());
        HttpURLConnection httpCon = (HttpURLConnection) new URL(urlString).openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("POST");
        OutputStreamWriter out = new OutputStreamWriter(
                httpCon.getOutputStream());
        out.write(content);
        out.close();
        httpCon.connect();

        int code = httpCon.getResponseCode();

        Assert.assertEquals(204, code);
        StateEntity stateEntity = appRepo.getStateDao().findByStateId("http://www.example.com/states/1");
        Assert.assertEquals("http://www.example.com/activities/1", stateEntity.getActivityId());
    }

}
