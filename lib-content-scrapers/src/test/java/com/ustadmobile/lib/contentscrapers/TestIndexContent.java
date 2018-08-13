package com.ustadmobile.lib.contentscrapers;


import com.ustadmobile.lib.contentscrapers.EdraakK12.IndexEdraakK12Content;

import org.apache.commons.io.IOUtils;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

        IndexEdraakK12Content indexObj = new IndexEdraakK12Content();
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(indexDispatcher);

        File tmpDir = Files.createTempDirectory("testedxcontentindexscraper").toFile();

        indexObj.findContent(MAIN_CONTENT_CONTENT_FILE, mockWebServer.url("/api/").toString(), 41, tmpDir);


    }



}
