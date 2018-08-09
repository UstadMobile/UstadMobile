package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.lib.contentscrapers.generated.TestBuildConfig;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;

@RunWith(Parameterized.class)
public class TestEdraakContentScraperWithParameters {

    private static MockWebServer mockWebServer;

    private static final String DETAIL_JSON_CONTENT_FILE = "/com/ustadmobile/lib/contentscrapers/edraakK12/edraak-detail.txt";

    private static final String VIDEO_LOCATION_FILE = "/com/ustadmobile/lib/contentscrapers/edraakK12/video.mp4";

    private static final String RESOURCE_PATH = "/com/ustadmobile/lib/contentscrapers/edraakK12/";

    @Parameterized.Parameters
    public static ArrayList<Object> urlParameters() throws IOException {
        //make the mock server start
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        ArrayList<Object> paramList = new ArrayList<>();
        paramList.add(mockWebServer.url(DETAIL_JSON_CONTENT_FILE).toString());
        if(TestBuildConfig.TEST_CONTENT_URL != null && !TestBuildConfig.TEST_CONTENT_URL.isEmpty())
            paramList.add(TestBuildConfig.TEST_CONTENT_URL);

        return paramList;
    }

    @Parameterized.Parameter
    public String url;



    static final Dispatcher dispatcher = new Dispatcher() {
        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

            try {

                if(request.getPath().contains("picture")){
                    int length = "/media/".length();
                    String fileName = request.getPath().substring(length,
                            request.getPath().indexOf(".png", length));
                    InputStream pictureIn = getClass().getResourceAsStream(RESOURCE_PATH + fileName + ".png");
                    BufferedSource source = Okio.buffer(Okio.source(pictureIn));
                    Buffer buffer = new Buffer();
                    source.readAll(buffer);
                    return new MockResponse().setResponseCode(200).setBody(buffer);
                } else if (request.getPath().contains(RESOURCE_PATH)) {

                    int index = request.getPath().indexOf(RESOURCE_PATH);
                    String fileName = request.getPath().substring(index,
                            request.getPath().indexOf(".txt", index));
                    return new MockResponse().setBody(IOUtils.toString(getClass().getResourceAsStream(fileName + ".txt"), UTF_ENCODING));

                } else if (request.getPath().equals("/media/video.mp4")) {
                    InputStream videoIn = getClass().getResourceAsStream(VIDEO_LOCATION_FILE);
                    BufferedSource source = Okio.buffer(Okio.source(videoIn));
                    Buffer buffer = new Buffer();
                    source.readAll(buffer);

                    return new MockResponse().setResponseCode(200).setBody(buffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new MockResponse().setResponseCode(404);
        }
    };

    @Test
    public void test() throws IOException {
        EdraakK12ContentScraper scraper = new EdraakK12ContentScraper(url, new File("C:\\Users\\suhai\\indexparameter\\"));
        scraper.scrapContent();
    }

}
