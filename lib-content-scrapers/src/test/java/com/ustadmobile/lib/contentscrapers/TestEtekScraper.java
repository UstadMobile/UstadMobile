package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.lib.contentscrapers.etekkatho.EtekkathoScraper;
import com.ustadmobile.lib.contentscrapers.etekkatho.IndexEtekkathoScraper;

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

public class TestEtekScraper {

    final Dispatcher dispatcher = new Dispatcher() {
        @Override
        public MockResponse dispatch(RecordedRequest request) {

            try {

                if (request.getPath().contains("json")) {

                    String fileName;
                    if(request.getPath().contains("?handle")){
                        fileName = request.getPath().substring(5,
                                request.getPath().indexOf("?handle"));
                    }else{
                       fileName = request.getPath().substring(5);
                    }
                    String body = IOUtils.toString(getClass().getResourceAsStream(fileName), UTF_ENCODING);
                    MockResponse response = new MockResponse().setResponseCode(200);
                    response.setHeader("ETag", UTF_ENCODING.hashCode());
                    if (!request.getMethod().equalsIgnoreCase("HEAD"))
                        response.setBody(body);

                    return response;

                } else if (request.getPath().contains("media")) {

                    String fileLocation = request.getPath().substring(6);
                    InputStream videoIn = getClass().getResourceAsStream(fileLocation);
                    BufferedSource source = Okio.buffer(Okio.source(videoIn));
                    Buffer buffer = new Buffer();
                    source.readAll(buffer);


                    MockResponse response = new MockResponse().setResponseCode(200);
                    response.setHeader("ETag", (String.valueOf(buffer.size())
                            + UTF_ENCODING).hashCode());
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
    public void givenServerOnline_whenEtekIsScrapedAgain_thenShouldDownloadOnlyOnce() throws IOException {

        File tmpDir = Files.createTempDirectory("testEtekScraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        EtekkathoScraper scraper = new EtekkathoScraper(
                mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/etek/lesson.html?handle=123-321").toString(),
                tmpDir);
        scraper.scrapeContent();

        File lessonFolder = new File(tmpDir, "123-321");
        Assert.assertEquals(true, lessonFolder.isDirectory());

        File lessonFile = new File(lessonFolder, "123-321");
        Assert.assertEquals(true, lessonFile.isFile() && lessonFile.exists() && lessonFile.length() > 0);

        long modified = lessonFile.lastModified();

        scraper.scrapeContent();

        Assert.assertEquals(modified, lessonFile.lastModified());


    }

    @Test
    public void givenWhenServerOnline_whenEtekisIndexred_AllDirectoriesAndFilesCorrectlyDownloaded() throws IOException {

        File tmpDir = Files.createTempDirectory("testEtekIndexScraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        IndexEtekkathoScraper scraper = new IndexEtekkathoScraper();
        scraper.findContent(mockWebServer.url("/json/com/ustadmobile/lib/contentscrapers/etek/etekhomepage.html").toString(),
                tmpDir);

        File educationFolder = new File(tmpDir, "Education");
        Assert.assertEquals(true, educationFolder.isDirectory());

        File assessmentFolder = new File(educationFolder, "Educational assessment");
        Assert.assertEquals(true, assessmentFolder.isDirectory());

        File lessonFolder = new File(assessmentFolder, "123-321");
        Assert.assertEquals(true, lessonFolder.isDirectory());

        File lessonFile = new File(lessonFolder, "123-321");
        Assert.assertEquals(true, lessonFile.isFile());

    }

}
