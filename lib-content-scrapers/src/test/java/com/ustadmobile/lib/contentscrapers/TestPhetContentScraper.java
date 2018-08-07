package com.ustadmobile.lib.contentscrapers;

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

public class TestPhetContentScraper {


    private String EN_LOCATION_FILE = "/com/ustadmobile/lib/contentscrapers/simulation_en.html";
    private String ES_LOCATION_FILE = "/com/ustadmobile/lib/contentscrapers/simulation_es.html";
    private String HTML_FILE_LOCATION = "/com/ustadmobile/lib/contentscrapers/phet-html-detail.txt";
    private String JAR_FILE_LOCATION = "/com/ustadmobile/lib/contentscrapers/phet-jar-detail.html";
    private String FLASH_FILE_LOCATION = "/com/ustadmobile/lib/contentscrapers/phet-flash-detail.html";

    private String SIM_EN = "simulation_en.html";
    private String SIM_ES = "simulation_es.html";


    final Dispatcher dispatcher = new Dispatcher() {


        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {


            try {

                if (request.getPath().startsWith("/api/simulation")) {
                    InputStream videoIn = getClass().getResourceAsStream(HTML_FILE_LOCATION);
                    BufferedSource source = Okio.buffer(Okio.source(videoIn));
                    Buffer buffer = new Buffer();
                    source.readAll(buffer);

                    return new MockResponse().setBody(buffer);

                } else if (request.getPath().equals("/media/simulation_en.html?download")) {
                    InputStream videoIn = getClass().getResourceAsStream(EN_LOCATION_FILE);
                    BufferedSource source = Okio.buffer(Okio.source(videoIn));
                    Buffer buffer = new Buffer();
                    source.readAll(buffer);

                    MockResponse mock = new MockResponse();
                    mock.setBody(buffer);
                    mock.addHeader("ETag", "16adca-5717010854ac0");
                    mock.addHeader("Last-Modified","Fri, 20 Jul 2018 15:36:51 GMT");

                    return mock;
                } else if(request.getPath().contains("/media/simulation_es.html?download")){
                    InputStream videoIn = getClass().getResourceAsStream(ES_LOCATION_FILE);
                    BufferedSource source = Okio.buffer(Okio.source(videoIn));
                    Buffer buffer = new Buffer();
                    source.readAll(buffer);

                    MockResponse mock = new MockResponse();
                    mock.setBody(buffer);
                    mock.addHeader("ETag", "16adca-5717010854ac0");
                    mock.addHeader("Last-Modified","Fri, 20 Jul 2018 15:36:51 GMT");

                    return mock;
                } else if(request.getPath().contains("flash")){

                    InputStream videoIn = getClass().getResourceAsStream(FLASH_FILE_LOCATION);
                    BufferedSource source = Okio.buffer(Okio.source(videoIn));
                    Buffer buffer = new Buffer();
                    source.readAll(buffer);

                    return new MockResponse().setBody(buffer);
                }else if(request.getPath().contains("jar")){

                    InputStream videoIn = getClass().getResourceAsStream(JAR_FILE_LOCATION);
                    BufferedSource source = Okio.buffer(Okio.source(videoIn));
                    Buffer buffer = new Buffer();
                    source.readAll(buffer);

                    return new MockResponse().setBody(buffer);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return new MockResponse().setResponseCode(404);
        }
    };



   @Test
    public void givenServerOnline_whenPhetContentScraped_thenShouldConvertAndDownload() throws IOException {
        File tmpDir = Files.createTempDirectory("testphetcontentscraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        PhetContentScraper scraper = new PhetContentScraper();
        scraper.convert(mockWebServer.url("/api/simulation/").toString(), tmpDir);

        File aboutFile = new File(tmpDir, ScraperConstants.ABOUT_HTML);
        Assert.assertTrue("About File Exists",aboutFile.length() > 0);

        File englishDir = new File(tmpDir, "en");
        Assert.assertTrue("English Folder exists", englishDir.isDirectory());

        File englishSimulation = new File(englishDir, SIM_EN);
        Assert.assertTrue("English Simulation exists", englishSimulation.length() > 0);

        long firstSimDownload = englishSimulation.lastModified();

        File engETag = new File(englishDir, ScraperConstants.ETAG_TXT);
        Assert.assertTrue("English ETag exists", engETag.length() > 0);

        File engModified = new File(englishDir, ScraperConstants.LAST_MODIFIED_TXT);
        Assert.assertTrue("English Last Modified exists", engModified.length() > 0);

        File spanishDir = new File(tmpDir, "es");
        Assert.assertTrue("Spanish Folder exists",spanishDir.isDirectory());

        File spanishSimulation = new File(spanishDir, SIM_ES);
        Assert.assertTrue("Spanish Simulation exists", spanishSimulation.length() > 0);

        File spanishETag = new File(englishDir, ScraperConstants.ETAG_TXT);
        Assert.assertTrue("Spanish ETag exists", spanishETag.length() > 0);

        File spanishModified = new File(spanishDir, ScraperConstants.LAST_MODIFIED_TXT);
        Assert.assertTrue("Spanish Last Modified exists", spanishModified.length() > 0);

        scraper.convert(mockWebServer.url("/api/simulation/").toString(), tmpDir);

        long secondSimDownload = englishSimulation.lastModified();

        Assert.assertTrue("didnt download 2nd time", firstSimDownload == secondSimDownload);

    }

    @Test(expected = IllegalArgumentException.class)
    public void givenServerOnline_whenPhetContentScraped_thenShouldThrowIllegalArgumentJarNotSupported() throws IOException {
        File tmpDir = Files.createTempDirectory("testphetcontentscraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        PhetContentScraper scraper = new PhetContentScraper();
        scraper.convert(mockWebServer.url("/legacy/jar").toString(), tmpDir);

    }

    @Test(expected = IllegalArgumentException.class)
    public void givenServerOnline_whenPhetContentScraped_thenShouldThrowIllegalArgumentFlashNotSupported() throws IOException {
        File tmpDir = Files.createTempDirectory("testphetcontentscraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        PhetContentScraper scraper = new PhetContentScraper();
        scraper.convert(mockWebServer.url("/legacy/flash").toString(), tmpDir);

    }

}
