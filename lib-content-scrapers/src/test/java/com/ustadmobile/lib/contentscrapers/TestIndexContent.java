package com.ustadmobile.lib.contentscrapers;


import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.lib.contentscrapers.edraakK12.IndexEdraakK12Content;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;

import org.apache.commons.io.IOUtils;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.*;

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
                    return new MockResponse().setBody(IOUtils.toString(getClass().getResourceAsStream(fileName + ".txt"), UTF_ENCODING));

                } else {
                     return new MockResponse().setBody(IOUtils.toString(getClass().getResourceAsStream(DETAIL_JSON_CONTENT_FILE), UTF_ENCODING));
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

        IndexEdraakK12Content indexObj = new IndexEdraakK12Content();
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(indexDispatcher);

        File tmpDir = Files.createTempDirectory("testedxcontentindexscraper").toFile();

        indexObj.findContent(MAIN_CONTENT_CONTENT_FILE, mockWebServer.url("/api/").toString(), 41, tmpDir);

        ContentEntryDao contentEntryDao = repo.getContentEntryDao();
        ContentEntryParentChildJoinDao parentChildDaoJoin = repo.getContentEntryParentChildJoinDao();
        ContentEntryFileDao fileDao = repo.getContentEntryFileDao();
        ContentEntryContentEntryFileJoinDao fileEntryJoin = repo.getContentEntryContentEntryFileJoinDao();

        ContentEntry parentEntry = contentEntryDao.findBySourceUrl("https://www.edraak.org/k12/");

        Assert.assertEquals(true, parentEntry.getEntryId().equalsIgnoreCase("https://www.edraak.org/k12/"));

        ContentEntry childEntry = contentEntryDao.findBySourceUrl("5a608815f3a50d049abf68e9");

        Assert.assertEquals(true, childEntry.getEntryId().equalsIgnoreCase("5a608815f3a50d049abf68e9"));

        ContentEntryParentChildJoin parentChildJoinEntry = parentChildDaoJoin.findParentByChildUuids(childEntry.getContentEntryUid());

        Assert.assertEquals(true, parentChildJoinEntry.getCepcjParentContentEntryUid() == parentEntry.getContentEntryUid());

        ContentEntry courseEntry = contentEntryDao.findBySourceUrl("5a60a25f0ed49f0498cb201d");

        Assert.assertEquals(true, courseEntry.getEntryId().equalsIgnoreCase("5a60a25f0ed49f0498cb201d"));

        List<ContentEntryContentEntryFileJoin> listOfFiles = fileEntryJoin.findChildByParentUUid(courseEntry.getContentEntryUid());

        Assert.assertEquals(true, listOfFiles.size() > 0);

        ContentEntryFile file = fileDao.findByUid(listOfFiles.get(0).getCecefjContentEntryFileUid());

        Assert.assertEquals(true, ScraperConstants.MIMETYPE_ZIP.equalsIgnoreCase(file.getMimeType()));

    }



}
