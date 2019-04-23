package com.ustadmobile.port.sharedse.impl.http;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.lib.db.entities.StatementEntity;
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

public class TestXapiStatementResponder {

    RouterNanoHTTPD httpd;
    private UmAppDatabase appRepo;

    @Before
    public void setup() throws IOException {

        UmAppDatabase appDatabase = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        appDatabase.clearAllTables();
        appRepo = appDatabase.getRepository("http://localhost/dummy/", "");

        httpd = new RouterNanoHTTPD(0);
        httpd.addRoute("/xapi/statements(.*)+", XapiStatementResponder.class, appRepo);
        httpd.start();
    }

    @Test
    public void testput() throws IOException {

        String urlString = "http://localhost:" + httpd.getListeningPort() + "/xapi/statements";
        String content =  UMIOUtils.INSTANCE.readToString(
                getClass().getResourceAsStream("/com/ustadmobile/port/sharedse/fullstatement"), "UTF-8");

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
        StatementEntity statement = appRepo.getStatementDao().findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee");
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement.getStatementId());
    }

    @Test
    public void testPost() throws IOException {

        String urlString = "http://localhost:" + httpd.getListeningPort() + "/xapi/statements";
        String content = UMIOUtils.INSTANCE.readToString(
                getClass().getResourceAsStream("/com/ustadmobile/port/sharedse/fullstatement"), "UTF-8");

        HttpURLConnection httpCon = (HttpURLConnection) new URL(urlString).openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("POST");
        OutputStreamWriter out = new OutputStreamWriter(
                httpCon.getOutputStream());
        out.write(content);
        out.close();
        httpCon.connect();

        int code = httpCon.getResponseCode();

        Assert.assertEquals(200, code);
        StatementEntity statement = appRepo.getStatementDao().findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee");
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement.getStatementId());
    }


    @Test
    public void givenAValidStatement_whenPostRequestHasQueryParamsWithMethodisPut_thenShouldReturn204() throws IOException {

        String urlString = "http://localhost:" + httpd.getListeningPort() + "/xapi/statements?method=PUT";
        String content =  UMIOUtils.INSTANCE.readToString(
                getClass().getResourceAsStream("/com/ustadmobile/port/sharedse/fullstatement"), "UTF-8");

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
        StatementEntity statement = appRepo.getStatementDao().findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee");
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement.getStatementId());

    }

    @Test
    public void givenAValidStatement_whenPutRequestHasStatementIdParam_thenShouldReturn() throws IOException {

        String urlString = "http://localhost:" + httpd.getListeningPort() + "/xapi/statements?statementId=" +
                URLEncoder.encode("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", StandardCharsets.UTF_8.toString());
        String content =  UMIOUtils.INSTANCE.readToString(
                getClass().getResourceAsStream("/com/ustadmobile/port/sharedse/fullstatement"), "UTF-8");

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
        StatementEntity statement = appRepo.getStatementDao().findByStatementId("6690e6c9-3ef0-4ed3-8b37-7f3964730bee");
        Assert.assertEquals("6690e6c9-3ef0-4ed3-8b37-7f3964730bee", statement.getStatementId());
    }


}
