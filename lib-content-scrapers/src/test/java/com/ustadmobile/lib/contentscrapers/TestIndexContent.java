package com.ustadmobile.lib.contentscrapers;


import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ScrapeQueueItemDao;
import com.ustadmobile.core.db.dao.ScrapeRunDao;
import com.ustadmobile.lib.contentscrapers.edraakK12.IndexEdraakK12Content;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;
import com.ustadmobile.lib.db.entities.ScrapeRun;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;

public class TestIndexContent {

    private final String MAIN_CONTENT_CONTENT_FILE = "/com/ustadmobile/lib/contentscrapers/edraakK12/edraak-main-content.txt";


    private final String DETAIL_JSON_CONTENT_FILE = "/com/ustadmobile/lib/contentscrapers/edraakK12/edraak-detail.txt";

    final String COMPONENT_API_PREFIX = "/api/component/";

    final Dispatcher indexDispatcher = new Dispatcher() {
        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

            try {

                if (request.getPath().contains(MAIN_CONTENT_CONTENT_FILE)) {

                    int prefixLength = COMPONENT_API_PREFIX.length();
                    String fileName = request.getPath().substring(prefixLength,
                            request.getPath().indexOf(".txt", prefixLength));
                    String body = IOUtils.toString(getClass().getResourceAsStream(fileName + ".txt"), UTF_ENCODING);
                    MockResponse response = new MockResponse().setResponseCode(200);
                    response.setHeader("ETag", UTF_ENCODING.hashCode());
                    if (!request.getMethod().equalsIgnoreCase("HEAD"))
                        response.setBody(body);

                    return response;

                } else if (request.getPath().contains(DETAIL_JSON_CONTENT_FILE) || request.getPath().contains("5a60a25f0ed49f0498cb201d")) {

                    String body = IOUtils.toString(getClass().getResourceAsStream(DETAIL_JSON_CONTENT_FILE), UTF_ENCODING);
                    MockResponse response = new MockResponse().setResponseCode(200);
                    response.setHeader("ETag", UTF_ENCODING.hashCode());
                    if (!request.getMethod().equalsIgnoreCase("HEAD"))
                        response.setBody(body);

                    return response;

                } else if (request.getPath().contains("/media/")) {

                    String fileLocation = "/com/ustadmobile/lib/contentscrapers/files/" + request.getPath().substring(7);
                    InputStream videoIn = getClass().getResourceAsStream(fileLocation);
                    BufferedSource source = Okio.buffer(Okio.source(videoIn));
                    Buffer buffer = new Buffer();
                    source.readAll(buffer);

                    MockResponse response = new MockResponse().setResponseCode(200);
                    response.setHeader("ETag", UTF_ENCODING.hashCode());
                    if (!request.getMethod().equalsIgnoreCase("HEAD"))
                        response.setBody(buffer);

                    return response;

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return new MockResponse().setResponseCode(404);
        }
    };


    @Test
    public void givenServerOnline_whenUrlFound_FindImportedContent() throws IOException {

        UmAppDatabase db = UmAppDatabase.getInstance(null);
        UmAppDatabase repo = db.getRepository("https://localhost", "");
        ScrapeRunDao runDao = db.getScrapeRunDao();
        ScrapeRun run = new ScrapeRun();
        run.setScrapeRunUid(943);
        run.setScrapeType("Edraak-Test");
        run.setStatus(ScrapeQueueItemDao.STATUS_PENDING);
        runDao.insert(run);

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(indexDispatcher);

        File tmpDir = Files.createTempDirectory("testedxcontentindexscraper").toFile();
        File containerDir = Files.createTempDirectory("container").toFile();

        IndexEdraakK12Content.startScrape(mockWebServer.url("/api/component/" + MAIN_CONTENT_CONTENT_FILE).toString(), tmpDir, containerDir, 943);

        ContentEntryDao contentEntryDao = repo.getContentEntryDao();
        ContentEntryParentChildJoinDao parentChildDaoJoin = repo.getContentEntryParentChildJoinDao();

        ContentEntry parentEntry = contentEntryDao.findBySourceUrl("https://www.edraak.org/k12/");

        Assert.assertEquals(true, parentEntry.getEntryId().equalsIgnoreCase("https://www.edraak.org/k12/"));

        ContentEntry childEntry = contentEntryDao.findBySourceUrl("5a608815f3a50d049abf68e9");

        Assert.assertEquals(true, childEntry.getEntryId().equalsIgnoreCase("5a608815f3a50d049abf68e9"));

        ContentEntryParentChildJoin parentChildJoinEntry = parentChildDaoJoin.findParentByChildUuids(childEntry.getContentEntryUid());

        Assert.assertEquals(true, parentChildJoinEntry.getCepcjParentContentEntryUid() == parentEntry.getContentEntryUid());

        ContentEntry courseEntry = contentEntryDao.findBySourceUrl("5a60a25f0ed49f0498cb201d");

        Assert.assertEquals(true, courseEntry.getEntryId().equalsIgnoreCase("5a60a25f0ed49f0498cb201d"));



    }


}
