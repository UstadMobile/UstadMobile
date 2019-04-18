package com.ustadmobile.lib.contentscrapers;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ContentEntryContentCategoryJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryContentEntryFileJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryDao;
import com.ustadmobile.core.db.dao.ContentEntryFileDao;
import com.ustadmobile.core.db.dao.ContentEntryParentChildJoinDao;
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao;
import com.ustadmobile.lib.contentscrapers.ck12.CK12ContentScraper;
import com.ustadmobile.lib.contentscrapers.ck12.IndexCategoryCK12Content;
import com.ustadmobile.lib.contentscrapers.ck12.practice.ScriptEngineReader;
import com.ustadmobile.lib.db.entities.ContentEntry;
import com.ustadmobile.lib.db.entities.ContentEntryContentEntryFileJoin;
import com.ustadmobile.lib.db.entities.ContentEntryFile;
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

import static com.ustadmobile.lib.contentscrapers.ScraperConstants.UTF_ENCODING;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class TestCK12ContentScraper {

    private static final String COMPONENT_API_PREFIX = "/c/";
    private final String PRACTICE_JSON = "/com/ustadmobile/lib/contentscrapers/ck12/ck12-practice.txt";
    private final String TEST_JSON = "/com/ustadmobile/lib/contentscrapers/ck12/ck12-test.txt";
    private final String QUESTION_JSON = "/com/ustadmobile/lib/contentscrapers/ck12/ck12-question-1.txt";
    private final String ANSWER_JSON = "/com/ustadmobile/lib/contentscrapers/ck12/answer.txt";
    private final String VIDEO_LOCATION_FILE = "/com/ustadmobile/lib/contentscrapers/files/video.mp4";
    private final String RESOURCE_PATH = "/com/ustadmobile/lib/contentscrapers/files/";
    private final String LAST_MODIFIED_FILE = "/com/ustadmobile/lib/contentscrapers/ck12/plix/last-modified.txt";


    private final String SLIDESHARE_HTML = "/com/ustadmobile/lib/contentscrapers/ck12/ck12-slideshare.txt";
    private final String VIDEO_YT_HTML = "/com/ustadmobile/lib/contentscrapers/ck12/ck-12-video-yt.txt";
    private final String CK_VID_HTML = "/com/ustadmobile/lib/contentscrapers/ck12/ck12-video-genie.txt";
    private final String READ_HTML = "/com/ustadmobile/lib/contentscrapers/ck12/ck12-read.txt";
    private final String MATH_JAX_HTML = "/com/ustadmobile/lib/contentscrapers/ck12/ck12-read-mathjax.txt";

    private final String PLIX_HTML = "/com/ustadmobile/lib/contentscrapers/ck12/plix/plix.txt";
    private final String PLIX_PATH = "/com/ustadmobile/lib/contentscrapers/ck12/plix/";


    String youtubeUrl = "https://www.ck12.org/c/biology/history-of-life/lecture/Origin-and-Evolution-of-Life/?referrer=concept_details";
    String ckVidUrl = "https://www.ck12.org/c/elementary-math-grade-1/add-to-10-with-images/enrichment/Overview-of-Addition-Sums-to-10/?referrer=concept_details";
    String slideShareUrl = "https://www.ck12.org/c/earth-science/observations-and-experiments/lecture/Inference-and-Observation-Activity/?referrer=concept_details";
    String mathJaxUrl = "https://www.ck12.org/c/geometry/midpoints-and-segment-bisectors/lesson/Midpoints-and-Segment-Bisectors-BSC-GEOM/?referrer=concept_details";
    String practiceUrl = "https://www.ck12.org/c/elementary-math-grade-1/add-to-10-with-images/asmtpractice/Add-to-10-with-Images-Practice?referrer=featured_content&collectionHandle=elementary-math-grade-1&collectionCreatorID=3&conceptCollectionHandle=elementary-math-grade-1-::-add-to-10-with-images?referrer=concept_details";
    String readUrl = "https://www.ck12.org/c/physical-science/chemistry-of-compounds/lesson/Chemistry-of-Compounds-MS-PS/?referrer=concept_details";
    String plixUrl = "https://www.ck12.org/c/trigonometry/distance-formula-and-the-pythagorean-theorem/plix/Pythagorean-Theorem-to-Determine-Distance-Tree-Shadows-53d147578e0e0876d4df82f1?referrer=concept_details";


    final Dispatcher dispatcher = new Dispatcher() {
        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

            try {

                if (request.getPath().startsWith(COMPONENT_API_PREFIX)) {

                    int prefixLength = COMPONENT_API_PREFIX.length();
                    String fileName = request.getPath().substring(prefixLength,
                            request.getPath().indexOf(".txt", prefixLength));
                    String body = IOUtils.toString(getClass().getResourceAsStream(fileName + ".txt"), UTF_ENCODING);
                    return new MockResponse().setBody(body);

                } else if (request.getPath().equals("/media/video.mp4")) {
                    InputStream videoIn = getClass().getResourceAsStream(VIDEO_LOCATION_FILE);
                    BufferedSource source = Okio.buffer(Okio.source(videoIn));
                    Buffer buffer = new Buffer();
                    source.readAll(buffer);

                    MockResponse response = new MockResponse().setResponseCode(200);
                    response.setHeader("ETag", (String.valueOf(buffer.size())
                            + VIDEO_LOCATION_FILE).hashCode());
                    if (!request.getMethod().equalsIgnoreCase("HEAD"))
                        response.setBody(buffer);

                    return response;
                } else if (request.getPath().contains("picture")) {
                    int length = "/media/".length();
                    String fileName = request.getPath().substring(length,
                            request.getPath().indexOf(".png", length));
                    InputStream pictureIn = getClass().getResourceAsStream(RESOURCE_PATH + fileName + ".png");
                    BufferedSource source = Okio.buffer(Okio.source(pictureIn));
                    Buffer buffer = new Buffer();
                    source.readAll(buffer);
                    MockResponse response = new MockResponse().setResponseCode(200);
                    response.setHeader("ETag", (String.valueOf(buffer.size())
                            + RESOURCE_PATH).hashCode());
                    if (!request.getMethod().equalsIgnoreCase("HEAD"))
                        response.setBody(buffer);

                    return response;

                } else if (request.getPath().contains("/plix/")) {
                    int length = "/plix/".length();
                    String fileName = request.getPath().substring(length);
                    InputStream pictureIn = getClass().getResourceAsStream(PLIX_PATH + fileName);
                    BufferedSource source = Okio.buffer(Okio.source(pictureIn));
                    Buffer buffer = new Buffer();
                    source.readAll(buffer);
                    MockResponse response = new MockResponse().setResponseCode(200);
                    response.setHeader("ETag", (String.valueOf(buffer.size())
                            + RESOURCE_PATH).hashCode());
                    if (!request.getMethod().equalsIgnoreCase("HEAD"))
                        response.setBody(buffer);

                    return response;
                } else if (request.getPath().contains("json")) {

                    int start = request.getPath().indexOf("/", request.getPath().indexOf("/") + 1);
                    String fileName = request.getPath().substring(start);
                    String body = IOUtils.toString(getClass().getResourceAsStream(fileName), UTF_ENCODING);
                    MockResponse response = new MockResponse().setResponseCode(200);
                    response.setHeader("ETag", UTF_ENCODING.hashCode());
                    if (!request.getMethod().equalsIgnoreCase("HEAD"))
                        response.setBody(body);

                    return response;

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return new MockResponse().setBody("");
        }
    };


    @Test
    public void givenServerOnline_whenVideoContentScraped_thenShouldConvertAndDownload() throws IOException {

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);
        File tmpDir = Files.createTempDirectory("testCK12contentscraper").toFile();

        CK12ContentScraper scraper = new CK12ContentScraper(mockWebServer.url("/c/" + CK_VID_HTML).toString(), tmpDir);
        scraper.scrapeVideoContent();

        String folderName = FilenameUtils.getBaseName(CK_VID_HTML);
        File folder = new File(tmpDir, folderName);
        folder.mkdirs();

        File file = new File(folder, "index.html");
        Assert.assertEquals("Html for video content", true, ContentScraperUtil.fileHasContent(file));

        File asset = new File(folder, "asset");
        Assert.assertEquals("asset folder created", true, asset.isDirectory());

        File thumbnail = new File(asset, folderName + "-video-thumbnail.jpg");
        Assert.assertEquals("thumbnail for content", true, ContentScraperUtil.fileHasContent(thumbnail));

        File video = new File(asset, "media_video.webm");
        Assert.assertEquals("video for content", true, ContentScraperUtil.fileHasContent(video));
    }

    @Test
    public void givenServerOnline_whenSlideShareVideoContentScraped_thenMp4FileShouldNotExist() throws IOException {

        File tmpDir = Files.createTempDirectory("testCK12contentscraper").toFile();
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        CK12ContentScraper scraper = new CK12ContentScraper(mockWebServer.url("/c/" + SLIDESHARE_HTML).toString(), tmpDir);
        scraper.scrapeVideoContent();

        String folderName = FilenameUtils.getBaseName(SLIDESHARE_HTML);
        File folder = new File(tmpDir, folderName);
        folder.mkdirs();

        File file = new File(folder, "index.html");
        Assert.assertEquals("Html for video content", true, ContentScraperUtil.fileHasContent(file));

        File asset = new File(folder, "asset");
        Assert.assertEquals("asset folder created", true, asset.isDirectory());

        File thumbnail = new File(asset, folderName + "-video-thumbnail.jpg");
        Assert.assertEquals("thumbnail for content", true, ContentScraperUtil.fileHasContent(thumbnail));

        File video = new File(asset, "_media_video.mp4");
        Assert.assertEquals("video for content", false, ContentScraperUtil.fileHasContent(video));
    }

    @Test
    public void givenServerOnline_whenYoutubeVideoContentScraped_thenMp4FileShouldNotExist() throws IOException {

        File tmpDir = Files.createTempDirectory("testCK12contentscraper").toFile();
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        CK12ContentScraper scraper = new CK12ContentScraper(mockWebServer.url("/c/" + VIDEO_YT_HTML).toString(), tmpDir);
        scraper.scrapeVideoContent();

        String folderName = FilenameUtils.getBaseName(VIDEO_YT_HTML);
        File folder = new File(tmpDir, folderName);
        folder.mkdirs();

        File file = new File(folder, "index.html");
        Assert.assertEquals("Html for video content", true, ContentScraperUtil.fileHasContent(file));

        File asset = new File(folder, "asset");
        Assert.assertEquals("asset folder created", true, asset.isDirectory());

        File thumbnail = new File(asset, folderName + "-video-thumbnail.jpg");
        Assert.assertEquals("thumbnail for content", true, ContentScraperUtil.fileHasContent(thumbnail));

        File video = new File(asset, "_media_video.mp4");
        Assert.assertEquals("video for content", false, ContentScraperUtil.fileHasContent(video));
    }


    @Test
    public void givenServerOnline_whenScrapeReadContentScraped_thenShouldConvertAndDownload() throws IOException {
        File tmpDir = Files.createTempDirectory("testCK12readcontentscraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        CK12ContentScraper scraper = new CK12ContentScraper(mockWebServer.url("/c/" + MATH_JAX_HTML).toString(), tmpDir);
        scraper.scrapeReadContent();

        String folderName = FilenameUtils.getBaseName(MATH_JAX_HTML);
        File folder = new File(tmpDir, folderName);
        folder.mkdirs();

        File index = new File(folder, "index.html");
        Assert.assertEquals("index html to display content", true, ContentScraperUtil.fileHasContent(index));
    }


    @Test
    public void givenServerOnline_whenReadContentScraped_thenShouldConvertAndDownload() throws IOException {
        File tmpDir = Files.createTempDirectory("testCK12readcontentscraper").toFile();

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        CK12ContentScraper scraper = new CK12ContentScraper(mockWebServer.url("/c/" + READ_HTML).toString(), tmpDir);
        scraper.scrapeReadContent();

        String folderName = FilenameUtils.getBaseName(READ_HTML);
        File folder = new File(tmpDir, folderName);
        folder.mkdirs();

        File index = new File(folder, "index.html");
        Assert.assertEquals("index html to display content", true, ContentScraperUtil.fileHasContent(index));

    }

    @Test
    public void givenServerOnline_whenPracticeContentScraped_thenShouldConvertAndDownload() throws IOException {
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);
        File tmpDir = Files.createTempDirectory("testCK12practicecontentscraper").toFile();

        CK12ContentScraper scraper = spy(new CK12ContentScraper(mockWebServer.url("/c/ck12-practice?").toString(), tmpDir));
        doReturn(mockWebServer.url("/c/" + PRACTICE_JSON).toString()).when(scraper).generatePracticeLink(Mockito.anyString());
        doReturn(mockWebServer.url("/c/" + TEST_JSON).toString()).when(scraper).generateTestUrl(Mockito.anyString());
        doReturn(mockWebServer.url("/c/" + QUESTION_JSON).toString()).when(scraper).generateQuestionUrl(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
        doReturn(IOUtils.toString(getClass().getResourceAsStream(ANSWER_JSON), UTF_ENCODING)).when(scraper).extractAnswerFromEncryption(Mockito.anyString());


        scraper.scrapePracticeContent();
        File practiceFolder = new File(tmpDir, "ck12-practice");
        Assert.assertEquals("directory for practice exists", true, practiceFolder.isDirectory());

        File questions = new File(practiceFolder, "questions.json");
        Assert.assertEquals("download questions json exists", true, ContentScraperUtil.fileHasContent(questions));

        File index = new File(practiceFolder, "index.html");
        Assert.assertEquals("index html to display questions", true, ContentScraperUtil.fileHasContent(index));

    }

    @Test
    public void testRhino() {
        ScriptEngineReader scriptEngineReader = new ScriptEngineReader();
        String result = scriptEngineReader.getResult("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");
        Assert.assertEquals("result matches answer json response", result, "{\"answer\": [\"<img width=\\\"248\\\" height=\\\"64\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/35.jpg\\\" alt=\\\"\\\" />\"], \"instance\": {\"multiAnswers\": false, \"solution\": \"<p>\\n  There are 4 yellow trees and 3 green trees. There are 7 trees altogether.\\n</p>\\n<p>\\n  4 + 3 = 7\\n</p>\", \"stem\": {\"displayText\": \"<p>\\n  Which picture shows 4 + 3 = 7?\\n</p>\"}, \"responseObjects\": [{\"optionKey\": \"<img width=\\\"254\\\" height=\\\"64\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/34.jpg\\\" alt=\\\"\\\" />\", \"isCorrect\": \"F\", \"displayText\": \"<img width=\\\"254\\\" height=\\\"64\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/34.jpg\\\" alt=\\\"\\\" />\", \"orderText\": \"a\", \"displayOrder\": 1}, {\"optionKey\": \"<img width=\\\"247\\\" height=\\\"73\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/37.jpg\\\" alt=\\\"\\\" />\", \"isCorrect\": \"F\", \"displayText\": \"<img width=\\\"247\\\" height=\\\"73\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/37.jpg\\\" alt=\\\"\\\" />\", \"orderText\": \"b\", \"displayOrder\": 2}, {\"optionKey\": \"<img width=\\\"248\\\" height=\\\"64\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/35.jpg\\\" alt=\\\"\\\" />\", \"isCorrect\": \"T\", \"displayText\": \"<img width=\\\"248\\\" height=\\\"64\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/35.jpg\\\" alt=\\\"\\\" />\", \"orderText\": \"c\", \"displayOrder\": 3}, {\"optionKey\": \"<img width=\\\"258\\\" height=\\\"77\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/36.jpg\\\" alt=\\\"\\\" />\", \"isCorrect\": \"F\", \"displayText\": \"<img width=\\\"258\\\" height=\\\"77\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/36.jpg\\\" alt=\\\"\\\" />\", \"orderText\": \"d\", \"displayOrder\": 4}], \"answer\": [\"<img width=\\\"248\\\" height=\\\"64\\\" src=\\\"https://s3.amazonaws.com/ck12bg.ck12.org/curriculum/102916/35.jpg\\\" alt=\\\"\\\" />\"], \"hints\": [\"<iframe width=\\\"95%\\\" height=\\\"95%\\\" frameborder=\\\"0\\\" src=\\\"https://braingenie.ck12.org/skills/102916/video_only\\\" style=\\\"border:none\\\"><p>Your browser does not support iframes.</p> </iframe>\"]}, \"allowVariants\": false, \"questionTypeName\": \"multiple-choice\", \"answered\": false}");
    }

    @Test
    public void givenServerOnline_whenPlixContentScraped_thenShouldConvertAndDownload() throws IOException {

        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(dispatcher);

        File tmpDir = Files.createTempDirectory("testCK12plixcontentscraper").toFile();

        CK12ContentScraper scraper = spy(new CK12ContentScraper(mockWebServer.url("/c/" + PLIX_HTML + "-53d147578e0e0876d4df82f1?").toString(), tmpDir));
        doReturn(mockWebServer.url("/c/" + LAST_MODIFIED_FILE).toString()).when(scraper).generatePlixLink(Mockito.anyString());
        scraper.scrapePlixContent();

        File plixFolder = new File(tmpDir, "plix");
        Assert.assertEquals("directory for plix exists", true, plixFolder.isDirectory());

        File indexJson = new File(plixFolder, "index.json");
        Assert.assertEquals("index json for all urls and thier path exists", true, ContentScraperUtil.fileHasContent(indexJson));

        File zip = new File(tmpDir, "plix.zip");
        Assert.assertEquals("zipped file exists", true, ContentScraperUtil.fileHasContent(zip));

    }


}
