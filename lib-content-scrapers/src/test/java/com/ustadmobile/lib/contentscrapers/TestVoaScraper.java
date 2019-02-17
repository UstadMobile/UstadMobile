package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.lib.contentscrapers.voa.VoaScraper;

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

public class TestVoaScraper {

    final Dispatcher dispatcher = new Dispatcher() {
        @Override
        public MockResponse dispatch(RecordedRequest request) {

            try {

                if (request.getPath().contains("json")) {

                    String fileName = request.getPath().substring(5);
                    String body = IOUtils.toString(getClass().getResourceAsStream(fileName), UTF_ENCODING);
                    MockResponse response = new MockResponse().setResponseCode(200);
                    response.setHeader("ETag", UTF_ENCODING.hashCode());
                    if (!request.getMethod().equalsIgnoreCase("HEAD"))
                        response.setBody(body);

                    return response;

                } else if (request.getPath().contains("post")) {

                    String data = IOUtils.toString(request.getBody().inputStream(), UTF_ENCODING);
                    String body;
                    if (data.contains("SelectedAnswerId")) {
                        String fileName = "/com/ustadmobile/lib/contentscrapers/voa/quizoneanswer.html";
                        body = IOUtils.toString(getClass().getResourceAsStream(fileName), UTF_ENCODING);
                    } else {
                        String fileName = "/com/ustadmobile/lib/contentscrapers/voa/quizone.html";
                        body = IOUtils.toString(getClass().getResourceAsStream(fileName), UTF_ENCODING);
                    }

                    MockResponse response = new MockResponse().setResponseCode(200);
                    response.setHeader("ETag", UTF_ENCODING.hashCode());
                    if (!request.getMethod().equalsIgnoreCase("HEAD"))
                        response.setBody(body);

                    return response;


                } else if (request.getPath().contains("content")) {

                    String fileLocation = request.getPath().substring(8);
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

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(request.getPath());
            }
            return new MockResponse().setResponseCode(404);
        }
    };


    @Test
    public void givenServerOnline_whenVoaIsScrapedAgain_thenShouldDownloadOnlyOnce() throws IOException {

        File tmpDir = Files.createTempDirectory("testVoaScraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        VoaScraper scraper = new VoaScraper(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/voa/audiovoa.html").toString(),
                tmpDir);
        scraper.scrapeContent();

        long firstDownloadTime = new File(tmpDir, "audiovoa.zip").lastModified();

        scraper.scrapeContent();

        Assert.assertEquals(firstDownloadTime, new File(tmpDir, "audiovoa.zip").lastModified());

    }

    @Test
    public void givenServerOnline_whenVoaContentWithNoQuizIsScrapedAgain_thenShouldDownloadContentOnlyOnce() throws IOException {

        File tmpDir = Files.createTempDirectory("testVoaScraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        VoaScraper scraper = new VoaScraper(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/voa/contentnoquiz.html").toString(),
                tmpDir);
        scraper.scrapeContent();

        long firstDownloadTime = new File(tmpDir, "contentnoquiz.zip").lastModified();

        scraper.scrapeContent();

        Assert.assertEquals(firstDownloadTime, new File(tmpDir, "contentnoquiz.zip").lastModified());

    }

    @Test
    public void givenServerOnline_whenVoaContentWithQuizIsScrapedAgain_thenShouldDownloadContentOnlyOnce() throws IOException {

        File tmpDir = Files.createTempDirectory("testVoaScraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        VoaScraper scraper = new VoaScraper(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/voa/testquiz.html").toString(),
                tmpDir);
        scraper.answerUrl = mockWebServer.url("/post/com/ustadmobile/lib/contentscrapers/voa/answer").toString();
        scraper.scrapeContent();

        long firstDownloadTime = new File(tmpDir, "testquiz.zip").lastModified();

        scraper.scrapeContent();

        Assert.assertEquals(firstDownloadTime, new File(tmpDir, "testquiz.zip").lastModified());

    }


}
